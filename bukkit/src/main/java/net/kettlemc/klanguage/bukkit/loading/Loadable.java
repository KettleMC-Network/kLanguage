package net.kettlemc.klanguage.bukkit.loading;

public interface Loadable {

    default void onLoad() {
    }

    default void onEnable() {
    }

    default void onDisable() {
    }
}
