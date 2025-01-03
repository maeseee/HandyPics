package org.ase;

import lombok.AllArgsConstructor;
import org.ase.config.Config;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.ApacheFtpClient;
import org.ase.ftp.FtpClient;
import org.ase.history.LastBackup;
import org.ase.image.ImageModifier;
import org.ase.transfer.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
public class HandyPics {

    private static final List<BackupFolder> FAVORIT_BACKUP_FOLDERS = List.of(
            new BackupFolder(Path.of("DCIM/MyAlbums/Best"), "Camera"), // Favourites on Oppo
            new BackupFolder(Path.of("Pictures/best"), "Camera") // Favourites on Pixel
    );
    private static final List<BackupFolder> BACKUP_FOLDERS = List.of(
            new BackupFolder(Path.of("DCIM"), "Camera"),
            new BackupFolder(Path.of("Pictures"), "Signal"), // Signal on Oppo
            new BackupFolder(Path.of("Download/Bluetooth"), "Bluetooth"), // Bluetooth on Oppo
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp")
    );

    private final Transfer transfer;
    private final LastBackup lastBackup;

    public void transferImagesFromHandy() {
        LocalDateTime lastBackupTime = loadLastBackupTime();

        transfer.backupPicturesInFolders(FAVORIT_BACKUP_FOLDERS, lastBackupTime, true);
        transfer.backupPicturesInFolders(BACKUP_FOLDERS, lastBackupTime, false);

        updateLastBackupTime();
    }

    private LocalDateTime loadLastBackupTime() {
        lastBackup.loadLastBackup();
        LocalDateTime lastBackupTime = lastBackup.readLastBackupTimeFromFile();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        System.out.println("Last backup time: " + lastBackupTime.format(formatter));
        return lastBackupTime;
    }

    private void updateLastBackupTime() {
        Path nowFile = lastBackup.createFileFromNow();
        lastBackup.storeNowFile(nowFile);
    }

    static HandyPics createHandyPics(FileAccessor fileAccessor, BufferedReader bufferedReader, Config config) {
        FtpClient ftpClient = new ApacheFtpClient(config.ipAddress());
        validateFtpAccess(ftpClient);
        TransferFile transferFile = new TransferFile(ftpClient, fileAccessor, new Retry(bufferedReader));
        TransferFolder transferFolder = new TransferFolder(transferFile, new Retry(bufferedReader));
        Transfer transfer = new Transfer(transferFolder, fileAccessor, config.destinationRootFolder(), new ImageModifier());
        LastBackup lastBackup = new LastBackup(ftpClient, fileAccessor, config.destinationRootFolder());
        return new HandyPics(transfer, lastBackup);
    }

    private static void validateFtpAccess(FtpClient ftpClient) {
        try {
            ftpClient.checkConnection();
        } catch (IOException e) {
            throw new RuntimeException("No connection to the FTP server!\n" + e);
        }
    }
}
