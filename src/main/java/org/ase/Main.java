package org.ase;

import org.ase.config.Config;
import org.ase.config.HandyPicStarter;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.AndroidFtpClient;
import org.ase.ftp.FtpAccessor;
import org.ase.history.LastBackup;
import org.ase.image.ImageModifier;
import org.ase.transfer.Retry;
import org.ase.transfer.TransferPictures;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        HandyPicStarter handyPicStarter = new HandyPicStarter(bufferedReader);
        Config config = handyPicStarter.readConfig();

        FtpAccessor ftpAccessor = createFtpAccessor(config.ipAddress());
        FileAccessor fileAccessor = new FileAccessor();
        TransferPictures transferPictures =
                new TransferPictures(ftpAccessor, fileAccessor, config.destinationRootFolder(), new ImageModifier(), new Retry(bufferedReader));
        LastBackup lastBackup = new LastBackup(ftpAccessor, config.destinationRootFolder());
        HandyPics handyPics = new HandyPics(transferPictures, lastBackup);
        handyPics.transferImagesFromHandy();

        System.out.println("ALL FINISHED SUCCESSFULLY :-)");
    }

    private static FtpAccessor createFtpAccessor(String ipAddress) {
        AndroidFtpClient ftpClient = new AndroidFtpClient(ipAddress);
        return new FtpAccessor(ftpClient);
    }
}