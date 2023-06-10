package pl.mnekos.multicurrencies.currency;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.data.CurrencyCacheStorage;
import pl.mnekos.multicurrencies.data.CurrencyDataLoader;
import pl.mnekos.multicurrencies.user.User;

import java.util.concurrent.atomic.AtomicReference;

public class CurrencyManager {

    private MultiCurrenciesPlugin plugin;

    public CurrencyManager(MultiCurrenciesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCurrencies() throws Exception {
        CurrencyCacheStorage storage = plugin.getStorage();
        if(!storage.areCurrenciesAvailable()) {
            plugin.getStorage().saveCurrencies(plugin.getCurrencyDataLoader().getCurrencies());
        }
    }

    public void loadUsers() throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>(null);

        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                handleUser(player);
            } catch (Exception e) {
                exception.set(e);
            }
        });

        if(exception.get() != null) {
            throw exception.get();
        }
    }

    public void handleUser(Player player) throws Exception {
        CurrencyCacheStorage storage = plugin.getStorage();

        CurrencyDataLoader loader = plugin.getCurrencyDataLoader();

        User user;

        if(!storage.isUserAvailable(player.getUniqueId())) {
            user = loader.getUser(player);

            if(user == null) {
                user = loader.createUser(player);
            }

            storage.saveUser(user);
        } else {
            user = storage.getUser(player.getUniqueId());

            String realDisplayName = loader.setLastDisplayName(player);

            if(!user.getDisplayName().equals(realDisplayName)) {
                storage.saveUserDisplayName(user.getUniqueId(), realDisplayName);
            }
        }
    }

    public void removeUser(Player player) {
        CurrencyCacheStorage storage = plugin.getStorage();

        if(storage.isUserAvailable(player.getUniqueId())) {
            storage.deleteUser(player.getUniqueId());
        }
    }


}
