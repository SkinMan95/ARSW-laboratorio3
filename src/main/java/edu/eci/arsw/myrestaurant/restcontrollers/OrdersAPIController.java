/*
 * Copyright (C) 2016 Pivotal Software, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.arsw.myrestaurant.restcontrollers;

import com.fasterxml.jackson.core.JsonGenerator;
import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.model.ProductType;
import edu.eci.arsw.myrestaurant.model.RestaurantProduct;
import edu.eci.arsw.myrestaurant.services.OrderServicesException;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hcadavid
 */
@RestController
@RequestMapping(value = "/orders")
public class OrdersAPIController {

    @Autowired
    RestaurantOrderServices data;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getResourceHandler() {
        //obtener datos que se enviarán a través del API

        List<Order> orders = new ArrayList<>();
        for (Integer tableWithOrder : data.getTablesWithOrders()) {
            orders.add(data.getTableOrder(tableWithOrder));
        }

        JsonObjectBuilder jsonDoc = Json.createObjectBuilder();

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Order order : orders) {
            JsonObjectBuilder doc = Json.createObjectBuilder();

            doc.add("table", order.getTableNumber());
            doc.add("dishes", getJsonArrayOfDishes(order));

            array.add(doc);
        }

        jsonDoc.add("tables", array);

        return new ResponseEntity<>(jsonDoc.build().toString(), HttpStatus.ACCEPTED);
    }

    private JsonArrayBuilder getJsonArrayOfDishes(Order ord) {

        JsonArrayBuilder array = Json.createArrayBuilder();

        for (String dish : ord.getOrderedDishes()) {
            JsonObjectBuilder dJson = Json.createObjectBuilder();
            dJson.add(dish, ord.getDishOrderedAmount(dish));
            array.add(dJson);
        }

        return array;
    }

    @GetMapping("/{idtable}")
    public ResponseEntity<?> getTableHandler(@PathVariable Long idtable) {
        Order order = data.getTableOrder(idtable.intValue());

        HttpStatus status = HttpStatus.ACCEPTED;
        JsonObjectBuilder jsonDoc = Json.createObjectBuilder();

        if (order == null) {
            status = HttpStatus.NOT_FOUND;
        } else {
            jsonDoc.add("table", order.getTableNumber());
            jsonDoc.add("dishes", getJsonArrayOfDishes(order));
        }

        return new ResponseEntity<>(jsonDoc.build().toString(), status);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postCreateNewOrder(@RequestBody String input) {

        final JsonParser parser = Json.createParser(new StringReader(input));

        try {
            boolean success = true;

            Event event = parser.next();
            event = parser.next();
            // table
            String table = null;
            Long tableNumber = null;

            //System.out.println(event);
            if (success = (event == Event.KEY_NAME)) {
                success = (table = parser.getString()).equals("table");
            }

            //System.out.println("1 " + success + " " + table);
            event = parser.next();
            if (success &= (event == Event.VALUE_NUMBER)) {
                tableNumber = parser.getLong();
            }

            //System.out.println("2 " + success + " " + event);
            event = parser.next();
            if (success = (event == Event.KEY_NAME)) {
                success = parser.getString().equals("dishes");
            }

            //System.out.println("3 " + success + " " + event);
            Map<String, Integer> dishes = null;
            event = parser.next();
            if (success &= (event == Event.START_ARRAY)) {
                dishes = getDishesFromJsonArray(parser);
            }

            success &= dishes != null;

            //System.out.println("4 " + success + " " + event);
            parser.close();

            if (success) {
                Order ord = generateOrder(tableNumber, dishes);

                data.addNewOrderToTable(ord);

                return new ResponseEntity<>(HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Not a valid request:\n" + input, HttpStatus.FORBIDDEN);
            }
        } catch (OrderServicesException | OrdersAPIControllerException ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("Error: " + ex, HttpStatus.FORBIDDEN);
        }
    }

    private Map<String, Integer> getDishesFromJsonArray(JsonParser parser) throws OrdersAPIControllerException {
        Event e = parser.next();
        boolean valid = (e == Event.START_OBJECT);

        //System.out.println("1 " + e + " " + valid);
        String key = "";
        Long value = 0L;

        Map<String, Integer> dishes = new HashMap<>();

        //e = parser.next();
        while (valid && e != Event.END_ARRAY) {
            valid &= (e == Event.START_OBJECT);
            //System.out.println("2 " + e + " " + valid);
            e = parser.next();
            if (valid &= (e == Event.KEY_NAME)) {
                key = parser.getString();
            }

            e = parser.next();
            //System.out.println("3 " + e + " " + valid);
            if (valid &= (e == Event.VALUE_NUMBER)) {
                value = parser.getLong();
            }

            if (valid) {
                dishes.put(key, value.intValue());
            }

            e = parser.next();
            valid &= (e == Event.END_OBJECT);
            //System.out.println("4 " + e + " " + valid);
            e = parser.next();
            //System.out.println("5 " + e + " " +valid);
        }

        if (!valid) {
            throw new OrdersAPIControllerException("Invalid dishes array");
        }

        return dishes;
    }

    private Order generateOrder(Long tableNumber, Map<String, Integer> dishes) throws OrderServicesException {
        Order r = new Order(tableNumber.intValue());

        for (String key : dishes.keySet()) {
            r.addDish(key, dishes.get(key));
        }

        return r;
    }

    @GetMapping("/{idtable}/total")
    public ResponseEntity<?> getTotalTableHandler(@PathVariable Long idtable) {
        HttpStatus status = HttpStatus.ACCEPTED;

        int total = 0;
        
        try {
            total = data.calculateTableBill(idtable.intValue());
        } catch (OrderServicesException ex) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(total, status);
    }
}
