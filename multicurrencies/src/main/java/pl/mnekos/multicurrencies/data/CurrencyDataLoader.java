package pl.mnekos.multicurrencies.data;

import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.user.User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CurrencyDataLoader {

    void createDataPlace() throws Exception;

    Collection<Currency> getCurrencies() throws Exception;

    Currency createCurrency(String name, String displayName, boolean freeFlow, String commandName) throws Exception;

    void deleteCurrency(long currencyId) throws Exception;

    void setCurrencyName(long currencyId, String name) throws Exception;

    void setCurrencyDisplayName(long currencyId, String name) throws Exception;

    void setFreeFlow(long currencyId, boolean freeFlow) throws Exception;

    void setCommandName(long currencyId, String commandName) throws Exception;

    User createUser(Player player) throws Exception;

    User getUser(long userId) throws Exception;

    User getUser(Player player) throws Exception;

    User getUser(UUID uuid) throws Exception;

    User getUser(String name) throws Exception;

    String setLastDisplayName(Player player) throws Exception;

    Map<Long, Amount> getUserCurrencies(long id) throws Exception;

    void createCurrency(long userId, long currencyId, double value, String reason) throws Exception;

    void addCurrency(long userId, long currencyId, double value, String reason) throws Exception;

    void setCurrency(long userId, long currencyId, double value, String reason) throws Exception;

    void removeCurrency(long userId, long currencyId, double value, String reason) throws Exception;

    void clearUserData(long userId, String reason) throws Exception;
}
