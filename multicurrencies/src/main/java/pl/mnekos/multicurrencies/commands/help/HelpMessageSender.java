package pl.mnekos.multicurrencies.commands.help;

import org.bukkit.command.CommandSender;
import pl.mnekos.multicurrencies.commands.CommandPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpMessageSender {

    private static final String HELP_HEADER_KEY = "help-header";
    private static final String HELP_FOOTER_KEY = "help-footer";
    private static final String HELP_SYNTAX_KEY = "help-syntax";

    public static void send(boolean headerfooter, CommandSender sender, List<CommandPart> commandParts, Map<String, String> messages) {
        String header = messages.get(HELP_HEADER_KEY);
        String footer = messages.get(HELP_FOOTER_KEY);
        String syntax = messages.get(HELP_SYNTAX_KEY);

        List<String> entries = new ArrayList<>();

        for(CommandPart entry : commandParts) {
            if(entry.checkPermissions(sender)) {
                entries.add(
                        syntax.replace("<command>", entry.getCommand()).replace("<args>", entry.getArgs()).replace("<description>", entry.getDescription())
                );
            }
        }

        if(entries.size() == 0) {
            sender.sendMessage(messages.get("non-permission"));
            return;
        }

        if(headerfooter) {
            sender.sendMessage(header);
        }

        entries.forEach(sender::sendMessage);

        if(headerfooter) {
            sender.sendMessage(footer);
        }
    }
}
