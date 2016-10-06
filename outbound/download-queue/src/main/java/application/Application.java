package application;

import org.apache.log4j.Logger;

/**
 * Created by andreas.naess on 05.10.2016.
 */

/**
 * This is an example application which uses the download queue web service. It acts as a client, which requests items
 * from the download queue, writes each item to disk and finally marks the item as "purged" (completed).
 */
public class Application {

    final static Logger logger = Logger.getLogger(Application.class);

    private static final String SERVICE_ENDPOINT =
            "https://tt02.altinn.basefarm.net/ArchiveExternal/DownloadQueueExternalBasic.svc";
    private static final String SERVICE_STREAM_ENDPOINT =
            "https://tt02.altinn.basefarm.net/ArchiveExternal/ServiceOwnerArchiveExternalStreamedBasic.svc";

    private static final String SYSTEM_USERNAME = "AAS_TEST";
    private static final String SYSTEM_PASSWORD = "6GMSx5n8";
    private static final String SERVICE_CODE = "3811";
    private static final int LANGUAGE_ID = 1033;
    private static int limit = 2;

    public static void main(String[] args) {
        // Create the client used to communicate with the download queue web service
        DownloadQueueClient downloadQueueClient = new DownloadQueueClient(SERVICE_ENDPOINT, SYSTEM_USERNAME, SYSTEM_PASSWORD,
                SERVICE_CODE, LANGUAGE_ID);
        // Create the client used to communicate with the data stream web service
        ServiceOwnerArchiveExternalStreamedBasicClient streamClient = new ServiceOwnerArchiveExternalStreamedBasicClient
                (SERVICE_STREAM_ENDPOINT, SYSTEM_USERNAME, SYSTEM_PASSWORD);

        DownloadQueueHandler queueHandler = new DownloadQueueHandler(downloadQueueClient, streamClient, limit);
        // Process the download queue
        logger.info("Starting...");
        queueHandler.run();
    }
}
