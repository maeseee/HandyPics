package org.ase;

import org.ase.config.Config;
import org.ase.config.HandyPicStarter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        HandyPicStarter handyPicStarter = new HandyPicStarter(bufferedReader);
        Config config = handyPicStarter.readConfig();

        HandyPics handyPics = HandyPics.getHandyPics(bufferedReader, config);
        handyPics.transferImagesFromHandy();

        System.out.println("ALL FINISHED SUCCESSFULLY :-)");
    }
}