package pl.mnekos.multicurrencies.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class FileManager {

    public static final String CONFIG_FILE_NAME = "config.yml";

    private final Plugin plugin;
    private File folder;
    private File configFile;

    public FileManager(Plugin plugin) {
        this.plugin = plugin;
        this.folder = this.plugin.getDataFolder();
        this.configFile = new File(folder, CONFIG_FILE_NAME);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void checkFiles() {
        if(!folder.exists()) folder.mkdir();
        if(!configFile.exists()) plugin.saveDefaultConfig();
    }

    public boolean isLoaded() {
        return configFile != null && configFile.exists();
    }

    public void saveData(FileConfiguration config) {
        try {
            config.save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occured while saving a file.", e);
        }
        checkFiles();
    }
}
