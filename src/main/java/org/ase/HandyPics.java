package org.ase;

import com.google.common.collect.ImmutableList;
import org.ase.config.Config;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
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

    private final Config config;
    private final FtpAccessor ftpAccessor;

    private LocalDateTime lastBackupTime;

    public HandyPics(Config config) {
        this.config = config;

        AndroidFtpClient ftpClient = new AndroidFtpClient(config.ipAddress());
        this.ftpAccessor = new FtpAccessor(ftpClient);
    }

    public void transferImagesFromHandy() {
        loadLastBackupTime();

        TransferPictures transferFavoritPictures =
                new TransferPictures(ftpAccessor, lastBackupTime, config.destinationRootFolder(), FAVORIT_BACKUP_FOLDERS);
        transferFavoritPictures.copy(true);

        TransferPictures transferPictures =
                new TransferPictures(ftpAccessor, lastBackupTime, config.destinationRootFolder(), BACKUP_FOLDERS);
        transferPictures.copy(false);

        // TODO save backup time
    }

    private void loadLastBackupTime() {
        LastBackup lastBackup = new LastBackup(config.destinationRootFolder());
        lastBackup.loadLastBackup(ftpAccessor);
        lastBackupTime = lastBackup.readLastBackupTimeFromFile();
    }
}
