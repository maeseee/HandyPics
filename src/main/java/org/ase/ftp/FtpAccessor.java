package org.ase.ftp;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class FtpAccessor {

    private final FtpClient ftpClient;

    public void copyFilesFrom(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) throws IOException {
        System.out.println("Copy files from " + sourceFolder);
        Collection<FileProperty> files = ftpClient.listFiles(sourceFolder);
        List<Path> filteredList = files.stream()
                .filter(fileProperty -> isModificationDateNewer(lastBackupTime, fileProperty.modificationDate()))
                .map(FileProperty::filePath)
                .filter(this::isNotInFileIgnoreList)
                .filter(this::isImageOrVideoFile)
                .filter(path -> doesNotExistYet(path, destinationFolder))
                .toList();

        int processedFiles = 0;
        for (Path sourceFile : filteredList) {
            processedFiles++;
            System.out.println("Copying " + sourceFile + " (" + processedFiles + "/" + filteredList.size() + ")");
            copyFileFrom(sourceFile, destinationFolder);
        }

        callSubdirectories(sourceFolder, destinationFolder, lastBackupTime);
    }

    public void callSubdirectories(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) throws IOException {
        Collection<Path> sourceDirectories = ftpClient.listDirectories(sourceFolder);
        List<Path> filteredList = sourceDirectories.stream()
                .filter(this::isNotInDirectoryIgnoreList)
                .filter(this::isNotHiddenDirectory)
                .toList();
        for (Path subSourceFolder : filteredList) {
            copyFilesFrom(subSourceFolder, destinationFolder, lastBackupTime);
        }
    }

    public void copyFileFrom(Path sourceFile, Path destinationFolder) throws IOException {
        String destination = destinationFolder.toString().replaceFirst("Favorite", "");
        Path destinationFile = Path.of(destination).resolve(sourceFile.getFileName());
        if (!Files.exists(destinationFile)) {
            ftpClient.downloadFile(sourceFile, destinationFile);
        }
    }

    public void storeFileTo(Path sourceFile, Path destinationFolder) throws IOException {
        ftpClient.putFileToPath(sourceFile, destinationFolder.resolve(sourceFile.getFileName()));
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
    boolean isNotInDirectoryIgnoreList(Path folder) {
        List<String> ignoreList = List.of("sent", "private", "audio", "thumbnails");
        String folderName = folder.getFileName().toString().toLowerCase().trim();
        return ignoreList.stream().noneMatch(folderName::contains);
    }

    @VisibleForTesting
    boolean isNotHiddenDirectory(Path path) {
        String directoryName = path.getFileName().toString();
        return !directoryName.startsWith(".");
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
}
