package org.ase;

import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Getter
public class ConfigReader {

    String ipAddress;
    String folderName;

    public void read() {
        ipAddress = readFtpServerAddress().orElseThrow(); // TODO retry
        folderName = readFolderName();
    }

    private Optional<String> readFtpServerAddress() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the IP address of the FTP server:");
        try {
            String inputString = reader.readLine();
            boolean validIpAddress = InetAddresses.isInetAddress(inputString);
            return validIpAddress ? Optional.of(inputString) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String readFolderName() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the folder name of the destination:");
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name");
        }
    }
}
