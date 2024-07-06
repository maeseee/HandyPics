package org.ase.transfer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpAccessor;
import org.ase.image.ImageModifier;
import org.ase.image.UnsupportedFileTypeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferPicturesTest {

    private final LocalDateTime lastBackupTime = LocalDateTime.now().minusDays(1);
    private final Path destinationRootFolder = Path.of("test");
    private final Path sourceFolder = Path.of("source");
    private final List<BackupFolder> backupFolders = List.of(new BackupFolder(sourceFolder, "subFolder"));

    @Mock
    private FtpAccessor ftpAccessor;
    @Mock
    private FileAccessor fileAccessor;
    @Mock
    private ImageModifier imageModifier;

    @Test
    void shouldCopyFilesFromSourceToDestination() throws IOException {
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier);

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
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, destinationRootFolder, imageModifier);

        testee.copy(backupFolders, lastBackupTime, true);

        verify(ftpAccessor).copyFilesFrom(sourceFolder, temporaryDestinationFolder, lastBackupTime);
        Path expectedImageFile = temporaryDestinationFolder.resolve("image.jpg");
        Path expectedDestinationFile = Path.of("test/subFolder/image.jpg");
        verify(imageModifier).setJpegRating(expectedImageFile, expectedDestinationFile, 5);
        verifyNoMoreInteractions(imageModifier);
    }
}