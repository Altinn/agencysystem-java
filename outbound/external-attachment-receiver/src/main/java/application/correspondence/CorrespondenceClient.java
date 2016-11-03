package application.correspondence;

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal;
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2;
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2;
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ObjectFactory;
import no.altinn.services.serviceengine.correspondence._2009._10.CorrespondenceAgencyExternalBasicSF;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by andreas.naess on 28.10.2016.
 */
public class CorrespondenceClient {

    // Agency properties
    private static String agencyUsername;
    private static String agencyPassword;
    private static String systemUserCode;
    private static String externalReference = UUID.randomUUID().toString();

    // Correspondence properties
    private static String serviceCode;
    private static String serviceEdition;

    // Correspondence content properties
    private static String languageCode;
    private static String messageTitle;
    private static String messageSummary;
    private static String messageBody;

    private static XMLGregorianCalendar visibleDateTime;
    private static XMLGregorianCalendar allowSystemDeleteDateTime;
    private static XMLGregorianCalendar dueDateTime;

    ICorrespondenceAgencyExternalBasic port;

    private static final String CORRESPONDENCE_ENDPOINT_URI =
            "https://tt02.altinn.basefarm.net/ServiceEngineExternal/CorrespondenceAgencyExternalBasic.svc";

    public CorrespondenceClient() throws IOException, DatatypeConfigurationException {

        Properties properties = new Properties();

        InputStream configStream = this.getClass().getClassLoader().getResourceAsStream("config.xml");

        properties.loadFromXML(configStream);

        // Agency properties
        this.agencyUsername = properties.getProperty("agencyUsername");
        this.agencyPassword = properties.getProperty("agencyPassword");
        this.systemUserCode = properties.getProperty("systemUserCode");


        // Correspondence properties
        this.serviceCode = properties.getProperty("serviceCode");
        this.serviceEdition = properties.getProperty("serviceEdition");

        // Correspondence content properties
        this.languageCode = properties.getProperty("languageCode");
        this.messageTitle = properties.getProperty("messageTitle");
        this.messageSummary = properties.getProperty("messageSummary");
        this.messageBody = properties.getProperty("messageBody");

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        this.visibleDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        c.add(Calendar.DAY_OF_MONTH, 1);
        this.allowSystemDeleteDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        c.add(Calendar.DAY_OF_MONTH, 1);
        this.dueDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        this.port = new CorrespondenceAgencyExternalBasicSF().getBasicHttpBindingICorrespondenceAgencyExternalBasic();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                CORRESPONDENCE_ENDPOINT_URI);


    }

    /**
     * Creates and sends a correspondence after processing the databatch.
     *
     * @param archiveReference The data batch archive reference
     * @param reportee         The organization that should receive the correspondence
     * @return
     * @throws ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage
     */
    public ReceiptExternal createAndSendCorrespondence(String archiveReference, String reportee)
            throws ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage {

        InsertCorrespondenceV2 correspondence = new InsertCorrespondenceV2();
        ObjectFactory objectFactory = new ObjectFactory();

        // Ensures the correspondence is delivered to the correct organization
        correspondence.setReportee(objectFactory.createInsertCorrespondenceV2Reportee(reportee));
        correspondence.setArchiveReference(objectFactory.createInsertCorrespondenceV2ArchiveReference(archiveReference));

        correspondence.setServiceCode(objectFactory.createInsertCorrespondenceV2ServiceCode(this.serviceCode));
        correspondence.setServiceEdition(objectFactory.createInsertCorrespondenceV2ServiceEdition(this.serviceEdition));

        // Correspondence content
        ExternalContentV2 externalContentV2 = new ExternalContentV2();
        externalContentV2.setLanguageCode(objectFactory.createExternalContentV2LanguageCode(languageCode));
        externalContentV2.setMessageTitle(objectFactory.createExternalContentV2MessageTitle(messageTitle));
        externalContentV2.setMessageSummary(objectFactory.createExternalContentV2MessageSummary(messageSummary));
        externalContentV2.setMessageBody(objectFactory.createExternalContentV2MessageBody(messageBody));

        correspondence.setContent(objectFactory.createInsertCorrespondenceV2Content(externalContentV2));

        correspondence.setVisibleDateTime(this.visibleDateTime);
        correspondence.setAllowSystemDeleteDateTime(objectFactory.
                createInsertCorrespondenceV2AllowSystemDeleteDateTime(this.allowSystemDeleteDateTime));
        correspondence.setDueDateTime(this.dueDateTime);

        return port.insertCorrespondenceBasicV2(this.agencyUsername, this.agencyPassword, this.systemUserCode, this.externalReference,
                correspondence);
    }
}
