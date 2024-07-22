package org.ase;

import com.google.common.collect.ImmutableList;
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

public class HandyPics {

    private final List<BackupFolder> FAVORIT_BACKUP_FOLDERS = ImmutableList.of(
            new BackupFolder(Path.of("DCIM/MyAlbums/Best"), "Camera"), // Favourites on Oppo
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner/best"), "Camera"), // Favourites old on Xiaomi
            new BackupFolder(Path.of("Pictures/Gallery/owner/best"), "Camera"), // Favourites on Xiaomi
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner"), "Camera") // Albums on Xiaomi
    );
    private final List<BackupFolder> BACKUP_FOLDERS = ImmutableList.of(
            new BackupFolder(Path.of("DCIM"), "Camera"),
            new BackupFolder(Path.of("Pictures"), "Signal"), // Signal on Oppo
            new BackupFolder(Path.of("Download/Bluetooth"), "Bluetooth"), // Bluetooth on Oppo
            new BackupFolder(Path.of("MIUI/ShareMe"), "Bluetooth"), // Bluetooth on Xiaomi
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp")
    );

    private final Transfer transfer;
    private final LastBackup lastBackup;

    public HandyPics(Transfer transfer, LastBackup lastBackup) {
        this.transfer = transfer;
        this.lastBackup = lastBackup;
    }

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

    static HandyPics getHandyPics(BufferedReader bufferedReader, Config config) {
        FtpClient ftpClient = new ApacheFtpClient(config.ipAddress());
        validateFtpAccess(ftpClient);
        FileAccessor fileAccessor = new FileAccessor();
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
