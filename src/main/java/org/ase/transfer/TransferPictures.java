package org.ase.transfer;

import lombok.AllArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.fileAccess.FileAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class TransferPictures {

    private final TransferFolder transferFolder;
    private final FileAccessor fileAccessor;
    private final Path destinationRootFolder;
    private final ImageModifier imageModifier;

    public void copy(List<BackupFolder> backupFolders, LocalDateTime lastBackupTime, boolean isFavorite) {
        fileAccessor.createDirectoryIfNotExists(destinationRootFolder);
        backupFolders.forEach(backupFolder -> copyFolder(backupFolder, lastBackupTime, isFavorite));
    }

    private void copyFolder(BackupFolder backupFolder, LocalDateTime lastBackupTime, boolean isFavorite) {
        String favoriteSubFolderName = isFavorite ? backupFolder.destinationSubName() + "Favorite" : backupFolder.destinationSubName();
        Path favoriteDestinationFolder = createDestinationFolder(favoriteSubFolderName);

        transferFolder.transfer(backupFolder.sourceFolder(), favoriteDestinationFolder, lastBackupTime);

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
        String filename = inputFile.getFileName().toString();
        Path destinationFile = destinationFolder.resolve(filename);
        try {
            imageModifier.setJpegRating(inputFile, destinationFile, 5);
        } catch (ImageReadException | ImageWriteException | UnsupportedFileTypeException e) {
            System.err.println(inputFile.getFileName() + " could not be starred: " + e.getMessage());
            fileAccessor.moveFileIfNotExists(inputFile, destinationFile);
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
