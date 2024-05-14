package org.ase;

public class Main {
    public static void main(String[] args) {
        // TODO check destination folder available -> exit
        // TODO check destination folder not empty -> warning
        // TODO mobile tipp for export

        ConfigReader configReader = new ConfigReader();
        configReader.read();
    }
}