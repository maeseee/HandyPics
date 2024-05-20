package org.ase.transfer;

import lombok.RequiredArgsConstructor;
import org.ase.ftp.FtpAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TransferPictures {

    private final static List<BackupFolder> BACKUP_FOLDERS = List.of(
            new BackupFolder(Path.of("DCIM/MyAlbums/Best"), "Best"), // Favourites on Oppo
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner/best"), "Best"), // Favourites old on Xiaomi
            new BackupFolder(Path.of("Pictures/Gallery/owner/best"), "Best"), // Favourites on Xiaomi
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner"), "Best"), // Albums on Xiaomi
            new BackupFolder(Path.of("DCIM"), "Camera"),
            new BackupFolder(Path.of("Pictures"), "Signal"), // Signal on Oppo
            new BackupFolder(Path.of("Bluetooth"), "Bluetooth"), // Bluetooth on Oppo
            new BackupFolder(Path.of("MIUI/ShareMe"), "Bluetooth"), // Bluetooth on Xiaomi
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp")
    );

    private final FtpAccessor accessor;
    private final LocalDateTime lastBackupTime;
    private final Path destinationPath;

    public void copy() {
        BACKUP_FOLDERS.forEach(this::copyFolder);
    }

    private void copyFolder(BackupFolder backupFolder) {
        Path destinationPath = this.destinationPath.resolve(backupFolder.destinationSubFolder());
        try {
            if (!Files.exists(destinationPath)) {
                Files.createDirectory(destinationPath);
            }
            accessor.copyFilesFrom(backupFolder.folder(), this.destinationPath.resolve(backupFolder.destinationSubFolder()), lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolder.folder() + " -> " + this.destinationPath.resolve(backupFolder.destinationSubFolder()) + "\n" +
                    e.getMessage());
        }
    }
}
