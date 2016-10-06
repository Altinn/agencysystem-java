import downloadqueue.ArchivedAttachmentDQBE;
import downloadqueue.DownloadQueueItemBE;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by andreas.naess on 05.10.2016.
 */

/**
 * Handles the processing of the download queue.
 */
public class DownloadQueueHandler {

    private DownloadQueueClient downloadQueueClient;
    ServiceOwnerArchiveExternalStreamedBasicClient streamClient;
    private int limit;

    final static Logger logger = Logger.getLogger(DownloadQueueHandler.class);

    public DownloadQueueHandler(DownloadQueueClient downloadQueueClient,
                                ServiceOwnerArchiveExternalStreamedBasicClient streamClient, int limit) {
        this.downloadQueueClient = downloadQueueClient;
        this.streamClient = streamClient;
        this.limit = limit;
    }

    /**
     * Processes the download queue.
     */
    public void run() {
        // Get the download queue.
        List<DownloadQueueItemBE> downloadQueue;
        try {
            downloadQueue = getDownloadQueue();
        } catch (Exception e) {
            return;
        }

        // If the queue is null, then it either failed to retrieve the queue, or it didn't contain any items
        if (downloadQueue == null) {
            return;
        }

        // Process the download queue. Can be time consuming.
        logger.info("Processing items...");
        for (int i = 0; i < limit; i++) {
            String archiveReference = downloadQueue.get(i).getArchiveReference().getValue();
            try {
                // Gets the pdf from the archive reference
                byte[] pdf = getPdf(archiveReference);

                // Writes the pdf to disk
                if (pdf != null) {
                    writePdf(pdf, archiveReference);
                }

                // Gets the attachments from the archive reference
                List<ArchivedAttachmentDQBE> attachments = getArchiveFormTaskAttachments(archiveReference);

                for (ArchivedAttachmentDQBE attachment : attachments) {

                    byte[] attachmentData;

                    // If attachment data is greater than 30MB, then use the stream.
                    if (attachment.getAttachmentData() == null) {
                        logger.info("Retrieving large attachment...");
                        attachmentData = getLargeAttachment(attachment.getAttachmentId());
                    } else {
                        attachmentData = attachment.getAttachmentData().getValue();
                    }

                    String attachmentFileName = attachment.getFileName().getValue();

                    // Writes the attachment to disk
                    writeAttachment(attachmentData, archiveReference, attachmentFileName);
                }

                // Purge download queue item based on archive reference
                purgeItem(archiveReference);
            } catch (IOException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
            logger.info("Element with archive ref: " + archiveReference + " completed");
        }
    }

    /**
     * Retrieves pdf based on archive reference
     *
     * @param archiveReference Used to identify items in the download queue.
     * @return The pdf as a byte array
     * @throws Exception Throws an exception if it failed to retrieve the pdf from the server
     */
    private byte[] getPdf(String archiveReference) throws Exception {
        byte[] pdf;

        // Gets the pdf
        try {
            pdf = downloadQueueClient.getFormSetPdfBasic(archiveReference);
        } catch (Exception e) {
            logger.error("Failed to get pdf" + e);
            throw e;
        }
        return pdf;
    }

    /**
     * Retrieves a large attachment based on archive reference
     *
     * @param attachmentId Used to identify the attachment.
     * @return The attachment as a byte array
     * @throws Exception Throws an exception if it failed to retrieve the attachment from the server
     */
    private byte[] getLargeAttachment(int attachmentId) throws Exception {
        byte[] attachment;

        // Gets the large attachment
        try {
            attachment = streamClient.getDownloadQueueItems(attachmentId);
        } catch (Exception e) {
            logger.error("Failed to get large attachment with ID: " + attachmentId + " " + e);
            throw e;
        }
        return attachment;
    }

    /**
     * Retrieves attachments based on archive reference
     *
     * @param archiveReference Used to identify items in the download queue.
     * @return A list of archived attachments
     * @throws Exception Throws and exception if it failed to retrieve the attachments form the server
     */
    private List<ArchivedAttachmentDQBE> getArchiveFormTaskAttachments(String archiveReference) throws Exception {
        List<ArchivedAttachmentDQBE> archivedAttachmentDQBE;

        // Gets the attachments
        try {
            archivedAttachmentDQBE = downloadQueueClient.getArchivedFormTaskBasicDQ(archiveReference).
                    getArchivedAttachmentDQBE();
        } catch (Exception e) {
            logger.error("Failed to get archived attachments" + e);
            throw e;
        }
        return archivedAttachmentDQBE;
    }

    /**
     * Marks an item in the download queue as purged. It can no longer be fetches by "getDownloadQueueItems"
     *
     * @param archiveReference A download queue item ID.
     * @throws Exception Throws an exception if the purge failed.
     */
    private void purgeItem(String archiveReference) throws Exception {
        try {
            downloadQueueClient.purgeItem(archiveReference);
        } catch (Exception e) {
            logger.error("Failed to purge attachment with archive reference: " + archiveReference + " " + e);
            throw e;
        }
    }

    /**
     * Retrives the download queue
     *
     * @return Download queue items
     * @throws Exception Throws an exception if it failed to retrive the download queue.
     */
    private List<DownloadQueueItemBE> getDownloadQueue() throws Exception {
        List<DownloadQueueItemBE> downloadQueueItems;
        // Retrieves the download queue
        try {
            downloadQueueItems = downloadQueueClient.getDownloadQueueItems();
        } catch (Exception e) {
            logger.error("Failed to get download queue items" + e);
            throw e;
        }

        if (downloadQueueItems == null) {
            logger.info("The download queue is empty");
            return null;
        }
        logger.info("Download queue items count: " + downloadQueueItems.size());
        return downloadQueueItems;
    }

    /**
     * Writes the pdf to disk in a folder "data" in the project root folder
     *
     * @param pdf              Byte array of the pdf to be written
     * @param archiveReference The archive reference is used to name the folder and pdf written in the "data" folder
     * @throws IOException Throws and logs an exception if the writing failed
     */
    private void writePdf(byte[] pdf, String archiveReference) throws IOException {
        File dataRootFolder = new File("data/");
        File dataArchiveFolder = new File("data/" + archiveReference);
        dataRootFolder.mkdir();
        dataArchiveFolder.mkdir();
        File filePath = new File("data/" + archiveReference + "/" + archiveReference + ".pdf");
        try {
            FileUtils.writeByteArrayToFile(filePath, pdf);
        } catch (IOException e) {
            logger.error("Failed to write pdf with archive reference:  " + archiveReference + " " + e);
            throw e;
        }
    }

    /**
     * Writes the attachments to disk in the path "data/'archive-reference'", where the archive reference is the item's
     * unique ID.
     *
     * @param attachmentData   Byte array of the attachment to be written
     * @param archiveReference The archive reference is used to get the correct file path.
     * @param fileName         Used to name the attachment with the correct extension.
     * @throws IOException Throws and logs an exception if the writing failed
     */
    private void writeAttachment(byte[] attachmentData, String archiveReference, String fileName) throws IOException {
        File filePath = new File("data/" + archiveReference + "/" + fileName);
        try {
            FileUtils.writeByteArrayToFile(filePath, attachmentData);
        } catch (IOException e) {
            logger.error("Failed to write: " + fileName + " " + e);
            throw e;
        }
    }
}
