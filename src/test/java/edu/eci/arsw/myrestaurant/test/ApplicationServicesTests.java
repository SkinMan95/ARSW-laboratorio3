package edu.eci.arsw.myrestaurant.test;

import edu.eci.arsw.myrestaurant.beans.BillCalculator;
import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.services.OrderServicesException;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
* CLASES DE EQUIVALENCIA:
* CE1:
*      Condicion entrada:
*          Se da una orden bien definida de 3 hamburgesas, 2 pizzas y 2 cervezas
*      Tipo: 
*          Valor - Comparacion
*      Clase de equivalencia valida: 
*          El valor final es 73511, condirando que se aplica impuesto de 16%
*          solo a las bebidas, y 19% a todo lo demas
*      Clase de equivalencia no valida: 
*          El valor final es distinto a 73511
* 
* CE2:
*      Condicion entrada:
*          Solo se pide un solo producto (HOTDOG) que le aplica impuesto del 19%
*      Tipo: 
*          Valor - Comparacion
*      Clase de equivalencia valida: 
*          El valor equivalente debe ser de $3570
*      Clase de equivalencia no valida: 
*          El valor equivalente es diferente de $3570
* 
* CE3:
*      Condicion entrada:
*          Se da intenta asignar una orden a una mesa ya con ordenes
*      Tipo: 
*          Error
*      Clase de equivalencia valida: 
*          Debe ocurrir una excepcion  de OrderServicesException
*      Clase de equivalencia no valida: 
*          El programa no falla
*/
@RunWith(SpringRunner.class)
@SpringBootTest()
public class ApplicationServicesTests {

    @Autowired
    RestaurantOrderServices data;

    @Test
    public void test1() throws OrderServicesException {
        assertTrue("RestaurantOrderServices is not null", data != null);
        
        int table = 2;
        
        Order o = new Order(table);
        
        o.addDish("HAMBURGER", 3); // 3 * 12300 * (1.19)
        o.addDish("PIZZA", 2); // 2 * 10000 * (1.19)
        o.addDish("BEER", 2); // 2 * 2500 * (1.16)
        
        data.addNewOrderToTable(o);
        
        assertEquals(73511, data.calculateTableBill(table));
    }
    
    @Test
    public void test2() throws OrderServicesException {
        assertTrue("RestaurantOrderServices is not null", data != null);
        
        int table = 4;
        
        Order o = new Order(table);
        
        o.addDish("HOTDOG", 1); // 3 * 12300 * (1.19)
        
        data.addNewOrderToTable(o);
        
        assertEquals(3570, data.calculateTableBill(table));
    }
    
    @Test(expected = OrderServicesException.class)
    public void test3() throws OrderServicesException {
        assertTrue("RestaurantOrderServices is not null", data != null);
        
        int table = 5;
        
        Order o = new Order(table);
        
        o.addDish("HOTDOG", 1); // 3 * 12300 * (1.19)
        
        data.addNewOrderToTable(o);
        
        o = new Order(table);
        
        o.addDish("HOTDOG", 1); // 3 * 12300 * (1.19)
        
        data.addNewOrderToTable(o);
    }

}
