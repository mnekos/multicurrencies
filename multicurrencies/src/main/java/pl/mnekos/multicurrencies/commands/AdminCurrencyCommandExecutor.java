package pl.mnekos.multicurrencies.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.*;
import pl.mnekos.multicurrencies.commands.factory.BukkitCommandFactory;
import pl.mnekos.multicurrencies.commands.help.HelpMessageSender;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminCurrencyCommandExecutor extends MultiCurrenciesCommandExecutor implements Asynchronized {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private ConfigurationSection section;
    private Map<String, String> messages;

    private String parentPermission;

    private static final String[] COMMAND_PARTS = new String[] {
            "currencies",
            "currency-info",
            "currency-create",
            "currency-delete",
            "currency-setfreeflow",
            "currency-setname",
            "currency-setdisplayname",
            "currency-setcommandname",
            "user-info",
            "user-add",
            "user-remove",
            "user-set",
            "user-clear",
            "user-reset",
            "loaddisabledcurrencycommands"
    };

    private List<CommandPart> commandParts = new ArrayList<>();


    public AdminCurrencyCommandExecutor(MultiCurrenciesPlugin plugin) {
        super(plugin);
        section = plugin.getConfigurationDataLoader().getConfigurationSection("admin-currencies-command");

        this.messages = plugin.getMessages();

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
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            HelpMessageSender.send(true, sender, commandParts, messages);
            return true;
        }

        IMultiCurrenciesAPI API = MultiCurrenciesPlugin.getAPI();

        if(args[0].equalsIgnoreCase("currencies")) {
            if(!hasAccess(sender, "currencies")) {
                return true;
            }

            if(args.length != 1) {
                sendHelpMessage(sender, "currencies");
                return true;
            }

            Collection<ResponseCurrency> currencies = API.getCurrencies();

            if(currencies.size() == 0) {
                sender.sendMessage(messages.get("currency-list") + messages.get("currency-list-empty"));
            } else {
                sender.sendMessage(messages.get("currency-list"));
                currencies.forEach(currency -> sender.sendMessage(messages.get("currency-list-entry").replace("%currency-name%", currency.getName()).replace("%currency-displayname%", currency.getDisplayName())));
            }

            return true;
        }

        else if(args[0].equalsIgnoreCase("currency")) {
            if(args.length < 3) {
                HelpMessageSender.send(true, sender, commandParts.stream().filter(part -> part.getName().startsWith("currency")).collect(Collectors.toList()), messages);
                return true;
            }

            ResponseCurrency currency = API.getCurrency(args[1]);

            if(args[2].equalsIgnoreCase("create")) {
                if(!hasAccess(sender, "currency-create")) {
                    return true;
                }
                if(args.length != 3) {
                    sendHelpMessage(sender, "currency-create");
                    return true;
                } else {
                    if(currency != null) {
                        sender.sendMessage(messages.get("currency-already-exists"));
                        return true;
                    }

                    if(args[1].length() > 32) {
                        sender.sendMessage(messages.get("currency-set-name-too-long"));
                        return true;
                    }

                    API.createCurrency(args[1], args[1], false, args[1]);
                    sender.sendMessage(messages.get("currency-successfully-created"));
                }
                return true;
            }

            if(currency == null) {
                sender.sendMessage(messages.get("not-found-currency"));
                return true;
            }

            if(args[2].equalsIgnoreCase("info")) {
                if(!hasAccess(sender, "currency-info")) {
                    return true;
                }
                if(args.length != 3) {
                    sendHelpMessage(sender, "currency-info");
                    return true;
                }
                sender.sendMessage(messages.get("currency-info-name").replace("%name%", currency.getName()));
                sender.sendMessage(messages.get("currency-info-id").replace("%id%", String.valueOf(currency.getId())));
                sender.sendMessage(messages.get("currency-info-free-flow").replace("%free-flow%", String.valueOf(currency.isFreeFlow())));
                sender.sendMessage(messages.get("currency-info-command-name").replace("%command-name%", currency.getCommandName()));
                sender.sendMessage(messages.get("currency-info-display-name").replace("%display-name%", currency.getDisplayName()));
                return true;
            }

            if(args[2].equalsIgnoreCase("delete")) {
                if(!hasAccess(sender, "currency-delete")) {
                    return true;
                }

                if(args.length != 3) {
                    sendHelpMessage(sender, "currency-info");
                    return true;
                }

                API.deleteCurrency(currency.getId());
                sender.sendMessage(messages.get("currency-successfully-deleted"));
                return true;
            }

            if(args[2].equalsIgnoreCase("setfreeflow")) {
                if(!hasAccess(sender, "currency-setfreeflow")) {
                    return true;
                }

                if(args.length != 4) {
                    sendHelpMessage(sender, "currency-setfreeflow");
                    return true;
                }

                boolean freeFlow = false;

                if(args[3].equalsIgnoreCase("true")) {
                    freeFlow = true;
                }
                else if(!args[3].equalsIgnoreCase("false")) {
                    sender.sendMessage(messages.get("true-false-invalid-value"));
                    return true;
                }

                API.setFreeFlow(currency.getId(), freeFlow);
                sender.sendMessage(messages.get("currency-successfully-set-free-flow"));
                return true;
            }

            if(args[2].equalsIgnoreCase("setname")) {
                if(!hasAccess(sender, "currency-setname")) {
                    return true;
                }

                if(args.length != 4) {
                    sendHelpMessage(sender, "currency-setname");
                    return true;
                }

                if(args[3].length() > 32) {
                    sender.sendMessage(messages.get("currency-set-name-too-long"));
                    return true;
                }

                API.setCurrencyName(currency.getId(), args[3]);
                sender.sendMessage(messages.get("currency-successfully-set-name"));
                return true;
            }

            if(args[2].equalsIgnoreCase("setdisplayname")) {
                if(!hasAccess(sender, "currency-setdisplayname")) {
                    return true;
                }

                if(args.length <= 3) {
                    sendHelpMessage(sender, "currency-setdisplayname");
                    return true;
                }

                StringBuilder displayNameBuilder = new StringBuilder();

                for(int i = 3; i < args.length; i++) {
                    displayNameBuilder.append(args[i]).append(" ");
                }

                String newDisplayName = displayNameBuilder.substring(0, displayNameBuilder.length() - 1);

                if(newDisplayName.length() > 64) {
                    sender.sendMessage(messages.get("currency-set-name-too-long"));
                    return true;
                }

                API.setCurrencyDisplayName(currency.getId(), ChatColor.translateAlternateColorCodes('&', newDisplayName));
                sender.sendMessage(messages.get("currency-successfully-set-display-name"));
                return true;
            }

            if(args[2].equalsIgnoreCase("setcommandname")) {
                if(!hasAccess(sender, "currency-setcommandname")) {
                    return true;
                }

                if(args.length != 4) {
                    sendHelpMessage(sender, "currency-setcommandname");
                    return true;
                }

                if(args[3].length() > 32) {
                    sender.sendMessage(messages.get("currency-set-name-too-long"));
                    return true;
                }

                API.setCommandName(currency.getId(), args[3]);
                sender.sendMessage(messages.get("currency-successfully-set-command-name"));
                return true;
            }

            HelpMessageSender.send(true, sender, commandParts.stream().filter(part -> part.getName().startsWith("currency")).collect(Collectors.toList()), messages);
            return true;
        }

        else if(args[0].equalsIgnoreCase("user")) {
            if(args.length < 3) {
                HelpMessageSender.send(true, sender, commandParts.stream().filter(part -> part.getName().startsWith("user")).collect(Collectors.toList()), messages);
                return true;
            }

            ResponseUser user = API.getUser(args[1]);

            if(user == null) {
                sender.sendMessage(messages.get("not-found-user"));
                return true;
            }

            if(args[2].equalsIgnoreCase("info")) {
                if(!hasAccess(sender, "user-info")) {
                    return true;
                }
                if(args.length != 3) {
                    sendHelpMessage(sender, "user-info");
                    return true;
                }
                sender.sendMessage(messages.get("user-info-name").replace("%name%", user.getDisplayName()));
                sender.sendMessage(messages.get("user-info-id").replace("%id%", String.valueOf(user.getNumericId())));
                sender.sendMessage(messages.get("user-info-uuid").replace("%uuid%", user.getUniqueId().toString()));
                sender.sendMessage(messages.get("user-info-currency-list") + (user.getCurrencyValues().size() == 0 ? messages.get("user-info-currency-list-empty") : ""));
                for(Map.Entry<ResponseCurrency, ResponseAmount> entry : user.getCurrencyValues().entrySet()) {
                    if(entry.getKey().getName() == null) continue;
                    sender.sendMessage(messages.get("user-info-currency-list-entry").replace("%currency%", entry.getKey().getName()).replace("%amount%", String.valueOf(entry.getValue().get())));
                }
                return true;
            }

            if(args[2].equalsIgnoreCase("add")) {
                if(!hasAccess(sender, "user-add")) {
                    return true;
                }
                if(args.length != 5) {
                    sendHelpMessage(sender, "user-add");
                    return true;
                }

                ResponseCurrency currency = API.getCurrency(args[3]);

                if(currency == null) {
                    sender.sendMessage(messages.get("not-found-currency"));
                    return true;
                }

                Double value;

                try {
                    value = Double.parseDouble(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(value <= 0 || value.isInfinite()) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(API.add(user.getNumericId(), currency.getId(), value, "ADMIN_COMMAND-" + sender.getName()) == OperationResponse.SUCCESS) {
                    sender.sendMessage(messages.get("user-added-amount").replace("%currency%", currency.getName()).replace("%amount%", value.toString()).replace("%player%", user.getDisplayName()));
                    return true;
                } else {
                    sender.sendMessage(messages.get("user-operation-failed"));
                    return true;
                }
            }

            else if(args[2].equalsIgnoreCase("remove")) {
                if(!hasAccess(sender, "user-remove")) {
                    return true;
                }
                if(args.length != 5) {
                    sendHelpMessage(sender, "user-remove");
                    return true;
                }

                ResponseCurrency currency = API.getCurrency(args[3]);

                if(currency == null) {
                    sender.sendMessage(messages.get("not-found-currency"));
                    return true;
                }

                Double value;

                try {
                    value = Double.parseDouble(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(value <= 0 || value.isInfinite()) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(API.remove(user.getNumericId(), currency.getId(), value, "ADMIN_COMMAND-" + sender.getName()) == OperationResponse.SUCCESS) {
                    sender.sendMessage(messages.get("user-removed-amount").replace("%currency%", currency.getName()).replace("%amount%", value.toString()).replace("%player%", user.getDisplayName()));
                    return true;
                } else {
                    sender.sendMessage(messages.get("user-operation-failed"));
                    return true;
                }
            }

            else if(args[2].equalsIgnoreCase("set")) {
                if(!hasAccess(sender, "user-set")) {
                    return true;
                }
                if(args.length != 5) {
                    sendHelpMessage(sender, "user-set");
                    return true;
                }

                ResponseCurrency currency = API.getCurrency(args[3]);

                if(currency == null) {
                    sender.sendMessage(messages.get("not-found-currency"));
                    return true;
                }

                Double value;

                try {
                    value = Double.parseDouble(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(value < 0 || value.isInfinite()) {
                    sender.sendMessage(messages.get("not-valid-value"));
                    return true;
                }

                if(API.set(user.getNumericId(), currency.getId(), value, "ADMIN_COMMAND-" + sender.getName()) == OperationResponse.SUCCESS) {
                    sender.sendMessage(messages.get("user-set-amount").replace("%currency%", currency.getName()).replace("%amount%", value.toString()).replace("%player%", user.getDisplayName()));
                    return true;
                } else {
                    sender.sendMessage(messages.get("user-operation-failed"));
                    return true;
                }
            }

            else if(args[2].equalsIgnoreCase("clear")) {
                if(!hasAccess(sender, "user-clear")) {
                    return true;
                }
                if(args.length != 4) {
                    sendHelpMessage(sender, "user-clear");
                    return true;
                }

                ResponseCurrency currency = API.getCurrency(args[3]);

                if(currency == null) {
                    sender.sendMessage(messages.get("not-found-currency"));
                    return true;
                }

                if(API.set(user.getNumericId(), currency.getId(), 0, "ADMIN_COMMAND-" + sender.getName()) == OperationResponse.SUCCESS) {
                    sender.sendMessage(messages.get("user-cleared-amount").replace("%currency%", currency.getName()).replace("%player%", user.getDisplayName()));
                    return true;
                } else {
                    sender.sendMessage(messages.get("user-operation-failed"));
                    return true;
                }
            }

            else if(args[2].equalsIgnoreCase("reset")) {
                if(!hasAccess(sender, "user-reset")) {
                    return true;
                }
                if(args.length != 3) {
                    sendHelpMessage(sender, "user-reset");
                    return true;
                }

                API.clearUserData(user.getNumericId(), "ADMIN_COMMAND-" + sender.getName());
                sender.sendMessage(messages.get("user-reset").replace("%player%", user.getDisplayName()));
                return true;
            }
        }

        else if(args[0].equalsIgnoreCase("loaddisabledcurrencycommands")) {
            if(!hasAccess(sender, "loaddisabledcurrencycommands")) {
                return true;
            }
            if(args.length != 1) {
                sendHelpMessage(sender, "loaddisabledcurrencycommands");
                return true;
            }

            plugin.getConfigurationDataLoader().reloadConfig();

            plugin.getStorage().setBlackListCurrencies(plugin.getConfigurationDataLoader().getDisabledCurrencyCommandsFor());

            sender.sendMessage(messages.get("loaded-disabled-currency-commands"));
            return true;
        }

        else if(args[0].equalsIgnoreCase("loadvaulthook")) {
            if(!hasAccess(sender, "loadvaulthook")) {
                return true;
            }
            if(args.length != 1) {
                sendHelpMessage(sender, "loadvaulthook");
                return true;
            }

            plugin.registerOrUpdateEconomy();
            sender.sendMessage(messages.get("loaded-vault-currency"));
            return true;
        }

        sender.sendMessage(messages.get("check-syntax-error").replace("<command>", label));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            String text = args[0];

            return TabCompleteUtils.getComplements(text, Stream.of("user", "currency", "currencies", "loaddisabledcurrencycommands"), true);
        }

        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("user")) {
                String text = args[2];

                return TabCompleteUtils.getComplements(text, Stream.of("info", "add", "remove", "set", "clear", "reset"), true);

            }

            if(args[0].equalsIgnoreCase("currency")) {
                String text = args[2];

                return TabCompleteUtils.getComplements(text, Stream.of("info", "create", "delete", "setfreeflow", "setname", "setdisplayname", "setcommandname"), true);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public PluginCommand createCommand() {
        section.set("permissionMessage", ChatColor.RED + plugin.getName() + ChatColor.GRAY + " v." + plugin.getDescription().getVersion() + " by " + ChatColor.RED + "mnekos" + ChatColor.DARK_GRAY + "#4359");

        Set<String> aliases = new HashSet<>(section.getStringList("aliases"));

        aliases.add("multicurrencies");

        section.set("aliases", aliases);

        return BukkitCommandFactory.newCommand(
                plugin,
                section,
                null,
                this,
                this);
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
