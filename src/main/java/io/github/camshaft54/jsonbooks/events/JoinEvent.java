package io.github.camshaft54.jsonbooks.events;

import io.github.camshaft54.jsonbooks.JSONBooks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) { // runs when each player joins
        Player player = event.getPlayer();
        // check if local version of plugin is not the newest version, then sends message to player
        if (!JSONBooks.local_version.equals(JSONBooks.online_version)) {
            player.sendMessage(ChatColor.DARK_RED + "[JSONBooks]: Current plugin version is v" +
                    JSONBooks.local_version + ", which is outdated. The most recent version is v" + JSONBooks.online_version + ".");
        }
    }
}
