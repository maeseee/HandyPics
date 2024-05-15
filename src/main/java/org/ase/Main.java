package org.ase;

public class Main {
    public static void main(String[] args) {
        // TODO check destination folder not empty -> warning

        ConfigReader configReader = new ConfigReader();
        configReader.read();
    }
}