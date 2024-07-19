package org.ase;

import com.google.common.collect.ImmutableList;
import org.ase.history.LastBackup;
import org.ase.transfer.BackupFolder;
import org.ase.transfer.TransferPictures;

import java.nio.file.Path;
import java.time.LocalDateTime;
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
            new BackupFolder(Path.of("Bluetooth"), "Bluetooth"), // Bluetooth on Oppo
            new BackupFolder(Path.of("MIUI/ShareMe"), "Bluetooth"), // Bluetooth on Xiaomi
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp")
    );

    private final TransferPictures transferPictures;
    private final LastBackup lastBackup;

    public HandyPics(TransferPictures transferPictures, LastBackup lastBackup) {
        this.transferPictures = transferPictures;
        this.lastBackup = lastBackup;
    }

    public void transferImagesFromHandy() {
        LocalDateTime lastBackupTime = loadLastBackupTime();

        transferPictures.copy(FAVORIT_BACKUP_FOLDERS, lastBackupTime, true);
        transferPictures.copy(BACKUP_FOLDERS, lastBackupTime, false);

        updateLastBackupTime();
    }

    private LocalDateTime loadLastBackupTime() {
        lastBackup.loadLastBackup();
        LocalDateTime lastBackupTime = lastBackup.readLastBackupTimeFromFile();
        System.out.println("Last backup time: " + lastBackupTime);
        return lastBackupTime;
    }

    private void updateLastBackupTime() {
        Path nowFile = lastBackup.createFileFromNow();
        lastBackup.storeNowFile(nowFile);
    }
}
