package application;

import application.util.ConfigLoader;
import application.util.Constants;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by andreas.naess on 29.09.2016.
 */

/**
 * This is an example SOAP web service server whose purpose is to receive attachments from Altinn. It is built using
 * the Spring Framework.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    final static Logger logger = Logger.getLogger(Application.class);

    public static void main(String[] args) {

            File dataBatchDirectory = new File(Constants.DATA_BATCH_DIRECTORY_PATH);
            File archiveDirectory = new File(Constants.ARCHIVE_DIRECTORY_PATH);

            // This is only true the first the application runs
            if (dataBatchDirectory.mkdir()) {
                System.out.println("temp-data folder created");
            }

            if (archiveDirectory.mkdir()) {
                System.out.println("archive folder created");
            }

            SpringApplication.run(Application.class, args);
    }
}

/**
 * Uncomment this to make a deployable war-file
 */
//@SpringBootApplication
//public class Application extends SpringBootServletInitializer {
//    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
//    }
//
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(Application.class);
//    }
//}