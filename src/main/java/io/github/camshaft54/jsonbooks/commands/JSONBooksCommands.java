package io.github.camshaft54.jsonbooks.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JSONBooksCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;

        if (command.getName().equalsIgnoreCase("jsonbook")) {
            if (strings.length >= 1) {
                String json = "";
                try {
                    Document doc = null;
                    player.sendMessage("JSONBooks: " + strings[0]);
                    doc = Jsoup.connect(strings[0]).get();
                    json = doc.body().text();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                player.sendMessage(json);
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                Bukkit.getUnsafe().modifyItemStack(book, json.toString());
                player.getInventory().addItem(book);
            }
            else {
                player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <json>");
            }
        }

        return true;
    }
}
