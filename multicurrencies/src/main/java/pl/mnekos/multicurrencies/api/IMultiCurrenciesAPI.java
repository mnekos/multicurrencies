package pl.mnekos.multicurrencies.api;

import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;

import java.util.Collection;
import java.util.UUID;

public interface IMultiCurrenciesAPI {

    boolean isUserLoaded(UUID uuid);

    boolean isUserLoaded(String name);

    boolean isCurrencyLoaded(String name);

    ResponseUser getLoadedUser(long userId);

    ResponseUser getLoadedUser(Player player);

    ResponseUser getLoadedUser(UUID uuid);

    ResponseUser getLoadedUser(String name);

    ResponseUser getUser(long userId);

    ResponseUser getUser(Player player);

    ResponseUser getUser(UUID uuid);

    ResponseUser getUser(String name);

    ResponseCurrency getCurrency(long id);

    ResponseCurrency getCurrency(String name);

    Collection<ResponseCurrency> getCurrencies();

    void createCurrency(String name, String displayName, boolean freeFlow, String commandName);

    void deleteCurrency(long currencyId);

    void deleteCurrency(String name);

    void setCurrencyName(long currencyId, String newName);

    void setCurrencyDisplayName(long currencyId, String newName);

    void setFreeFlow(long currencyId, boolean freeFlow);

    void setCommandName(long currencyId, String commandName);

    double get(long userId, long currencyId);

    double get(String player, long currencyId);

    double get(Player player, long currencyId);

    double get(UUID uuid, long currencyId);

    double get(ResponseUser user, long currencyId);

    boolean has(long userId, long currencyId, double value);

    boolean has(String player, long currencyId, double value);

    boolean has(Player player, long currencyId, double value);

    boolean has(UUID uuid, long currencyId, double value);

    OperationResponse add(long userId, long currencyId, double value, String reason);

    OperationResponse add(String player, long currencyId, double value, String reason);

    OperationResponse add(Player player, long currencyId, double value, String reason);

    OperationResponse add(UUID uuid, long currencyId, double value, String reason);

    OperationResponse set(long userId, long currencyId, double value, String reason);

    OperationResponse set(String player, long currencyId, double value, String reason);

    OperationResponse set(Player player, long currencyId, double value, String reason);

    OperationResponse set(UUID uuid, long currencyId, double value, String reason);

    OperationResponse remove(long userId, long currencyId, double value, String reason);

    OperationResponse remove(String player, long currencyId, double value, String reason);

    OperationResponse remove(Player player, long currencyId, double value, String reason);

    OperationResponse remove(UUID uuid, long currencyId, double value, String reason);

    OperationResponse pay(ResponseUser payer, ResponseUser receiver, long currencyId, double value);

    void clearUserData(long userId, String reason);

    MultiCurrenciesPlugin getPlugin();

}
