package io.github.camshaft54.jsonbooks.commands;

import io.github.camshaft54.jsonbooks.JSONBooks;
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

public class JSONBooksCommands implements CommandExecutor {
    public static Inventory jsonGui;
    public static String json;
    public static ItemStack[] jsonBookPaymentItems;
    public static Inventory bookGui;
    public static ItemStack original;
    public static ItemStack[] bookCopierPaymentItems;
    public static int amount;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // check if commandSender is a player
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (command.getName().equals("jsonbook")) {
            // if player specifies too many arguments, send error
            if (args.length != 1) {
                player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /jsonbook <link to paste>");
                return true;
            }

            // get JSON and check if it contains runCommand
            String link = args[0];
            json = getJSON(player, link, JSONBooks.cmdAllowed);
            if (json == null) return true;

            // create JSONBook gui
            jsonGui = guiSetup(Bukkit.getServer().createInventory(player, 18,"JSON Book"));
            // add info book to gui
            ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta writtenBookMeta = writtenBook.getItemMeta();
            assert writtenBookMeta != null;
            ArrayList<String> writtenBookLore = new ArrayList<>();
            writtenBookLore.add("This GUI appeared because you ran the /jsonbook command.");
            writtenBookLore.add("If you are in survival and your server administrator enabled it,");
            writtenBookLore.add("you will see on the inventory line below this book a piece of paper.");
            writtenBookLore.add("All items to the right of the paper are the payment items that you");
            writtenBookLore.add("need in order to purchase a json book.");
            writtenBookMeta.setLore(writtenBookLore);
            writtenBookMeta.setDisplayName("JSONBooks Command Info");
            writtenBook.setItemMeta(writtenBookMeta);
            jsonGui.setItem(0, writtenBook);

            // add preview button to gui
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta enchantedBookMeta = enchantedBook.getItemMeta();
            assert enchantedBookMeta != null;
            enchantedBookMeta.setDisplayName("Click to open a preview of the book");
            enchantedBook.setItemMeta(enchantedBookMeta);
            jsonGui.setItem(1, enchantedBook);

            // add payment items to gui
            jsonBookPaymentItems = new ItemStack[JSONBooks.jsonBookPaymentTypes.length];
            for (int i = 0; i < JSONBooks.jsonBookPaymentTypes.length; i++) {
                jsonBookPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.jsonBookPaymentTypes[i].toUpperCase()), JSONBooks.jsonBookPaymentAmounts[i]);
                jsonGui.setItem(10+i,jsonBookPaymentItems[i]);
            }
            player.openInventory(jsonGui);
            return true;
        }
        // if command is /book
        else if (command.getName().equals("book")) {
            if (args.length != 0) {
                player.sendMessage("JSONBooks: Invalid Arguments\nUsage: /book");
                return true;
            }

            original = player.getInventory().getItemInMainHand().clone();
            if (original.getType() != Material.WRITTEN_BOOK || (JSONBooks.writableBookCopying && original.getType() != Material.WRITABLE_BOOK)) {
                player.sendMessage("JSONBooks: That's not a Written Book, silly!");
                return true;
            }

            amount = 1;

            bookGui = guiSetup(Bukkit.getServer().createInventory(player, 18,"Book Copier"));
            bookCopierPaymentItems = new ItemStack[JSONBooks.bookCopierPaymentTypes.length];
            for (int i = 0; i < JSONBooks.bookCopierPaymentTypes.length; i++) {
                bookCopierPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.bookCopierPaymentTypes[i].toUpperCase()), JSONBooks.bookCopierPaymentAmounts[i]);
                bookGui.setItem(10 + i, bookCopierPaymentItems[i]);
            }

            // add info book to gui
            ItemStack bookCopierInfo = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta bookCopierInfoMeta = bookCopierInfo.getItemMeta();
            assert bookCopierInfoMeta != null;
            ArrayList<String> writtenBookLore = new ArrayList<>();
            writtenBookLore.add("This GUI appeared because you ran the /book command.");
            writtenBookLore.add("If you are in survival and your server administrator enabled it,");
            writtenBookLore.add("you will see on the inventory line below this book a piece of paper.");
            writtenBookLore.add("All items to the right of the paper are the payment items that you");
            writtenBookLore.add("need in order to purchase copies of the book in your main hand.");
            bookCopierInfoMeta.setLore(writtenBookLore);
            bookCopierInfoMeta.setDisplayName("Book Copier Command Info");
            bookCopierInfo.setItemMeta(bookCopierInfoMeta);
            bookGui.setItem(0, bookCopierInfo);

            ItemStack add = new ItemStack(Material.WATER_BUCKET);
            ItemMeta addMeta = add.getItemMeta();
            assert addMeta != null;
            addMeta.setDisplayName("Add another copy");
            add.setItemMeta(addMeta);
            bookGui.setItem(1, add);

            ItemStack subtract = new ItemStack(Material.BUCKET);
            ItemMeta subtractMeta = subtract.getItemMeta();
            assert subtractMeta != null;
            subtractMeta.setDisplayName("Remove a copy");
            subtract.setItemMeta(subtractMeta);
            bookGui.setItem(2, subtract);

            player.openInventory(bookGui);
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
            if (JSONBooks.consoleDebug) {
                e.printStackTrace();
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage("[JSONBooks]: Player " + player.getDisplayName() + " sent invalid link to JSONBooks (" + link + ")." +
                        " To see more info, enable consoleDebug in config.");
            }
            return null;
        }
        // if commands aren't allowed and the book contains commands, don't give book to player
        if (!cmdAllowed && json.contains("\"action\":\"run_command\"") && player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage("JSONBooks: Running commands in books has been disallowed by the server administrator.");
            return null;
        }
        return json;
    }

    private Inventory guiSetup(Inventory gui) {
        // add purchase button to gui
        ItemStack scute = new ItemStack(Material.SCUTE);
        ItemMeta scuteMeta = scute.getItemMeta();
        assert scuteMeta != null;
        scuteMeta.setDisplayName("Click to complete purchase");
        scute.setItemMeta(scuteMeta);

        // add cancel button to gui
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        assert barrierMeta != null;
        barrierMeta.setDisplayName("Click to close GUI");
        barrier.setItemMeta(barrierMeta);

        // add payment info to gui
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        assert paperMeta != null;
        paperMeta.setDisplayName("Total payment required (if in survival) ->");
        paper.setItemMeta(paperMeta);

        // add all items created to gui and open gui
        gui.setItem(7, scute);
        gui.setItem(8, barrier);
        gui.setItem(9, paper);
        return gui;
    }
}
