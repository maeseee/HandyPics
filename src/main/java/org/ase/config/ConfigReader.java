package org.ase.config;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Getter
public class ConfigReader {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final Path workingPath;

    public ConfigReader(Path workingPath) {
        this.workingPath = workingPath;
    }

    public Config read() {
        validateWorkingPath();

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
            if (Strings.isNullOrEmpty(inputString)) {
                return Optional.empty();
            }
            if (!isValidFolderName(inputString)) {
                return Optional.empty();
            }
            return Optional.of(Paths.get(workingPath + inputString));
        } catch (IOException e) {
            throw new RuntimeException("Could not read folder name");
        }
    }

    private boolean isValidFolderName(String inputString) {
        return inputString.matches("^[A-Za-z][A-Za-z0-9]*$");
    }
}
