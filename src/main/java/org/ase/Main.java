package org.ase;

import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;

public class Main {
    public static void main(String[] args) {
        ConfigReader configReader = new ConfigReader();
        configReader.read();

        AndroidFtpClient ftpClient = new AndroidFtpClient(configReader.getIpAddress());
        new FtpAccessor(ftpClient, configReader.getFolderPath());
    }
}