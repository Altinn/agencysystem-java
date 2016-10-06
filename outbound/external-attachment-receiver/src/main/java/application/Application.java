package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by andreas.naess on 29.09.2016.
 */

/**
 * This is an example SOAP web service server whose purpose is to receive attachments from Altinn. It is built using
 * the Spring Framework.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}