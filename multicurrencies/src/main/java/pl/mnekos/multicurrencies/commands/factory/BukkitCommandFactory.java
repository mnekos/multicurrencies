package pl.mnekos.multicurrencies.commands.factory;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

public class BukkitCommandFactory {

    public static PluginCommand newCommand(@Nonnull Plugin plugin, @Nonnull ConfigurationSection config, @Nullable String prefix, @Nullable CommandExecutor executor, @Nullable TabCompleter tabCompleter) {
        String name = config.getString("name");
        List<String> aliases = config.getStringList("aliases");
        String description = config.getString("description");
        String permission = config.getString("permission");
        String permissionMessage = ChatColor.translateAlternateColorCodes('&', prefix != null ? prefix : "" + config.getString("permissionMessage"));
        String usage = ChatColor.translateAlternateColorCodes('&', prefix != null ? prefix : "" + config.getString("usage"));
        return newCommand(name, plugin, aliases, description, permission, permissionMessage, usage, executor, tabCompleter);
    }

    public static PluginCommand newCommand(@Nonnull String name, @Nonnull Plugin plugin, @Nullable List<String> aliases, @Nullable String description, @Nullable String permission, @Nullable String permissionMessage, @Nullable String usage, @Nullable CommandExecutor executor, @Nullable TabCompleter tabCompleter) {
        Validate.notNull(name, "Command name cannot be null!");
        Validate.notNull(plugin, "Plugin cannot be null!");

        PluginCommand command = getCommand(name, plugin);

        if(aliases != null) {
            command.setAliases(aliases);
        }

        if(description != null) {
            command.setDescription(description);
        }

        if(permission != null) {
            command.setPermission(permission);
        }

        if(permissionMessage != null) {
            command.setPermissionMessage(permissionMessage);
        }

        if(usage != null) {
            command.setUsage(usage);
        }

        if(executor != null) {
            command.setExecutor(executor);
        }

        if(tabCompleter != null) {
            command.setTabCompleter(tabCompleter);
        }

        getCommandMap().register(plugin.getDescription().getName(), command);
        return command;
    }

    public static PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);

            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot create new instance of PluginCommand.");
        }

        return command;
    }

    public static CommandMap getCommandMap() {
        CommandMap commandMap = null;

        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");

                f.setAccessible(true);

                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot get CommandMap field.");
        }

        return commandMap;
    }
}
