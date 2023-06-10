package pl.mnekos.multicurrencies.data.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;

public class LuaManager {
    private JedisPool pool;

    public LuaManager.Script createScript(String script) {
        try (Jedis jedis = pool.getResource()) {
            String hash = jedis.scriptLoad(script);
            LuaManager.Script scr = new LuaManager.Script(script, hash);
            return scr;
        }
    }

    public LuaManager(JedisPool pool) {
        this.pool = pool;
    }

    public class Script {
        private final String script;
        private final String hashed;

        public Object eval(List<String> keys, List<String> args) {
            try(Jedis jedis = pool.getResource()) {
                Object data;
                try {
                    data = jedis.evalsha(this.hashed, keys, args);
                } catch (JedisDataException ex) {
                    if (!ex.getMessage().startsWith("NOSCRIPT")) {
                        throw ex;
                    }

                    data = jedis.eval(this.script, keys, args);
                }

                return data;
            }
        }

        public Script(String script, String hashed) {
            this.script = script;
            this.hashed = hashed;
        }
    }
}
