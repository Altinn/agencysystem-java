# External Attachment Receiver
This SOAP web service server is built using the Spring framework (https://spring.io/guides/gs/producing-web-service/).
The web service consumes SOAP requests, performs XML-validation based on XSD-files, saves the payload + attachments to disk, and sends and creates correspondences.

The services used are:
- OnlineBatchReciever
- CorrespondenceAgencyExternalBasic

What you will need to run this project:

- JDK 1.7 or later http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html
- Maven 3.0+ https://maven.apache.org/download.cgi

Both Java and Maven environment variables have to be set to run the application https://www.tutorialspoint.com/maven/maven_environment_setup.htm

Before packaging and deploying the application, configure the following java-file: **src/main/java/application/util/Constants.java**
Set the path constants to where you want the data batches to be stored.
Also configure the properties-file **src/main/resources/log4j.properties**. Set the "log4j.appender.file.File" attribute to where you want the log file to be stored.

How to run:

1. Clone the project: https://github.com/Altinn/agencysystem-java.git
1. Navigate to the project root folder "agencysystem-java/outbound/external-attachment-receiver/"
2. Execute the command: **mvn clean package**. This will create a deployable war-file in the **target**-folder.
3. Deploy the war-file on the server of your choice.
4. The wsdl should now be accessible through **http://<ip-address>:<port>/external-attachment-receiver-1.0-SNAPSHOT/ws/externalattachment.wsdl**

The soap-ui-tests folder contains a package of tests that can be imported into SOAP UI to test the endpoint.

Incomming data batches are stored in a temp-data folder, before being moved to an archive-folder when the correspondences have been successfully delivered. If something went wrong during the correspondence process, the data batch will be moved to a corrupted folder.


The javadoc folder contains documentation of application package.

How to test:

1. Import **external-attachment-receiver-soapui-project.xml** from "agencysystem-java/outbound/external-attachment-receiver/soap-ui-tests" into soap UI.
2. Run Request 1 towards the running server. E.g. http://localhost:8080/external-attachment-receiver-1.0-SNAPSHOT/ws
