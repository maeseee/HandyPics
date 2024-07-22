package org.ase.config;

import org.ase.fileAccess.FileAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandyPicStarterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private FileAccessor fileAccessor;
    @Mock
    private BufferedReader reader;

    @BeforeEach
    void initializeOutputStream() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void revertOutputStream() {
        System.setOut(System.out);
    }

    @Test
    void shouldCreateFolder_whenNotExists() {
        HandyPicStarter testee = new HandyPicStarter(fileAccessor, reader);
        Path folderPath = Path.of("unittest");

        testee.prepareFolderPath(folderPath);

        verify(fileAccessor).createDirectoryIfNotExists(folderPath);
        assertThat(outContent.toString()).isEmpty();
    }

    @Test
    void shouldDrawWarning_whenFolderNotEmpty() throws IOException {
        when(fileAccessor.filesInDirectory(any())).thenReturn(singletonList(Path.of("image.jpg")));
        when(reader.readLine()).thenReturn(" ");
        HandyPicStarter testee = new HandyPicStarter(fileAccessor, reader);
        Path folderPath = Path.of("unittest");

        testee.prepareFolderPath(folderPath);

        verify(fileAccessor).createDirectoryIfNotExists(folderPath);
        assertThat(outContent.toString()).isNotEmpty();
    }
}