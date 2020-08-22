package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class JSONBooks extends JavaPlugin {

    FileConfiguration config = this.getConfig();
    public static Material paymentItem;
    public static int paymentAmount;
    public static String paymentItemString;
    public static Boolean cmdAllowed;

    @Override
    public void onEnable() {
        config.addDefault("payment", "diamond");
        config.addDefault("amount", 2);
        config.addDefault("cmdAllowed",true);
        config.options().copyDefaults(true);
        saveConfig();
        paymentItem = Material.valueOf(config.getString("payment").toUpperCase());
        paymentAmount = config.getInt("amount");
        paymentItemString = config.getString("payment");
        cmdAllowed = config.getBoolean("cmdAllowed");
        JSONBooksCommands commands = new JSONBooksCommands();
        getCommand("jsonbook").setExecutor(commands);
        getServer().getConsoleSender().sendMessage(ChatColor.DARK_BLUE + "[JSONBooks]: Plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JSONBooks]: Plugin is disabled!");
    }

}
