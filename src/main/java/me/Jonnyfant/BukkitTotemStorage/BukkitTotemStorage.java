package me.Jonnyfant.BukkitTotemStorage;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTotemStorage extends JavaPlugin {
    private final String CFG_MAX_STORAGE_KEY = "Amount of Totem storage per player";
    private final int CFG_MAX_STORAGE_DEFAULT = 3;

    private final String CFG_COSTS_KEY = "Cost to save a totem";
    private final Map<String, Integer> CFG_COSTS_DEFAULT = new HashMap<String, Integer>() {
        {
            put(Material.TOTEM.name(), 1);
            put(Material.DIAMOND.name(), 1);

        }
    };

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
        if (getConfig().getInt(CFG_MAX_STORAGE_KEY) <= getConfig().getInt(p.getName())) {
            p.sendMessage("You currently stored the maximum amount of Tokens of undying: "
                    + getConfig().getInt(CFG_MAX_STORAGE_KEY));
            return true;
        }

        // init Hashmap for costs
        List<Map<?, ?>> costsList = getConfig().getMapList(CFG_COSTS_KEY);
        Map<String, Integer> costs = (Map<String, Integer>) costsList.get(0);

        // Checking if all costs are present
        Iterator<Entry<String, Integer>> iterator = costs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();

            if (!stealStuff(Material.getMaterial(entry.getKey()), p, entry.getValue(), false)) {
                p.sendMessage("You need " + entry.getValue() + " " + Material.getMaterial(entry.getKey()).name()
                        + " in your inventory to save a totem. Your current storage contains " +
                        getConfig().getInt(p.getName()) + " totems, maximum is "
                        + getConfig().getInt(CFG_MAX_STORAGE_KEY)
                        + ".");
                return false;
            }
        }

        // Stealing costs from inventory
        iterator = costs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
            stealStuff(Material.getMaterial(entry.getKey()), p, entry.getValue(), false);
        }
        // Adding to balance in config
        getConfig().set(p.getName(), getConfig().getInt(p.getName()) + 1);
        p.sendMessage("Adding one Totem of undying to your storage. Balance: " + getConfig().getInt(p.getName())
                + " Maximum: " + getConfig().getInt(CFG_MAX_STORAGE_KEY));
        saveConfig();
        return true;
    }

    // Checks if a certain amount of a Material exists in a players inventory. if
    // the boolean steal is true, it will delete the Material from the inventory as
    // well.
    // Returns true if it is possible to steal the given Material and amount.
    // Does not work if the Material is split onto multiple stacks in the inventory.
    // This could be fixed in the future.
    public boolean stealStuff(Material m, Player p, int amount, boolean steal) {
        // Don't do anything if trying to steal 0 of a Material, to avoid having to have
        // the Material in inventory anyway.
        if (amount <= 0)
            return true;
        int found = 0;
        int stolen = 0;
        PlayerInventory inv = p.getInventory();
        for (ItemStack is : inv.getContents()) {
            if (is != null) {
                if (is.getType().equals(m)) {
                    // Allows method to be used both as a check if it would be possible and to
                    // actually delete the items.
                    found += is.getAmount();
                    if (steal) {
                        is.setAmount(is.getAmount() - (amount - stolen));
                    }
                    if (found >= amount)
                        return true;
                }
            }
        }
        return false;
    }

    public void loadConfig() {
        getConfig().addDefault(CFG_MAX_STORAGE_KEY, CFG_MAX_STORAGE_DEFAULT);
        getConfig().addDefault(CFG_COSTS_KEY, CFG_COSTS_DEFAULT);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
