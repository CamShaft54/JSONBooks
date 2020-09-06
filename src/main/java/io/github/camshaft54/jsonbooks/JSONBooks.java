package io.github.camshaft54.jsonbooks;

import io.github.camshaft54.jsonbooks.commands.JSONBooksCommands;
import io.github.camshaft54.jsonbooks.events.JSONBooksEvents;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

import static org.apache.commons.lang.ArrayUtils.toPrimitive;

public class JSONBooks extends JavaPlugin {

    FileConfiguration config = this.getConfig();
    public static String[] jsonBookPaymentTypes;
    public static int[] jsonBookPaymentAmounts;
    public static String[] bookCopierPaymentTypes;
    public static int[] bookCopierPaymentAmounts;
    public static Boolean cmdAllowed;
    public static Boolean writableBookCopying;
    public static String online_version;
    public static String local_version;

    public static class Command implements TabCompleter {
        // list of TabCompleter commands
        private final String[] jsonBookCommands = {"preview"};
        private final String[] bookCopierCommands = {""};

        @Override
        public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            final List<String> completions = new ArrayList<>();
            // copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
            if (command.getName().equals("jsonbook") && strings.length == 2) {
                StringUtil.copyPartialMatches(strings[0], Arrays.asList(jsonBookCommands), completions);
            }
            else if (strings.length == 1) {
                StringUtil.copyPartialMatches(strings[0], Arrays.asList(bookCopierCommands), completions);
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
        // adds "/book" to Minecraft commands
        Objects.requireNonNull(getCommand("book")).setExecutor(commands);
        // adds TabCompleter to "/jsonbook"
        Objects.requireNonNull(getCommand("jsonbook")).setTabCompleter(new Command());
        // adds TabCompleter to "/book"
        Objects.requireNonNull(getCommand("book")).setTabCompleter(new Command());
        // registers events class with the player join message
        getServer().getPluginManager().registerEvents(new JSONBooksEvents(), this);
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
        config.addDefault("jsonBookPaymentTypes", new String[]{"diamond", "book"});
        config.addDefault("jsonBookPaymentAmounts", new int[]{1, 1});
        config.addDefault("cmdAllowed",true);
        config.addDefault("bookCopierPaymentTypes", new String[]{"book", "feather", "ink_sac"});
        config.addDefault("bookCopierPaymentAmounts", new int[]{1, 1, 1});
        config.addDefault("writableBookCopying", false);
        config.options().copyDefaults(true);
        saveConfig();
        // assign config values to variables
        jsonBookPaymentTypes = config.getStringList("jsonBookPaymentTypes").toArray(new String[0]);
        jsonBookPaymentAmounts = toPrimitive(config.getIntegerList("jsonBookPaymentAmounts").toArray(new Integer[0]));
        bookCopierPaymentTypes = config.getStringList("bookCopierPaymentTypes").toArray(new String[0]);
        bookCopierPaymentAmounts = toPrimitive(config.getIntegerList("bookCopierPaymentAmounts").toArray(new Integer[0]));
        cmdAllowed = config.getBoolean("cmdAllowed");
        writableBookCopying = config.getBoolean("writableBookCopying");
    }
}
