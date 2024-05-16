package org.ase;

import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;

import java.io.File;
import java.io.IOException;

public class Main {

    private final static File LAST_BACKUP_TIME_PATH = new File("DCIM/"); // Do not save it on the root of the phone as it automatically gets deleted

    public Main() {
        ConfigReader configReader = new ConfigReader();
        configReader.read();

        AndroidFtpClient ftpClient = new AndroidFtpClient(configReader.getIpAddress());
        FtpAccessor ftpAccessor = new FtpAccessor(ftpClient, configReader.getFolderPath());

        try {
            // TODO retry
            ftpAccessor.copyFileFrom(LAST_BACKUP_TIME_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}