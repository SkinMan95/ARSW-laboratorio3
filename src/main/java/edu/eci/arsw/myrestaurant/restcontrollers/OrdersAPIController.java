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
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
            Set<String> dishes = order.getOrderedDishes();
            doc.add("dishes", dishes.toString());
            array.add(doc);
        }
        jsonDoc.add("tables", array);

        return new ResponseEntity<>(jsonDoc.build().toString(), HttpStatus.ACCEPTED);
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
            Set<String> dishes = order.getOrderedDishes();
            jsonDoc.add("dishes", dishes.toString());
        }
        
        return new ResponseEntity<>(jsonDoc.build().toString(), status);
    }
}
