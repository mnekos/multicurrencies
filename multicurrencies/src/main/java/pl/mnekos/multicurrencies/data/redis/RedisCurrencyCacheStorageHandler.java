package pl.mnekos.multicurrencies.data.redis;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.mnekos.multicurrencies.handlers.Handler;
import redis.clients.jedis.Jedis;

public class RedisCurrencyCacheStorageHandler extends Handler {

    private RedisCurrencyCacheStorage storage;

    public RedisCurrencyCacheStorageHandler(RedisCurrencyCacheStorage storage) {
        super(storage.getPlugin(), true);
        this.storage = storage;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try(Jedis jedis = storage.getJedisPool().getResource()) {
            jedis.sadd("multicurrencies:server:" + storage.getServerId().toString(), String.valueOf(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try(Jedis jedis = storage.getJedisPool().getResource()) {
            jedis.srem("multicurrencies:server:" + storage.getServerId().toString(), String.valueOf(event.getPlayer().getUniqueId()));
        }
    }

}
