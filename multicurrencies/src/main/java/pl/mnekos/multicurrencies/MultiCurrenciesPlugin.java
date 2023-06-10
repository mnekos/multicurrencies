package pl.mnekos.multicurrencies;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.mnekos.multicurrencies.api.IMultiCurrenciesAPI;
import pl.mnekos.multicurrencies.api.MultiCurrenciesAPI;
import pl.mnekos.multicurrencies.commands.AdminCurrencyCommandExecutor;
import pl.mnekos.multicurrencies.commands.MultiCurrenciesCommandExecutor;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.currency.CurrencyManager;
import pl.mnekos.multicurrencies.data.*;
import pl.mnekos.multicurrencies.handlers.Handler;
import pl.mnekos.multicurrencies.handlers.PlayerJoinHandler;
import pl.mnekos.multicurrencies.handlers.PlayerQuitHandler;
import pl.mnekos.multicurrencies.placeholder.MultiCurrenciesPlaceholder;
import pl.mnekos.multicurrencies.vault.MultiCurrenciesEconomy;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MultiCurrenciesPlugin extends JavaPlugin {

    private static IMultiCurrenciesAPI API = null;

    public static IMultiCurrenciesAPI getAPI() {
        return API;
    }

    private ConfigurationDataLoader configurationDataLoader;
    private CurrencyDataLoader currencyDataLoader;

    private CurrencyCacheStorage storage;

    private CurrencyManager manager;

    private final Set<Handler> handlers = new HashSet<>();
    private final Set<MultiCurrenciesCommandExecutor> executors = new HashSet<>();

    private Map<String, String> messages;

    private MultiCurrenciesEconomy economy = null;

    private BukkitTask gcTask = null;

    @Override
    public void onEnable() {
        this.configurationDataLoader = new YamlConfigurationDataLoader(this);

        configurationDataLoader.createDataPlace();

        messages = getConfigurationDataLoader().getMessages();

        this.currencyDataLoader = new MySQLCurrencyDataLoader(this);

        try {
            currencyDataLoader.createDataPlace();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Cannot create connection with database or create data place.", e);
        }

        Class<? extends CurrencyCacheStorage> storageClass = configurationDataLoader.getCurrencyCacheStorageClass();

        try {
            Constructor<? extends CurrencyCacheStorage> constructor = storageClass.getConstructor(MultiCurrenciesPlugin.class);

            storage = constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            getLogger().log(Level.SEVERE, "Cannot find constructor " + storageClass.getSimpleName() + "(pl.mnekos.multicurrencies.MultiCurrenciesPlugin)", e);
            getLogger().log(Level.SEVERE, "Switched for default storage type (RAM - pl.mnekos.multicurrencies.data.LocalCurrencyCacheStorage)");
            storage = new LocalCurrencyCacheStorage(this);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            getLogger().log(Level.SEVERE, "Cannot create instance of " + storageClass.getName(), e);
            getLogger().log(Level.SEVERE, "Switched for default storage type (RAM - pl.mnekos.multicurrencies.data.LocalCurrencyCacheStorage)");
            storage = new LocalCurrencyCacheStorage(this);
        }

        storage.setBlackListCurrencies(configurationDataLoader.getDisabledCurrencyCommandsFor());

        manager = new CurrencyManager(this);

        try {
            manager.loadCurrencies();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Cannot load currencies!", e);
        }

        try {
            manager.loadUsers();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Cannot load online users!", e);
        }

        registerHandlers();
        registerExecutors();

        API = new MultiCurrenciesAPI(this);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiCurrenciesPlaceholder().register();
        }

        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            registerOrUpdateEconomy();
        }

        gcTask = new BukkitRunnable() {

            @Override
            public void run() {
                storage.gc();
            }

        }.runTaskTimerAsynchronously(this, 20 * 30, 20 * 60 * 15);

        getLogger().log(Level.INFO, "Enabled MultiCurrencies v." + getDescription().getVersion() + " by mnekos. You found a bug? Contact with me! Discord: mnekos#4359");
    }

    public void registerOrUpdateEconomy() {
        if(Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        Currency currency = getStorage().getCurrency(configurationDataLoader.getVaultCurrency());

        if(currency != null) {
            if(economy == null) {
                Bukkit.getServicesManager().register(Economy.class, economy = new MultiCurrenciesEconomy(), this, ServicePriority.High);
            }
            economy.setCurrencyId(currency.getId());
        }
    }

    @Override
    public void onDisable() {
        unregisterHandlers();
        unregisterExecutors();

        if(!gcTask.isCancelled()) gcTask.cancel();

        try {
            storage.close();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot close storage " + storage.getClass().getName());
        }
    }

    public ConfigurationDataLoader getConfigurationDataLoader() {
        return configurationDataLoader;
    }

    public CurrencyDataLoader getCurrencyDataLoader() {
        return currencyDataLoader;
    }

    public CurrencyCacheStorage getStorage() {
        return storage;
    }

    public CurrencyManager getManager() {
        return manager;
    }

    public void registerHandlers() {
        handlers.add(new PlayerJoinHandler(this, true));
        handlers.add(new PlayerQuitHandler(this, true));
    }

    public void unregisterHandlers() {
        synchronized (handlers) {
            for(Handler handler : handlers) {
                handler.unregister();
            }

            handlers.clear();
        }
    }

    public void registerExecutors() {
        registerExecutor(new AdminCurrencyCommandExecutor(this));
    }

    public void registerExecutor(MultiCurrenciesCommandExecutor executor) {
        executors.add(executor);
    }

    public void unregisterExecutors() {
        synchronized (executors) {
            for(MultiCurrenciesCommandExecutor executor : executors) {
                try {
                    executor.unregister();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    getLogger().log(Level.SEVERE, "Cannot unregister command executor for command '" + executor.getCommand() + "'", e);
                }
            }

            executors.clear();
        }
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public Set<MultiCurrenciesCommandExecutor> getCommandExecutors() {
        return executors;
    }

}
