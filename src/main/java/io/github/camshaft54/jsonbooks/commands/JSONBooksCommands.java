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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JSONBooksCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (args.length != 1) {
            player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <json>");
            return true;
        }

        ItemStack payment = new ItemStack(JSONBooks.paymentItem);
        int paymentAmount = JSONBooks.paymentAmount;
        ItemStack fullPayment = new ItemStack(JSONBooks.paymentItem, paymentAmount);
        // Get JSON and check if it contains runCommand
        String json = getJSON(player, args[0],JSONBooks.cmdAllowed);
        if (json == null) {
            return true;
        }
        // If the player is in creative mode they do not need to pay
        if (player.getGameMode() == GameMode.CREATIVE || paymentAmount <= 0) {
            giveBook(player, json);
            return true;
        }
        // Check player's inventory for the payment
        if (player.getInventory().containsAtLeast(payment, paymentAmount)) {
            player.sendMessage(player.getDisplayName() + " paid " + paymentAmount + " x " + JSONBooks.paymentItemString + " for 1 book.");
            giveBook(player, json);
            player.getInventory().removeItem(fullPayment);
            return true;
        }
        player.sendMessage("Insufficient funds. The set payment for this command is " + JSONBooks.paymentAmount + " x " + JSONBooks.paymentItemString + ".");
        return true;
    }

    private String getJSON(Player player, String link, Boolean cmdAllowed) {
        String json = "";
        try {
            Document doc = Jsoup.connect(link).get();
            json = doc.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!cmdAllowed && json.contains("\"action\":\"run_command\"") && player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage("JSONBooks: Running commands in books has been disallowed by the server administrator.");
            return null;
        }
        return json;
    }

    private void giveBook(Player player, String json) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        Bukkit.getUnsafe().modifyItemStack(book, json);
        player.sendMessage("JSONBooks: Gave " + player.getDisplayName() + " 1 JSON Book");
        player.getInventory().addItem(book);
    }
}
