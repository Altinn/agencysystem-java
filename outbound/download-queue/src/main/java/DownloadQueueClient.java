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

    private String serviceEndpoint;
    private String systemUsername;
    private String systemPassword;
    private String serviceCode;
    private int languageId;

    public DownloadQueueClient(String serviceEndpoint, String systemUsername, String systemPassword,
                               String serviceCode, int languageId) {
        this.serviceEndpoint = serviceEndpoint;
        this.systemUsername = systemUsername;
        this.systemPassword = systemPassword;
        this.serviceCode = serviceCode;
        this.languageId = languageId;
    }

    public List<DownloadQueueItemBE> getDownloadQueueItems() throws Exception {
        IDownloadQueueExternalBasic port = new DownloadQueueExternalBasic().getBasicHttpBindingIDownloadQueueExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
        DownloadQueueItemBEList downloadQueueItems = port.getDownloadQueueItems(systemUsername, systemPassword, serviceCode);
        return downloadQueueItems.getDownloadQueueItemBE();
    }

    public byte[] getFormSetPdfBasic(String archiveReference) throws Exception {
        IDownloadQueueExternalBasic port = new DownloadQueueExternalBasic().getBasicHttpBindingIDownloadQueueExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
        byte[] formSetPdfBasic = port.getFormSetPdfBasic(systemUsername, systemPassword,
                archiveReference, languageId);
        return formSetPdfBasic;
    }

    public ArchivedAttachmentExternalListDQBE getArchivedFormTaskBasicDQ(String archiveReference) throws Exception {
        IDownloadQueueExternalBasic port = new DownloadQueueExternalBasic().getBasicHttpBindingIDownloadQueueExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
        ArchivedFormTaskDQBE archivedFormTaskBasicDQ = port.getArchivedFormTaskBasicDQ(systemUsername, systemPassword,
                archiveReference, languageId);
        return archivedFormTaskBasicDQ.getAttachments().getValue();
    }

    public String purgeItem(String archiveReference) throws Exception {
        IDownloadQueueExternalBasic port = new DownloadQueueExternalBasic().getBasicHttpBindingIDownloadQueueExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
        String status = port.purgeItem(systemUsername, systemPassword, archiveReference);
        return status;
    }
}
