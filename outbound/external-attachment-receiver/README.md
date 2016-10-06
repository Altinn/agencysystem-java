# External Attachment Receiver
This SOAP web service server is built using the Spring framework (https://spring.io/guides/gs/producing-web-service/).
The web service consumes SOAP requests, performs XML-validation based on XSD-files, and saves the payload + attachments to disk. The payload and attachments will be written to a "data" folder in the server root folder.

What you will need to run this project:

- JDK 1.8 or later (http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html)
- Maven 3.0+ (https://maven.apache.org/download.cgi)

Both Java and Maven environment variables have to be set to run the application. (https://www.tutorialspoint.com/maven/maven_environment_setup.htm)

The application utilizes Spring Boot which embeds a Tomcat server within an executable .jar file. This means there is no need to install a specific server to run the application locally.

How to run:

1. Clone the project: https://github.com/Altinn/agencysystem-java.git
1. Navigate to the project root folder (agencysystem-java/outbound/external-attachment-receiver/)
2. Execute the command: **mvn clean package spring-boot:run** 
This will automatically create an executable jar, and run in it on the defualt port 8080

How to change the default port:

- Navigate to the project root folder
- Execute the command: **mvn clean package**
- Navigate to the "target" folder (agencysystem-java/outbound/external-attachment-receiver/target/)
- Execute the command: **java -jar external-attachment-receiver-1.0-SNAPSHOT --server.port="port number"** 

The soap-ui-tests folder contains a package of tests that can be imported into SOAP UI to test the endpoint.

Errors are written to a logging file in the server root folder.

The javadoc folder contains documentation of application package.