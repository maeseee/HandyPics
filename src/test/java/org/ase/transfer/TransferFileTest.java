package org.ase.transfer;

import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FileProperty;
import org.ase.ftp.FtpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferFileTest {
    @Mock
    private FtpClient ftpClient;
    @Mock
    private FileAccessor fileAccessor;
    @Mock
    private BufferedReader reader;

    private final LocalDateTime lastBackupTime = LocalDateTime.now().minusDays(1);
    private Retry retry = new Retry(reader);

    @BeforeEach
    void setup() {
        retry = new Retry(reader);
    }

    @Test
    void shouldTransferFiles_whenWanted() throws IOException {
        Path sourceFile = Path.of("test/image.jpg");
        Path destinationFolder = Path.of("test");
        FileProperty fileProperty = new FileProperty(sourceFile, LocalDateTime.now());
        Collection<FileProperty> fileProperties = List.of(fileProperty);
        when(ftpClient.listFiles(any())).thenReturn(fileProperties);
        TransferFile testee = new TransferFile(ftpClient, fileAccessor, retry);

        testee.transfer(sourceFile, destinationFolder, lastBackupTime);

        verify(ftpClient).downloadFile(sourceFile, Path.of("test/image.jpg"));
    }

    @Test
    void shouldIgnoreFile_whenNameContainsTrash() {
        TransferFile testee = new TransferFile(ftpClient, fileAccessor, retry);

        boolean notIgnored = testee.isNotInFileIgnoreList(Path.of("ThisIsATrashFile.txt"));

        assertFalse(notIgnored);
    }

    @ParameterizedTest(name = "{index} => path={0}")
    @CsvSource({
            "pic.jpg, true",
            "pic.txt, false",
            "doc.docx, false",
            "jgp.bla, false"
    })
    void shouldFilterFile_whenNotAnImageOrVideo(Path path, boolean imageOrVideo) {
        TransferFile testee = new TransferFile(ftpClient, fileAccessor, retry);

        boolean imageOrVideoFile = testee.isImageOrVideoFile(path);

        if (imageOrVideo) {
            assertTrue(imageOrVideoFile);
        } else {
            assertFalse(imageOrVideoFile);
        }
    }

    @Test
    void shouldIgnoreFile_whenAlreadyOnDestination() {
        when(fileAccessor.fileExists(any())).thenReturn(true);
        TransferFile testee = new TransferFile(ftpClient, fileAccessor, retry);

        boolean ignored = testee.fileExistsOnDestination(Path.of("File.txt"));

        assertTrue(ignored);
    }

    @Test
    void shouldIgnoreFavoritFile_whenAlreadyOnDestination() {
        when(fileAccessor.fileExists(any())).thenReturn(false);
        when(fileAccessor.fileExists(Path.of("File.txt"))).thenReturn(true);
        TransferFile testee = new TransferFile(ftpClient, fileAccessor, retry);

        boolean ignored = testee.fileExistsOnDestination(Path.of("FileFavorite.txt"));

        assertTrue(ignored);
    }
}