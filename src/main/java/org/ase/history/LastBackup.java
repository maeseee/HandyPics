package org.ase.history;

import lombok.AllArgsConstructor;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpClient;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
public class LastBackup {

    private final static Path LAST_BACKUP_FILE = Path.of("DCIM/LastBackup.txt");

    private final FtpClient ftpClient;
    private final FileAccessor fileAccessor;
    private final Path destinationWorkFolder;

    public void loadLastBackup() {
        Path destinationLastBackupFile = destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName());
        if (fileAccessor.fileExists(destinationLastBackupFile)) {
            System.out.println("Last backup file exists and will not be overwritten!");
            return;
        }
        try {
            ftpClient.downloadFile(LAST_BACKUP_FILE, destinationLastBackupFile);
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Access to the ftp has been denied!\n" + e);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the file with the last backup time:\n" + e);
        }
    }

    public LocalDateTime readLastBackupTimeFromFile() {
        Path lastBackupFilePath = destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName());
        if (!fileAccessor.fileExists(lastBackupFilePath)) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        String firstLine = fileAccessor.readFirstLineInFile(destinationWorkFolder.resolve(LAST_BACKUP_FILE.getFileName()));
        try {
            double epochSeconds = Double.parseDouble(firstLine.trim());
            return LocalDateTime.ofEpochSecond((long) epochSeconds, 0, ZoneOffset.UTC);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format in the first line: " + e.getMessage());
        }
    }

    public Path createFileFromNow() {
        Path nowPath = destinationWorkFolder.resolve("Now.txt");
        fileAccessor.writeFile(nowPath, getDateTimeString());
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
