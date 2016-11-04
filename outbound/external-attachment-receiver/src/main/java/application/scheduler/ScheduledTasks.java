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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Created by andreas.naess on 01.11.2016.
 */

/**
 * This class handles the creation and delivery of correspondences.
 */
@Component
public class ScheduledTasks {

    final static Logger logger = Logger.getLogger(ScheduledTasks.class);

    @Autowired
    private ApplicationContext appContext;

    private File tempDataFolder;
    private File archiveFolder;
    private File corruptedFolder;

    private WatchService watcher;
    private Path dir;

    public ScheduledTasks() {
        this.tempDataFolder = new File(Constants.TEMP_DATA_PATH);
        this.archiveFolder = new File(Constants.ARCHIVE_DIRECTORY_PATH);
        this.corruptedFolder = new File(Constants.CORRUPT_DIRECTORY_PATH);

        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.dir = Paths.get(this.tempDataFolder.toURI());
            dir.register(watcher, ENTRY_CREATE);
            System.out.println("Watch Service registered for dir: " + dir.getFileName());
        } catch (IOException e) {
            logger.error("Could not watch the folder for changes. Server shutdown" + e);
            SpringApplication.exit(appContext);
        }
    }

    /**
     * This method loops at a fixed rate every 5th second. It checks if a new file has been created in the data/temp-data
     * folder. If a new file has been created, it will try and create and send a correspondence depending on the content of the
     * data batch. It will move the content to the archive or corrupted folder depending on whether the correspondence was
     * delivered successfully or not.
     */
    @Scheduled(fixedRate = 5000)
    public void handleCorrespondence() {

        // Watch key is used to watch for directory changes. In this case it only triggers if a new item has been created.
        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException ex) {
            return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            // This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can occur regardless if events
            // are lost or discarded.
            if (kind == OVERFLOW) {
                continue;
            }

            // The filename is the context of the event.
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            Path filename = ev.context();

            File dataBatchFolder = new File(Constants.TEMP_DATA_PATH + "/" + filename);

            // If the correspondence failed, then move the databatch to the corrupted folder.
            if (!processCorrespondence(dataBatchFolder)) {
                moveToCorruptedFolder(dataBatchFolder);
            }
        }

        // Reset the key -- this step is critical if you want to receive further watch events.
        // If the key is no longer valid, the directory is inaccessible so exit the loop.
        key.reset();
    }

    /**
     * Processes the correspondence
     *
     * @param dataBatchFolder
     * @return Returns true if success, false if it failed.
     */
    private boolean processCorrespondence(File dataBatchFolder) {

        File dataBatchFile = new File(dataBatchFolder.getPath() + "/" + dataBatchFolder.getName() + ".xml");

        DataBatch dataBatch;

        // Unmarshall the databatch
        try {
            dataBatch = unmarshalXML(dataBatchFile);
        } catch (Exception e) {
            logger.error("Unmarshalling error: " + e);
            return false;
        }

        // If the dataBatch is null, then something went wrong during the unmarshalling
        if (dataBatch == null) {
            logger.error("The data batch is null, nothing to send.");
            return false;
        }

        // Send Correspondences
        try {
            ArrayList<Pair<String, ReceiptExternal>> receiptList = createAndSendCorrespondences(dataBatch);
            boolean atLeastOneError = false;
            for (Pair<String, ReceiptExternal> receiptPair : receiptList) {
                if (receiptPair.getValue().getReceiptStatusCode() != ReceiptStatusEnum.OK) {
                    atLeastOneError = true;
                    logger.error("Error: " + receiptPair.getValue().getReceiptStatusCode() + " " +
                            receiptPair.getValue().getReceiptText().getValue() + " " +
                            "Unable to send correspondence with archive reference: " + receiptPair.getKey());
                }
            }
            if (atLeastOneError) {
                return false;
            }
        } catch (Exception e) {
            logger.error("Unable to send correspondence: " + e);
            return false;
        }

        // Move from temp folder to archive
        try {
            File destination = new File(this.archiveFolder.getPath() + File.separator + dataBatchFolder.getName());
            FileUtils.moveDirectory(dataBatchFolder, destination);
        } catch (IOException e) {
            logger.error("Unable to move the databatch folder(" + dataBatchFolder.getName() + ")to the archive folder: " + e);
        }
        return true;
    }

    private DataBatch unmarshalXML(File dataBatchFile) throws JAXBException, SAXException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DataBatch.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (DataBatch) unmarshaller.unmarshal(dataBatchFile);
    }

    private ArrayList<Pair<String, ReceiptExternal>> createAndSendCorrespondences(DataBatch dataBatch) throws IOException,
            DatatypeConfigurationException {
        ArrayList<Pair<String, ReceiptExternal>> receiptList = new ArrayList<>();

        CorrespondenceClient correspondenceClient = new CorrespondenceClient();

        for (DataUnit dataUnit : dataBatch.getDataUnits().getDataUnit()) {
            String archiveReference = dataUnit.getArchiveReference();
            String reportee = dataUnit.getReportee();

            ReceiptExternal receipt;
            try {
                receipt = correspondenceClient.createAndSendCorrespondence(archiveReference, reportee);
            } catch (ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage e) {
                logger.error("Something went wrong when sending correspondence: " + e);
                continue;
            }
            receiptList.add(new Pair(dataUnit.getArchiveReference(), receipt));
        }
        return receiptList;
    }

    private void moveToCorruptedFolder(File dataBatchFolder) {
        try {
            File destination = new File(this.corruptedFolder.getPath() + "/" + dataBatchFolder.getName());
            FileUtils.moveDirectory(dataBatchFolder, destination);
        } catch (IOException e) {
            logger.error("Unable to move the databatch folder(" + dataBatchFolder.getName() + ")to the corrupted folder: " + e);
        }
    }
}
