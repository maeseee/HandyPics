package org.ase.fileAccess;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FileAccessor {

    public List<Path> filesInDirectory(Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.filter(Files::isRegularFile)
                    .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteDirectory(Path sourceFolder) {
        try {
            FileUtils.deleteDirectory(sourceFolder.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDirectoryIfNotExists(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                System.err.println(directory + " could not be created!\n" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void moveFileIfNotExists(Path inputFile, Path destinationFile) {
        if (Files.exists(destinationFile)) {
            return;
        }
        try {
            Files.move(inputFile, destinationFile);
        } catch (IOException e) {
            System.err.println(inputFile.getFileName() + " could not be moved: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean fileExists(Path file) {
        return Files.exists(file);
    }
}
