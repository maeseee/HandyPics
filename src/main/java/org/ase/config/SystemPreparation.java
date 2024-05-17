package org.ase.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class SystemPreparation {

    public final static Path DESTINATION_PATH = Path.of("C:/Users/maese/Bilder/FromHandy");

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void showWarningForExport() {
        System.out.println("Have you copied the favourites pictures to the album \"Best\"?");
        try {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input to continue");
        }
    }

    public void prepareFolderPath(Path folderPath) {
        validateDestinationPath();
        createFolderPathIfNotExists(folderPath);
        printWarningIfFolderPathNotEmpty(folderPath);
    }

    private void validateDestinationPath() {
        boolean exists = Files.exists(DESTINATION_PATH);
        if (!exists) {
            throw new RuntimeException("Destination path does not exist");
        }
    }

    private void createFolderPathIfNotExists(Path folderPath) {
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printWarningIfFolderPathNotEmpty(Path folderPath) {
        try (Stream<Path> stream = Files.list(folderPath)) {
            boolean hasContent = stream.findAny().isPresent();
            if (hasContent) {
                System.out.println("Destination folder is NOT empty!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error on opening destination folder " + folderPath);
        }
    }
}
