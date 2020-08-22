# JSONBooks - A 1.16.2 Spigot Plugin
#### This plugin adds the /jsonbook command, allowing players to create custom Written Books with JSON data for a payment.
## Usage of /jsonbook
#### `/jsonbook <url to raw paste with json data>`
The /jsonbook requires a url of a raw paste from pastebin.com (https://pastebin.com/raw), this can be obtained by making a paste and pressing the `raw` button. If the player is Survival mode or Adventure mode, the quantity of payment specified in `config.yml` will be removed from their inventory.
## `config.yml` (Default Configuration)
    payment: diamond
    amount: 2
    cmdAllowed: false
- `payment` is the item type of the payment. If you don't want payment in survival mode, set it to 0.
- `amount` is the amount of the payment needed in survival mode.
- `cmdAllowed` prevents players from making click events in the book that run commands. `true` means players can run commands, `false` means they cannot.
## About
This plugin was created by Camshaft54 and uses some payment code from [MetalTurtle18's Custom Head Command](https://www.spigotmc.org/resources/custom-heads-command-1-16-2.82856/) plugin. This plugin also uses the library [jsoup](https://jsoup.org) to get the raw paste data.

This is my first Spigot plugin so any feedback is welcome. I will do my best to make any changes or suggestions given.
