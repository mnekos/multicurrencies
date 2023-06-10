package pl.mnekos.multicurrencies.data.redis;

import pl.mnekos.multicurrencies.commands.CurrencyCommandExecutor;
import pl.mnekos.multicurrencies.currency.Currency;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class RedisSubscriber implements Closeable {

    private RedisCurrencyCacheStorage storage;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Jedis subscription;
    private JedisPubSub pubSub;

    public RedisSubscriber(RedisCurrencyCacheStorage storage) {
        this.storage = storage;
    }

    public void handleUpdatedBlackList() {
        for(Currency currency : storage.getCurrencies()) {
            unregisterAndRegisterCurrencyCommandExecutor(currency.getId());
        }
    }

    public void subscribe() {
        subscription = storage.getJedisPool().getResource();

        executorService.submit(() -> {

            pubSub = new JedisPubSub() {

                @Override
                public void onMessage(String channel, String message) {
                    storage.getPlugin().getLogger().log(Level.INFO, "[REDIS] Received message \"" + message + "\" from channel \"" + channel + "\".");

                    String[] parts = message.split(":");

                    String action = parts[0];
                    long currencyId = 0L;

                    try {
                        currencyId = Long.parseLong(parts[1]);
                    } catch (Exception e) {}

                    if(currencyId == 0) {
                        storage.getPlugin().getLogger().log(Level.WARNING, "[REDIS] Invalid currency ID.");
                        return;
                    }

                    if(action.equalsIgnoreCase("currency-add")) {
                        registerCurrencyCommandExecutor(currencyId);
                    }

                    else if(action.equalsIgnoreCase("currency-remove")) {
                        unregisterCurrencyCommandExecutor(currencyId);
                    }

                    else if(action.equalsIgnoreCase("currency-setname")) {}

                    else if(action.equalsIgnoreCase("currency-setdisplayname")) {}

                    else if(action.equalsIgnoreCase("currency-setfreeflow")) {}

                    else if(action.equalsIgnoreCase("currency-setcommandname")) {
                        unregisterAndRegisterCurrencyCommandExecutor(currencyId);
                    }

                    else {
                        storage.getPlugin().getLogger().log(Level.WARNING, "[REDIS] Invalid message.");
                    }
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    storage.getPlugin().getLogger().log(Level.INFO, "[REDIS] Subscribed channel \"" + channel + "\".");
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    storage.getPlugin().getLogger().log(Level.INFO, "[REDIS] Unsubscribed channel \"" + channel + "\".");
                }

                @Override
                public void finalize() {
                    if(isSubscribed()) {
                        unsubscribe();
                    }
                }

            };

            subscription.subscribe(pubSub, RedisCurrencyCacheStorage.CHANNEL_NAME);

        });
    }

    public void unsubscribe() {
        pubSub.unsubscribe();
        subscription.getClient().unsubscribe();
        subscription.close();
        executorService.shutdown();
    }

    @Override
    public void close() {
        unsubscribe();
    }

    private void registerCurrencyCommandExecutor(long currencyId) {
        Currency currency = storage.getCurrency(currencyId);

        if(storage.getBlackListCurrencies().contains(currency.getName())) {
            return;
        }

        storage.getPlugin().registerExecutor(new CurrencyCommandExecutor(storage.getPlugin(), currencyId));
    }

    private void unregisterCurrencyCommandExecutor(long currencyId) {
        storage.getPlugin().getCommandExecutors().removeIf(executor -> {
            if(executor instanceof CurrencyCommandExecutor) {
                if(((CurrencyCommandExecutor) executor).getCurrencyId() == currencyId) {
                    try {
                        executor.unregister();
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        storage.getPlugin().getLogger().log(Level.SEVERE, "Cannot delete command.", e);
                    }
                    return true;
                }
            }

            return false;
        });
    }

    public void unregisterAndRegisterCurrencyCommandExecutor(long currencyId) {
        unregisterCurrencyCommandExecutor(currencyId);
        registerCurrencyCommandExecutor(currencyId);
    }
}
