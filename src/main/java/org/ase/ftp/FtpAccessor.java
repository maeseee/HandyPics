package org.ase.ftp;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class FtpAccessor {

    private final FtpClient ftpClient;

    public void copyFilesFrom(Path sourcePath, Path destinationPath, LocalDateTime lastBackupTime) throws IOException {
        ftpClient.open();
        Collection<FileProperty> files = ftpClient.listFiles(sourcePath);
        List<Path> filteredList = files.stream()
                .filter(fileProperty -> isModificationDateNewer(lastBackupTime, fileProperty.modificationDate()))
                .map(FileProperty::filePath)
                .filter(this::isNotInFileIgnoreList)
                .filter(this::isImageOrVideoFile)
                .toList();

        for (Path filePath : filteredList) {
            download(filePath, destinationPath.resolve(filePath.getFileName()));
        }
        ftpClient.close();

        callSubdirectories(sourcePath, destinationPath, lastBackupTime);
    }

    public void callSubdirectories(Path sourcePath, Path destinationPath, LocalDateTime lastBackupTime) throws IOException {
        Collection<Path> directories = ftpClient.listDirectories(sourcePath);
        List<Path> filteredList = directories.stream()
                .filter(this::isNotInDirectoryIgnoreList)
                .toList();
        for (Path path : filteredList) {
            copyFilesFrom(path, destinationPath, lastBackupTime);
        }
    }

    public void copyFileFrom(Path sourcePath, Path destinationPath) throws IOException {
        ftpClient.open();
        download(sourcePath, destinationPath);
        ftpClient.close();
    }

    private void download(Path sourcePath, Path destinationPath) throws IOException {
        ftpClient.downloadFile(sourcePath.toString(), destinationPath.resolve(sourcePath.getFileName()).toString());
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
    boolean isNotInDirectoryIgnoreList(Path path) {
        List<String> ignoreList = List.of("sent", "private");
        String folderName = path.getFileName().toString().toLowerCase().trim();
        return ignoreList.stream().noneMatch(folderName::contains);
    }

    @VisibleForTesting
    boolean isImageOrVideoFile(Path path) {
        List<String> fileEndings = List.of(
                ".jpg", ".jpeg", ".png", ".giv", ".tiff", ".bmp", ".svg", // image files
                ".mp4", ".avi", ".mkv", ".mov", ".avchd", ".h264", ".265" // video files
        );

        String fileName = path.getFileName().toString().toLowerCase().trim();
        return fileEndings.stream().anyMatch(fileName::endsWith);
    }
}
