package application;

import application.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by andreas.naess on 29.09.2016.
 */

/**
 * Writes to disk, and performs duplication checks.
 */
public class FileHandler {

    private String receiversReference;

    final static Logger logger = Logger.getLogger(FileHandler.class);

    public FileHandler(String receiversReference) {
        this.receiversReference = receiversReference;
    }

    /**
     * Writes the databatch and attachments (if any) to disk. The content is written to the "data" folder within the
     * application root folder
     *
     * @param dataBatch   Escaped xml, the request payload
     * @param attachments External attachments are saved as .zip files
     * @return The path to the created databatch
     * @throws IOException Throws an exception if it fails to write to disk.
     */
    public File write(String dataBatch, byte[] attachments) throws IOException {
        if (attachments != null && attachments.length != 0) {
            File attachmentPath = new File(Constants.TEMP_DATA_PATH + "/" + receiversReference + "/" + receiversReference + ".zip");
            FileUtils.writeByteArrayToFile(attachmentPath, attachments);
        }

        File dataBatchPath = new File(Constants.TEMP_DATA_PATH + "/" + receiversReference + "/" + receiversReference + ".xml");
        FileUtils.write(dataBatchPath, dataBatch, "UTF-8");
        return dataBatchPath;
    }

    /**
     * Before writing the dataBatch to disk, it is important to check if it already exists.
     * This is accomplished by checking the receivers reference for uniqueness.
     *
     * @return True if the file exists, false if not.
     */
    public boolean fileExists() {
        File dataBatachArchiveFolder = new File(Constants.ARCHIVE_DIRECTORY_PATH + "/" + receiversReference);
        if (dataBatachArchiveFolder.exists()) {
            logger.error("A file with the same receivers reference(" + dataBatachArchiveFolder.getPath() + ")already exists; do not write to disk");
            return true;
        }

        File dataBatachCorruptFolder = new File(Constants.CORRUPT_DIRECTORY_PATH + "/" + receiversReference);
        if (dataBatachCorruptFolder.exists()) {
            logger.error("A file with the same receivers reference(" + dataBatachCorruptFolder.getPath() + ")already exists; do not write to disk");
            return true;
        }

        File dataBatachTempFolder = new File(Constants.TEMP_DATA_PATH + "/" + receiversReference);
        if (dataBatachCorruptFolder.exists()) {
            logger.error("A file with the same receivers reference(" + dataBatachTempFolder.getPath() + ")already exists; do not write to disk");
            return true;
        }
        return false;
    }
}