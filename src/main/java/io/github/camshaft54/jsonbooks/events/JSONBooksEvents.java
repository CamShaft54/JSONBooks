package io.github.camshaft54.jsonbooks.events;

import io.github.camshaft54.jsonbooks.JSONBooks;
import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static io.github.camshaft54.jsonbooks.commands.JSONBooksCommands.json;

public class JSONBooksEvents implements Listener {
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) { // runs when each player joins
        Player player = event.getPlayer();
        // check if local version of plugin is not the newest version, then sends message to player
        if (!JSONBooks.local_version.equals(JSONBooks.online_version)) {
            player.sendMessage(ChatColor.DARK_RED + "[JSONBooks]: Current plugin version is v" +
                    JSONBooks.local_version + ", which is outdated. The most recent version is v" + JSONBooks.online_version + ".");
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() == JSONBooksCommands.jsonGui) {

            e.setCancelled(true);

            final ItemStack clickedItem = e.getCurrentItem();

            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            final Player player = (Player) e.getWhoClicked();

            if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals("Click to complete purchase")) {
                // if json is null from getJSON then don't give the player the book
                if (json == null) return;
                // if player is in another game mode check for payment and take it
                if (player.getGameMode() != GameMode.CREATIVE) {
                    for (int i = 0; i < JSONBooks.jsonBookPaymentTypes.length; i++) {
                        if (!player.getInventory().containsAtLeast(JSONBooksCommands.jsonBookPaymentItems[i], JSONBooks.jsonBookPaymentAmounts[i])) {
                            player.sendMessage("JSONBooks: Insufficient Funds, see GUI for list of payment items needed.");
                            return;
                        }
                    }
                    for (int i = 0; i < JSONBooks.jsonBookPaymentTypes.length; i++) {
                        player.getInventory().removeItem(JSONBooksCommands.jsonBookPaymentItems[i]);
                    }
                }
                // give player the book
                giveBook(player, json, false);
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Click to close GUI")) {
                player.closeInventory();
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Click to open a preview of the book")) {
                giveBook(player, json, true);
            }
        } else if (e.getInventory() == JSONBooksCommands.bookGui) {
            e.setCancelled(true);

            final ItemStack clickedItem = e.getCurrentItem();

            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            final Player player = (Player) e.getWhoClicked();

            JSONBooksCommands.original.setAmount(JSONBooksCommands.amount);
            if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals("Click to complete purchase")) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    for (int i = 0; i < JSONBooks.bookCopierPaymentTypes.length; i++) {
                        JSONBooksCommands.bookCopierPaymentItems[i] = new ItemStack(Material.valueOf(JSONBooks.bookCopierPaymentTypes[i].toUpperCase()), JSONBooksCommands.amount * JSONBooks.bookCopierPaymentAmounts[i]);
                        if (!player.getInventory().containsAtLeast(JSONBooksCommands.bookCopierPaymentItems[i], JSONBooksCommands.amount * JSONBooks.bookCopierPaymentAmounts[i])) {
                            player.sendMessage("JSONBooks: Insufficient Funds, see GUI for list of payment items needed.");
                            return;
                        }
                    }
                    for (int i = 0; i < JSONBooks.bookCopierPaymentTypes.length; i++) {
                        player.getInventory().removeItem(JSONBooksCommands.bookCopierPaymentItems[i]);
                    }
                }
                player.getInventory().addItem(JSONBooksCommands.original);
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Click to close GUI")) {
                player.closeInventory();
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Add another copy")) {
                JSONBooksCommands.amount += 1;
                if (JSONBooksCommands.amount > 64) {
                    player.sendMessage("JSONBooks: Maximum of 64 copies per purchase!");
                    JSONBooksCommands.amount = 64;
                }
                updateAmount(JSONBooksCommands.amount);
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Remove a copy")) {
                JSONBooksCommands.amount -= 1;
                if (JSONBooksCommands.amount <= 0) {
                    player.sendMessage("JSONBooks: Minimum of one copy required!");
                    JSONBooksCommands.amount = 1;
                }
                updateAmount(JSONBooksCommands.amount);
            }
        }
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

    private void updateAmount(int amount) {
        for (int i = 0; i < JSONBooksCommands.bookCopierPaymentItems.length; i++) {
            ItemStack payment = JSONBooksCommands.bookGui.getItem(10+i);
            assert payment != null;
            payment.setAmount(amount * JSONBooks.bookCopierPaymentAmounts[i]);
            JSONBooksCommands.bookGui.setItem(10+i, payment);
        }
    }
}
