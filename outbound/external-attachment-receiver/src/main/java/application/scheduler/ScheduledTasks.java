package application.scheduler;

import application.correspondence.CorrespondenceClient;
import application.util.Constants;
import generated.DataBatch;
import generated.DataUnit;
import javafx.util.Pair;
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal;
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreas.naess on 01.11.2016.
 */

@Component
public class ScheduledTasks {

    final static Logger logger = Logger.getLogger(ScheduledTasks.class);

    private File tempDataFolder;
    private File archiveFolder;
    private File corruptedFolder;
    private List<Pair<String, ReceiptExternal>> archiveRefReceiptPairList;

    public ScheduledTasks() {
        this.tempDataFolder = new File(Constants.TEMP_DATA_PATH);
        this.archiveFolder = new File(Constants.ARCHIVE_DIRECTORY_PATH);
        this.corruptedFolder = new File(Constants.CORRUPT_DIRECTORY_PATH);
    }

    @Scheduled(fixedRate = 5000)
    public void handleCorrespondence() {
        File[] dataBatchFolders = tempDataFolder.listFiles();

        this.archiveRefReceiptPairList = new ArrayList<>();

        for (File dataBatchFolder : dataBatchFolders) {

            switch (processCorrespondence(dataBatchFolder)) {
                case UNMARSHALING_ERROR:
                    // Could not unmarshal from XML to java-object. The contract was not met. Should move the databatch
                    // to a corrupted files storage.
                    moveToCorruptedFolder(dataBatchFolder, false);
                    break;
                case MOVE_DATABATCH_ERROR:
                    // IO Error, Make sure you have read an write permissions
                    break;
                // If it reaches this point, then the databatch folder has successfully moved to the archive folder.
                case SOME_CORRESPONDENCES_FAILED:
                    // It managed to send some correspondence, while others failed. The data batch has been moved to
                    // archive, and the errors have been logged
                    break;
                case ALL_CORRESPONDENCES_FAILED:
                    // All correspondences failed, the databatch should be a corrupted files storage.
                    moveToCorruptedFolder(dataBatchFolder, true);
                case OK:
                    // Everything OK, do nothing.
                    break;
            }
        }
    }

    private CorrespondenceResult processCorrespondence(File dataBatchFolder) {

        File dataBatchFile = new File(dataBatchFolder.getPath() + "/" + dataBatchFolder.getName() + ".xml");

        DataBatch dataBatch;

        // Unmarshall the databatch
        try {
            dataBatch = unmarshalXML(dataBatchFile);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Unmarshalling error: " + e);
            return CorrespondenceResult.UNMARSHALING_ERROR;
        }

        // If the dataBatch is null, then something went wrong during the unmarshalling
        if (dataBatch == null) {
            return CorrespondenceResult.UNMARSHALING_ERROR;
        }

        // Move from temp folder to archive
        try {
            File destination = new File(this.archiveFolder.getPath() + "/" + dataBatchFolder.getName());
            FileUtils.moveDirectory(dataBatchFolder, destination);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Unable to move the databatch folder to archive: " + e);
            return CorrespondenceResult.MOVE_DATABATCH_ERROR;
        }

        // Send Correspondence
        try {
            createAndSendCorrespondences(dataBatch);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Unable to read the config file: " + e);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            logger.error("Unable to set dates: " + e);
        }

        return checkCorrespondenceStatusCodes();
    }

    private DataBatch unmarshalXML(File dataBatchFile) throws JAXBException, SAXException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DataBatch.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (DataBatch) unmarshaller.unmarshal(dataBatchFile);
    }

    /**
     * Creates and sends correspondence based on databatch. It also adds a pair of the receipt and status code to a
     * collection. This list is used to catch and log errors that can occur during this process.
     *
     * @param dataBatch
     * @throws IOException
     * @throws DatatypeConfigurationException
     * @throws ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage
     */
    private void createAndSendCorrespondences(DataBatch dataBatch) throws IOException,
            DatatypeConfigurationException {

        CorrespondenceClient correspondenceClient = new CorrespondenceClient();

        for (DataUnit dataUnit : dataBatch.getDataUnits().getDataUnit()) {
            String archiveReference = dataUnit.getArchiveReference();
            String reportee = dataUnit.getReportee();

            ReceiptExternal receipt;
            try {
                receipt = correspondenceClient.createAndSendCorrespondence(archiveReference, reportee);
            } catch (ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage e) {
                e.printStackTrace();
                logger.error("Something went wrong when sending correspondence: " + e);
                return;
            }
            this.archiveRefReceiptPairList.add(new Pair(archiveReference, receipt));
        }
    }

    /**
     * As multiple correspondences are created and sent depending on the content of the data batch,
     * it is important to track which ones that were not sent.
     *
     * @return Correspondence result codes.
     */
    private CorrespondenceResult checkCorrespondenceStatusCodes() {
        boolean atLeastOneCorrespondenceFailed = false;
        boolean atLeastOneCorrespondenceSucceeded = false;


        for (Pair<String, ReceiptExternal> archiveRefReceiptPair : this.archiveRefReceiptPairList) {
            if (archiveRefReceiptPair.getValue().getReceiptStatusCode() != ReceiptStatusEnum.OK) {
                atLeastOneCorrespondenceFailed = true;
                logger.error("Error: " + archiveRefReceiptPair.getValue().getReceiptStatusCode() + " " +
                        archiveRefReceiptPair.getValue().getReceiptText().getValue() + " " +
                        "Unable to send correspondence with archive reference: " + archiveRefReceiptPair.getKey());
            } else {
                atLeastOneCorrespondenceSucceeded = true;
            }
        }

        if (atLeastOneCorrespondenceSucceeded && atLeastOneCorrespondenceFailed) {
            return CorrespondenceResult.SOME_CORRESPONDENCES_FAILED;
        }

        if (atLeastOneCorrespondenceFailed && !atLeastOneCorrespondenceSucceeded) {
            return CorrespondenceResult.ALL_CORRESPONDENCES_FAILED;
        }

        return CorrespondenceResult.OK;
    }

    private void moveToCorruptedFolder(File dataBatchFolder, boolean folderInArchive) {
        try {
            String dataBatchFolderName = dataBatchFolder.getName();
            File destination = new File(this.corruptedFolder.getPath() + "/" + dataBatchFolderName);
            if (folderInArchive) {
                File archivedFolder = new File(this.archiveFolder + "/" + dataBatchFolderName);
                FileUtils.moveDirectory(archivedFolder, destination);
            }
            else{
                FileUtils.moveDirectory(dataBatchFolder, destination);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Unable to move the databatch folder to the corrupted folder: " + e);
        }
    }
}

enum CorrespondenceResult {
    DATA_BATCH_ALREADY_ARCHIVED, UNMARSHALING_ERROR, MOVE_DATABATCH_ERROR, OK, SOME_CORRESPONDENCES_FAILED, ALL_CORRESPONDENCES_FAILED
}
