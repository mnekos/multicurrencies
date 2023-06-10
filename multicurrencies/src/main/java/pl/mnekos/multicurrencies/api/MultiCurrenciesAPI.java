package pl.mnekos.multicurrencies.api;

import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.factory.ResponseCurrencyFactory;
import pl.mnekos.multicurrencies.api.factory.ResponseUserFactory;
import pl.mnekos.multicurrencies.data.CurrencyCacheStorage;
import pl.mnekos.multicurrencies.user.User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MultiCurrenciesAPI implements IMultiCurrenciesAPI {

    private MultiCurrenciesPlugin plugin;

    public MultiCurrenciesAPI(MultiCurrenciesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isUserLoaded(String name) {
        return plugin.getStorage().isUserAvailable(name);
    }

    @Override
    public boolean isCurrencyLoaded(String name) {
        return plugin.getStorage().isCurrencyAvailable(name);
    }

    @Override
    public boolean isUserLoaded(UUID uuid) {
        return plugin.getStorage().isUserAvailable(uuid);
    }

    @Override
    public ResponseUser getLoadedUser(long userId) {
        User user = plugin.getStorage().getUser(userId);

        if(user == null) {
            return null;
        }

        return ResponseUserFactory.newResponseUser(user);
    }

    @Override
    public ResponseUser getLoadedUser(Player player) {
        return getLoadedUser(player.getUniqueId());
    }

    @Override
    public ResponseUser getLoadedUser(UUID uuid) {
        User user = plugin.getStorage().getUser(uuid);

        if(user == null) {
            return null;
        }

        return ResponseUserFactory.newResponseUser(user);
    }

    @Override
    public ResponseUser getLoadedUser(String name) {
        User user = plugin.getStorage().getUser(name);

        if(user == null) {
            return null;
        }

        return ResponseUserFactory.newResponseUser(user);
    }

    @Override
    public ResponseUser getUser(long userId) {
        ResponseUser user = getLoadedUser(userId);

        if(user != null) {
            return user;
        }

        try {
            User userObj = plugin.getCurrencyDataLoader().getUser(userId);

            if(userObj == null) {
                return null;
            }

            plugin.getStorage().saveUser(userObj);

            return ResponseUserFactory.newResponseUser(userObj);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot get user from database.", e);
            return null;
        }
    }

    @Override
    public ResponseUser getUser(Player player) {
        ResponseUser user = getLoadedUser(player);

        if(user != null) {
            return user;
        }

        try {
            User userObj = plugin.getCurrencyDataLoader().getUser(player);

            if(userObj == null) {
                return null;
            }

            plugin.getStorage().saveUser(userObj);

            return ResponseUserFactory.newResponseUser(userObj);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot get user from database.", e);
            return null;
        }
    }

    @Override
    public ResponseUser getUser(UUID uuid) {
        ResponseUser user = getLoadedUser(uuid);

        if(user != null) {
            return user;
        }

        try {
            User userObj = plugin.getCurrencyDataLoader().getUser(uuid);

            if(userObj == null) {
                return null;
            }

            plugin.getStorage().saveUser(userObj);

            return ResponseUserFactory.newResponseUser(userObj);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot get user from database.", e);
            return null;
        }
    }

    @Override
    public ResponseUser getUser(String name) {
        ResponseUser user = getLoadedUser(name);

        if(user != null) {
            return user;
        }

        try {
            User userObj = plugin.getCurrencyDataLoader().getUser(name);

            if(userObj == null) {
                return null;
            }

            plugin.getStorage().saveUser(userObj);

            return ResponseUserFactory.newResponseUser(userObj);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot get user from database.", e);
            return null;
        }
    }

    @Override
    public ResponseCurrency getCurrency(long id) {
        return ResponseCurrencyFactory.newResponseCurrency(plugin.getStorage().getCurrency(id));
    }

    @Override
    public ResponseCurrency getCurrency(String name) {
        return ResponseCurrencyFactory.newResponseCurrency(plugin.getStorage().getCurrency(name));
    }

    @Override
    public Collection<ResponseCurrency> getCurrencies() {
        return plugin.getStorage().getCurrencies().stream().map(currency -> ResponseCurrencyFactory.newResponseCurrency(currency)).collect(Collectors.toList());
    }

    @Override
    public void createCurrency(String name, String displayName, boolean freeFlow, String commandName) {
        try {
            plugin.getStorage().saveCurrency(plugin.getCurrencyDataLoader().createCurrency(name, displayName, freeFlow, commandName));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot create new currency.", e);
        }
    }

    @Override
    public void deleteCurrency(long currencyId) {
        plugin.getStorage().removeCurrency(currencyId);

        try {
            plugin.getCurrencyDataLoader().deleteCurrency(currencyId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot delete currency.", e);
        }
    }

    @Override
    public void deleteCurrency(String name) {
        deleteCurrency(getCurrency(name).getId());
    }

    @Override
    public void setCurrencyName(long currencyId, String newName) {
        CurrencyCacheStorage storage = plugin.getStorage();

        storage.setCurrencyName(currencyId, newName);

        try {
            plugin.getCurrencyDataLoader().setCurrencyName(currencyId, newName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot set name for currency.", e);
        }
    }

    @Override
    public void setCurrencyDisplayName(long currencyId, String newName) {
        CurrencyCacheStorage storage = plugin.getStorage();

        storage.setCurrencyDisplayName(currencyId, newName);

        try {
            plugin.getCurrencyDataLoader().setCurrencyDisplayName(currencyId, newName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot set display name for currency.", e);
        }
    }

    @Override
    public void setFreeFlow(long currencyId, boolean freeFlow) {
        CurrencyCacheStorage storage = plugin.getStorage();

        storage.setFreeFlow(currencyId, freeFlow);

        try {
            plugin.getCurrencyDataLoader().setFreeFlow(currencyId, freeFlow);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot set free flow for currency.", e);
        }
    }

    @Override
    public void setCommandName(long currencyId, String commandName) {
        CurrencyCacheStorage storage = plugin.getStorage();

        storage.setCommandName(currencyId, commandName);

        try {
            plugin.getCurrencyDataLoader().setCommandName(currencyId, commandName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot set command name for currency.", e);
        }
    }

    @Override
    public double get(long userId, long currencyId) {
        return get(getUser(userId), currencyId);
    }

    @Override
    public double get(String player, long currencyId) {
        return get(getUser(player), currencyId);
    }

    @Override
    public double get(Player player, long currencyId) {
        return get(getUser(player), currencyId);
    }

    @Override
    public double get(UUID uuid, long currencyId) {
        return get(getUser(uuid), currencyId);
    }

    @Override
    public double get(ResponseUser user, long currencyId) {
        if(user == null) {
            return 0D;
        }

        Map.Entry<ResponseCurrency, ResponseAmount> amount = user.getCurrencyValues().entrySet().stream().filter(entry -> entry.getKey().getId() == currencyId).findAny().orElse(null);

        return amount == null ? 0D : amount.getValue().get();
    }

    @Override
    public boolean has(long userId, long currencyId, double value) {
        return get(userId, currencyId) >= value;
    }

    @Override
    public boolean has(String player, long currencyId, double value) {
        return get(player, currencyId) >= value;
    }

    @Override
    public boolean has(Player player, long currencyId, double value) {
        return get(player, currencyId) >= value;
    }

    @Override
    public boolean has(UUID uuid, long currencyId, double value) {
        return get(uuid, currencyId) >= value;
    }

    @Override
    public OperationResponse add(long userId, long currencyId, double value, String reason) {
        User user;

        do {
            CurrencyCacheStorage storage = plugin.getStorage();
            user = storage.getUser(userId);

            if(user != null) {
                boolean create = !user.getCurrencyValues().containsKey(currencyId);
                if(storage.add(currencyId, user.getUniqueId(), value)) {
                    try {
                        if(create) {
                            plugin.getCurrencyDataLoader().createCurrency(userId, currencyId, value, reason);
                        } else {
                            plugin.getCurrencyDataLoader().addCurrency(userId, currencyId, value, reason);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Cannot add currency value.", e);
                    }
                    return OperationResponse.SUCCESS;
                } else {
                    return OperationResponse.FAILURE;
                }
            } else {
                try {
                    storage.saveUser(plugin.getCurrencyDataLoader().getUser(userId));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Cannot load user.", e);
                }
            }
        } while(user == null);

        return OperationResponse.FAILURE;
    }

    @Override
    public OperationResponse add(String player, long currencyId, double value, String reason) {
        return add(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse add(Player player, long currencyId, double value, String reason) {
        return add(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse add(UUID uuid, long currencyId, double value, String reason) {
        return add(getUser(uuid).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse set(long userId, long currencyId, double value, String reason) {
        User user;

        do {
            CurrencyCacheStorage storage = plugin.getStorage();
            user = storage.getUser(userId);

            if(user != null) {
                boolean create = !user.getCurrencyValues().containsKey(currencyId);
                if(storage.set(currencyId, user.getUniqueId(), value)) {
                    try {
                        if(create) {
                            plugin.getCurrencyDataLoader().createCurrency(userId, currencyId, value, reason);
                        } else {
                            plugin.getCurrencyDataLoader().setCurrency(userId, currencyId, value, reason);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Cannot add currency value.", e);
                    }
                    return OperationResponse.SUCCESS;
                } else {
                    return OperationResponse.FAILURE;
                }
            } else {
                try {
                    storage.saveUser(plugin.getCurrencyDataLoader().getUser(userId));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Cannot load user.", e);
                }
            }
        } while(user == null);

        return OperationResponse.FAILURE;
    }

    @Override
    public OperationResponse set(String player, long currencyId, double value, String reason) {
        return set(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse set(Player player, long currencyId, double value, String reason) {
        return set(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse set(UUID uuid, long currencyId, double value, String reason) {
        return set(getUser(uuid).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse remove(long userId, long currencyId, double value, String reason) {
        User user;

        do {
            CurrencyCacheStorage storage = plugin.getStorage();
            user = storage.getUser(userId);

            if(user != null) {
                boolean create = !user.getCurrencyValues().containsKey(currencyId);
                if(storage.remove(currencyId, user.getUniqueId(), value) && !create) {
                    try {
                        plugin.getCurrencyDataLoader().removeCurrency(userId, currencyId, value, reason);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Cannot add currency value.", e);
                    }
                    return OperationResponse.SUCCESS;
                } else {
                    return OperationResponse.FAILURE;
                }
            } else {
                try {
                    storage.saveUser(plugin.getCurrencyDataLoader().getUser(userId));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Cannot load user.", e);
                }
            }
        } while(user == null);

        return OperationResponse.FAILURE;
    }

    @Override
    public OperationResponse remove(String player, long currencyId, double value, String reason) {
        return remove(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse remove(Player player, long currencyId, double value, String reason) {
        return remove(getUser(player).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse remove(UUID uuid, long currencyId, double value, String reason) {
        return remove(getUser(uuid).getNumericId(), currencyId, value, reason);
    }

    @Override
    public OperationResponse pay(ResponseUser payer, ResponseUser receiver, long currencyId, double value) {
        CurrencyCacheStorage storage = plugin.getStorage();

        if(!storage.canRemove(currencyId, payer.getUniqueId(), value) || !storage.canAdd(currencyId, receiver.getUniqueId(), value)) {
            return OperationResponse.FAILURE;
        }

        if(remove(payer.getNumericId(), currencyId, value, "PAY_SENDER-" + receiver.getNumericId()) == OperationResponse.FAILURE) {
            return OperationResponse.FAILURE;
        }

        if(add(receiver.getNumericId(), currencyId, value, "PAY_RECEIVER-" + payer.getNumericId()) == OperationResponse.FAILURE) {
            add(payer.getNumericId(), currencyId, value, "PAY_RETURN_OPERATION_FAILURE-" + receiver.getNumericId());
            return OperationResponse.FAILURE;
        }

        return OperationResponse.SUCCESS;
    }

    @Override
    public void clearUserData(long userId, String reason) {
        User user = plugin.getStorage().getUser(userId);

        if(user != null) {
            plugin.getStorage().clearUserData(userId);
        }

        try {
            plugin.getCurrencyDataLoader().clearUserData(userId, reason);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot clear user data.", e);
        }
    }

    @Override
    public MultiCurrenciesPlugin getPlugin() {
        return plugin;
    }

}
