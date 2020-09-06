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
        if (e.getInventory() == JSONBooksCommands.gui) {

            e.setCancelled(true);

            final ItemStack clickedItem = e.getCurrentItem();

            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            final Player player = (Player) e.getWhoClicked();

            if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals("Click to purchase book")) {
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
}
