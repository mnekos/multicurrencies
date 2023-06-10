package pl.mnekos.multicurrencies.api;

public class ResponseCurrency {

    private final long id;
    private final String name;
    private final String displayName;
    private final boolean freeFlow;
    private final String commandName;

    public ResponseCurrency(long id, String name, String displayName, boolean freeFlow, String commandName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.freeFlow = freeFlow;
        this.commandName = commandName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFreeFlow() {
        return freeFlow;
    }

    public String getCommandName() {
        return commandName;
    }

}
