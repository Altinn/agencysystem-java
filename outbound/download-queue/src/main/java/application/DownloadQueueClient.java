package application;

import downloadqueue.*;

import javax.xml.ws.BindingProvider;
import java.util.List;

/**
 * Created by andreas.naess on 05.10.2016.
 */

/**
 * This class interacts with the download queue web service. For this communication to work, java classes will have to
 * be generated from the "DownloadQueueExternalBasic.wsdl". The classes are automatically generated when executing the
 * "mvn clean package" command. The cxf-plugin with goal: wsdl2java handles this. As the classes are auto-generated,
 * they are put in the "target/generated-sources/cxf" directory.
 */
public class DownloadQueueClient {

    private String systemUsername;
    private String systemPassword;
    private String serviceCode;
    private int languageId;
    IDownloadQueueExternalBasic port;

    public DownloadQueueClient(String serviceEndpoint, String systemUsername, String systemPassword,
                               String serviceCode, int languageId) {
        this.systemUsername = systemUsername;
        this.systemPassword = systemPassword;
        this.serviceCode = serviceCode;
        this.languageId = languageId;

        this.port = new DownloadQueueExternalBasic().getBasicHttpBindingIDownloadQueueExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
    }

    /**
     * Retrieves the download queue by using the IDownloadQueueExternalBasic interface.
     *
     * @return A list of download queue items.
     * @throws Exception Throws an exception if it failed to retrieve the items.
     */
    public List<DownloadQueueItemBE> getDownloadQueueItems() throws Exception {
        DownloadQueueItemBEList downloadQueueItems = port.getDownloadQueueItems(systemUsername, systemPassword, serviceCode);
        return downloadQueueItems.getDownloadQueueItemBE();
    }

    /**
     * Retrieves the pdf using the IDownloadQueueExternalBasic interface based on archive reference.
     *
     * @param archiveReference Used to identify the item on the download queue.
     * @return The pdf as a byte array.
     * @throws Exception Throws an exception if it failed to retrieve the pdf from the server
     */
    public byte[] getFormSetPdfBasic(String archiveReference) throws Exception {
        byte[] formSetPdfBasic = port.getFormSetPdfBasic(systemUsername, systemPassword, archiveReference, languageId);
        return formSetPdfBasic;
    }

    /**
     * Retrieves attachments using the IDownloadQueueExternalBasic interface based on archive reference.
     *
     * @param archiveReference Used to identify the item on the download queue.
     * @return A list of archived attachments.
     * @throws Exception Throws and exception if it failed to retrieve the attachments form the server.
     */
    public ArchivedAttachmentExternalListDQBE getArchivedFormTaskBasicDQ(String archiveReference) throws Exception {
        ArchivedFormTaskDQBE archivedFormTaskBasicDQ = port.getArchivedFormTaskBasicDQ(systemUsername, systemPassword,
                archiveReference, languageId);
        return archivedFormTaskBasicDQ.getAttachments().getValue();
    }

    /**
     * Using the IDownloadQueueExternalBasic interface, it marks  an item on the download queue as purged. It can no longer be fetches by "getDownloadQueueItems".
     *
     * @param archiveReference Used to identify the item on the download queue.
     * @throws Exception Throws an exception if the purge failed.
     */
    public void purgeItem(String archiveReference) throws Exception {
        port.purgeItem(systemUsername, systemPassword, archiveReference);
    }
}
