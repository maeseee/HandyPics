package org.ase.ftp;

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
import java.nio.file.Paths;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class ApacheFtpClientTest {

    private FakeFtpServer fakeFtpServer;

    private AndroidFtpClient ftpClient;

    @BeforeEach
    public void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("android", "mySweetHandyAccess", "/data"));
        //fakeFtpServer.setportServerControlPort(21);

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(2221);

        fakeFtpServer.start();

        ftpClient = new AndroidFtpClient("localhost");
        ftpClient.open();
    }

    @AfterEach
    public void teardown() throws IOException {
        ftpClient.close();
        fakeFtpServer.stop();
    }

    @Test
    public void givenRemoteFile_whenListingRemoteFiles_thenItIsContainedInList() throws IOException {
        Collection<String> files = ftpClient.listFiles(Paths.get(""));

        assertThat(files).contains("foobar.txt");
    }

    @Test
    public void givenRemoteFile_whenDownloading_thenItIsOnTheLocalFilesystem() throws IOException {
        ftpClient.downloadFile("/foobar.txt", "downloaded_buz.txt");

        assertThat(new File("downloaded_buz.txt")).exists();
        new File("downloaded_buz.txt").deleteOnExit();
    }

    @Test
    @SuppressWarnings("unused")
    public void givenLocalFile_whenUploadingIt_thenItExistsOnRemoteLocation() throws IOException {
        File myFile = new File("myFile.txt");
        boolean created = myFile.createNewFile();

        ftpClient.putFileToPath(myFile, "/buz.txt");

        assertThat(fakeFtpServer.getFileSystem().exists("/buz.txt")).isTrue();
        myFile.deleteOnExit();
    }
}