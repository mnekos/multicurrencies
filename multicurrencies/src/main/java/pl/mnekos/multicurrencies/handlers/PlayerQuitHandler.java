package pl.mnekos.multicurrencies.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;

public class PlayerQuitHandler extends Handler {

    public PlayerQuitHandler(MultiCurrenciesPlugin plugin, boolean register) {
        super(plugin, register);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Handler.EXECUTOR_SERVICE.submit(() -> plugin.getManager().removeUser(event.getPlayer()));
    }
}
