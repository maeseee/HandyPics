package org.ase;

import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Getter
public class ConfigReader {

    private String ipAddress;
    private String folderName;

    public void read() {
        while (ipAddress == null) {
            readFtpServerAddress();
        }
        while (folderName == null) {
            readFolderName();
        }
    }

    private void readFtpServerAddress() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the IP address of the FTP server:");
        try {
            String inputString = reader.readLine();
            boolean validIpAddress = InetAddresses.isInetAddress(inputString);
            if (validIpAddress) {
                ipAddress = inputString;
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read ip address");
        }
    }

    private void readFolderName() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the folder name of the destination:");
        try {
            folderName = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name");
        }
    }
}
