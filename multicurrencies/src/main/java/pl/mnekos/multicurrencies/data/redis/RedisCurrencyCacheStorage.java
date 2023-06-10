package pl.mnekos.multicurrencies.data.redis;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import javafx.util.Pair;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.currency.Currency;
import pl.mnekos.multicurrencies.data.Amount;
import pl.mnekos.multicurrencies.data.CurrencyCacheStorage;
import pl.mnekos.multicurrencies.user.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class RedisCurrencyCacheStorage extends CurrencyCacheStorage {

    protected static final String CHANNEL_NAME = "multicurrencies";

    private JedisPool pool;
    private LuaManager luaManager;
    protected LuaManager.Script currency_action;
    protected LuaManager.Script get_currency;
    protected LuaManager.Script get_user;
    protected LuaManager.Script clear_user_currencies;
    protected LuaManager.Script remove_not_used_data;
    private RedisSubscriber subscriber;

    private UUID serverId;

    private RedisCurrencyCacheStorageHandler handler;
    private RedisCurrencyCacheStorageHeartBeat heartBeat;

    public RedisCurrencyCacheStorage(MultiCurrenciesPlugin plugin) {
        super(plugin);
        try {
            this.pool = plugin.getConfigurationDataLoader().getRedisPool();
            this.luaManager = new LuaManager(pool);

            try {
                this.currency_action = luaManager.createScript(new String(ByteStreams.toByteArray(MultiCurrenciesPlugin.class.getClassLoader().getResourceAsStream("lua/currency_action.lua")), StandardCharsets.UTF_8));
                this.get_currency = luaManager.createScript(new String(ByteStreams.toByteArray(MultiCurrenciesPlugin.class.getClassLoader().getResourceAsStream("lua/get_currency.lua")), StandardCharsets.UTF_8));
                this.get_user = luaManager.createScript(new String(ByteStreams.toByteArray(MultiCurrenciesPlugin.class.getClassLoader().getResourceAsStream("lua/get_user.lua")), StandardCharsets.UTF_8));
                this.clear_user_currencies = luaManager.createScript(new String(ByteStreams.toByteArray(MultiCurrenciesPlugin.class.getClassLoader().getResourceAsStream("lua/clear_user_currencies.lua")), StandardCharsets.UTF_8));
                this.remove_not_used_data = luaManager.createScript(new String(ByteStreams.toByteArray(MultiCurrenciesPlugin.class.getClassLoader().getResourceAsStream("lua/remove_not_used_data.lua")), StandardCharsets.UTF_8));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Cannot create lua script.", e);
            }

            File idFile = new File(plugin.getDataFolder(), "id.txt");

            if(idFile.exists()) {
                FileReader reader = new FileReader(idFile);
                BufferedReader bufferedReader = new BufferedReader(reader);

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    serverId = UUID.fromString(line);
                }
                reader.close();
            } else {
                serverId = UUID.randomUUID();

                FileWriter writer = new FileWriter(idFile, true);

                writer.write(serverId.toString());

                writer.close();
            }

            this.subscriber = new RedisSubscriber(this);
            subscriber.subscribe();

            this.handler = new RedisCurrencyCacheStorageHandler(this);
            this.heartBeat = new RedisCurrencyCacheStorageHeartBeat(this);

            this.heartBeat.startLife();
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred.", e);
        }
    }

    public JedisPool getJedisPool() {
        return pool;
    }

    public UUID getServerId() {
        return serverId;
    }

    @Override
    public void setBlackListCurrencies(List<String> list) {
        super.setBlackListCurrencies(list);
        subscriber.handleUpdatedBlackList();
    }

    @Override
    public void saveCurrencies(Collection<Currency> currencies) {
            currencies.forEach(this::saveCurrency);
    }

    @Override
    public void saveCurrency(Currency currency) {
        try(Jedis jedis = pool.getResource()) {
            String key = "currency:" + currency.getId();

            Pipeline pipeline = jedis.pipelined();

            pipeline.hset(key, "name", currency.getName());
            pipeline.hset(key, "displayname", currency.getDisplayName());
            pipeline.hset(key, "freeflow", String.valueOf(currency.isFreeFlow()));
            pipeline.hset(key, "commandname", currency.getCommandName());

            pipeline.sadd("currencies", String.valueOf(currency.getId()));

            pipeline.publish(CHANNEL_NAME, "currency-add:" + currency.getId());

            pipeline.sync();
        }
    }

    @Override
    public void removeCurrency(long currencyId) {
        try(Jedis jedis = pool.getResource()) {
            String key = "currency:" + currencyId;

            Pipeline pipeline = jedis.pipelined();

            pipeline.del(key);
            pipeline.srem("currencies", String.valueOf(currencyId));

            pipeline.publish(CHANNEL_NAME, "currency-remove:" + currencyId);

            pipeline.sync();

        }
    }

    @Override
    public Collection<Currency> getCurrencies() {
        Collection<String> data = (Collection<String>) get_currency.eval(ImmutableList.of(), ImmutableList.of());

        Set<Currency> currencies = new HashSet<>();

        Object[] dataAsArray = data.toArray();

        int currenciesAmount = dataAsArray.length / 5;

        for(int i = 0; i < currenciesAmount; i++) {
            currencies.add(getCurrency(dataAsArray, i * 5));
        }

        return currencies;
    }

    @Override
    public Currency getCurrency(long id) {
        Collection<String> data = (Collection<String>) get_currency.eval(ImmutableList.of(String.valueOf(id)), ImmutableList.of());

        Object[] dataAsArray = data.toArray();

        if(dataAsArray.length != 5) {
            return null;
        }

        return getCurrency(dataAsArray, 0);
    }

    @Override
    public Currency getCurrency(String name) {
        Collection<String> data = (Collection<String>) get_currency.eval(ImmutableList.of(), ImmutableList.of(name));

        Object[] dataAsArray = data.toArray();

        if(dataAsArray.length != 5) {
            return null;
        }

        return getCurrency(dataAsArray, 0);
    }

    private Currency getCurrency(Object[] dataAsArray, int startIndex) {
        return new Currency(Long.parseLong((String) dataAsArray[startIndex + 0]), (String) dataAsArray[startIndex + 1], (String) dataAsArray[startIndex + 2], Boolean.valueOf((String) dataAsArray[startIndex + 3]), (String) dataAsArray[startIndex + 4]);
    }

    @Override
    public void setCurrencyName(long currencyId, String newName) {
        try(Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hset("currency:" + currencyId, "name", newName);
            pipeline.publish(CHANNEL_NAME, "currency-setname:" + currencyId);
            pipeline.sync();
        }
    }

    @Override
    public void setCurrencyDisplayName(long currencyId, String newName) {
        try(Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hset("currency:" + currencyId, "displayname", newName);
            pipeline.publish(CHANNEL_NAME, "currency-setdisplayname:" + currencyId);
            pipeline.sync();
        }
    }

    @Override
    public void setFreeFlow(long currencyId, boolean freeFlow) {
        try(Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hset("currency:" + currencyId, "freeflow", String.valueOf(freeFlow));
            pipeline.publish(CHANNEL_NAME, "currency-setfreeflow:" + currencyId + ":" + freeFlow);
            pipeline.sync();
        }
    }

    @Override
    public void setCommandName(long currencyId, String newName) {
        try(Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hset("currency:" + currencyId, "commandname", newName);
            pipeline.publish(CHANNEL_NAME, "currency-setcommandname:" + currencyId);
            pipeline.sync();
        }
    }

    @Override
    public boolean isCurrencyAvailable(String name) {
        return getCurrency(name) != null;
    }

    @Override
    public boolean areCurrenciesAvailable() {
        try(Jedis jedis = pool.getResource()) {
            return jedis.exists("currencies");
        }
    }

    @Override
    public void saveUsers(Collection<User> users) {
        users.forEach(this::saveUser);
    }

    @Override
    public void saveUser(User user) {
        try(Jedis jedis = pool.getResource()) {
            String key = "currency_user:" + user.getNumericId();

            Pipeline pipeline = jedis.pipelined();

            pipeline.hset(key, "uuid", user.getUniqueId().toString());
            pipeline.hset(key, "displayname", user.getDisplayName());

            for(Map.Entry<Long, Amount> entry : user.getCurrencyValues().entrySet()) {
                pipeline.hset(key + ":currencies", String.valueOf(entry.getKey()), String.valueOf(entry.getValue().get()));
            }

            pipeline.sadd("currency_users", String.valueOf(user.getNumericId()));

            pipeline.sync();
        }
    }

    @Override
    public void saveUserDisplayName(UUID user, String displayName) {
        try(Jedis jedis = pool.getResource()) {
            jedis.hset("currency_user:" + getUser(user).getNumericId(), "displayname", displayName);
        }
    }

    @Override
    public void deleteUser(UUID uuid) {
        try(Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            long numericId = getUser(uuid).getNumericId();

            pipeline.del("currency_user:" + numericId);
            pipeline.del("currency_user:" + numericId + ":currencies");

            pipeline.srem("currency_users", String.valueOf(numericId));

            pipeline.sync();
        }
    }

    @Override
    public User getUser(long userId) {
        Collection<String> data = (Collection<String>) get_user.eval(ImmutableList.of(String.valueOf(userId)), ImmutableList.of());

        Object[] dataAsArray = data.toArray();

        return getUser(dataAsArray, 0).getKey();
    }

    @Override
    public User getUser(UUID uuid) {
        Collection<String> data = (Collection<String>) get_user.eval(ImmutableList.of(), ImmutableList.of("uuid", uuid.toString()));

        Object[] dataAsArray = data.toArray();

        return getUser(dataAsArray, 0).getKey();
    }

    @Override
    public User getUser(String name) {
        Collection<String> data = (Collection<String>) get_user.eval(ImmutableList.of(), ImmutableList.of("name", name));

        Object[] dataAsArray = data.toArray();

        return getUser(dataAsArray, 0).getKey();
    }

    @Override
    public Collection<User> getLoadedUsers() {
        Collection<String> data = (Collection<String>) get_user.eval(ImmutableList.of(), ImmutableList.of());

        Object[] dataAsArray = data.toArray();

        Set<User> users = new HashSet<>();

        if(dataAsArray.length == 0) {
            return users;
        }

        int stoppedAt = 0;

        while(stoppedAt < dataAsArray.length) {
            Pair<User, Integer> pair = getUser(dataAsArray, stoppedAt);

            users.add(pair.getKey());
            stoppedAt = pair.getValue();
        }

        return users;

    }

    private Pair<User, Integer> getUser(Object[] dataAsArray, int startIndex) {
        if(dataAsArray.length == 0) {
            return new Pair<>(null, 0);
        }

        int index = startIndex;
        long id = Long.parseLong((String) dataAsArray[index]);
        UUID uuid = UUID.fromString((String) dataAsArray[index + 1]);
        String displayName = (String) dataAsArray[index + 2];
        Map<Long, Amount> currencyValues = new HashMap<>();

        int endIndex = index + 3;

        for(int i = endIndex; i < dataAsArray.length; i++) {
            String[] parts = ((String) dataAsArray[i]).split(":");

            if(parts.length != 2) {
                endIndex = i;
                break;
            }

            currencyValues.put(Long.parseLong(parts[0]), new Amount(Double.parseDouble(parts[1])));
        }

        return new Pair<>(new User(id, uuid, displayName, currencyValues), endIndex);
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
        return get(currency.getId(), uuid);
    }

    private double get(long currencyId, UUID uuid) {
        User user = getUser(uuid);

        if(user == null) {
            return 0D;
        }

        Amount amount = user.getCurrencyValues().get(currencyId);

        if(amount == null) {
            return 0D;
        }

        return amount.get();
    }

    @Override
    public boolean hasCurrency(Currency currency, UUID uuid) {
        User user = getUser(uuid);

        if(user == null) {
            return false;
        }

        Amount amount = user.getCurrencyValues().get(currency.getId());

        return amount != null;
    }

    @Override
    public boolean add(long currencyId, UUID uuid, double value) {
        return getBooleanByInt(currency_action.eval(ImmutableList.of(String.valueOf(currencyId), "add"), ImmutableList.of(uuid.toString(), String.valueOf(value))));
    }

    @Override
    public boolean set(long currencyId, UUID uuid, double value) {
        return getBooleanByInt(currency_action.eval(ImmutableList.of(String.valueOf(currencyId), "set"), ImmutableList.of(uuid.toString(), String.valueOf(value))));
    }

    @Override
    public boolean remove(long currencyId, UUID uuid, double value) {
        return getBooleanByInt(currency_action.eval(ImmutableList.of(String.valueOf(currencyId), "remove"), ImmutableList.of(uuid.toString(), String.valueOf(value))));
    }

    @Override
    public boolean canAdd(long currencyId, UUID uuid, double value) {
        return true;
    }

    @Override
    public boolean canSet(long currencyId, UUID uuid, double value) {
        return true;
    }

    @Override
    public boolean canRemove(long currencyId, UUID uuid, double value) {
        return get(currencyId, uuid) >= value;
    }

    private boolean getBooleanByInt(Object o) {
        return ((long) o) == 1 ? true : false;
    }

    @Override
    public void clearUserData(long userId) {
        clear_user_currencies.eval(ImmutableList.of(String.valueOf(userId)), ImmutableList.of());
    }

    @Override
    public void gc() {
        long counter = (long) remove_not_used_data.eval(ImmutableList.of(String.valueOf(System.currentTimeMillis())), ImmutableList.of());

        plugin.getLogger().log(Level.INFO, "[GC] Removed " + counter + " not used user's data.");
    }

    @Override
    public void close() {
        heartBeat.kill();
        handler.unregister();
        subscriber.close();
        pool.destroy();
    }
}
