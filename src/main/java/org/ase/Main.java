package org.ase;

import org.ase.config.Config;
import org.ase.config.ConfigReader;
import org.ase.config.SystemPreparation;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        SystemPreparation systemPreparation = new SystemPreparation();
        systemPreparation.showWarningForExport();
        Path workingPath = Path.of("C:/Users/maese/Bilder/FromHandy");
        ConfigReader configReader = new ConfigReader(workingPath);
        Config config = configReader.read();
        systemPreparation.prepareFolderPath(config.folderPath());

        AndroidFtpClient ftpClient = new AndroidFtpClient(config.ipAddress());
        FtpAccessor ftpAccessor = new FtpAccessor(ftpClient, config.folderPath());

        LastBackup lastBackup = new LastBackup(config.folderPath());
        lastBackup.loadLastBackup(ftpAccessor);
    }
}