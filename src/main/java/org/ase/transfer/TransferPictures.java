package org.ase.transfer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.ftp.FtpAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TransferPictures {

    private final static List<BackupFolder> BACKUP_FOLDERS = List.of(
            new BackupFolder(Path.of("DCIM/MyAlbums/Best"), "Camera", true), // Favourites on Oppo
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner/best"), "Camera", true), // Favourites old on Xiaomi
            new BackupFolder(Path.of("Pictures/Gallery/owner/best"), "Camera", true), // Favourites on Xiaomi
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner"), "Camera", true), // Albums on Xiaomi
            new BackupFolder(Path.of("DCIM"), "Camera", false),
            new BackupFolder(Path.of("Pictures"), "Signal", false), // Signal on Oppo
            new BackupFolder(Path.of("Bluetooth"), "Bluetooth", false), // Bluetooth on Oppo
            new BackupFolder(Path.of("MIUI/ShareMe"), "Bluetooth", false), // Bluetooth on Xiaomi
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp", false)
    );

    private final FtpAccessor accessor;
    private final LocalDateTime lastBackupTime;
    private final Path destinationRootFolder;

    public void copy() {
        BACKUP_FOLDERS.forEach(this::copyFolder);
    }

    private void copyFolder(BackupFolder backupFolder) {
        Path destinationFolder = destinationRootFolder.resolve(backupFolder.destinationSubFolderName());
        createFolderIfNotExists(destinationFolder);
        try {
            accessor.copyFilesFrom(backupFolder.sourceFolder(), destinationFolder, lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolder.sourceFolder() + " -> " + destinationFolder + "\n" + e.getMessage());
        }

        if (backupFolder.bestRating()) {
            setBestRating(destinationFolder);
        }
    }

    private void createFolderIfNotExists(Path destinationFolder) {
        if (!Files.exists(destinationFolder)) {
            try {
                Files.createDirectory(destinationFolder);
            } catch (IOException e) {
                System.err.println(destinationFolder + " could not be created!\n" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void setBestRating(Path destinationPath) {
        ImageModifier imageModifier = new ImageModifier();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(destinationPath)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    try {
                        String filename = "rated_" + file.getFileName().toString(); // TODO use original file name
                        imageModifier.setJpegRating(file, file.getParent().resolve(filename), 5);
                    } catch (ImageReadException | ImageWriteException | UnsupportedFileTypeException e) {
                        System.err.println(file.getFileName() + " could not be starred: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
