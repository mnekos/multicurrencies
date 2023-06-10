package pl.mnekos.multicurrencies.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.*;
import pl.mnekos.multicurrencies.commands.factory.BukkitCommandFactory;
import pl.mnekos.multicurrencies.commands.help.HelpMessageSender;
import pl.mnekos.multicurrencies.currency.Currency;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CurrencyCommandExecutor extends MultiCurrenciesCommandExecutor implements Asynchronized {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private long currencyId;

    private ConfigurationSection section;
    private Map<String, String> messages;

    private String parentPermission;

    private static final String[] COMMAND_PARTS = new String[] {
            "pay",
            "info"
    };

    private List<CommandPart> commandParts = new ArrayList<>();

    public CurrencyCommandExecutor(MultiCurrenciesPlugin plugin, long currencyId) {
        super(plugin);
        this.section = plugin.getConfigurationDataLoader().getConfigurationSection("currency-command");

        this.messages = plugin.getMessages();
        this.currencyId = currencyId;

        this.parentPermission = section.getString("parent-permission");

        ConfigurationSection section0 = section.getConfigurationSection("parts");

        setCommand();

        for(String part : COMMAND_PARTS) {
            ConfigurationSection section1 = section0.getConfigurationSection(part);
            commandParts.add(
                    new CommandPart(
                            part,
                            command.getName(),
                            ChatColor.translateAlternateColorCodes('&', section1.getString("args-syntax-message")),
                            ChatColor.translateAlternateColorCodes('&', section1.getString("description")),
                            Arrays.asList(parentPermission),
                            section1.getString("permission")
                    )
            );
        }
    }

    @Override
    public boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.get("executable-only-as-player"));
            return true;
        }

        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            HelpMessageSender.send(true, sender, getCurrency().isFreeFlow() ? commandParts : commandParts.stream().filter(part -> !part.getName().equals("pay")).collect(Collectors.toList()), messages);
            return true;
        }

        IMultiCurrenciesAPI API = MultiCurrenciesPlugin.getAPI();

        if(args[0].equalsIgnoreCase("info")) {
            if(!hasAccess(sender, "info")) {
                return true;
            }

            if(args.length != 1) {
                sendHelpMessage(sender, "info");
                return true;
            }

            Map.Entry<ResponseCurrency, ResponseAmount> amount = API.getLoadedUser((Player) sender).getCurrencyValues().entrySet().stream().filter(entry -> entry.getKey().getId() == currencyId).findAny().orElse(null);

            if(amount == null) {
                sender.sendMessage(messages.get("user-account-balance")
                        .replace("%currency%", getCurrency().getDisplayName())
                        .replace("%amount%", String.valueOf(0))
                );
            } else {
                sender.sendMessage(messages.get("user-account-balance")
                        .replace("%currency%", amount.getKey().getDisplayName())
                        .replace("%amount%", String.valueOf(amount.getValue().get()))
                );
            }

            return true;
        }

        Currency currency = getCurrency();

        if(args[0].equalsIgnoreCase("pay") && currency.isFreeFlow()) {
            if(!hasAccess(sender, "pay")) {
                return true;
            }

            if(args.length != 3) {
                sendHelpMessage(sender, "pay");
                return true;
            }

            ResponseUser userFrom = API.getUser((Player) sender);
            ResponseUser userTo = API.getUser(args[1]);

            if(userFrom.getUniqueId().equals(userTo.getUniqueId())) {
                sender.sendMessage(messages.get("user-cannot-pay-yourself"));
                return true;
            }

            if(userTo == null) {
                sender.sendMessage(messages.get("not-found-user"));
                return true;
            }

            double value;

            try {
                value = Double.parseDouble(args[2]);

                if(value <= 0) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }
            } catch (Exception e) {
                sender.sendMessage(messages.get("not-valid-value"));
                return true;
            }

            long factor = 100L;
            value = value * factor;
            long tmp = Math.round(value);
            value = (double) tmp / factor;

            if(!API.has(userFrom.getNumericId(), currencyId, value)) {
                sender.sendMessage(messages.get("not-enough-currency-amount"));
                return true;
            }

            if(API.pay(userFrom, userTo, currencyId, value) == OperationResponse.SUCCESS) {
                sender.sendMessage(messages.get("user-pay")
                        .replace("%currency%", currency.getDisplayName())
                        .replace("%amount%", String.valueOf(value))
                        .replace("%player%", userTo.getDisplayName())
                );
            } else {
                sender.sendMessage(messages.get("user-operation-failed"));
            }
            return true;
        }

        sender.sendMessage(messages.get("check-syntax-error").replace("<command>", label));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            String text = args[0];

            return TabCompleteUtils.getComplements(text, Arrays.stream(COMMAND_PARTS), true);
        }

        return Collections.emptyList();
    }

    public long getCurrencyId() {
        return currencyId;
    }

    private Currency getCurrency() {
        return plugin.getStorage().getCurrency(currencyId);
    }

    @Override
    public PluginCommand createCommand() {
        Currency currency = getCurrency();

        Function<String, String> formatter = string ->
            ChatColor.translateAlternateColorCodes('&', string)
                    .replace("<command>", currency.getCommandName())
                    .replace("<currency-display-name>", currency.getDisplayName())
                    .replace("<currency-name>", currency.getName());

        return BukkitCommandFactory.newCommand(
                formatter.apply(section.getString("name")),
                plugin,
                null,
                formatter.apply(section.getString("description")),
                formatter.apply(section.getString("permission")),
                ChatColor.RED + plugin.getName() + ChatColor.GRAY + " v." + plugin.getDescription().getVersion() + " by " + ChatColor.RED + "mnekos" + ChatColor.DARK_GRAY + "#4359",
                formatter.apply(section.getString("usage")),
                this,
                this
        );
    }

    @Override
    public void shutdownExecutorService() {
        executorService.shutdown();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    private boolean hasAccess(CommandSender sender, String part) {
        CommandPart partObj = commandParts.stream().filter(commandPart -> commandPart.getName().equalsIgnoreCase(part)).collect(Collectors.toList()).stream().findAny().orElse(null);

        if(partObj == null) {
            throw new NullPointerException("Cannot find CommandPart \"" + part + "\".");
        }

        boolean test = partObj.checkPermissions(sender);

        if(!test) {
            sender.sendMessage(messages.get("non-permission"));
        }

        return test;
    }

    private void sendHelpMessage(CommandSender sender, String part) {
        HelpMessageSender.send(false, sender, commandParts.stream().filter(commandPart -> commandPart.getName().equalsIgnoreCase(part)).collect(Collectors.toList()), messages);
    }
}
