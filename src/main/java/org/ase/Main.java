package org.ase;

import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;

public class Main {

    public Main() {
        ConfigReader configReader = new ConfigReader();
        configReader.read();

        AndroidFtpClient ftpClient = new AndroidFtpClient(configReader.getIpAddress());
        FtpAccessor ftpAccessor = new FtpAccessor(ftpClient, configReader.getFolderPath());

        LastBackup lastBackup = new LastBackup(configReader.getFolderPath());
        lastBackup.loadLastBackup(ftpAccessor);
    }

    public static void main(String[] args) {
        new Main();
    }
}