package org.ase.config;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import org.ase.fileAccess.FileAccessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class HandyPicStarter {
    private static final Path DESTINATION_ROOT = Path.of("C:/Users/maese/Bilder/FromHandy");

    private final FileAccessor fileAccessor;
    private final BufferedReader reader;

    public Config readConfig() {
        showWarningForExport();
        ConfigReader configReader = new ConfigReader(fileAccessor, DESTINATION_ROOT, reader);
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
        fileAccessor.createDirectoryIfNotExists(destinationWorkPath);
        printWarningIfDestinationFolderNotEmpty(destinationWorkPath);
    }

    private void anyUserInput() {
        try {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input to continue");
        }
    }

    private void printWarningIfDestinationFolderNotEmpty(Path destinationWorkPath) {
        if (!fileAccessor.filesInDirectory(destinationWorkPath).isEmpty()) {
            System.out.println("Destination folder is NOT empty! Press any key to continue...");
            anyUserInput();
        }
    }
}
