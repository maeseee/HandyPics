package org.ase.history;

import org.ase.ftp.FtpAccessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LastBackup {

    private final static Path LAST_BACKUP_FILE_PATH = Path.of("DCIM/LastBackup.txt");

    private final Path folderPath;

    public LastBackup(Path folderPath) {
        this.folderPath = folderPath;
    }

    public void loadLastBackup(FtpAccessor ftpAccessor) {
        try {
            ftpAccessor.copyFileFrom(LAST_BACKUP_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the backup time file: " + e.getMessage());
        }
    }

    public LocalDateTime readLastBackupTimeFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(folderPath + "/" + LAST_BACKUP_FILE_PATH.getFileName()))) {
            String firstLine = br.readLine();
            if (firstLine != null) {
                double epochSeconds = Double.parseDouble(firstLine.trim());
                return LocalDateTime.ofEpochSecond((long) epochSeconds, 0, ZoneOffset.UTC);
            } else {
                System.err.println("The file is empty.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format in the first line: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = new LastBackup(Paths.get("C:\\Users\\maese\\Bilder\\FromHandy\\test\\")).readLastBackupTimeFromFile();
        System.out.println(localDateTime);

    }
}
