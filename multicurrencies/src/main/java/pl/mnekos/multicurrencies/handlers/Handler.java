package pl.mnekos.multicurrencies.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Handler implements Listener {

    public static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    protected MultiCurrenciesPlugin plugin;

    public Handler(MultiCurrenciesPlugin plugin, boolean register) {
        this.plugin = plugin;
        if(register) register();
    }

    public Handler register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
