package io.github.camshaft54.jsonbooks.commands;

import io.github.camshaft54.jsonbooks.JSONBooks;
import org.apache.commons.lang.StringUtils;
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

import java.util.Arrays;

public class JSONBooksCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // check if commandSender is a player
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (command.getName().equals("jsonbook")) {
            // if player specifies too many arguments, send error
            if (args.length > 2) {
                player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <paste url> (preview)");
                return true;
            }
            // get JSON and check if it contains runCommand
            String link = args[0];
            String json = getJSON(player, link, JSONBooks.cmdAllowed);
            // if json is null from getJSON then don't give the player the book
            if (json == null) {return true;}
            // if player is in another game mode check for payment and take it
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack[] jsonBookPaymentItems = new ItemStack[JSONBooks.jsonBookPaymentTypes.length];
                for (int i = 0; i < JSONBooks.jsonBookPaymentTypes.length; i++) {
                    jsonBookPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.jsonBookPaymentTypes[i].toUpperCase()), JSONBooks.jsonBookPaymentAmounts[i]);
                    if (!player.getInventory().containsAtLeast(jsonBookPaymentItems[i], JSONBooks.jsonBookPaymentAmounts[i])) {
                        player.sendMessage("JSONBooks: Insufficient Funds, " + Arrays.toString(JSONBooks.jsonBookPaymentTypes) + " x " + Arrays.toString(JSONBooks.jsonBookPaymentAmounts) + " is required per book.");
                        return true;
                    }
                    player.getInventory().removeItem(jsonBookPaymentItems[i]);
                }
            }
            // give player the book
            giveBook(player, json, (args.length == 2 && args[1].equals("preview")));
            return true;
        }
        // if command is /book
        if (args.length != 1 || !StringUtils.isNumeric(args[0])) {
            player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /book <copies of book>");}
        int amount = Integer.parseInt(args[0]);
        ItemStack original = player.getInventory().getItemInMainHand().clone();
        if (original.getType() != Material.WRITTEN_BOOK || (JSONBooks.writableBookCopying && original.getType() != Material.WRITABLE_BOOK)) {
            player.sendMessage("JSONBooks: That's not a Written Book, silly!");
            return true;
        }
        original.setAmount(amount);
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack[] bookCopierPaymentItems = new ItemStack[JSONBooks.bookCopierPaymentTypes.length];
            for (int i = 0; i < JSONBooks.bookCopierPaymentTypes.length; i++) {
                bookCopierPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.bookCopierPaymentTypes[i].toUpperCase()), amount * JSONBooks.bookCopierPaymentAmounts[i]);
                if (!player.getInventory().containsAtLeast(bookCopierPaymentItems[i], amount * JSONBooks.bookCopierPaymentAmounts[i])) {
                    player.sendMessage("JSONBooks: Insufficient Funds, " + Arrays.toString(JSONBooks.bookCopierPaymentTypes) + " x " + Arrays.toString(JSONBooks.bookCopierPaymentAmounts) + " is required per book.");
                    return true;
                }
                player.getInventory().removeItem(bookCopierPaymentItems[i]);
            }
        }
        player.getInventory().addItem(original);
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
        if (preview) {player.openBook(book);}
        // otherwise give player book
        else {player.getInventory().addItem(book);}
    }
}
