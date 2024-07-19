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
            RetryCommand cmd = askForRetryCommand();
            switch (cmd) {
                case EXIT -> throw new RuntimeException(e);
                case RETRY -> callWithRetry(method);
                case IGNORE -> continueToTheNext();
            }
        }
    }

    private RetryCommand askForRetryCommand() {
        System.err.println("The process had an exception. What should be done? [EXIT, RETRY, IGNORE]");
        String inputString;
        try {
            inputString = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read ip address");
        }
        if (inputString.toLowerCase().startsWith("e")) {
            return RetryCommand.EXIT;
        }
        if (inputString.toLowerCase().startsWith("i")) {
            return RetryCommand.IGNORE;
        }
        return RetryCommand.RETRY;
    }

    private void continueToTheNext() {
    }
}
