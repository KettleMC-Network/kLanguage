package net.kettlemc.klanguage.bukkit;

import net.kettlemc.klanguage.bukkit.loading.Loadable;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginLoader extends JavaPlugin {

    Loadable plugin;

    @Override
    public void onLoad() {
        this.plugin = new KLanguageBukkit(this);
        plugin.onLoad();
    }

    @Override
    public void onEnable() {
        plugin.onEnable();
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }
}
