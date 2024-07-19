package org.ase.transfer;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FileProperty;
import org.ase.ftp.FtpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class TransferFile {

    private final FtpClient ftpClient;
    private final FileAccessor fileAccessor;
    private final Retry retry;

    public void transfer(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) {
        retry.callWithRetry(() -> {
            try {
                transferFiles(sourceFolder, destinationFolder, lastBackupTime);
            } catch (IOException e) {
                System.err.println("ERROR copying folder: " + sourceFolder + " -> " + destinationFolder + "\n" + e);
                throw new RuntimeException(e);
            }
        });
    }

    public Collection<Path> listDirectories(Path folder) throws IOException {
        return ftpClient.listDirectories(folder);
    }

    private void transferFiles(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) throws IOException {
        System.out.println("Copy files from " + sourceFolder);
        Collection<FileProperty> files = ftpClient.listFiles(sourceFolder);
        List<Path> filteredList = filterFiles(destinationFolder, lastBackupTime, files);

        int processedFiles = 0;
        for (Path sourceFile : filteredList) {
            processedFiles++;
            System.out.println("Copying " + sourceFile + " (" + processedFiles + "/" + filteredList.size() + ")");
            copyFileFrom(sourceFile, destinationFolder);
        }
    }

    private void copyFileFrom(Path sourceFile, Path destinationFolder) throws IOException {
        Path destinationFile = destinationFolder.resolve(sourceFile.getFileName());
        if (!fileExistsOnDestination(destinationFile)) {
            ftpClient.downloadFile(sourceFile, destinationFile);
        }
    }

    private List<Path> filterFiles(Path destinationFolder, LocalDateTime lastBackupTime, Collection<FileProperty> files) {
        return files.stream()
                .filter(fileProperty -> isModificationDateNewer(lastBackupTime, fileProperty.modificationDate()))
                .map(FileProperty::filePath)
                .filter(this::isNotInFileIgnoreList)
                .filter(this::isImageOrVideoFile)
                .filter(path -> doesNotExistYet(path, destinationFolder))
                .toList();
    }

    private boolean isModificationDateNewer(LocalDateTime lastBackupTime, LocalDateTime modificationDate) {
        return lastBackupTime.isBefore(modificationDate);
    }

    @VisibleForTesting
    boolean isNotInFileIgnoreList(Path path) {
        List<String> ignoreList = List.of("trash");
        String fileName = path.getFileName().toString().toLowerCase().trim();
        return ignoreList.stream().noneMatch(fileName::contains);
    }

    @VisibleForTesting
    boolean isImageOrVideoFile(Path file) {
        List<String> fileEndings = List.of(
                ".jpg", ".jpeg", ".png", ".giv", ".tiff", ".bmp", ".svg", // image files
                ".mp4", ".avi", ".mkv", ".mov", ".avchd", ".h264", ".265" // video files
        );

        String fileName = file.getFileName().toString().toLowerCase().trim();
        return fileEndings.stream().anyMatch(fileName::endsWith);
    }

    @VisibleForTesting
    boolean doesNotExistYet(Path file, Path destinationPath) {
        Path destinationFile = destinationPath.resolve(file.getFileName().toString());
        return !Files.exists(destinationFile);
    }

    @VisibleForTesting
    boolean fileExistsOnDestination(Path file) {
        if (fileAccessor.fileExists(file)) {
            return true;
        }
        String fileWithoutFavoriteString = file.toString().replaceFirst("Favorite", "");
        Path fileWithoutFavorite = Path.of(fileWithoutFavoriteString);
        return fileAccessor.fileExists(fileWithoutFavorite);
    }
}
