package org.ase.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class SystemPreparation {

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
        createFolderPathIfNotExists(folderPath);
        printWarningIfFolderPathNotEmpty(folderPath);
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
