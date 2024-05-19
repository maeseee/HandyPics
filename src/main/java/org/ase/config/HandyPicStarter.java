package org.ase.config;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HandyPicStarter {
    private static final Path WORKING_PATH = Path.of("C:/Users/maese/Bilder/FromHandy");

    private final BufferedReader reader;

    public void showWarningForExport() {
        System.out.println("Have you copied the favourites pictures to the album \"Best\"?");
        try {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input to continue");
        }
    }

    public void prepareFolderPath(Path destinationWorkPath) {
        createFolderPathIfNotExists(destinationWorkPath);
        printWarningIfFolderPathNotEmpty(destinationWorkPath);
    }

    public Config readConfig() {
        ConfigReader configReader = new ConfigReader(WORKING_PATH, reader);
        return configReader.readConfig();
    }

    private void createFolderPathIfNotExists(Path destinationWorkPath) {
        try {
            Files.createDirectories(destinationWorkPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printWarningIfFolderPathNotEmpty(Path destinationWorkPath) {
        try (Stream<Path> stream = Files.list(destinationWorkPath)) {
            boolean hasContent = stream.findAny().isPresent();
            if (hasContent) {
                System.out.println("Destination folder is NOT empty!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error on opening destination folder " + destinationWorkPath);
        }
    }
}
