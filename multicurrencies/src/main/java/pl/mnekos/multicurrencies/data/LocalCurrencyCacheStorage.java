package pl.mnekos.multicurrencies.data;

import org.bukkit.Bukkit;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.commands.CurrencyCommandExecutor;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.user.User;

import java.util.*;
import java.util.logging.Level;

public class LocalCurrencyCacheStorage extends CurrencyCacheStorage {

    private Collection<Currency> currencies = null;
    private Collection<User> users = new HashSet<>();

    public LocalCurrencyCacheStorage(MultiCurrenciesPlugin plugin) {
        super(plugin);
    }

    @Override
    public void setBlackListCurrencies(List<String> list) {
        super.setBlackListCurrencies(list);

        if(currencies != null) {
            for(Currency currency : currencies) {
                unregisterCurrencyCommandExecutor(currency.getId());
                registerCurrencyCommandExecutor(currency.getId());
            }
        }
    }

    @Override
    public void saveCurrencies(Collection<Currency> currencies) {
        if(currencies == null) {
            this.currencies = new HashSet<>();
        }

        currencies.forEach(this::saveCurrency);
    }

    @Override
    public void saveCurrency(Currency currency) {
        if(currencies == null) {
            currencies = new HashSet<>();
        }
        currencies.add(currency);
        registerCurrencyCommandExecutor(currency.getId());
    }

    @Override
    public void removeCurrency(long currencyId) {
        unregisterCurrencyCommandExecutor(currencyId);
        currencies.removeIf(currency -> currency.getId() == currencyId);
    }

    @Override
    public Collection<Currency> getCurrencies() {
        return currencies;
    }

    @Override
    public Currency getCurrency(long id) {
        return currencies.stream().filter(currency -> currency.getId() == id).findAny().orElse(null);
    }

    @Override
    public Currency getCurrency(String name) {
        return currencies.stream().filter(currency -> currency.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public void setCurrencyName(long currencyId, String newName) {
        getCurrency(currencyId).setName(newName);
    }

    @Override
    public void setCurrencyDisplayName(long currencyId, String newName) {
        getCurrency(currencyId).setDisplayName(newName);
    }

    @Override
    public void setFreeFlow(long currencyId, boolean freeFlow) {
        Currency currency = getCurrency(currencyId);

        if(currency.isFreeFlow() == freeFlow) {
            return;
        }

        currency.setFreeFlow(freeFlow);
    }

    @Override
    public void setCommandName(long currencyId, String newName) {
        Currency currency = getCurrency(currencyId);

        currency.setCommandName(newName);

        unregisterCurrencyCommandExecutor(currency.getId());
        registerCurrencyCommandExecutor(currency.getId());
    }

    @Override
    public boolean isCurrencyAvailable(String name) {
        return getCurrency(name) != null;
    }

    @Override
    public boolean areCurrenciesAvailable() {
        return currencies != null;
    }

    @Override
    public void saveUsers(Collection<User> users) {
        users.forEach(this::saveUser);
    }

    @Override
    public void saveUser(User user) {
        if(user == null) {
            Thread.dumpStack();
        }

        users.add(user);
    }

    @Override
    public void saveUserDisplayName(UUID user, String displayName) {
        getUser(user).setDisplayName(displayName);
    }

    @Override
    public void deleteUser(UUID uuid) {
        users.removeIf(user -> user.getUniqueId().equals(uuid));
    }

    @Override
    public User getUser(long userId) {
        return users.stream().filter(user -> (user.getNumericId() == userId)).findAny().orElse(null);
    }

    @Override
    public User getUser(UUID uuid) {
        return users.stream().filter(user -> user.getUniqueId().equals(uuid)).findAny().orElse(null);
    }

    @Override
    public User getUser(String name) {
        return users.stream().filter(user -> user.getDisplayName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    @Override
    public Collection<User> getLoadedUsers() {
        return users;
    }

    @Override
    public boolean isUserAvailable(UUID uuid) {
        return getUser(uuid) != null;
    }

    @Override
    public boolean isUserAvailable(String name) {
        return getUser(name) != null;
    }

    @Override
    public double get(Currency currency, UUID uuid) {
        User user = getUser(uuid);

        Amount amount = user.getCurrencyValues().get(currency.getId());

        return user != null ? amount != null ? amount.get() : 0 : -1D;
    }

    @Override
    public boolean hasCurrency(Currency currency, UUID uuid) {
        User user = getUser(uuid);

        Amount amount = user.getCurrencyValues().get(currency.getId());

        return amount != null;
    }

    @Override
    public boolean add(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }

        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        synchronized (currencies) {
            if(amount != null) {
                return currencies.get(currency.getId()).add(value);
            } else {
                currencies.put(currency.getId(), new Amount(value));
                return true;
            }
        }
    }

    @Override
    public boolean set(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }

        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        if(amount == null) {
            return this.add(currencyId, uuid, value);
        } else {
            return currencies.get(currency.getId()).set(value);
        }
    }

    @Override
    public boolean remove(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }


        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        return amount != null && amount.remove(value);
    }

    @Override
    public boolean canAdd(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }

        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        synchronized (currencies) {
            if(amount != null) {
                return currencies.get(currency.getId()).canAdd(value);
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean canSet(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }

        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        if(amount == null) {
            return canAdd(currencyId, uuid, value);
        } else {
            return currencies.get(currency.getId()).canSet(value);
        }
    }

    @Override
    public boolean canRemove(long currencyId, UUID uuid, double value) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }


        Currency currency = getCurrency(currencyId);

        if(currency == null) {
            return false;
        }

        Map<Long, Amount> currencies = user.getCurrencyValues();

        Amount amount = currencies.get(currency.getId());

        return amount != null && amount.canRemove(value);
    }

    @Override
    public void clearUserData(long userId) {
        Map<Long, Amount> map = getUser(userId).getCurrencyValues();
        synchronized (map) {
            for(Long id : map.keySet()) {
                map.remove(id);
                map.put(id, new Amount(0));
            }
        }
    }

    @Override
    public void gc() {
        int counter = 0;

        for(User user : new HashSet<>(users)) {
            if(Bukkit.getPlayer(user.getUniqueId()) == null) {
                users.remove(user);
                counter++;
            }
        }

        plugin.getLogger().log(Level.INFO, "[GC] Removed " + counter + " not used user's data.");
    }

    private void registerCurrencyCommandExecutor(long currencyId) {
        Currency currency = getCurrency(currencyId);

        if(blackListCurrencies.contains(currency.getName())) {
            return;
        }

        plugin.registerExecutor(new CurrencyCommandExecutor(plugin, currencyId));
    }

    private void unregisterCurrencyCommandExecutor(long currencyId) {
        plugin.getCommandExecutors().removeIf(executor -> {
            if(executor instanceof CurrencyCommandExecutor) {
                if(((CurrencyCommandExecutor) executor).getCurrencyId() == currencyId) {
                    try {
                        executor.unregister();
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        plugin.getLogger().log(Level.SEVERE, "Cannot delete command.", e);
                    }
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public void close() {
        return;
    }
}
