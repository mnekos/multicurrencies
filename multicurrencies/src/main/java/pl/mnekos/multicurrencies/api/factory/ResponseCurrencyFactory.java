package pl.mnekos.multicurrencies.api.factory;

import pl.mnekos.multicurrencies.api.ResponseCurrency;
import pl.mnekos.multicurrencies.currency.Currency;

public class ResponseCurrencyFactory {

    public static ResponseCurrency newResponseCurrency(Currency currency) {
        return currency != null ?
                new ResponseCurrency(
                        currency.getId(),
                        currency.getName(),
                        currency.getDisplayName(),
                        currency.isFreeFlow(),
                        currency.getCommandName()
                )
                : null;
    }
}
