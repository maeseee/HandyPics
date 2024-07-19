package org.ase.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferFolderTest {
    @Mock
    private TransferFile transferFile;
    @Mock
    private BufferedReader reader;

    private final LocalDateTime lastBackupTime = LocalDateTime.now().minusDays(1);
    private final Path destinationFolder = Path.of("test");
    private final Path sourceFolder = Path.of("source");
    private Retry retry = new Retry(reader);

    @BeforeEach
    void setup() {
        retry = new Retry(reader);
    }

    @Test
    void shouldTransferFoldersFromSourceToDestination() {
        TransferFolder testee = new TransferFolder(transferFile, retry);

        testee.transfer(sourceFolder, destinationFolder, lastBackupTime);

        verify(transferFile).transfer(sourceFolder, destinationFolder, lastBackupTime);
    }

    @Test
    void shouldRetry_whenExceptionThrown() throws IOException {
        doThrow(IOException.class).when(transferFile).listDirectories(any());
        when(reader.readLine()).thenReturn("R").thenReturn("E");
        TransferFolder testee = new TransferFolder(transferFile, retry);

        assertThrows(RuntimeException.class, () -> testee.transfer(sourceFolder, destinationFolder, lastBackupTime));

        verify(transferFile, times(2)).listDirectories(sourceFolder);
    }

    @Test
    void shouldIgnoreFolder_whenNameContainsPrivate() {
        TransferFolder testee = new TransferFolder(transferFile, retry);

        boolean notIgnored = testee.isNotInDirectoryIgnoreList(Path.of("ThisIsAPrivateFolder"));

        assertFalse(notIgnored);
    }

    @Test
    void shouldIgnoreFolder_whenFolderIsHidden() {
        TransferFolder testee = new TransferFolder(transferFile, retry);

        boolean notIgnored = testee.isNotHiddenDirectory(Path.of(".ThisIsAHiddenFolder"));

        assertFalse(notIgnored);
    }
}