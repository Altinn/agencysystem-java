package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by andreas.naess on 29.09.2016.
 */

/**
 * This is an example SOAP web service server whose purpose is to receive data batches with attachments from Altinn,
 * and send correspondence to the end user. It is built using the Spring Framework.
 */
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}

/***
 * Uncomment this to create a runnable jar-file
 */
//@SpringBootApplication
//@EnableScheduling
//public class Application {
//
//    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
//    }
//}