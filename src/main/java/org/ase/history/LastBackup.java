package org.ase.history;

import org.ase.ftp.FtpClient;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LastBackup {

    private final static Path LAST_BACKUP_FILE = Path.of("DCIM/LastBackup.txt");

    private final FtpClient ftpClient;
    private final Path destinationWorkFolder;

    public LastBackup(FtpClient ftpClient, Path destinationWorkFolder) {
        this.ftpClient = ftpClient;
        this.destinationWorkFolder = destinationWorkFolder;
    }

    public void loadLastBackup() {
        try {
            Path destinationLastBackupFile = destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName());
            if (!Files.exists(destinationLastBackupFile)) {
                ftpClient.downloadFile(LAST_BACKUP_FILE, destinationLastBackupFile);
            }
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Access to the ftp has been denied!\n" + e);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the file with the last backup time:\n" + e);
        }
    }

    public LocalDateTime readLastBackupTimeFromFile() {
        Path lastBackupFilePath = destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName());
        if (!Files.exists(lastBackupFilePath)) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName()).toString()))) {
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new RuntimeException("Could not read the file with the last backup time: " + LAST_BACKUP_FILE.getFileName());
            }
            double epochSeconds = Double.parseDouble(firstLine.trim());
            return LocalDateTime.ofEpochSecond((long) epochSeconds, 0, ZoneOffset.UTC);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format in the first line: " + e.getMessage());
        }
    }

    public Path createFileFromNow() {
        Path nowPath = destinationWorkFolder.resolve("Now.txt");
        File nowFile = nowPath.toFile();
        try (FileWriter fileWriter = new FileWriter(nowFile, true)) {
            fileWriter.write(getDateTimeString());
        } catch (IOException e) {
            throw new RuntimeException("Error creating Now.txt file: " + e.getMessage());
        }
        return nowPath;
    }

    public void storeNowFile(Path nowFile) {
        try {
            ftpClient.uploadFile(nowFile, LAST_BACKUP_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Could not update the file with the last backup time: " + e.getMessage());
        }
    }

    private String getDateTimeString() {
        return String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

}
