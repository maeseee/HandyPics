package org.ase;

import org.ase.config.Config;
import org.ase.config.HandyPicStarter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        HandyPicStarter handyPicStarter = new HandyPicStarter(bufferedReader);
        handyPicStarter.showWarningForExport();
        Config config = handyPicStarter.readConfig();
        handyPicStarter.prepareFolderPath(config.destinationWorkPath());

        HandyPics handyPics = new HandyPics(config);
        handyPics.transferImagesFromHandy();
        handyPics.setRatingOnBestPics();
    }
}