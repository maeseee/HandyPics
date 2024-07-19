package org.ase.transfer;

import org.ase.ftp.FtpAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferFolderTest {
    @Mock
    private FtpAccessor ftpAccessor;
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
    void shouldCopyFolderFromSourceToDestination() throws IOException {
        TransferFolder testee = new TransferFolder(ftpAccessor, retry);

        testee.transfer(sourceFolder, destinationFolder, lastBackupTime);

        verify(ftpAccessor).copyFilesFrom(sourceFolder, destinationFolder, lastBackupTime);
    }

    @Test
    void shouldRetry_whenExceptionThrown() throws IOException {
        doThrow(IOException.class).when(ftpAccessor).copyFilesFrom(any(), any(), eq(lastBackupTime));
        when(reader.readLine()).thenReturn("Y").thenReturn("N");
        TransferFolder testee = new TransferFolder(ftpAccessor, retry);

        assertThrows(RuntimeException.class, () -> testee.transfer(sourceFolder, destinationFolder, lastBackupTime));

        verify(ftpAccessor, times(2)).copyFilesFrom(sourceFolder, destinationFolder, lastBackupTime);
    }
}