package org.ase.transfer;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.io.FileUtils;
import org.ase.ftp.FtpAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class TransferPictures {

    private final List<BackupFolder> favouriteBackupFolders = ImmutableList.of(
            new BackupFolder(Path.of("DCIM/MyAlbums/Best"), "Camera"), // Favourites on Oppo
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner/best"), "Camera"), // Favourites old on Xiaomi
            new BackupFolder(Path.of("Pictures/Gallery/owner/best"), "Camera"), // Favourites on Xiaomi
            new BackupFolder(Path.of("MIUI/Gallery/cloud/owner"), "Camera") // Albums on Xiaomi
    );
    private final List<BackupFolder> backupFolders = ImmutableList.of(
            new BackupFolder(Path.of("DCIM"), "Camera"),
            new BackupFolder(Path.of("Pictures"), "Signal"), // Signal on Oppo
            new BackupFolder(Path.of("Bluetooth"), "Bluetooth"), // Bluetooth on Oppo
            new BackupFolder(Path.of("MIUI/ShareMe"), "Bluetooth"), // Bluetooth on Xiaomi
            new BackupFolder(Path.of("Android/media/com.whatsapp/WhatsApp/Media"), "Whatsapp")
    );

    private final FtpAccessor accessor;
    private final LocalDateTime lastBackupTime;
    private final Path destinationRootFolder;

    public void copy() {
        favouriteBackupFolders.forEach(backupFolders -> copyFolder(backupFolders, true));
        backupFolders.forEach(backupFolders -> copyFolder(backupFolders, false));
    }

    private void copyFolder(BackupFolder backupFolders, boolean isFavorite) {
        String favoriteSubFolderName = isFavorite ? backupFolders.destinationSubName() + "Favorite" : backupFolders.destinationSubName();
        Path favoriteDestinationFolder = createDestinationFolder(favoriteSubFolderName);
        try {
            accessor.copyFilesFrom(backupFolders.sourceFolder(), favoriteDestinationFolder, lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolders.sourceFolder() + " -> " + favoriteDestinationFolder + "\n" + e.getMessage());
        }
        if (isFavorite) {
            Path destinationFolder = createDestinationFolder(backupFolders.destinationSubName());
            setBestRating(favoriteDestinationFolder, destinationFolder);
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

    private void setBestRating(Path favoriteDestinationFolder, Path destinationFolder) {
        ImageModifier imageModifier = new ImageModifier();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(favoriteDestinationFolder)) {
            for (Path inputFile : stream) {
                if (Files.isRegularFile(inputFile)) {
                    try {
                        String filename = inputFile.getFileName().toString();
                        imageModifier.setJpegRating(inputFile, destinationFolder.resolve(filename), 5);
                    } catch (ImageReadException | ImageWriteException | UnsupportedFileTypeException e) {
                        System.err.println(inputFile.getFileName() + " could not be starred: " + e.getMessage());
                    }
                }
            }
            FileUtils.deleteDirectory(favoriteDestinationFolder.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Path createDestinationFolder(String subFolderName) {
        Path folder = destinationRootFolder.resolve(subFolderName);
        createFolderIfNotExists(folder);
        return folder;
    }
}
