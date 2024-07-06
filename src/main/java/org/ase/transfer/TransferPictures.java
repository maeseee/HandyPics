package org.ase.transfer;

import lombok.AllArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class TransferPictures {

    private final FtpAccessor ftpAccessor;
    private final FileAccessor fileAccessor;
    private final LocalDateTime lastBackupTime;
    private final Path destinationRootFolder;
    private final List<BackupFolder> backupFolders;

    public void copy(boolean isFavorite) {
        fileAccessor.createDirectoryIfNotExists(destinationRootFolder);
        backupFolders.forEach(backupFolder -> copyFolder(backupFolder, isFavorite));
    }

    private void copyFolder(BackupFolder backupFolder, boolean isFavorite) {
        String favoriteSubFolderName = isFavorite ? backupFolder.destinationSubName() + "Favorite" : backupFolder.destinationSubName();
        Path favoriteDestinationFolder = createDestinationFolder(favoriteSubFolderName);
        try {
            ftpAccessor.copyFilesFrom(backupFolder.sourceFolder(), favoriteDestinationFolder, lastBackupTime);
        } catch (IOException e) {
            // TODO retry!
            System.err.println("ERROR: " + backupFolder.sourceFolder() + " -> " + favoriteDestinationFolder + "\n" + e.getMessage());
        }
        if (isFavorite) {
            Path destinationFolder = createDestinationFolder(backupFolder.destinationSubName());
            setBestRating(favoriteDestinationFolder, destinationFolder);
        }
    }

    private void setBestRating(Path sourceFolder, Path destinationFolder) {
        List<Path> inputFiles = fileAccessor.filesInDirectory(sourceFolder);
        inputFiles.forEach(inputFile -> createFileWithBestRating(destinationFolder, inputFile));
        fileAccessor.deleteDirectory(sourceFolder);
    }

    private void createFileWithBestRating(Path destinationFolder, Path inputFile) {
        ImageModifier imageModifier = new ImageModifier();
        try {
            String filename = inputFile.getFileName().toString();
            imageModifier.setJpegRating(inputFile, destinationFolder.resolve(filename), 5);
        } catch (ImageReadException | ImageWriteException | UnsupportedFileTypeException e) {
            System.err.println(inputFile.getFileName() + " could not be starred: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path createDestinationFolder(String subFolderName) {
        Path folder = destinationRootFolder.resolve(subFolderName);
        fileAccessor.createDirectoryIfNotExists(folder);
        return folder;
    }
}
