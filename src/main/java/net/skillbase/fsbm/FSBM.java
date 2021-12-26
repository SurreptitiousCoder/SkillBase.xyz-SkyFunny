package net.skillbase.fsbm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.skillbase.fsbm.command.SkyFunnyCommand;
import net.skillbase.fsbm.command.UpdateCommand;
import net.skillbase.fsbm.modules.ModuleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Mod(modid = "skyfunnyloader", version = "1.4", acceptedMinecraftVersions = "[1.8.9]")
public class FSBM
{
    public static final String MODID = "skyfunnyloader";
    public static final String VERSION = "1.4";
    private final Minecraft mc;
    private ModuleManager moduleManager;
    private static Configuration config;
    private static FSBM instance;
    public static boolean loadedBefore;
    public static HashMap<String, KeyBinding> moduleNameToKeybindingMap;
    public final ArrayList<String> loadedBeforeRestartMods;
    
    public FSBM() {
        this.mc = Minecraft.getMinecraft();
        this.loadedBeforeRestartMods = new ArrayList<String>();
    }
    
    public static Configuration getConfig() {
        return FSBM.config;
    }
    
    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        FSBM.instance = this;
        (FSBM.config = new Configuration(event.getSuggestedConfigurationFile())).renameProperty("general", "Fast Fishing", "Enable Fast Fishing");
        FSBM.config.renameProperty("general", "Enable Entity Clear Protection", "Enable Entity Clear Protection");
        FSBM.config.setCategoryPropertyOrder("general", new ArrayList<String>(Arrays.asList("Enable AutoFish", "Enable MultiRod", "Enable Break Protection", "Re-Cast Delay", "Enable Entity Clear Protection", "Enable Aggressive Bite Detection", "Handle Problems", "Enable Fast Fishing")));
        Settings.syncConfig();
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) throws Exception {
        this.load();
        ClientCommandHandler.instance.registerCommand(new SkyFunnyCommand());
        ClientCommandHandler.instance.registerCommand(new UpdateCommand());

    }
    
    public void load() throws Exception {
        this.moduleManager = new ModuleManager(this.mc);
    }
    
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }
    
    public static FSBM getInstance() {
        return FSBM.instance;
    }
    
    public void unloadManager() {
        this.moduleManager.unload();
        this.moduleManager = null;
        System.gc();
    }
    
    static {
        FSBM.loadedBefore = false;
        FSBM.moduleNameToKeybindingMap = new HashMap<>();
    }
}
