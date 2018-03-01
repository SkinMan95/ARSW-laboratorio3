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

import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.services.OrderServicesException;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hcadavid
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/orders")
public class OrdersAPIController {

    @Autowired
    RestaurantOrderServices ros;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List> getResourceHandler() {
        //obtener datos que se enviarán a través del API

        List<Order> orders = new ArrayList<>();
        for (Integer tableWithOrder : ros.getTablesWithOrders()) {
            orders.add(ros.getTableOrder(tableWithOrder));
        }

        return new ResponseEntity<List>(orders, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{idtable}")
    public ResponseEntity<Order> getTableHandler(@PathVariable Long idtable) {
        Order order = ros.getTableOrder(idtable.intValue());

        HttpStatus status = HttpStatus.ACCEPTED;

        if (order == null) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<Order>(order, status);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postCreateNewOrder(@RequestBody Order input) {
        try {
            ros.addNewOrderToTable(input);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (OrderServicesException ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("Error: " + ex, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{idtable}/total")
    public ResponseEntity<?> getTotalTableHandler(@PathVariable Long idtable) {
        HttpStatus status = HttpStatus.ACCEPTED;

        int total = 0;

        try {
            total = ros.calculateTableBill(idtable.intValue());
        } catch (OrderServicesException ex) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(total, status);
    }

    @PutMapping()
    public ResponseEntity<?> putAddProductOrder(@RequestBody Order orderProducts) {
        try {
            Order original = ros.getTableOrder(orderProducts.getTableNumber());
            
            for (String dish : orderProducts.getOrderedDishes()) {
                original.addDish(dish, orderProducts.getDishOrderedAmount(dish));
            }
            
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("Error: " + ex, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{idtable}")
    public ResponseEntity<?> deleteProductOrder(@PathVariable Long idtable) {
        try {
            ros.releaseTable(idtable.intValue());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (OrderServicesException ex) {
            return new ResponseEntity<>("table " + idtable + " doesn't have reservations", HttpStatus.BAD_REQUEST);
        }
    }
}
