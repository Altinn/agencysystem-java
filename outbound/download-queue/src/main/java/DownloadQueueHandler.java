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
     * Processes the download queue in the following order:
     * - Get the download queue
     * - Loop through the queue. The limit variable limits the number of loops, as it is time consuming to process all
     * elements.
     * - Write each item which includes a pdf and attachment to disk
     * - Mark the item as purged, so that it cannot be fetched again
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

    private void purgeItem(String archiveReference) throws Exception {
        try {
            downloadQueueClient.purgeItem(archiveReference);
        } catch (Exception e) {
            logger.error("Failed to purge attachment with archive reference: " + archiveReference + " " + e);
            throw e;
        }
    }

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
