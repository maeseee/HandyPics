package org.ase.ftp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FtpAccessorTest {

    @Mock
    private FtpClient ftpClient;

    @Test
    void shouldIgnoreFolder_whenNameContainsPrivate() {
        FtpAccessor testee = new FtpAccessor(ftpClient);

        boolean notIgnored = testee.isNotInDirectoryIgnoreList(Path.of("ThisIsAPrivateFolder"));

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
            "pic.jpg,true",
            "pic.txt,false",
            "doc.docx,false"
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
