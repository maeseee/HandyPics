package org.ase;

import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Getter
public class ConfigReader {

    private final static String DESTINATION_PATH = "C:\\Users\\maese\\Bilder\\FromHandy\\";

    private String ipAddress;
    private String folderName;

    public void read() {
        while (ipAddress == null) {
            readFtpServerAddress();
        }

        validateDestinationPath();
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
            String inputString = reader.readLine();
            folderName = DESTINATION_PATH + inputString;
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name");
        }
    }

    private void validateDestinationPath() {
        boolean exists = new File(DESTINATION_PATH).exists();
        if (!exists) {
            throw new RuntimeException("Destination path does not exist");
        }
    }
}
