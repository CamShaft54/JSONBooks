package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class JSONBooks extends JavaPlugin {

    @Override
    public void onEnable() {
        JSONBooksCommands commands = new JSONBooksCommands();
        getCommand("jsonbook").setExecutor(commands);
        getServer().getConsoleSender().sendMessage(ChatColor.DARK_BLUE + "[JSONBooks]: Plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JSONBooks]: Plugin is disabled!");
    }

}
