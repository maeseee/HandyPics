package org.ase.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class ConfigReader {

    private final Path workingPath;
    private final BufferedReader reader;

    public Config readConfig() {
        validateWorkingPath();

        Optional<String> ipAddress;
        do {
            ipAddress = readFtpServerAddress();
        } while (ipAddress.isEmpty());

        Optional<Path> destinationFolder;
        do {
            destinationFolder = readDestinationFolder();
        } while (destinationFolder.isEmpty());

        return new Config(ipAddress.get(), destinationFolder.get());
    }

    private void validateWorkingPath() {
        boolean exists = Files.exists(workingPath);
        if (!exists) {
            throw new RuntimeException("Destination path does not exist");
        }
    }

    private Optional<String> readFtpServerAddress() {
        System.out.println("Enter the IP address of the FTP server:");
        try {
            String inputString = reader.readLine();
            if (isValidIpAddress(inputString)) {
                return Optional.of(inputString);
            }
            int numberInSubnet = Integer.parseInt(inputString);
            String ipAddress = "192.168.50." + numberInSubnet;
            if (isValidIpAddress(ipAddress)) {
                return Optional.of(ipAddress);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read ip address");
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @VisibleForTesting
    boolean isValidIpAddress(String inputString) {
        return InetAddresses.isInetAddress(inputString);
    }

    private Optional<Path> readDestinationFolder() {
        System.out.println("Enter the folder name of the destination:");
        try {
            String inputString = reader.readLine();
            if (Strings.isNullOrEmpty(inputString)) {
                return Optional.empty();
            }
            if (!isValidFolderName(inputString)) {
                return Optional.empty();
            }
            return Optional.of(workingPath.resolve(inputString));
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name of the destination");
        }
    }

    @VisibleForTesting
    boolean isValidFolderName(String inputString) {
        return inputString.matches("^[A-Za-z][A-Za-z0-9]*$");
    }
}
