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
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (args.length > 2) {
            player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <raw paste url> (preview)");
            return true;
        }

        // Define variables for first payment
        ItemStack payment1 = new ItemStack(JSONBooks.paymentItem1);
        int paymentAmount1 = JSONBooks.paymentAmount1;
        ItemStack fullPayment1 = new ItemStack(JSONBooks.paymentItem1, paymentAmount1);
        // Define variables for second payment
        ItemStack payment2 = new ItemStack(JSONBooks.paymentItem2);
        int paymentAmount2 = JSONBooks.paymentAmount2;
        ItemStack fullPayment2 = new ItemStack(JSONBooks.paymentItem2, paymentAmount2);
        // Create string that contains info about how much the json book costs.
        String paymentString = paymentAmount1 + " x " + JSONBooks.paymentItemString1;
        if (JSONBooks.paymentAmount2 != 0) {
            paymentString += " and " + paymentAmount2 + " x " + JSONBooks.paymentItemString2;
        }
        // Get JSON and check if it contains runCommand
        String link = args[0];
        String json = getJSON(player, link, JSONBooks.cmdAllowed);
        // If json is null from getJSON then don't give the player the book.
        if (json == null) {
            return true;
        }
        // If player is in creative or payment is 0, they don't need to pay.
        if (player.getGameMode() == GameMode.CREATIVE || paymentAmount1 <= 0) {
            if (args.length == 2 && args[1].equals("preview")) {
                giveBook(player, json, true);
                return true;
            }
            player.sendMessage("JSONBooks: Gave " + player.getDisplayName() + " a book.");
            giveBook(player, json, false);
            return true;
        }
        // Check player's inventory for payment 1 and 2
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
        if (link.length() > 21 && link.substring(0,21).matches("https://.astebin\\.com/")) {
            link = link.substring(0, 21) + "raw/" + link.substring(21);
        }
        try {
            Connection connection = Jsoup.connect(link);
            connection.userAgent("Mozilla/5.0");
            Document doc = connection.get();
            json = doc.body().text();
        } catch (Exception e) {
            player.sendMessage("JSONBooks: Invalid link. The correct format is https://www.pastebin.com/...");
            e.printStackTrace();
            return null;
        }
        if (!cmdAllowed && json.contains("\"action\":\"run_command\"") && player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage("JSONBooks: Running commands in books has been disallowed by the server administrator.");
        }
        return json;
    }

    // gives player book with JSON
    private void giveBook(Player player, String json, Boolean preview) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        //noinspection deprecation
        Bukkit.getUnsafe().modifyItemStack(book, json);
        if (preview) {
            player.openBook(book);
        }
        else {
            player.getInventory().addItem(book);
        }
    }
}
