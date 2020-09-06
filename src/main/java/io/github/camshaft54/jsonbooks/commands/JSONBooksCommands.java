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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

public class JSONBooksCommands implements CommandExecutor {
    public static Inventory gui;
    public static String json;
    public static Boolean previewEnabled;
    public static ItemStack[] jsonBookPaymentItems;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // check if commandSender is a player
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (command.getName().equals("jsonbook")) {
            // if player specifies too many arguments, send error
            if (args.length > 2) {
                return true;
            }

            // get JSON and check if it contains runCommand
            String link = args[0];
            json = getJSON(player, link, JSONBooks.cmdAllowed);
            previewEnabled = (args.length == 2 && args[1].equals("preview"));
            if (json == null) return true;

            // create JSONBook gui
            gui = Bukkit.getServer().createInventory(player, 18,"JSON Book");

            // add info book to gui
            ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta writtenBookMeta = writtenBook.getItemMeta();
            assert writtenBookMeta != null;
            ArrayList<String> writtenBookLore = new ArrayList<>();
            writtenBookLore.add("This GUI appeared because you ran the /jsonbook command.");
            writtenBookLore.add("If you are in survival and your server administrator");
            writtenBookLore.add("enabled it, you will see on the");
            writtenBookLore.add("inventory line below this book a piece of paper.");
            writtenBookLore.add("All items to the right of the paper are the payment");
            writtenBookLore.add("items that you need to purchase a book.");
            writtenBookLore.add("The quantity of each item needed is");
            writtenBookLore.add("listed when you hover over each item.");
            writtenBookMeta.setLore(writtenBookLore);
            writtenBookMeta.setDisplayName("JSONBooks Command Info");
            writtenBook.setItemMeta(writtenBookMeta);

            // add preview button to gui
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta enchantedBookMeta = enchantedBook.getItemMeta();
            assert enchantedBookMeta != null;
            enchantedBookMeta.setDisplayName("Click to open a preview of the book");
            enchantedBook.setItemMeta(enchantedBookMeta);

            // add purchase button to gui
            ItemStack scute = new ItemStack(Material.SCUTE);
            ItemMeta scuteMeta = scute.getItemMeta();
            assert scuteMeta != null;
            scuteMeta.setDisplayName("Click to purchase book");
            scute.setItemMeta(scuteMeta);

            // add cancel button to gui
            ItemStack red = new ItemStack(Material.RED_DYE);
            ItemMeta redMeta = red.getItemMeta();
            assert redMeta != null;
            redMeta.setDisplayName("Click to close GUI");
            red.setItemMeta(redMeta);

            // add payment info to gui
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta paperMeta = paper.getItemMeta();
            assert paperMeta != null;
            paperMeta.setDisplayName("Payment required for one book ->");
            paper.setItemMeta(paperMeta);

            // add payment items to gui
            jsonBookPaymentItems = new ItemStack[JSONBooks.jsonBookPaymentTypes.length];
            for (int i = 0; i < JSONBooks.jsonBookPaymentTypes.length; i++) {
                jsonBookPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.jsonBookPaymentTypes[i].toUpperCase()), JSONBooks.jsonBookPaymentAmounts[i]);
                gui.setItem(10+i,jsonBookPaymentItems[i]);
            }

            // add all items created to gui and open gui
            gui.setItem(0, writtenBook);
            gui.setItem(1, enchantedBook);
            gui.setItem(7, scute);
            gui.setItem(8, red);
            gui.setItem(9, paper);
            player.openInventory(gui);
            return true;
        }
        // if command is /book
        else if (command.getName().equals("book")) {
            if (args.length != 1 || !StringUtils.isNumeric(args[0])) {
                player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /book <copies of book>");
            }
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
                }
                for (int i = 0; i < JSONBooks.bookCopierPaymentTypes.length; i++) {
                    player.getInventory().removeItem(bookCopierPaymentItems[i]);
                }
            }
            player.getInventory().addItem(original);
            return true;
        }
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
}
