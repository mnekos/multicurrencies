package pl.mnekos.multicurrencies.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import pl.mnekos.multicurrencies.MultiCurrenciesPlugin;
import pl.mnekos.multicurrencies.api.IMultiCurrenciesAPI;
import pl.mnekos.multicurrencies.api.OperationResponse;
import pl.mnekos.multicurrencies.api.ResponseCurrency;
import pl.mnekos.multicurrencies.api.ResponseUser;

import java.util.List;

public class MultiCurrenciesEconomy implements Economy {

    private long currencyId = -1L;
    private IMultiCurrenciesAPI API = MultiCurrenciesPlugin.getAPI();

    public void setCurrencyId(long id) {
        this.currencyId = id;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private ResponseCurrency currency() {
        return API.getCurrency(currencyId);
    }

    @Override
    public String getName() {
        return "MultiCurrencies-" + currency().getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double value) {
        long factor = 100L;
        value = value * factor;
        long tmp = Math.round(value);
        return String.valueOf((double) tmp / factor);
    }

    @Override
    public String currencyNamePlural() {
        return currency().getDisplayName();
    }

    @Override
    public String currencyNameSingular() {
        return currency().getDisplayName();
    }

    @Override
    public boolean hasAccount(String player) {
        return API.getUser(player) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return API.getUser(offlinePlayer.getUniqueId()) != null;
    }

    @Override
    public boolean hasAccount(String player, String world) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String world) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String player) {
        return API.get(player, currencyId);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return API.get(player.getUniqueId(), currencyId);
    }

    @Override
    public double getBalance(String player, String world) {
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String player, double value) {
        return API.has(player, currencyId, value);
    }

    @Override
    public boolean has(OfflinePlayer player, double value) {
        return API.has(player.getUniqueId(), currencyId, value);
    }

    @Override
    public boolean has(String player, String world, double value) {
        return API.has(player, currencyId, value);
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double value) {
        return API.has(player.getUniqueId(), currencyId, value);
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, double value) {
        OperationResponse operationResponse = API.remove(player, currencyId, value, "VAULT_HOOK_PLUGIN_CHANGE");;

        ResponseUser user = API.getUser(player);

        if(user == null) {
            error(0, 0, "Cannot find user.");
        }

        double newAmount = API.get(user, currencyId);

        EconomyResponse response = new EconomyResponse(newAmount + value, newAmount, getResponseType(operationResponse), "Not used.");

        return response;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double value) {
        OperationResponse operationResponse = API.remove(player.getUniqueId(), currencyId, value, "VAULT_HOOK_PLUGIN_CHANGE");

        ResponseUser user = API.getUser(player.getUniqueId());

        if(user == null) {
            error(0, 0, "Cannot find user.");
        }

        double newAmount = API.get(user, currencyId);

        EconomyResponse response = new EconomyResponse(newAmount + value, newAmount, getResponseType(operationResponse), "Not used.");

        return response;
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, String world, double value) {
        return withdrawPlayer(player, value);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double value) {
        return withdrawPlayer(player, value);
    }

    @Override
    public EconomyResponse depositPlayer(String player, double value) {
        OperationResponse operationResponse = API.add(player, currencyId, value, "VAULT_HOOK_PLUGIN_CHANGE");;

        ResponseUser user = API.getUser(player);

        if(user == null) {
            error(0, 0, "Cannot find user.");
        }

        double newAmount = API.get(user, currencyId);

        EconomyResponse response = new EconomyResponse(newAmount - value, newAmount, getResponseType(operationResponse), "Not used.");

        return response;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double value) {
        OperationResponse operationResponse = API.add(player.getUniqueId(), currencyId, value, "VAULT_HOOK_PLUGIN_CHANGE");;

        ResponseUser user = API.getUser(player.getUniqueId());

        if(user == null) {
            error(0, 0, "Cannot find user.");
        }

        double newAmount = API.get(user, currencyId);

        EconomyResponse response = new EconomyResponse(newAmount - value, newAmount, getResponseType(operationResponse), "Not used.");

        return response;
    }

    @Override
    public EconomyResponse depositPlayer(String player, String world, double value) {
        return depositPlayer(player, value);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double value) {
        return depositPlayer(player, value);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    private EconomyResponse error(long before, long after, String message) {
        return new EconomyResponse(before, after, EconomyResponse.ResponseType.FAILURE, message);
    }

    private EconomyResponse.ResponseType getResponseType(OperationResponse response) {
        if(response == OperationResponse.SUCCESS) {
            return EconomyResponse.ResponseType.SUCCESS;
        } else if(response == OperationResponse.FAILURE) {
            return EconomyResponse.ResponseType.FAILURE;
        } else {
            return EconomyResponse.ResponseType.NOT_IMPLEMENTED;
        }
    }
}
