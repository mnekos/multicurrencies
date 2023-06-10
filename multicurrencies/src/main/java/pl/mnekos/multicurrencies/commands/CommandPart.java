package pl.mnekos.multicurrencies.commands;

import org.bukkit.permissions.Permissible;

import java.util.List;

public class CommandPart {

    private String name;
    private String command;
    private String args;
    private String description;
    private List<String> parentPermissions;
    private String permission;

    public CommandPart(String name, String command, String args, String description, List<String> parentPermissions, String permission) {
        this.name = name;
        this.command = command;
        this.args = args;
        this.description = description;
        this.parentPermissions = parentPermissions;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getParentPermissions() {
        return parentPermissions;
    }

    public String getPermission() {
        return permission;
    }

    public boolean checkPermissions(Permissible permissible) {
        for(String perm : parentPermissions) {
            if(permissible.hasPermission(perm)) {
                return true;
            }
        }

        return permissible.hasPermission(permission);
    }
}
