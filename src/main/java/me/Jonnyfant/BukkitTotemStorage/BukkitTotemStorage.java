package me.Jonnyfant.BukkitTotemStorage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BukkitTotemStorage extends JavaPlugin {
    private final String CONFIG_AMOUNT_KEY = "Amount of Totem storage per player";
    private final int CONFIG_DEFAULT_AMOUNT = 3;
    private final String CONFIG_DIAMOND_KEY = "Dimaonds cost per saved Totem";
    private final int CONFIG_DEFAULT_DIAMOND = 1;

    @Override
    public void onEnable() {
        loadConfig();
        getServer().getPluginManager().registerEvents(new TotemEventListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        if (sender.hasPermission("totemstorage.use") && sender instanceof Player) {
            switch (commandName.toLowerCase()) {
                case "storetotem":
                    return saveTotem(args, (Player) sender);
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean saveTotem(String[] args, Player p) {
        reloadConfig();
        getConfig().addDefault(p.getName(), 0);
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        if (getConfig().getInt(CONFIG_AMOUNT_KEY) > getConfig().getInt(p.getName())) {
            if (stealStuff(Material.TOTEM, p, 1, false) && stealStuff(Material.DIAMOND, p, getConfig().getInt(CONFIG_DIAMOND_KEY), false)) {
                stealStuff(Material.TOTEM, p, 1, true);
                stealStuff(Material.DIAMOND, p, getConfig().getInt(CONFIG_DIAMOND_KEY), true);
                getConfig().set(p.getName(), getConfig().getInt(p.getName()) + 1);
                p.sendMessage("Adding one Totem of undying to your storage. Balance: " + getConfig().getInt(p.getName()) + " Maximum: " + getConfig().getInt(CONFIG_AMOUNT_KEY));
                saveConfig();
                return true;
            } else {
                p.sendMessage("You need a Totem of undying and " + getConfig().getInt(CONFIG_DIAMOND_KEY) +
                        " diamond(s) in your inventory to pay for storing a totem. Your current storage contains " +
                        getConfig().getInt(p.getName()) + " totems, maximum is " + getConfig().getInt(CONFIG_AMOUNT_KEY) + ".");
                return false;
            }
        } else {
            p.sendMessage("You currently stored the maximum amount of Tokens of undying: " + getConfig().getInt(CONFIG_AMOUNT_KEY));
            return true;
        }
    }

    public boolean stealStuff(Material m, Player p, int amount, boolean steal) {
        PlayerInventory inv = p.getInventory();
        for (ItemStack is : inv.getContents()) {
            if (is != null) {
                if (is.getType().equals(m) && is.getAmount() >= amount) {
                    if (steal) {
                        is.setAmount(is.getAmount() - amount);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void loadConfig() {
        getConfig().addDefault(CONFIG_AMOUNT_KEY, CONFIG_DEFAULT_AMOUNT);
        getConfig().addDefault(CONFIG_DIAMOND_KEY, CONFIG_DEFAULT_DIAMOND);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
