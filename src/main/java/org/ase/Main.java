package org.ase;

import org.ase.config.Config;
import org.ase.config.ConfigReader;
import org.ase.config.SystemPreparation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SystemPreparation systemPreparation = new SystemPreparation(bufferedReader);
        systemPreparation.showWarningForExport();
        Path workingPath = Path.of("C:/Users/maese/Bilder/FromHandy");
        ConfigReader configReader = new ConfigReader(workingPath, bufferedReader);
        Config config = configReader.read();
        systemPreparation.prepareFolderPath(config.destinationWorkPath());

        HandyPics handyPics = new HandyPics(config);
        handyPics.transferImagesFromHandy();
        handyPics.setRatingOnBestPics();
    }
}