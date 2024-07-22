package org.ase;

import org.ase.config.Config;
import org.ase.config.HandyPicStarter;
import org.ase.fileAccess.FileAccessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        FileAccessor fileAccessor = new FileAccessor();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        HandyPicStarter handyPicStarter = new HandyPicStarter(fileAccessor, bufferedReader);
        Config config = handyPicStarter.readConfig();

        HandyPics handyPics = HandyPics.createHandyPics(fileAccessor, bufferedReader, config);
        handyPics.transferImagesFromHandy();

        System.out.println("ALL FINISHED SUCCESSFULLY :-)");
    }
}