package pl.mnekos.multicurrencies.api.factory;

import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.ResponseUser;
import pl.mnekos.multicurrencies.user.User;

import java.util.stream.Collectors;

public class ResponseUserFactory {

    public static ResponseUser newResponseUser(User user) {
        return user != null ?
                new ResponseUser(
                        user.getNumericId(),
                        user.getUniqueId(),
                        user.getDisplayName(),
                        user.getCurrencyValues()
                                .entrySet()
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                entry -> MultiCurrenciesPlugin.getAPI().getCurrency(entry.getKey()),
                                                entry -> entry.getValue()
                                        )
                                )
                ) :
                null;
    }

}
