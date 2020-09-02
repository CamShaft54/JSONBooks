package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import io.github.camshaft54.jsonbooks.events.JoinEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

public class JSONBooks extends JavaPlugin {

    FileConfiguration config = this.getConfig();
    public static Material paymentItem1;
    public static int paymentAmount1;
    public static Material paymentItem2;
    public static int paymentAmount2;
    public static String paymentItemString1;
    public static String paymentItemString2;
    public static Boolean cmdAllowed;
    public static String online_version;
    public static String local_version;

    public static class Command implements TabCompleter {
        // list of TabCompleter commands
        private final String[] COMMANDS = {"", "preview"};

        @Override
        public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            final List<String> completions = new ArrayList<>();
            // copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
            if (strings.length == 2) {
                StringUtil.copyPartialMatches(strings[1], Arrays.asList(COMMANDS), completions);
            }
            // sorts the list
            Collections.sort(completions);
            return completions;
        }
    }

    @Override
    public void onEnable() {
        // sets up config
        configSetup();
        // creates new instance of commands class
        JSONBooksCommands commands = new JSONBooksCommands();
        // adds "/jsonbook" to Minecraft commands
        Objects.requireNonNull(getCommand("jsonbook")).setExecutor(commands);
        // adds TabCompleter to "/jsonbook"
        Objects.requireNonNull(getCommand("jsonbook")).setTabCompleter(new Command());
        // registers events class with the player join message
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        // gets local version of JSONBooks plugin
        local_version = getDescription().getVersion();
        // gets online version of JSONBooks plugin from pastebin
        try {
            Document doc = Jsoup.connect("https://pastebin.com/raw/2JX2df9Y").get();
            online_version = doc.body().text();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        // if local version is outdated, send message to console.
        if (!local_version.equals(online_version)) {
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_BLUE + "[JSONBooks]: Current plugin version is v" +
                    local_version + ", which is outdated. The most recent version is v" + online_version + ".");
        }
    }

    private void configSetup() {
        // set values in config and save it
        config.addDefault("payment 1", "diamond");
        config.addDefault("amount 1", 1);
        config.addDefault("payment 2", "book");
        config.addDefault("amount 2", 1);
        config.addDefault("cmdAllowed",true);
        config.options().copyDefaults(true);
        saveConfig();
        // assign config values to variables
        paymentItem1 = Material.valueOf(Objects.requireNonNull(config.getString("payment 1")).toUpperCase());
        paymentAmount1 = config.getInt("amount 1");
        paymentItem2 = Material.valueOf(Objects.requireNonNull(config.getString("payment 2")).toUpperCase());
        paymentAmount2 = config.getInt("amount 2");
        paymentItemString1 = config.getString("payment 1");
        paymentItemString2 = config.getString("payment 2");
        cmdAllowed = config.getBoolean("cmdAllowed");
    }
}
