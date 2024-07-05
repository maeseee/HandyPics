package org.ase.transfer;

import com.google.common.collect.ImmutableList;
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

public class TransferPictures {

    private final FtpAccessor accessor;
    private final LocalDateTime lastBackupTime;
    private final List<BackupFolder> favouriteBackupFolders;
    private final List<BackupFolder> backupFolders;

    public TransferPictures(FtpAccessor accessor, LocalDateTime lastBackupTime, Path destinationRootFolder) {
        this.accessor = accessor;
        this.lastBackupTime = lastBackupTime;
        this.favouriteBackupFolders = ImmutableList.of(
                new BackupFolder(Path.of("DCIM/MyAlbums/Best"), createDestinationFolder(destinationRootFolder, "Camera")), // Favourites on Oppo
                new BackupFolder(Path.of("MIUI/Gallery/cloud/owner/best"), createDestinationFolder(destinationRootFolder, "Camera")),
                // Favourites old on Xiaomi
                new BackupFolder(Path.of("Pictures/Gallery/owner/best"), createDestinationFolder(destinationRootFolder, "Camera")),
                // Favourites on Xiaomi
                new BackupFolder(Path.of("MIUI/Gallery/cloud/owner"), createDestinationFolder(destinationRootFolder, "Camera")) // Albums on Xiaomi
        );
        this.backupFolders = ImmutableList.of(
                new BackupFolder(Path.of("DCIM"), createDestinationFolder(destinationRootFolder, "Camera")),
                new BackupFolder(Path.of("Pictures"), createDestinationFolder(destinationRootFolder, "Signal")), // Signal on Oppo
                new BackupFolder(Path.of("Bluetooth"), createDestinationFolder(destinationRootFolder, "Bluetooth")), // Bluetooth on Oppo
                new BackupFolder(Path.of("MIUI/ShareMe"), createDestinationFolder(destinationRootFolder, "Bluetooth")), // Bluetooth on Xiaomi
                new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), createDestinationFolder(destinationRootFolder, "Whatsapp"))
        );
    }

    public void copy() {
        favouriteBackupFolders.forEach(this::copyFolder);
        favouriteBackupFolders.forEach(backupFolder -> setBestRating(backupFolder.destinationSubFolder()));
        backupFolders.forEach(this::copyFolder);
    }

    private void copyFolder(BackupFolder backupFolder) {
        createFolderIfNotExists(backupFolder.destinationSubFolder());
        try {
            accessor.copyFilesFrom(backupFolder.sourceFolder(), backupFolder.destinationSubFolder(), lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolder.sourceFolder() + " -> " + backupFolder.destinationSubFolder() + "\n" + e.getMessage());
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

    private static Path createDestinationFolder(Path destinationRootFolder, String subFolderName) {
        return destinationRootFolder.resolve(subFolderName);
    }
}
