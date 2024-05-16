package org.ase;

import com.google.common.net.InetAddresses;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Getter
public class ConfigReader {

    private final static String DESTINATION_PATH = "C:\\Users\\maese\\Bilder\\FromHandy\\";

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private String ipAddress;
    private Path folderPath;

    public void read() {
        readWarningForExport();

        while (ipAddress == null) {
            readFtpServerAddress();
        }

        validateDestinationPath();
        while (folderPath == null) {
            readFolderName();
        }
        createFolderPathIfNotExists();
        printWarningIfFolderPathNotEmpty();
    }

    private void readWarningForExport() {
        System.out.println("Have you copied the favourites pictures to the album \"Best\"?");
        try {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input to continue");
        }
    }

    private void readFtpServerAddress() {
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
        System.out.println("Enter the folder name of the destination:");
        try {
            String inputString = reader.readLine();
            folderPath = Paths.get(DESTINATION_PATH + inputString);
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

    private void createFolderPathIfNotExists() {
        try {
            Files.createDirectory(folderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printWarningIfFolderPathNotEmpty() {
        try (Stream<Path> stream = Files.list(folderPath)) {
            boolean hasContent = stream.findAny().isPresent();
            if (hasContent) {
                System.out.println("Destination folder is NOT empty!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error on opening destination folder " + folderPath.toString());
        }
    }
}
