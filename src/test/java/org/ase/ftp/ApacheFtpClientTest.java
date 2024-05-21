package org.ase.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ApacheFtpClientTest {

    private FakeFtpServer fakeFtpServer;

    private AndroidFtpClient ftpClient;

    @BeforeEach
    public void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("android", "mySweetHandyAccess", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "content"));
        fileSystem.add(new DirectoryEntry("/data/subFolder"));
        fileSystem.add(new FileEntry("/data/subFolder/subFoobar.txt", "content 123"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(2221);

        fakeFtpServer.start();

        ftpClient = new AndroidFtpClient("localhost", new PrintCommandListener(new PrintWriter(System.out)));
    }

    @AfterEach
    public void teardown() throws IOException {
        fakeFtpServer.stop();
    }

    @Test
    public void shouldHaveFilesInItsList_whenListingRemoteFiles() throws IOException {
        Collection<FileProperty> files = ftpClient.listFiles(Paths.get(""));

        List<Path> filePathList = files.stream()
                .map(FileProperty::filePath)
                .collect(toList());
        assertThat(filePathList).hasSize(1);
        assertThat(filePathList).contains(Path.of("foobar.txt"));
        assertThat(filePathList).doesNotContain(Path.of("subFolder"));
    }

    @Test
    public void shouldReturnEmptyList_whenSourceFolderDoesNotExist() throws IOException {
        Collection<FileProperty> files = ftpClient.listFiles(Paths.get("notExistingFolder"));

        List<Path> filePathList = files.stream()
                .map(FileProperty::filePath)
                .collect(toList());
        assertThat(filePathList).isEmpty();
    }

    @Test
    public void shouldHaveDirectoriesInItsList_whenListingRemoteDirs() throws IOException {
        Collection<Path> files = ftpClient.listDirectories(Paths.get(""));

        assertThat(files).hasSize(1);
        assertThat(files).doesNotContain(Path.of("foobar.txt"));
        assertThat(files).contains(Path.of("subFolder"));
    }

    @Test
    public void shouldHaveFileOnTheLocalFilesystem_whenDownloading() throws IOException {
        ftpClient.downloadFile("/data/foobar.txt", "downloaded_buz.txt");

        assertThat(new File("downloaded_buz.txt")).exists();
        new File("downloaded_buz.txt").deleteOnExit();
    }

    @Test
    @SuppressWarnings("unused")
    public void shouldHaveFileOnTheServer_whenUploadingIt() throws IOException {
        File myFile = new File("myFile.txt");
        boolean created = myFile.createNewFile();

        ftpClient.putFileToPath(myFile, "/buz.txt");

        assertThat(fakeFtpServer.getFileSystem().exists("/buz.txt")).isTrue();
        myFile.deleteOnExit();
    }
}