# Download Queue
This is a client that consumes elements from the Altinn download queue. The client requests items from the queue, saves them to disk, and marks them as "purged" (completed).

What you will need to run this project:

- JDK 1.8 or later http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html
- Maven 3.0+ https://maven.apache.org/download.cgi

Both Java and Maven environment variables have to be set to run the application https://www.tutorialspoint.com/maven/maven_environment_setup.htm

**How to run:** 

1. Clone the project: https://github.com/Altinn/agencysystem-java.git
2. Navigate to the project root folder "agencysystem-java/outbound/download-queue/"
3. Execute the command: **mvn clean package**
5. Execute the command: **java -jar target/download-queue-1.0-SNAPSHOT.jar**

**NOTES**:

- The project depends on auto-generated java files (generated from wsdl-files), thus it is important to perform a maven build before executing the application.
- The project can also be opened in an IDE preferrebly Intellij, and then packaged and executed from within the IDE.
