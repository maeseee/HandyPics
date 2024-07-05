package org.ase;

import org.ase.config.Config;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;
import org.ase.transfer.TransferPictures;

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

        TransferPictures transferPictures = new TransferPictures(ftpAccessor, lastBackupTime, config.destinationRootFolder());
        transferPictures.copy();

        // TODO save backup time
    }

    private void loadLastBackupTime() {
        LastBackup lastBackup = new LastBackup(config.destinationRootFolder());
        lastBackup.loadLastBackup(ftpAccessor);
        lastBackupTime = lastBackup.readLastBackupTimeFromFile();
    }
}
