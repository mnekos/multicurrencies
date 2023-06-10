package pl.mnekos.multicurrencies.data;

import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.user.User;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class CurrencyCacheStorage implements Closeable {

    protected MultiCurrenciesPlugin plugin;
    protected List<String> blackListCurrencies = null;

    public CurrencyCacheStorage(MultiCurrenciesPlugin plugin) {
        this.plugin = plugin;
    }

    public MultiCurrenciesPlugin getPlugin() {
        return plugin;
    }

    public List<String> getBlackListCurrencies() {
        return blackListCurrencies;
    }

    public void setBlackListCurrencies(List<String> blackListCurrencies) {
        this.blackListCurrencies = blackListCurrencies;
    }

    public abstract void saveCurrencies(Collection<Currency> currencies);

    public abstract void saveCurrency(Currency currency);

    public abstract void removeCurrency(long currencyId);

    public abstract Collection<Currency> getCurrencies();

    public abstract Currency getCurrency(long id);

    public abstract Currency getCurrency(String name);

    public abstract void setCurrencyName(long currencyId, String newName);

    public abstract void setCurrencyDisplayName(long currencyId, String newName);

    public abstract void setFreeFlow(long currencyId, boolean freeFlow);

    public abstract void setCommandName(long currencyId, String newName);

    public abstract boolean isCurrencyAvailable(String name);

    public abstract boolean areCurrenciesAvailable();

    public abstract void saveUsers(Collection<User> users);

    public abstract void saveUser(User user);

    public abstract void saveUserDisplayName(UUID user, String displayName);

    public abstract void deleteUser(UUID uuid);

    public abstract User getUser(long userId);

    public abstract User getUser(UUID uuid);

    public abstract User getUser(String name);

    public abstract Collection<User> getLoadedUsers();

    public abstract boolean isUserAvailable(UUID uuid);

    public abstract boolean isUserAvailable(String name);

    public abstract double get(Currency currency, UUID uuid);

    public abstract boolean hasCurrency(Currency currency, UUID uuid);

    public abstract boolean add(long currencyId, UUID uuid, double value);

    public abstract boolean set(long currencyId, UUID uuid, double value);

    public abstract boolean remove(long currencyId, UUID uuid, double value);

    public abstract boolean canAdd(long currencyId, UUID uuid, double value);

    public abstract boolean canSet(long currencyId, UUID uuid, double value);

    public abstract boolean canRemove(long currencyId, UUID uuid, double value);

    public abstract void clearUserData(long userId);

    public abstract void gc();

}
