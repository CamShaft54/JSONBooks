package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JSONBooks extends JavaPlugin {

    FileConfiguration config = this.getConfig();
    public static Material paymentItem1;
    public static int paymentAmount1;
    public static Material paymentItem2;
    public static int paymentAmount2;
    public static String paymentItemString1;
    public static String paymentItemString2;
    public static Boolean cmdAllowed;

    public class Command implements TabCompleter {
        private final String[] COMMANDS = {"", "preview"};
        //create a static array of values

        @Override
        public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            //create new array
            final List<String> completions = new ArrayList<>();
            //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
            if (strings.length == 2) {
                StringUtil.copyPartialMatches(strings[1], Arrays.asList(COMMANDS), completions);
            }
            //sort the list
            Collections.sort(completions);
            return completions;
        }
    }

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
        getCommand("jsonbook").setTabCompleter(new Command());
        getServer().getConsoleSender().sendMessage(ChatColor.DARK_BLUE + "[JSONBooks]: Plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JSONBooks]: Plugin is disabled!");
    }

}
