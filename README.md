# Multicurrencies

Multicurrencies is a Minecraft server plugin that provides the ability to create an unlimited number of currencies with various configurations for transferring them between players. Its standout feature is the synchronization of data across different servers in a network using a Redis server. You can configure currencies to be available on specific servers. This project also supports servers that utilize `Player#getDisplayName` instead of `Player#getName` in their commands. Data (including logs and transactions history) is stored in MySQL database. All the data can be returned to the specifc date, base on stored history. It causes no need for storing backups due to economy collapse (e.x. admin sent everybody 1000$).

## Features

- Create multiple currencies: With Multicurrencies, you can create and manage multiple currencies within your Minecraft server.
- Currency configuration: Each currency can be customized with its own settings, such as name, symbol, and availability on specific servers.
- Currency command: For each currency, plugin will create independent command. It's really unique feature due to lack of this option in bukkit API.
- Currency transfers: Players can transfer currencies to other players using simple commands.
- Redis synchronization: The plugin leverages a Redis server to synchronize currency data across different servers in your network, ensuring consistency and accessibility.
- Player display name support: Multicurrencies is compatible with servers that use `Player#getDisplayName` instead of `Player#getName` in their commands, ensuring seamless integration with various server setups.
- MySQL database storage: All currency and player data is securely stored in a MySQL database, providing durability and reliability.
- All messages are changable and you can translate it your language (including names of the commands)

## Installation

1. Download the Multicurrencies plugin JAR file from the official source (link to be provided).
2. Place the downloaded JAR file into the `plugins` folder of your Minecraft server.
3. Start or restart the server to enable the plugin.
4. Configure the plugin settings in the `config.yml` file located in the `plugins/Multicurrencies` directory.
5. Set up a Redis server and configure the connection details in the `config.yml` file.
6. Configure the MySQL database connection details in the `config.yml` file.
7. Start or restart the server again to apply the updated configurations.

## Usage

### List of admin commands:

```
/admincurrencies currencies - Shows the list of currencies.
/admincurrencies currency [<name>] info - Displays information about a currency.
/admincurrencies currency [<name>] create - Creates a currency.
/admincurrencies currency [<name>] delete <delete player data about currency (true/false) (default true)> - Deletes a currency.
/admincurrencies currency [<name>] setfreeflow [<status>] - Changes the currency's free flow status (whether players can transfer it between each other).
/admincurrencies currency [<name>] setname [<name>] - Changes the currency's name.
/admincurrencies currency [<name>] setdisplayname [<name>] - Changes the displayed name of the currency.
/admincurrencies currency [<name>] setcommandname [<name>] - Changes the command name for the currency.
/admincurrencies user [<name>] info - Displays information about a user.
/admincurrencies user [<name>] add [<currency>] [<amount>] - Adds an amount of currency to a user's account.
/admincurrencies user [<name>] remove [<currency>] [<amount>] - Removes an amount of currency from a user's account.
/admincurrencies user [<name>] set [<currency>] [<amount>] - Changes the amount of currency in a user's account.
/admincurrencies user [<name>] clear [<currency>] - Sets the currency value to 0 for a user's account.
/admincurrencies user [<name>] reset - Resets the amount of all currencies to 0 for a user's account.
/admincurrencies loaddisabledcurrencycommands - Reloads the list of currencies for which commands are not created.
```
  
