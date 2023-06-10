package pl.mnekos.multicurrencies.commands;

import java.util.concurrent.ExecutorService;

public interface Asynchronized {

    void shutdownExecutorService();

    ExecutorService getExecutorService();

}
