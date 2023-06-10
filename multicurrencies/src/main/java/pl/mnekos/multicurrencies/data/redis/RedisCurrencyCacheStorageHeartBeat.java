package pl.mnekos.multicurrencies.data.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisCurrencyCacheStorageHeartBeat {

    private RedisCurrencyCacheStorage storage;
    private BukkitTask task;

    public RedisCurrencyCacheStorageHeartBeat(RedisCurrencyCacheStorage storage) {
        this.storage = storage;
    }

    public void startLife() {
        try(Jedis jedis = storage.getJedisPool().getResource()) {
            jedis.del("multicurrencies:server:" + storage.getServerId().toString());

            Pipeline pipeline = jedis.pipelined();

            for(Player player : Bukkit.getOnlinePlayers()) {
                pipeline.sadd("multicurrencies:server:" + storage.getServerId().toString(), String.valueOf(player.getUniqueId()));
            }

            pipeline.sync();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                try(Jedis jedis = storage.getJedisPool().getResource()) {
                    jedis.hset("multicurrencies:heartbeats", storage.getServerId().toString(), String.valueOf(System.currentTimeMillis()));
                }
            }
        }.runTaskTimerAsynchronously(storage.getPlugin(), 1L, 100L);
    }

    public void kill() {
        task.cancel();
        try(Jedis jedis = storage.getJedisPool().getResource()) {
            jedis.hset("multicurrencies:heartbeats", storage.getServerId().toString(), String.valueOf(0));
            jedis.del("multicurrencies:server:" + storage.getServerId().toString());
        }
    }

}
