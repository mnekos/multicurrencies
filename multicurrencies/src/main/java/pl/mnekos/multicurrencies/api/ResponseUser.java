package pl.mnekos.multicurrencies.api;

import pl.mnekos.multicurrencies.data.Amount;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResponseUser {

    private final long id;
    private final UUID uuid;
    private final String displayName;
    private final Map<ResponseCurrency, ResponseAmount> currencyValues;

    public ResponseUser(long id, UUID uuid, String displayName, Map<ResponseCurrency, Amount> currencyValues) {
        this.id = id;
        this.uuid = uuid;
        this.displayName = displayName;
        this.currencyValues = currencyValues.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .collect(
                        Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> new ResponseAmount(entry.getValue().get())
                        )
                );

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

    public Map<ResponseCurrency, ResponseAmount> getCurrencyValues() {
        return currencyValues;
    }
}