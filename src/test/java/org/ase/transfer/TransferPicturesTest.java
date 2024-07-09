package org.ase.transfer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferPicturesTest {
    @Mock
    private FtpAccessor ftpAccessor;
    @Mock
    private FileAccessor fileAccessor;
    @Mock
    private ImageModifier imageModifier;
    @Mock
    private BufferedReader reader;

    private final LocalDateTime lastBackupTime = LocalDateTime.now().minusDays(1);
    private final Path destinationRootFolder = Path.of("test");
    private final Path sourceFolder = Path.of("source");
    private Retry retry = new Retry(reader);

    @BeforeEach
    void setup() {
        retry = new Retry(reader);
    }

    @Test
    void shouldCopyFilesFromSourceToDestination() throws IOException {
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier, retry);
        List<BackupFolder> backupFolders = List.of(new BackupFolder(sourceFolder, "subFolder"));

        testee.copy(backupFolders, lastBackupTime, false);

        Path expectedDestinationFolder = Path.of("test/subFolder");
        verify(ftpAccessor).copyFilesFrom(sourceFolder, expectedDestinationFolder, lastBackupTime);
        verifyNoInteractions(imageModifier);
    }

    @Test
    void shouldCopyFilesFromSourceToFavoritDestination_whenIsFavorit()
            throws IOException, ImageWriteException, UnsupportedFileTypeException, ImageReadException {
        Path temporaryDestinationFolder = Path.of("test/subFolderFavorite");
        List<Path> inputFiles = List.of(temporaryDestinationFolder.resolve("image.jpg"));
        when(fileAccessor.filesInDirectory(temporaryDestinationFolder)).thenReturn(inputFiles);
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier, retry);
        List<BackupFolder> backupFolders = List.of(new BackupFolder(sourceFolder, "subFolder"));

        testee.copy(backupFolders, lastBackupTime, true);

        verify(ftpAccessor).copyFilesFrom(sourceFolder, temporaryDestinationFolder, lastBackupTime);
        Path expectedImageFile = temporaryDestinationFolder.resolve("image.jpg");
        Path expectedDestinationFile = Path.of("test/subFolder/image.jpg");
        verify(imageModifier).setJpegRating(expectedImageFile, expectedDestinationFile, 5);
        verifyNoMoreInteractions(imageModifier);
    }

    @Test
    void shouldNotCopyFiles_whenBackupFoldersEmpty() {
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier, retry);
        List<BackupFolder> backupFolders = emptyList();

        testee.copy(backupFolders, lastBackupTime, false);

        verifyNoInteractions(ftpAccessor);
        verifyNoInteractions(imageModifier);
    }

    @Test
    void shouldRetry_whenExceptionThrown() throws IOException {
        doThrow(IOException.class).when(ftpAccessor).copyFilesFrom(any(), any(), eq(lastBackupTime));
        when(reader.readLine()).thenReturn("Y").thenReturn("N");
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier, retry);
        List<BackupFolder> backupFolders = List.of(new BackupFolder(sourceFolder, "subFolder"));

        assertThrows(RuntimeException.class, () -> testee.copy(backupFolders, lastBackupTime, false));

        verify(ftpAccessor, times(2)).copyFilesFrom(sourceFolder, destinationRootFolder.resolve("subFolder"), lastBackupTime);
        verifyNoInteractions(imageModifier);
    }
}