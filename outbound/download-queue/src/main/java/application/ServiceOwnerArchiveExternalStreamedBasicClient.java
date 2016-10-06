package application;

import stream.IServiceOwnerArchiveExternalStreamedBasic;
import stream.ServiceOwnerArchiveExternalStreamedBasicSF;

import javax.xml.ws.BindingProvider;

/**
 * Created by andreas.naess on 05.10.2016.
 */

/**
 * This class interacts with the data streaming web service. For this communication to work, java classes will have to
 * be generated from the "ServiceOwnerArchiveExternalStreamedBasic.wsdl". The classes are automatically generated when
 * executing the "mvn clean package" command. The cxf-plugin with goal: wsdl2java handles this. As the classes are
 * auto-generated, they are put in the "target/generated-sources/cxf" directory.
 */
public class ServiceOwnerArchiveExternalStreamedBasicClient {

    private String serviceEndpoint;
    private String systemUsername;
    private String systemPassword;

    IServiceOwnerArchiveExternalStreamedBasic port;

    public ServiceOwnerArchiveExternalStreamedBasicClient(String serviceEndpoint, String systemUsername, String systemPassword) {
        this.serviceEndpoint = serviceEndpoint;
        this.systemUsername = systemUsername;
        this.systemPassword = systemPassword;

        port = new ServiceOwnerArchiveExternalStreamedBasicSF().
                getBasicHttpBindingIServiceOwnerArchiveExternalStreamedBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceEndpoint);
    }

    /**
     * Retrieves a large attachment using the IServiceOwnerArchiveExternalStreamedBasic interface based on attachment ID.
     * @param attachmentId A unique ID which identifies the attachment.
     * @return The attachment as a byte array.
     * @throws Exception Throws an exception if it failed to retrieve the attachment from the server.
     */
    public byte[] getDownloadQueueItems(int attachmentId) throws Exception {
        byte[] attachmentDataStreamedBasic = port.getAttachmentDataStreamedBasic(systemUsername, systemPassword, attachmentId);
        return attachmentDataStreamedBasic;
    }
}
