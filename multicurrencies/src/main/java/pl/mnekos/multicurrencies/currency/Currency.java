package pl.mnekos.multicurrencies.currency;

public class Currency {

    private long id;
    private String name;
    private String displayName;
    private boolean freeFlow;
    private String commandName;

    public Currency(long id, String name, String displayName, boolean freeFlow, String commandName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.freeFlow = freeFlow;
        this.commandName = commandName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isFreeFlow() {
        return freeFlow;
    }

    public void setFreeFlow(boolean freeFlow) {
        this.freeFlow = freeFlow;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", freeFlow=" + freeFlow +
                ", commandName='" + commandName + '\'' +
                '}';
    }
}
