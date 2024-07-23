package org.ase.fileAccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileAccessorTest {

    private final String fileName = "file.txt";
    private final Path path = Path.of("test");
    private final Path movedPath = Path.of("movedTest");
    private final Path file = path.resolve(fileName);
    private final FileAccessor testee = new FileAccessor();

    @BeforeEach
    @AfterEach
    void deleteFiles() {
        testee.deleteDirectory(path);
        testee.deleteDirectory(movedPath);
    }

    @Test
    void shouldCreateDirectory_whenNotExistsJet() {
        testee.createDirectoryIfNotExists(path);

        assertThat(testee.fileExists(path)).isTrue();
        assertThat(testee.fileExists(file)).isFalse();
    }

    @Test
    void shouldWriteFile() {
        testee.createDirectoryIfNotExists(path);

        testee.writeFile(file, "MyText");
        List<Path> files = testee.filesInDirectory(path);
        String firstLine = testee.readFirstLineInFile(file);

        assertThat(testee.fileExists(file)).isTrue();
        assertThat(files.size()).isEqualTo(1);
        assertThat(files.get(0)).isEqualTo(file);
        assertThat(firstLine).isEqualTo("MyText");
    }

    @Test
    void shouldMoveFile() {
        Path movedFile = movedPath.resolve(fileName);
        testee.writeFile(file, "MyText");

        testee.moveFileIfNotExists(file, movedFile);
        String firstLine = testee.readFirstLineInFile(movedFile);

        assertThat(testee.fileExists(file)).isFalse();
        assertThat(testee.fileExists(movedFile)).isTrue();
        assertThat(firstLine).isEqualTo("MyText");
    }
}