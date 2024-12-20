package org.ase.fileAccess;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
                Files.createDirectories(directory);
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
        createDirectoryIfNotExists(destinationFile.getParent());
        try {
            Files.move(inputFile, destinationFile);
        } catch (IOException e) {
            System.err.println(inputFile.getFileName() + " could not be moved: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public long numberOfLines(Path inputFile) throws IOException {
        Stream<String> lines = Files.lines(inputFile);
        return lines.count();
    }

    public boolean fileExists(Path file) {
        return Files.exists(file);
    }

    public String readFirstLineInFile(Path file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file.toString()))) {
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new RuntimeException("Could not read the file " + file);
            }
            return firstLine;
        } catch (IOException e) {
            throw new RuntimeException("Error reading the file: " + e.getMessage());
        }
    }

    public void writeFile(Path file, String content) {
        createDirectoryIfNotExists(file.getParent());
        try (FileWriter fileWriter = new FileWriter(file.toFile(), true)) {
            fileWriter.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Error creating Now.txt file: " + e.getMessage());
        }
    }
}
