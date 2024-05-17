package org.ase.config;

import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Getter
public class ConfigReader {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public Config read() {
        Optional<String> ipAddress;
        do {
            ipAddress = readFtpServerAddress();
        } while (ipAddress.isEmpty());

        Optional<Path> folderName;
        do {
            folderName = readFolderName();
        } while (folderName.isEmpty());

        return new Config(ipAddress.get(), folderName.get());
    }

    private Optional<String> readFtpServerAddress() {
        System.out.println("Enter the IP address of the FTP server:");
        try {
            String inputString = reader.readLine();
            boolean validIpAddress = InetAddresses.isInetAddress(inputString);
            if (validIpAddress) {
                return Optional.of(inputString);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read ip address");
        }
        return Optional.empty();
    }

    private Optional<Path> readFolderName() {
        System.out.println("Enter the folder name of the destination:");
        try {
            String inputString = reader.readLine();
            return Optional.of(Paths.get(SystemPreparation.DESTINATION_PATH + inputString));
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name");
        }
    }
}
