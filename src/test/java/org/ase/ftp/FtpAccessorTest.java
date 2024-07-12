package org.ase.ftp;

import org.ase.fileAccess.FileAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FtpAccessorTest {

    @Mock
    private FtpClient ftpClient;

    @Test
    void shouldCopyFile_whenWanted() throws IOException {
        Path sourceFile = Path.of("test/image.jpg");
        Path destinationFolder = Path.of("test");
        FtpAccessor testee = new FtpAccessor(ftpClient);

        testee.copyFileFrom(sourceFile, destinationFolder);

        verify(ftpClient).downloadFile(sourceFile, Path.of("test/image.jpg"));
    }

    @Test
    @SuppressWarnings("unused")
    void shouldIgnoreFile_whenAlreadyThere() throws IOException {
        FileAccessor fileAccessor = new FileAccessor();
        fileAccessor.createDirectoryIfNotExists(Path.of("test"));
        Path sourceFile = Path.of("test/image.jpg");
        Files.createFile(sourceFile);
        Path destinationFolder = Path.of("test");
        FtpAccessor testee = new FtpAccessor(ftpClient);

        testee.copyFileFrom(sourceFile, destinationFolder);

        verifyNoInteractions(ftpClient);
        boolean emptyFileDeleted = sourceFile.toFile().delete();
        boolean deleted = destinationFolder.toFile().delete();
    }

    @Test
    @SuppressWarnings("unused")
    void shouldIgnoreFavoriteFile_whenAlreadyInThere() throws IOException {
        FileAccessor fileAccessor = new FileAccessor();
        fileAccessor.createDirectoryIfNotExists(Path.of("test"));
        Path sourceFile = Path.of("test/image.jpg");
        fileAccessor.createIfNotExists(sourceFile);
        Path destinationFolder = Path.of("testFavorite");
        FtpAccessor testee = new FtpAccessor(ftpClient);

        testee.copyFileFrom(sourceFile, destinationFolder);

        verifyNoInteractions(ftpClient);
        boolean emptyFileDeleted = sourceFile.toFile().delete();
        boolean deleted = destinationFolder.toFile().delete();
    }

    @Test
    void shouldIgnoreFolder_whenNameContainsPrivate() {
        FtpAccessor testee = new FtpAccessor(ftpClient);

        boolean notIgnored = testee.isNotInDirectoryIgnoreList(Path.of("ThisIsAPrivateFolder"));

        assertFalse(notIgnored);
    }

    @Test
    void shouldIgnoreFolder_whenFolderIsHidden() {
        FtpAccessor testee = new FtpAccessor(ftpClient);

        boolean notIgnored = testee.isNotHiddenDirectory(Path.of(".ThisIsAHiddenFolder"));

        assertFalse(notIgnored);
    }

    @Test
    void shouldIgnoreFile_whenNameContainsTrash() {
        FtpAccessor testee = new FtpAccessor(ftpClient);

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
        FtpAccessor testee = new FtpAccessor(ftpClient);

        boolean imageOrVideoFile = testee.isImageOrVideoFile(path);

        if (imageOrVideo) {
            assertTrue(imageOrVideoFile);
        } else {
            assertFalse(imageOrVideoFile);
        }
    }
}
