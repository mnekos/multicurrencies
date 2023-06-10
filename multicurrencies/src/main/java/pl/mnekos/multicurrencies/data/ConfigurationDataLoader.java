package pl.mnekos.multicurrencies.data;

import org.bukkit.configuration.ConfigurationSection;
import pl.mnekos.multicurrencies.data.mysql.MySQL;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

public interface ConfigurationDataLoader {

    void createDataPlace();

    MySQL getMySQL();

    Class<? extends CurrencyCacheStorage> getCurrencyCacheStorageClass();

    JedisPool getRedisPool();

    boolean useDisplayName();

    ConfigurationSection getConfigurationSection(String name);

    Map<String, String> getMessages();

    List<String> getDisabledCurrencyCommandsFor();

    String getVaultCurrency();

    void reloadConfig();

}
