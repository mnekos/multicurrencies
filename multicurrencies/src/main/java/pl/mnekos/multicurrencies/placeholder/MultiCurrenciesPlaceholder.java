package pl.mnekos.multicurrencies.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.IMultiCurrenciesAPI;
import pl.mnekos.multicurrencies.api.ResponseAmount;
import pl.mnekos.multicurrencies.api.ResponseCurrency;
import pl.mnekos.multicurrencies.api.ResponseUser;

import java.util.Map;

public class MultiCurrenciesPlaceholder extends PlaceholderExpansion {

    private IMultiCurrenciesAPI API = MultiCurrenciesPlugin.getAPI();

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "multicurrencies";
    }

    @Override
    public String getAuthor() {
        return "mnekos";
    }

    @Override
    public String getVersion() {
        return API.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier){
        String[] args = identifier.split("_");

        if(args.length < 1) {
            return null;
        }

        ResponseCurrency currency = API.getCurrency(args[0]);

        if(currency == null) {
            try {
                currency = API.getCurrency(Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if(currency == null) {
            return null;
        }

        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("amount")) {
                ResponseUser user = API.getUser(player.getUniqueId());

                if(user == null) {
                    return null;
                }

                final ResponseCurrency finalCurrency = currency;

                Map.Entry<ResponseCurrency, ResponseAmount> entry = user.getCurrencyValues().entrySet().stream().filter(e -> e.getKey().getId() == finalCurrency.getId()).findAny().orElse(null);

                if(entry == null) {
                    return "0.00";
                }

                return String.valueOf(entry.getValue().get());
            }

            if(args[1].equalsIgnoreCase("displayname")) {
                return currency.getDisplayName();
            }

            if(args[1].equalsIgnoreCase("name")) {
                return currency.getName();
            }

            if(args[1].equalsIgnoreCase("commandname")) {
                return currency.getCommandName();
            }

            if(args[1].equalsIgnoreCase("freeflow")) {
                return String.valueOf(currency.isFreeFlow());
            }
        }

        if(args.length == 3) {
            if(args[1].equalsIgnoreCase("amount")) {
                ResponseUser user = API.getUser(args[2]);

                if(user == null) {
                    return null;
                }

                final ResponseCurrency finalCurrency = currency;

                Map.Entry<ResponseCurrency, ResponseAmount> entry = user.getCurrencyValues().entrySet().stream().filter(e -> e.getKey().getId() == finalCurrency.getId()).findAny().orElse(null);

                if(entry == null) {
                    return "0.00";
                }

                return String.valueOf(entry.getValue().get());
            }
        }
        return null;
    }
}
