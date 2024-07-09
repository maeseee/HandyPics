package org.ase.transfer;

import java.io.BufferedReader;
import java.io.IOException;

public class Retry {

    private final BufferedReader reader;

    public Retry(BufferedReader reader) {
        this.reader = reader;
    }

    public void callWithRetry(Runnable method) {
        try {
            method.run();
        } catch (Exception e) {
            if (askForRetry()) {
                callWithRetry(method);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean askForRetry() {
        System.out.println("The programm ended with an exception. Should a retry be done now? [Y/N]");
        try {
            String inputString = reader.readLine();
            return inputString.toLowerCase().startsWith("y");
        } catch (IOException e) {
            throw new RuntimeException("Could not read ip address");
        }
    }
}
