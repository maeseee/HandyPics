package org.ase;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.config.Config;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;
import org.ase.transfer.TransferPictures;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class HandyPics {

    private final Config config;
    private final FtpAccessor ftpAccessor;

    private LocalDateTime lastBackupTime;

    public HandyPics(Config config) {
        this.config = config;

        AndroidFtpClient ftpClient = new AndroidFtpClient(config.ipAddress());
        this.ftpAccessor = new FtpAccessor(ftpClient);
    }

    public void transferImagesFromHandy() {
        loadLastBackupTime();

        TransferPictures transferPictures = new TransferPictures(ftpAccessor, lastBackupTime, config.destinationWorkPath());
        transferPictures.copy();
    }

    public void setRatingOnBestPics() {
        ImageModifier imageModifier = new ImageModifier();
        Path folderBest = config.destinationWorkPath().resolve("Best");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderBest)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    try {
                        imageModifier.setJpegRating(file, file, 5);
                    } catch (ImageReadException | ImageWriteException | UnsupportedFileTypeException e) {
                        System.err.println(file.getFileName() + " could not be starred:" + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void loadLastBackupTime() {
        LastBackup lastBackup = new LastBackup(config.destinationWorkPath());
        lastBackup.loadLastBackup(ftpAccessor);
        lastBackupTime = lastBackup.readLastBackupTimeFromFile();
    }
}
