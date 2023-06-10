package pl.mnekos.multicurrencies.commands;

import org.bukkit.command.*;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.commands.factory.BukkitCommandFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class MultiCurrenciesCommandExecutor implements CommandExecutor, TabCompleter {

    protected PluginCommand command;
    protected MultiCurrenciesPlugin plugin;
    protected CommandMap map;

    public MultiCurrenciesCommandExecutor(MultiCurrenciesPlugin plugin) {
        this.plugin = plugin;
        map = BukkitCommandFactory.getCommandMap();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(this instanceof Asynchronized) {
            Asynchronized async = (Asynchronized) this;
            async.getExecutorService().submit(() -> {
                try {
                    executeCommand(sender, command, label, args);
                } catch(Exception exception) {
                    plugin.getLogger().log(Level.SEVERE, "An error occured while executing command " + command.getName() + ".", exception);
                }
            });
            return true;
        }
        executeCommand(sender, command, label, args);
        return true;
    }

    public abstract boolean executeCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);

    public void setCommand() {
        command = createCommand();
    }

    public abstract PluginCommand createCommand();

    public PluginCommand getCommand() {
        return command;
    }

    public void unregister() throws NoSuchFieldException, IllegalAccessException {
        command.unregister(map);

        Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");

        knownCommands.setAccessible(true);

        Map<String, Command> value = (Map<String, Command>) knownCommands.get(map);

        for(Map.Entry<String, Command> entry : new HashMap<>(value).entrySet()) {
            if(entry.getValue().equals(command)) {
                value.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}