### Here is config.yml (default translations are in Polish)
  ```
  # PlaceholderAPI placeholders:
# multicurrencies_<currency>_amount - amount of currency for player
# multicurrencies_<currency>_amount_<player> - amount of currency for other player
# multicurrencies_<currency>_displayname - display name of currency
# multicurrencies_<currency>_name - name of currency
# multicurrencies_<currency>_commandname - command name of currency
# multicurrencies_<currency>_freeflow - free flow of currency

# <currency> means currency name or id



mysql-properties:
  database:
    ip: localhost
    port: 3306
    name: "multicurrencies"
  user:
    name: "multicurrencies"
    password: "elVBRkJP56AqYV9n"
  connection-pool:
    maximum-pool-size: 5
    minimum-idle: 5
    maximum-lifetime: 1800000
    connection-timeout: 5000


currencies-cache-storage:
  # Cache storage class. You can put here your class or choose one below.
  #
  # Available classes:
  #
  # REDIS - Synchronizes currencies on multiple servers. Loads player data once on the entire server network. Redis server required. ** RECOMMENDED **
  #   pl.mnekos.multicurrencies.data.redis.RedisCurrencyCacheStorage
  #
  # RAM - The data is contained in the RAM. If you use THE SAME database for this plugin in two other servers, it will cause many synchronization bugs.
  #   pl.mnekos.multicurrencies.data.LocalCurrencyCacheStorage
  #
  #
  cache-storage: pl.mnekos.multicurrencies.data.redis.RedisCurrencyCacheStorage

  # What is redis? https://redis.io/
  redis-properties:
    ip: localhost
    port: 6379
    # if your redis server does not use auth, leave it blank.
    password: ""
    max-redis-connections: 16

# Plugin does not connect to mojang api. If you search for player by name, it will find player with name equals ignore case to given.
# name is stored in database and means player's LAST name or LAST display name if 'use-display-name' is enabled.


# ** WARNING **
# Not recommended changing the nickname to registered player's nickname. Display name MUST be unique. Normal username must be unique too.
# Because commands to manage this plugin finds player with the name ignoring case (ex. AuthMe can block users with different letter size).

use-display-name: true

# ** INFO **
# Currency's command is create automatically, when free flow is activated.

# List of currency names, for which plugin will not create command, when free flow is activated. Useful if you make many currencies with the same names on different servers.
disabled-currency-commands-for: []

# Vault hook currency name.
vault-currency: 'gold'

currency-command:
  name: <command>
  description: '&7Zarządzanie kontem dla waluty &r<currency-display-name>&7.'
  permission: 'multicurrencies.command.currency'
  usage: '&c/<command> &6help'
  parent-permission: 'multicurrencies.currency.manage'
  parts:
    pay:
      args-syntax-message: 'pay [<gracz>] [<ilość>]'
      description: 'Przelewa walutę na konto innego gracza.'
      permission: 'multicurrencies.currency.pay'
    info:
      args-syntax-message: 'info'
      description: 'Pokazuje informacje na temat twojego konta.'
      permission: 'multicurrencies.currency.info'



admin-currencies-command:
  name: admincurrencies
  aliases:
    - acurrencies
    - acurr
  description: 'Zarządzanie walutami.'
  # Allows to this command. Permissions from 'parts' doesn't work without this permission.'
  permission: 'multicurrencies.admin.command'
  usage: '&c/admincurrencies &6help'
  # This permission gives access to all parts of commands.
  # BUT HE MUST HAVE 'permission'
  parent-permission: 'multicurrencies.admin.manage'
  parts:
    currencies:
      args-syntax-message: 'currencies'
      description: 'Pokazuje listę walut.'
      permission: 'multicurrencies.admin.currencies.list'
    currency-info:
      args-syntax-message: 'currency [<nazwa>] info'
      description: 'Pokazuje informacje o walucie.'
      permission: 'multicurrencies.admin.currencies.info'
    currency-create:
      args-syntax-message: 'currency [<nazwa>] create'
      description: 'Tworzy walutę.'
      permission: 'multicurrencies.admin.currencies.create'
    currency-delete:
      args-syntax-message: 'currency [<nazwa>] delete <usuwać dane graczy o walucie (true/false) (domyślnie true)>'
      description: 'Usuwa walutę.'
      permission: 'multicurrencies.admin.currencies.delete'
    currency-setfreeflow:
      args-syntax-message: 'currency [<nazwa>] setfreeflow [<status>]'
      description: 'Zmienia przepływalność waluty (czy gracze mogą je przesyłać między sobą).'
      permission: 'multicurrencies.admin.currencies.freeflow'
    currency-setname:
      args-syntax-message: 'currency [<nazwa>] setname [<nazwa>]'
      description: 'Zmienia nazwę waluty.'
      permission: 'multicurrencies.admin.currencies.name'
    currency-setdisplayname:
      args-syntax-message: 'currency [<nazwa>] setdisplayname [<nazwa>]'
      description: 'Zmienia nazwę wyświetlaną waluty.'
      permission: 'multicurrencies.admin.currencies.displayname'
    currency-setcommandname:
      args-syntax-message: 'currency [<nazwa>] setcommandname [<nazwa>]'
      description: 'Zmienia nazwę komendy dla waluty.'
      permission: 'multicurrencies.admin.currencies.commandname'
    user-info:
      args-syntax-message: 'user [<nazwa>] info'
      description: 'Pokazuje informacje o użytkowniku.'
      permission: 'multicurrencies.admin.user.info'
    user-add:
      args-syntax-message: 'user [<nazwa>] add [<waluta>] [<ilość>]'
      description: 'Dodaje ilość waluty do konta użytkownika.'
      permission: 'multicurrencies.admin.user.add'
    user-remove:
      args-syntax-message: 'user [<nazwa>] remove [<waluta>] [<ilość>]'
      description: 'Usuwa ilość waluty z konta użytkownika.'
      permission: 'multicurrencies.admin.user.remove'
    user-set:
      args-syntax-message: 'user [<nazwa>] set [<waluta>] [<ilość>]'
      description: 'Zmienia ilość waluty na koncie użytkownika.'
      permission: 'multicurrencies.admin.user.set'
    user-clear:
      args-syntax-message: 'user [<nazwa>] clear [<waluta>]'
      description: 'Zmienia wartość waluty na 0 dla konta użytkownika.'
      permission: 'multicurrencies.admin.user.clear'
    user-reset:
      args-syntax-message: 'user [<nazwa>] reset'
      description: 'Resetuje ilość wszystkich walut na 0 dla konta użytkownika.'
      permission: 'multicurrencies.admin.user.reset'
    loaddisabledcurrencycommands:
      args-syntax-message: 'loaddisabledcurrencycommands'
      description: 'Przeładowuje liste waluty, dla których komendy nie są tworzone.'
      permission: 'multicurrencies.reload'
    loadvaulthook:
      args-syntax-message: 'loadvaulthook'
      description: 'Przeładowuje zmienną ''vault-currency''.'
      permission: 'multicurrencies.reload'

messages:
  non-permission: '&cNie masz dostępu do tej komendy!'
  true-false-invalid-value: '&cZła wartość argumentu! Wymagane &etrue&7/&efalse'
  help-header: '&c=========={ &lPOMOC &c}=========='
  help-footer: '&c============================='
  help-syntax: '&c/<command> <args> &8- &7<description>'
  check-syntax-error: '&cError! Sprawdź składnię komendy &e/<command> help'
  currency-list: '&6Wszystkie waluty: &c'
  currency-list-empty: 'Lista jest pusta'
  currency-list-entry: '&c- &e%currency-name% &7(&6%currency-displayname%&7)'
  not-found-currency: '&cNie znaleziono waluty!'
  currency-info-name: '&cWaluta &e%name%&c:'
  currency-info-id: '  &c> &7ID&8: &c%id%'
  currency-info-free-flow: '  &c> &7Swobodny przepływ&8: &c%free-flow%'
  currency-info-command-name: '  &c> &7Nazwa komendy&8: &c%command-name%'
  currency-info-display-name: '  &c> &7Nazwa wyświetlana&8: &r%display-name%'
  currency-already-exists: '&cTa waluta już istnieje!'
  currency-successfully-created: '&aStworzono walutę!'
  currency-successfully-deleted: '&aUsunięto walutę!'
  currency-successfully-set-free-flow: '&aZmieniono przepływalność waluty!'
  currency-successfully-set-name: '&aZmieniono nazwę waluty!'
  currency-successfully-set-display-name: '&aZmieniono nazwę wyświetlaną waluty!'
  currency-successfully-set-command-name: '&aZmieniono nazwę komendy waluty!'
  currency-set-name-too-long: '&cPodałeś za długą nazwę!'
  not-found-user: '&cNie znaleziono gracza!'
  user-info-name: '&cGracz &e%name%&c:'
  user-info-id: '  &c> &7ID&8: &7%id%'
  user-info-uuid: '  &c> &7UUID&8: &7%uuid%'
  user-info-currency-list: '  &c> &7Waluty&8: &7'
  user-info-currency-list-empty: 'Brak'
  user-info-currency-list-entry: '  &c> > &7%currency% &c> &8%amount%'
  not-valid-value: '&cPodana wartość jest nie poprawna.'
  user-added-amount: '&aPomyślnie dodano &e%amount% &6%currency% &ado konta &e%player%&a.'
  user-removed-amount: '&aPomyślnie usunięto &e%amount% &6%currency% &ado konta &e%player%&a.'
  user-set-amount: '&aPomyślnie zmieniono ilość waluty &6%currency% &ana &e%amount% &ana koncie &e%player%&a.'
  user-cleared-amount: '&aPomyślnie zmieniono ilość waluty &6%currency% &ana &e0 &ana koncie &e%player%&a.'
  user-reset: '&aPomyślnie wyczyszczono dane wszystkich walut dla konta &e%player%&a.'
  user-operation-failed: '&cOperacja zakończona porażką.'
  executable-only-as-player: '&cKomenda wykonywalna jedynie jako gracz!'
  user-pay: '&aPomyślnie przekazano &e%amount% &awaluty &6%currency% &agraczowi &e%player%&a.'
  user-account-balance: '&7[%currency%&7] &aStan konta&7: &e%amount%'
  user-cannot-pay-yourself: '&cNie możesz przelać waluty na to samo konto!'
  loaded-disabled-currency-commands: '&aPrzeładowano listę walut, dla których komendy nie są tworzone.'
  not-enough-currency-amount: '&cBrak środków!'
  loaded-vault-currency: '&aPrzeładowano zmienną odpowiedzialną za łączenie z Vault.'

  ```
