package org.ase.history;

import org.ase.ftp.FtpAccessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LastBackup {

    private final static Path LAST_BACKUP_FILE_PATH = Path.of("DCIM/LastBackup.txt");

    private final FtpAccessor ftpAccessor;
    private final Path destinationWorkPath;

    public LastBackup(FtpAccessor ftpAccessor, Path destinationWorkPath) {
        this.ftpAccessor = ftpAccessor;
        this.destinationWorkPath = destinationWorkPath;
    }

    public void loadLastBackup() {
        try {
            ftpAccessor.copyFileFrom(LAST_BACKUP_FILE_PATH, destinationWorkPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the backup time file: " + e.getMessage());
        }
    }

    public LocalDateTime readLastBackupTimeFromFile() {
        Path lastBackupFilePath = destinationWorkPath.resolve(LAST_BACKUP_FILE_PATH.getFileName());
        if (!Files.exists(lastBackupFilePath)) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(destinationWorkPath.resolve(LAST_BACKUP_FILE_PATH.getFileName()).toString()))) {
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new RuntimeException("Could not read the backup time file: " + LAST_BACKUP_FILE_PATH.getFileName());
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
        Path nowPath = destinationWorkPath.resolve("Now.txt");
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
            ftpAccessor.storeFileTo(LAST_BACKUP_FILE_PATH, nowFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the backup time file: " + e.getMessage());
        }
    }

    private String getDateTimeString() {
        return String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

}
