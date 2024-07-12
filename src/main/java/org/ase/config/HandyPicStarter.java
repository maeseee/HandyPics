package org.ase.config;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HandyPicStarter {
    private static final Path DESTINATION_ROOT = Path.of("C:/Users/maese/Bilder/FromHandy");

    private final BufferedReader reader;

    public Config readConfig() {
        showWarningForExport();
        ConfigReader configReader = new ConfigReader(DESTINATION_ROOT, reader);
        Config config = configReader.readConfig();
        prepareFolderPath(config.destinationRootFolder());
        return config;
    }

    private void showWarningForExport() {
        System.out.println("Have you copied the favourites pictures to the album \"Best\"?");
        anyUserInput();
    }

    @VisibleForTesting
    void prepareFolderPath(Path destinationWorkPath) {
        createFolderPathIfNotExists(destinationWorkPath);
        printWarningIfDestinationFolderNotEmpty(destinationWorkPath);
    }

    private void anyUserInput() {
        try {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input to continue");
        }
    }

    private void createFolderPathIfNotExists(Path destinationWorkPath) {
        try {
            Files.createDirectories(destinationWorkPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printWarningIfDestinationFolderNotEmpty(Path destinationWorkPath) {
        try (Stream<Path> stream = Files.list(destinationWorkPath)) {
            boolean hasContent = stream.findAny().isPresent();
            if (hasContent) {
                System.out.println("Destination folder is NOT empty! Press any key to continue...");
                anyUserInput();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error on opening the destination folder " + destinationWorkPath);
        }
    }
}
