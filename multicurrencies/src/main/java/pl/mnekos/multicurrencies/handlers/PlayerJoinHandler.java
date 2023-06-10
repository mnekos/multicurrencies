package pl.mnekos.multicurrencies.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;

import java.util.logging.Level;

public class PlayerJoinHandler extends Handler {

    public PlayerJoinHandler(MultiCurrenciesPlugin plugin, boolean register) {
        super(plugin, register);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Handler.EXECUTOR_SERVICE.submit(() -> {
            try {
                plugin.getManager().handleUser(event.getPlayer());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Cannot handle user " + event.getPlayer().getName() + ".", e);
            }
        });
    }
}