package application;

import application.util.Constants;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

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

    public static void main(String[] args) {

        File dataRootFolder = new File(Constants.DATA_ROOT_PATH);
        File tempDataFolder = new File(Constants.TEMP_DATA_PATH);
        File archiveFolder = new File(Constants.ARCHIVE_DIRECTORY_PATH);
        File corruptedFolder = new File(Constants.CORRUPT_DIRECTORY_PATH);

        // This is only true the first the application runs

        if (dataRootFolder.mkdir()) {
            System.out.println("root data folder created");
        }

        if (tempDataFolder.mkdir()) {
            System.out.println("temp-data folder created");
        }

        if (archiveFolder.mkdir()) {
            System.out.println("archive folder created");
        }

        if (corruptedFolder.mkdir()) {
            System.out.println("corrupted folder created");
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