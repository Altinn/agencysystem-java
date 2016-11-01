package application.scheduler;

import application.correspondence.CorrespondenceClient;
import application.util.Constants;
import generated.DataBatch;
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by andreas.naess on 01.11.2016.
 */

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private File dataBatchDirectory;
    private File archiveDirectory;

    public ScheduledTasks() {
        dataBatchDirectory = new File(Constants.DATA_BATCH_DIRECTORY_PATH);
        archiveDirectory = new File(Constants.ARCHIVE_DIRECTORY_PATH);
    }

    @Scheduled(fixedRate = 1000)
    public void handleCorrespondence() {

        File[] dataBatchFolders = dataBatchDirectory.listFiles();

        if (dataBatchFolders.length > 0) {
            for (File dataBatchFolder : dataBatchFolders) {

                File dataBatchFile = new File(dataBatchFolder.getPath() + "/" + dataBatchFolder.getName() + ".xml");

                switch (processCorrespondence(dataBatchFile)) {
                    case UNMARSHALING_ERROR:
                        break;
                }
            }
        }
    }

    private CorrespondenceResult processCorrespondence(File dataBatchFile) {

        DataBatch dataBatch = null;

        // Unmarshall the databatch
        try {
            dataBatch = unmarshalXML(dataBatchFile);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        // If the dataBatch is null, then something went wrong during the unmarshalling
        if (dataBatch == null) {
            return CorrespondenceResult.UNMARSHALING_ERROR;
        }

        // Send Correspondence
        try {
            createAndSendCorrespondence(dataBatch);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        } catch (ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage e) {
            e.printStackTrace();
        }

        // Remove from temp

        return CorrespondenceResult.OK;
    }

    private DataBatch unmarshalXML(File dataBatchFile) throws JAXBException, SAXException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DataBatch.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (DataBatch) unmarshaller.unmarshal(dataBatchFile);
    }

    private ReceiptExternal createAndSendCorrespondence(DataBatch dataBatch) throws IOException, DatatypeConfigurationException,
            ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage {
        CorrespondenceClient correspondenceClient = new CorrespondenceClient();
        return correspondenceClient.createCorrespondence();
    }
}

enum CorrespondenceResult{
    UNMARSHALING_ERROR, OK
}
