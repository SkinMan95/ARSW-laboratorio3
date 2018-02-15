package edu.eci.arsw.myrestaurant.restcontrollers;

/**
 *
 * @author Alejandro Anzola email: alejandro.anzola@mail.escuelaing.edu.co
 */
public class OrdersAPIControllerException extends Exception {
    private static final long serialVersionUID = 3216531353135465313L;
    
    public OrdersAPIControllerException() {
    }

    public OrdersAPIControllerException(String message) {
        super(message);
    }

    public OrdersAPIControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrdersAPIControllerException(Throwable cause) {
        super(cause);
    }
}
