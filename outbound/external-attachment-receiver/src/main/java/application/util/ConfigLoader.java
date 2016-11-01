package application.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by andreas.naess on 01.11.2016.
 */
public class ConfigLoader {

    public static Properties Load() throws IOException {
        Properties properties = new Properties();
        InputStream configStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.xml");
        properties.loadFromXML(configStream);
        return properties;
    }
}
