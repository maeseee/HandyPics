package org.ase.fileAccess;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileAccessorTest {

    @Test
    void shouldCreateDirectory_whenNotExistsJet() {
        String fileName = "file.txt";
        Path path = Path.of("test");
        Path file = path.resolve(fileName);
        FileAccessor testee = new FileAccessor();
        testee.deleteDirectory(path);

        testee.createDirectoryIfNotExists(path);

        assertThat(testee.fileExists(path)).isTrue();
        assertThat(testee.fileExists(file)).isFalse();
    }

    @Test
    void shouldWriteFile() {
        String fileName = "file.txt";
        Path path = Path.of("test");
        Path file = path.resolve(fileName);
        FileAccessor testee = new FileAccessor();
        testee.deleteDirectory(path);
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
        String fileName = "file.txt";
        Path path = Path.of("test");
        Path movedPath = Path.of("movedTest");
        Path file = path.resolve(fileName);
        Path movedFile = movedPath.resolve(fileName);
        FileAccessor testee = new FileAccessor();
        testee.deleteDirectory(path);
        testee.deleteDirectory(movedPath);
        testee.writeFile(file, "MyText");

        testee.moveFileIfNotExists(file, movedFile);
        String firstLine = testee.readFirstLineInFile(movedFile);

        assertThat(testee.fileExists(file)).isFalse();
        assertThat(testee.fileExists(movedFile)).isTrue();
        assertThat(firstLine).isEqualTo("MyText");
    }
}