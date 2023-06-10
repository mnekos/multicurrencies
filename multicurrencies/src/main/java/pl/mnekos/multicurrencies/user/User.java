package pl.mnekos.multicurrencies.user;


import pl.mnekos.multicurrencies.data.Amount;

import java.util.Map;
import java.util.UUID;

public class User {

    private long id;
    private UUID uuid;
    private String displayName;
    private Map<Long, Amount> currencyValues;

    public User(long id, UUID uuid, String displayName, Map<Long, Amount> currencyValues) {
        this.id = id;
        this.uuid = uuid;
        this.displayName = displayName;
        this.currencyValues = currencyValues;
    }

    public long getNumericId() {
        return id;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<Long, Amount> getCurrencyValues() {
        return currencyValues;
    }

    public void setCurrencyValues(Map<Long, Amount> currencyValues) {
        this.currencyValues = currencyValues;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", displayName='" + displayName + '\'' +
                ", currencyValues=" + currencyValues +
                '}';
    }
}
