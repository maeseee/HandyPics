package org.ase.config;

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
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class HandyPicStarterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

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
    @SuppressWarnings("unused")
    void shouldCreateFolder_whenNotExists() {
        HandyPicStarter testee = new HandyPicStarter(reader);
        Path folderPath = Path.of("unittest");

        testee.prepareFolderPath(folderPath);

        boolean exists = Files.exists(folderPath);
        assertThat(exists).isTrue();
        boolean deleted = folderPath.toFile().delete();
        assertThat(outContent.toString()).isEmpty();
    }

    @Test
    @SuppressWarnings("unused")
    void shouldDrawWarning_whenFolderNotEmpty() throws IOException {
        HandyPicStarter testee = new HandyPicStarter(reader);
        Path folderPath = Path.of("unittest");
        Files.createDirectory(folderPath);
        Path notEmptyPath = Path.of("unittest/notEmpty");
        Files.createFile(notEmptyPath);

        testee.prepareFolderPath(folderPath);

        boolean exists = Files.exists(folderPath);
        assertThat(exists).isTrue();
        assertThat(outContent.toString()).isNotEmpty();
        boolean emptyFileDeleted = notEmptyPath.toFile().delete();
        boolean deleted = folderPath.toFile().delete();
    }
}