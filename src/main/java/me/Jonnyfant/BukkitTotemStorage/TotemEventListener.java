package me.Jonnyfant.BukkitTotemStorage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TotemEventListener implements Listener {
    private JavaPlugin plugin;

    public TotemEventListener(JavaPlugin p) {
        plugin=p;
    }

    @EventHandler
    public void onTotemActivate(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player && event.isCancelled()) {
            Player p = (Player) event.getEntity();
            plugin.reloadConfig();
            if (plugin.getConfig().getInt(event.getEntity().getName()) > 0) {
                event.setCancelled(false);
                plugin.getConfig().set(p.getName(),plugin.getConfig().getInt(p.getName())-1);
                plugin.saveConfig();
                plugin.reloadConfig();
                p.sendMessage("You just used a Totem of Undying from your Totem Storage. You now have " + plugin.getConfig().getInt(p.getName()) + " Totems left.");
            }
        }
    }
}
