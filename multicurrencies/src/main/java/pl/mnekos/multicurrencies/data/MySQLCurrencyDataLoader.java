package pl.mnekos.multicurrencies.data;

import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.data.mysql.MySQL;
import pl.mnekos.multicurrencies.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MySQLCurrencyDataLoader implements CurrencyDataLoader {

    private MultiCurrenciesPlugin plugin;
    private boolean displayName;
    private MySQL database;

    public MySQLCurrencyDataLoader(MultiCurrenciesPlugin plugin) {
        this.plugin = plugin;
        this.displayName = plugin.getConfigurationDataLoader().useDisplayName();
    }

    @Override
    public void createDataPlace() throws Exception {
        this.database = plugin.getConfigurationDataLoader().getMySQL();

        database.executeUpdate("CREATE TABLE IF NOT EXISTS `currencies` ( `id` BIGINT NOT NULL AUTO_INCREMENT , `name` VARCHAR(32) NOT NULL , `display_name` VARCHAR(64) NOT NULL , `free_flow` BOOLEAN NOT NULL , `command_name` VARCHAR(32) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
        database.executeUpdate("CREATE TABLE IF NOT EXISTS `users` ( `id` BIGINT NOT NULL AUTO_INCREMENT , `uuid` CHAR(36) NOT NULL , `last_display_name` VARCHAR(64) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
        database.executeUpdate("CREATE TABLE IF NOT EXISTS `user_currencies` ( `id` BIGINT NOT NULL AUTO_INCREMENT , `user_id` BIGINT NOT NULL , `currency_id` BIGINT NOT NULL , `amount` DOUBLE NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
        database.executeUpdate("CREATE TABLE IF NOT EXISTS `currency_logs` ( `id` BIGINT NOT NULL AUTO_INCREMENT , `date` DATETIME NOT NULL , `currency_id` BIGINT NOT NULL , `action` VARCHAR(255) NOT NULL , `value` VARCHAR(255) NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
        database.executeUpdate("CREATE TABLE IF NOT EXISTS `user_logs` ( `id` BIGINT NOT NULL AUTO_INCREMENT , `date` DATETIME NOT NULL , `user_id` BIGINT NOT NULL , `action` VARCHAR(255) NOT NULL , `value` VARCHAR(255) NULL , `reason` VARCHAR(255) NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    }

    @Override
    public Set<Currency> getCurrencies() throws Exception {
        AtomicReference<Set<Currency>> currencies = new AtomicReference<>(new HashSet<>());

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try {
                while(result.next()) {
                    currencies.get().add(
                            new Currency(
                                    result.getLong("id"),
                                    result.getString("name"),
                                    result.getString("display_name"),
                                    result.getBoolean("free_flow"),
                                    result.getString("command_name")
                            )
                    );
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT * FROM `currencies`", consumer);

        if(ex.get() != null) {
            throw ex.get();
        }

        return currencies.get();
    }

    @Override
    public Currency createCurrency(String name, String displayName, boolean freeFlow, String commandName) throws Exception {
        database.executeUpdate("INSERT INTO `currencies`(`id`, `name`, `display_name`, `free_flow`, `command_name`) VALUES (0, ?, ?, ?, ?)", name, displayName, freeFlow, commandName);

        AtomicReference<Currency> currency = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try {
                while(result.next()) {
                    currency.set(
                            new Currency(
                                    result.getLong("id"),
                                    name,
                                    displayName,
                                    freeFlow,
                                    commandName
                            )
                    );
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `id` FROM `currencies` WHERE `name`=?", consumer, name);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), currency.get().getId(), "CREATE", currency.get().toString());

        if(ex.get() != null) {
            throw ex.get();
        }

        return currency.get();
    }

    @Override
    public void deleteCurrency(long currencyId) throws Exception {
        database.executeUpdate("DELETE FROM `currencies` WHERE `id`=?", currencyId);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), currencyId, "DELETE");
    }

    @Override
    public void setCurrencyName(long currencyId, String name) throws Exception {
        database.executeUpdate("UPDATE `currencies` SET `name`=? WHERE `id`=?", name, currencyId);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), currencyId, "SET_NAME", name);
    }

    @Override
    public void setCurrencyDisplayName(long currencyId, String name) throws Exception {
        database.executeUpdate("UPDATE `currencies` SET `display_name`=? WHERE `id`=?", name, currencyId);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), currencyId, "SET_DISPLAY_NAME", name);
    }

    @Override
    public void setFreeFlow(long currencyId, boolean freeFlow) throws Exception {
        database.executeUpdate("UPDATE `currencies` SET `free_flow`=? WHERE `id`=?", freeFlow, currencyId);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), currencyId, "SET_FREE_FLOW", freeFlow);
    }

    @Override
    public void setCommandName(long currencyId, String commandName) throws Exception {
        database.executeUpdate("UPDATE `currencies` SET `command_name`=? WHERE `id`=?", commandName, currencyId);
        database.executeUpdate("INSERT INTO `currency_logs`(`id`, `date`, `currency_id`, `action`, `value`) VALUES (0, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), currencyId, "SET_COMMAND_NAME", commandName);
    }

    @Override
    public User createUser(Player player) throws SQLException {
        String playerDisplayName = displayName(player);

        database.executeUpdate("INSERT INTO `users`(`id`, `uuid`, `last_display_name`) VALUES (0, ?, ?)", player.getUniqueId().toString(), playerDisplayName);

        AtomicReference<User> user = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    if(user.get() != null) {
                        plugin.getLogger().log(Level.SEVERE, "Created more than one user with the same UUID!");
                        database.executeUpdate("DELETE FROM `users` WHERE `uuid`=?", player.getUniqueId().toString());
                        user.set(createUser(player));
                    }
                    user.set(
                            new User(
                                    result.getLong("id"),
                                    player.getUniqueId(),
                                    playerDisplayName,
                                    new HashMap<>()
                            )
                    );
                    break;
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `id` FROM `users` WHERE `uuid`=?", consumer, player.getUniqueId().toString());

        if(ex.get() != null) {
            throw ex.get();
        }

        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), user.get().getNumericId(), "CREATE", user.get().toString());

        return user.get();
    }

    @Override
    public User getUser(long userId) throws Exception {
        AtomicReference<User> user = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    user.set(
                            new User(
                                    userId,
                                    UUID.fromString(result.getString("uuid")),
                                    result.getString("last_display_name"),
                                    null
                            )
                    );
                    break;
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `uuid`, `last_display_name` FROM `users` WHERE `id`=?", consumer, userId);

        if(ex.get() != null) {
            throw ex.get();
        }

        if(user.get() == null) {
            return null;
        }

        user.get().setCurrencyValues(getUserCurrencies(user.get().getNumericId()));

        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), user.get().getNumericId(), "CREATE", user.get().toString());

        return user.get();
    }


    @Override
    public User getUser(Player player) throws Exception {
        AtomicReference<User> user = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    user.set(
                            new User(
                                    result.getLong("id"),
                                    player.getUniqueId(),
                                    result.getString("last_display_name"),
                                    null
                            )
                    );
                    break;
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `id`, `last_display_name` FROM `users` WHERE `uuid`=?", consumer, player.getUniqueId().toString());

        if(ex.get() != null) {
            throw ex.get();
        }

        if(user.get() == null) {
            return null;
        }

        user.get().setCurrencyValues(getUserCurrencies(user.get().getNumericId()));

        String name = displayName(player);

        if(!user.get().getDisplayName().equals(name)) {
            setLastDisplayName(player);
            user.get().setDisplayName(name);
        }

        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), user.get().getNumericId(), "CREATE", user.get().toString());

        return user.get();
    }

    @Override
    public User getUser(UUID uuid) throws Exception {
        AtomicReference<User> user = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    user.set(
                            new User(
                                    result.getLong("id"),
                                    uuid,
                                    result.getString("last_display_name"),
                                    null
                            )
                    );
                    break;
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `id`, `last_display_name` FROM `users` WHERE `uuid`=?", consumer, uuid.toString());

        if(ex.get() != null) {
            throw ex.get();
        }

        if(user.get() == null) {
            return null;
        }

        user.get().setCurrencyValues(getUserCurrencies(user.get().getNumericId()));

        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), user.get().getNumericId(), "CREATE", user.get().toString());

        return user.get();
    }

    @Override
    public User getUser(String name) throws Exception {
        AtomicReference<User> user = new AtomicReference<>(null);

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    user.set(
                            new User(
                                    result.getLong("id"),
                                    UUID.fromString(result.getString("uuid")),
                                    result.getString("last_display_name"),
                                    null
                            )
                    );
                    break;
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT * FROM `users` WHERE UPPER(`last_display_name`)=?", consumer, name.toUpperCase());

        if(ex.get() != null) {
            throw ex.get();
        }

        if(user.get() == null) {
            return null;
        }

        user.get().setCurrencyValues(getUserCurrencies(user.get().getNumericId()));

        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, null)", new java.sql.Timestamp(new Date().getTime()), user.get().getNumericId(), "CREATE", user.get().toString());

        return user.get();
    }

    @Override
    public String setLastDisplayName(Player player) throws Exception {
        String name = displayName(player);

        database.executeUpdate("UPDATE `users` SET `last_display_name`=? WHERE `uuid`=?", name, player.getUniqueId().toString());
        return name;
    }

    @Override
    public Map<Long, Amount> getUserCurrencies(long id) throws Exception {
        AtomicReference< Map<Long, Amount>> currencies = new AtomicReference<>(new HashMap<>());

        AtomicReference<SQLException> ex = new AtomicReference<>(null);

        Consumer<ResultSet> consumer = result -> {
            try{
                while(result.next()) {
                    currencies.get().put(
                            result.getLong("currency_id"),
                            new Amount(result.getDouble("amount"))
                    );
                }
            } catch (SQLException exception) {
                ex.set(exception);
            }
        };

        database.query("SELECT `currency_id`, `amount` FROM `user_currencies` WHERE `user_id`=?", consumer, id);

        if(ex.get() != null) {
            throw ex.get();
        }

        return currencies.get();
    }

    @Override
    public void createCurrency(long userId, long currencyId, double value, String reason) throws Exception {
        database.executeUpdate("INSERT INTO `user_currencies`(`id`, `user_id`, `currency_id`, `amount`) VALUES (0, ?, ?, ?)", userId, currencyId, value);
        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), userId, "CREATE_ADD_CURRENCY", currencyId + "-" + value, reason);
    }

    @Override
    public void addCurrency(long userId, long currencyId, double value, String reason) throws Exception {
        database.executeUpdate("UPDATE `user_currencies` SET `amount`=`amount` + ? WHERE `user_id`=? AND `currency_id`=?", value, userId, currencyId);
        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), userId, "ADD_CURRENCY", currencyId + "-" + value, reason);
    }

    @Override
    public void setCurrency(long userId, long currencyId, double value, String reason) throws Exception {
        database.executeUpdate("UPDATE `user_currencies` SET `amount`=? WHERE `user_id`=? AND `currency_id`=?", value, userId, currencyId);
        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), userId, "SET_CURRENCY", currencyId + "-" + value, reason);
    }

    @Override
    public void removeCurrency(long userId, long currencyId, double value, String reason) throws Exception {
        database.executeUpdate("UPDATE `user_currencies` SET `amount`=`amount` - ? WHERE `user_id`=? AND `currency_id`=?", value, userId, currencyId);
        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, ?, ?)", new java.sql.Timestamp(new Date().getTime()), userId, "REMOVE_CURRENCY", currencyId + "-" + value, reason);
    }

    @Override
    public void clearUserData(long userId, String reason) throws Exception {
        database.executeUpdate("UPDATE `user_currencies` SET `amount`=0 WHERE `user_id`=?", userId);
        database.executeUpdate("INSERT INTO `user_logs`(`id`, `date`, `user_id`, `action`, `value`, `reason`) VALUES (0, ?, ?, ?, null, ?)", new java.sql.Timestamp(new Date().getTime()), userId, "CLEAR_DATA", reason);
    }

    private String displayName(Player player) {
        return displayName ? player.getDisplayName() : player.getName();
    }
}
