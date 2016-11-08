package application;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

/**
 * Created by andreas.naess on 29.09.2016.
 */

/**
 * Contains the web service configuration. Exposes the wsdl to: http://ip-address:port/ws/externalattachment
 */
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    /**
     * Configures the endpoint url. The endpoint is reached from the following url: http://ip-address:port/ws/
     * @param applicationContext The ApplicationContext is the central interface within a Spring application for
     * providing configuration information to the application.
     * @return A servlet mapping used to configure the endpoint URL
     */
    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/ws/*");
    }

    /**
     * Publishes the wsdl to: http://ip-address:port/ws/externalattachment.wsdl
     *
     * @return The wsdl definition
     */
    @Bean(name = "externalattachment")
    public Wsdl11Definition defaultWsdl11Definition() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/wsdl/OnlineBatchReciever.wsdl"));
        return wsdl11Definition;
    }
}
