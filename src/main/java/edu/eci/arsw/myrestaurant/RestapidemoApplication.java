package edu.eci.arsw.myrestaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:applicationContext.xml")
public class RestapidemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestapidemoApplication.class, args);
	}
}
