package pl.mnekos.multicurrencies.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import pl.mnekos.multicurrencies.data.mysql.MySQL;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YamlConfigurationDataLoader implements ConfigurationDataLoader {

    private Plugin plugin;

    public YamlConfigurationDataLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createDataPlace() {
        new FileManager(plugin).checkFiles();
    }

    @Override
    public MySQL getMySQL() {
        FileConfiguration configuration = plugin.getConfig();

        String ip = configuration.getString("mysql-properties.database.ip");
        int port = configuration.getInt("mysql-properties.database.port");
        String database = configuration.getString("mysql-properties.database.name");

        HikariConfig config = new HikariConfig();

        config.setUsername(configuration.getString("mysql-properties.user.name"));
        config.setPassword(configuration.getString("mysql-properties.user.password"));

        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=yes&characterEncoding=UTF-8");

        config.setMaximumPoolSize(configuration.getInt("mysql-properties.connection-pool.maximum-pool-size"));
        config.setMinimumIdle(configuration.getInt("mysql-properties.connection-pool.minimum-idle"));
        config.setMaxLifetime(configuration.getInt("mysql-properties.connection-pool.maximum-lifetime"));
        config.setConnectionTimeout(configuration.getInt("mysql-properties.connection-pool.connection-timeout"));

        return new MySQL(new HikariDataSource(config));
    }

    @Override
    public Class<? extends CurrencyCacheStorage> getCurrencyCacheStorageClass() {
        String className = plugin.getConfig().getString("currencies-cache-storage.cache-storage");

        try {
            Class<?> clazz = Class.forName(className);

            if(!CurrencyCacheStorage.class.isAssignableFrom(clazz)) {
                plugin.getLogger().log(Level.SEVERE, "Class " + className + " does not extend CurrencyCacheStorage!");
                plugin.getLogger().log(Level.SEVERE, "Switched for default storage type (RAM - pl.mnekos.multicurrencies.data.LocalCurrencyCacheStorage)");
            }

            return (Class<? extends CurrencyCacheStorage>) clazz;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot find class " + className, e);
            plugin.getLogger().log(Level.SEVERE, "Switched for default storage type (RAM - pl.mnekos.multicurrencies.data.LocalCurrencyCacheStorage)", e);
            return LocalCurrencyCacheStorage.class;
        }
    }

    @Override
    public JedisPool getRedisPool() {
        ConfigurationSection configuration = plugin.getConfig().getConfigurationSection("currencies-cache-storage.redis-properties");

        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(configuration.getInt("max-redis-connections", 16));
        config.setJmxEnabled(false);

        String ip = configuration.getString("ip");
        int port = configuration.getInt("port");

        String password = configuration.getString("password");

        return new JedisPool(config, ip, port, 0, password.isEmpty() ? null : password);
    }

    @Override
    public boolean useDisplayName() {
        return plugin.getConfig().getBoolean("use-display-name");
    }

    @Override
    public ConfigurationSection getConfigurationSection(String name) {
        return plugin.getConfig().getConfigurationSection(name);
    }


    @Override
    public Map<String, String> getMessages() {
        return plugin.
                getConfig().
                getConfigurationSection("messages").
                getKeys(false).
                stream().
                collect(
                        Collectors.toMap(
                                key -> key,
                                key -> ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages." + key))
                        )
                );
    }

    @Override
    public List<String> getDisabledCurrencyCommandsFor() {
        return plugin.getConfig().getStringList("disabled-currency-commands-for");
    }

    @Override
    public String getVaultCurrency() {
        return plugin.getConfig().getString("vault-currency");
    }

    @Override
    public void reloadConfig() {
        plugin.reloadConfig();
    }


}
