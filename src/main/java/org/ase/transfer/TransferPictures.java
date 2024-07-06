package org.ase.transfer;

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

    private final FtpAccessor accessor;
    private final LocalDateTime lastBackupTime;
    private final Path destinationRootFolder;
    private final List<BackupFolder> backupFolders;

    public void copy(boolean isFavorite) {
        backupFolders.forEach(backupFolder -> copyFolder(backupFolder, isFavorite));
    }

    private void copyFolder(BackupFolder backupFolder, boolean isFavorite) {
        String favoriteSubFolderName = isFavorite ? backupFolder.destinationSubName() + "Favorite" : backupFolder.destinationSubName();
        Path favoriteDestinationFolder = createDestinationFolder(favoriteSubFolderName);
        try {
            accessor.copyFilesFrom(backupFolder.sourceFolder(), favoriteDestinationFolder, lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolder.sourceFolder() + " -> " + favoriteDestinationFolder + "\n" + e.getMessage());
        }
        if (isFavorite) {
            Path destinationFolder = createDestinationFolder(backupFolder.destinationSubName());
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
