package org.ase.transfer;

import com.google.common.annotations.VisibleForTesting;
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

public class TransferPictures {

    private final FtpAccessor ftpAccessor;
    private final FileAccessor fileAccessor;
    private final Path destinationRootFolder;
    private final ImageModifier imageModifier;

    public TransferPictures(FtpAccessor ftpAccessor, FileAccessor fileAccessor, Path destinationRootFolder) {
        this(ftpAccessor, fileAccessor, destinationRootFolder, new ImageModifier());
    }

    @VisibleForTesting
    TransferPictures(FtpAccessor ftpAccessor, FileAccessor fileAccessor, Path destinationRootFolder, ImageModifier imageModifier) {
        this.ftpAccessor = ftpAccessor;
        this.fileAccessor = fileAccessor;
        this.destinationRootFolder = destinationRootFolder;
        this.imageModifier = imageModifier;
    }

    public void copy(List<BackupFolder> backupFolders, LocalDateTime lastBackupTime, boolean isFavorite) {
        fileAccessor.createDirectoryIfNotExists(destinationRootFolder);
        backupFolders.forEach(backupFolder -> copyFolder(backupFolder, lastBackupTime, isFavorite));
    }

    private void copyFolder(BackupFolder backupFolder, LocalDateTime lastBackupTime, boolean isFavorite) {
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
