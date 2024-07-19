package org.ase;

import org.ase.config.Config;
import org.ase.config.HandyPicStarter;
import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.ApacheFtpClient;
import org.ase.history.LastBackup;
import org.ase.image.ImageModifier;
import org.ase.transfer.Retry;
import org.ase.transfer.Transfer;
import org.ase.transfer.TransferFile;
import org.ase.transfer.TransferFolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        HandyPicStarter handyPicStarter = new HandyPicStarter(bufferedReader);
        Config config = handyPicStarter.readConfig();

        HandyPics handyPics = getHandyPics(bufferedReader, config);
        handyPics.transferImagesFromHandy();

        System.out.println("ALL FINISHED SUCCESSFULLY :-)");
    }

    private static HandyPics getHandyPics(BufferedReader bufferedReader, Config config) {
        ApacheFtpClient ftpClient = new ApacheFtpClient(config.ipAddress());
        FileAccessor fileAccessor = new FileAccessor();
        TransferFile transferFile = new TransferFile(ftpClient, fileAccessor, new Retry(bufferedReader));
        TransferFolder transferFolder = new TransferFolder(transferFile, new Retry(bufferedReader));
        Transfer transfer = new Transfer(transferFolder, fileAccessor, config.destinationRootFolder(), new ImageModifier());
        LastBackup lastBackup = new LastBackup(ftpClient, config.destinationRootFolder());
        return new HandyPics(transfer, lastBackup);
    }
}