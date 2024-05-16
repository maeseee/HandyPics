package org.ase;

import org.ase.ftp.FtpAccessor;

public class Main {
    public static void main(String[] args) {
        ConfigReader configReader = new ConfigReader();
        configReader.read();

        new FtpAccessor(configReader.getIpAddress(), configReader.getFolderPath());
    }
}