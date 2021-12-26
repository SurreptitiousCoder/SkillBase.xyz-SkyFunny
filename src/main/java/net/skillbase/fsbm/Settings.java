package net.skillbase.fsbm;

public class Settings
{
    public static boolean config_autofish_enable;
    public static boolean config_autofish_multirod;
    public static boolean config_autofish_preventBreak;
    public static boolean config_autofish_entityClearProtect;
    public static int config_autofish_recastDelay;
    public static boolean config_autofish_aggressiveBiteDetection;
    public static boolean config_autofish_handleProblems;
    
    public static void syncConfig() {
        Settings.config_autofish_enable = FSBM.getConfig().getBoolean("Enable AutoFish", "general", true, "Automatically reel in and re-cast when a fish nibbles the hook. If set to false, all other AutoFish functionality is disabled.");
        Settings.config_autofish_multirod = FSBM.getConfig().getBoolean("Enable MultiRod", "general", false, "Automatically switch to a new fishing rod when the current rod breaks, if one is available in the hotbar.");
        Settings.config_autofish_preventBreak = FSBM.getConfig().getBoolean("Enable Break Protection", "general", false, "Stop fishing or switch to a new rod just before the current rod breaks.  Useful if you want to repair your enchanted fishing rods.");
        Settings.config_autofish_entityClearProtect = FSBM.getConfig().getBoolean("Enable Entity Clear Protection", "general", true, "When playing on a server, re-cast after the server clears entities.  Useful if you are playing on a server that periodically deletes all entities (including fishing hooks) from the world, which causes you to stop fishing.");
        Settings.config_autofish_recastDelay = FSBM.getConfig().getInt("Re-Cast Delay", "general", 1, 1, 10, "Time (in seconds) to wait before automatically re-casting. Increase this value if server lag causes re-casting to fail.");
        Settings.config_autofish_aggressiveBiteDetection = FSBM.getConfig().getBoolean("Enable Aggressive Bite Detection", "general", true, "When playing on a server, be more aggressive about detecting fish bites.  Improves multiplayer bite detection from ~85% to ~95%, but false positives will be more likely, especially if other players are fishing very close by.");
        Settings.config_autofish_handleProblems = FSBM.getConfig().getBoolean("Handle Problems", "general", false, "[UNSAFE] Re-cast when problems detected (e.g. if not in water or if a MOB has been hooked). If enabled, non-fishing use of the fishing rod (e.g. intentionally hooking MOBs) will be affected.");
        if (FSBM.getConfig().hasChanged()) {
            FSBM.getConfig().save();
        }
    }
}
