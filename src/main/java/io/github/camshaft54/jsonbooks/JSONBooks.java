package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class JSONBooks extends JavaPlugin {

    FileConfiguration config = this.getConfig();
    public static Material paymentItem1;
    public static int paymentAmount1;
    public static Material paymentItem2;
    public static int paymentAmount2;
    public static String paymentItemString1;
    public static String paymentItemString2;
    public static Boolean cmdAllowed;

    @Override
    public void onEnable() {
        config.addDefault("payment 1", "diamond");
        config.addDefault("amount 1", 1);
        config.addDefault("payment 2", "book");
        config.addDefault("amount 2", 1);
        config.addDefault("cmdAllowed",true);
        config.options().copyDefaults(true);
        saveConfig();
        paymentItem1 = Material.valueOf(config.getString("payment 1").toUpperCase());
        paymentAmount1 = config.getInt("amount 1");
        paymentItem2 = Material.valueOf(config.getString("payment 2").toUpperCase());
        paymentAmount2 = config.getInt("amount 2");
        paymentItemString1 = config.getString("payment 1");
        paymentItemString2 = config.getString("payment 2");
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
