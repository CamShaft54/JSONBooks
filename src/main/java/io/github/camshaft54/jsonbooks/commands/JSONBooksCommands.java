package io.github.camshaft54.jsonbooks.commands;

import io.github.camshaft54.jsonbooks.JSONBooks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JSONBooksCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // check if commandSender is a player
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        // if player specifies too many arguments, send error
        if (args.length > 2) {
            player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <paste url> (preview)");
            return true;
        }

        // define variables for first payment
        ItemStack payment1 = new ItemStack(JSONBooks.paymentItem1);
        int paymentAmount1 = JSONBooks.paymentAmount1;
        ItemStack fullPayment1 = new ItemStack(JSONBooks.paymentItem1, paymentAmount1);
        // define variables for second payment
        ItemStack payment2 = new ItemStack(JSONBooks.paymentItem2);
        int paymentAmount2 = JSONBooks.paymentAmount2;
        ItemStack fullPayment2 = new ItemStack(JSONBooks.paymentItem2, paymentAmount2);
        // create string that contains info about how much the json book costs.
        String paymentString = paymentAmount1 + " x " + JSONBooks.paymentItemString1;
        if (JSONBooks.paymentAmount2 != 0) {
            paymentString += " and " + paymentAmount2 + " x " + JSONBooks.paymentItemString2;
        }
        // get JSON and check if it contains runCommand
        String link = args[0];
        String json = getJSON(player, link, JSONBooks.cmdAllowed);
        // if json is null from getJSON then don't give the player the book
        if (json == null) {
            return true;
        }
        // if player is in creative or payment is 0, they don't need to pay
        if (player.getGameMode() == GameMode.CREATIVE || paymentAmount1 <= 0) {
            if (args.length == 2 && args[1].equals("preview")) {
                giveBook(player, json, true);
                return true;
            }
            player.sendMessage("JSONBooks: Gave " + player.getDisplayName() + " a book.");
            giveBook(player, json, false);
            return true;
        }
        // check player's inventory for payment 1 and 2
        if (player.getInventory().containsAtLeast(payment1, paymentAmount1) && player.getInventory().containsAtLeast(payment2, paymentAmount2)) {
            if (args.length == 2 && args[1].equals("preview")) {
                giveBook(player, json, true);
                return true;
            }
            player.sendMessage("JSONBooks: " + player.getDisplayName() + " paid " + paymentString + " for a book.");
            giveBook(player, json, false);
            player.getInventory().removeItem(fullPayment1);
            player.getInventory().removeItem(fullPayment2);
            return true;
        }
        player.sendMessage("JSONBooks: Insufficient funds. The set payment for this command is " + paymentString + ".");
        return true;
    }

    // gets JSON from paste and checks for run_command (if specified)
    private String getJSON(Player player, String link, Boolean cmdAllowed) {
        String json;
        // if pastebin link isn't raw, add it
        if (link.length() > 21 && link.substring(0,21).matches("https://.astebin\\.com/")) {
            link = link.substring(0, 21) + "raw/" + link.substring(21);
        }
        // connect to pastebin and get JSON, if fails sends error message to player and stack trace to console
        try {
            Connection connection = Jsoup.connect(link);
            connection.userAgent("Mozilla/5.0");
            Document doc = connection.get();
            json = doc.body().text();
        } catch (Exception e) {
            player.sendMessage("JSONBooks: Invalid link. The correct format is https://www.pastebin.com/... or https://www.hastebin.com/...");
            e.printStackTrace();
            return null;
        }
        // if commands aren't allowed and the book contains commands, don't give book to player
        if (!cmdAllowed && json.contains("\"action\":\"run_command\"") && player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage("JSONBooks: Running commands in books has been disallowed by the server administrator.");
            return null;
        }
        return json;
    }

    // gives player book with JSON
    private void giveBook(Player player, String json, Boolean preview) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        //noinspection deprecation
        Bukkit.getUnsafe().modifyItemStack(book, json);
        // if player wants preview of book, show book
        if (preview) {
            player.openBook(book);
        }
        // otherwise give player book
        else {
            player.getInventory().addItem(book);
        }
    }
}
