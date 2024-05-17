package org.ase;

import org.ase.config.Config;
import org.ase.config.ConfigReader;
import org.ase.config.SystemPreparation;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;
import org.ase.picture.TransferPictures;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SystemPreparation systemPreparation = new SystemPreparation(bufferedReader);
        systemPreparation.showWarningForExport();
        Path workingPath = Path.of("C:/Users/maese/Bilder/FromHandy");
        ConfigReader configReader = new ConfigReader(workingPath, bufferedReader);
        Config config = configReader.read();
        systemPreparation.prepareFolderPath(config.destinationWorkPath());

        AndroidFtpClient ftpClient = new AndroidFtpClient(config.ipAddress());
        FtpAccessor ftpAccessor = new FtpAccessor(ftpClient);

        LastBackup lastBackup = new LastBackup(config.destinationWorkPath());
        lastBackup.loadLastBackup(ftpAccessor);
        LocalDateTime lastBackupTime = lastBackup.readLastBackupTimeFromFile();

        TransferPictures transferPictures = new TransferPictures(ftpAccessor, lastBackupTime, config.destinationWorkPath());
        transferPictures.copy();
    }
}