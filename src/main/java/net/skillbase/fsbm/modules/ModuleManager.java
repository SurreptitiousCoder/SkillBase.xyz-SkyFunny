package net.skillbase.fsbm.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.skillbase.fsbm.FSBM;
import net.skillbase.fsbm.Settings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ModuleManager implements IWorldAccess
{
    public ArrayList<Module> loadedModules;
    private static final String LINK_REGEX = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,2083}\\.){1,4}([a-zA-Z]){2,6}(\\/(([a-zA-Z-_\\/\\.0-9#:?=&;,]){0,2083})?){0,2083}?[^ \\n | \"]*)";
    private final ScheduledExecutorService scheduler;
    public static final boolean debug = false;
    private static OS os;
    private final Minecraft mc;
    
    public ModuleManager(final Minecraft mc) throws Exception {
        this.loadedModules = new ArrayList<Module>();
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.mc = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);

        ArrayList<Module> modules = new ArrayList<>();
        modules.add(new AntiAFK());
        modules.add(new AutoFishing());
        modules.add(new SeaCreatures());
        modules.add(new AutoForager());
        modules.add(new FlipperHelper());

        modules.forEach(module -> {
            module.initMM(this);
            module.setup(module.getClass().getSimpleName(), FSBM.loadedBefore);
            this.loadedModules.add(module);
            if (!FSBM.loadedBefore) {
                FSBM.moduleNameToKeybindingMap.put(module.getClass().getSimpleName(), module.getKeyBinding());
            }
            module.setEnabled(true);
        });
    }
    
    public void loadModule(final Module module) {
        module.initMM(this);
        module.setup(module.getName(), FSBM.loadedBefore);
        this.loadedModules.add(module);
    }
    
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }


    public ArrayList<Module> getLoadedModules() {
        return this.loadedModules;
    }
    
    public CustomModLoadingErrorDisplayException getError(final int startOffset, final String... messages) {
        return new CustomModLoadingErrorDisplayException() {
            public void initGui(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer) {
            }
            
            public void drawScreen(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer, final int mouseRelX, final int mouseRelY, final float tickTime) {
                errorScreen.drawDefaultBackground();
                int offset = startOffset;
                for (final String message : messages) {
                    errorScreen.drawCenteredString(fontRenderer, message, errorScreen.width / 2, offset, 16777215);
                    offset += 15;
                }
            }
        };
    }
    
    public void unload() {
        for (final Module module : this.loadedModules) {
            if (module.isEnabled()) {
                FSBM.getInstance().loadedBeforeRestartMods.add(module.getName());
                module.onDisable();
            }
            module.disable();
        }
        this.loadedModules.clear();
        this.loadedModules = null;
        MinecraftForge.EVENT_BUS.unregister((Object)this);
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) throws Exception {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (this.mc.thePlayer == null || this.mc.theWorld == null) {
            return;
        }
        for (final Module module : this.loadedModules) {
            if (!module.enabled) {
                continue;
            }
            module.onUpdate(event);
        }
    }
    
    @SubscribeEvent
    public void onPress(final InputEvent.KeyInputEvent event) {
        for (final Module module : this.loadedModules) {
            if (module.getKeyBinding() == null) {
                continue;
            }
            if (!module.getKeyBinding().isPressed()) {
                continue;
            }
            module.toggle();
        }
    }
    
    @SubscribeEvent
    public void onChat(final ClientChatReceivedEvent event) {
        for (final Module module : this.loadedModules) {
            if (module.isEnabled()) {
                module.onChat(event);
            }
        }
    }
    
    @SubscribeEvent
    public void onSound(final PlaySoundEvent event) {
        for (final Module module : this.loadedModules) {
            if (module.isEnabled()) {
                module.onSound(event);
            }
        }
    }
    
    @SubscribeEvent
    public void onWorldLoad(final WorldEvent.Load event) {
        if (event.world != null && !event.world.isRemote) {
            event.world.addWorldAccess((IWorldAccess)this);
        }
    }
    
    @SubscribeEvent
    public void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
    }
    
    private void setFinalStaticField(final Class<?> clazz, final String fieldName, final Object value) throws ReflectiveOperationException {
        final Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        final Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & 0xFFFFFFEF);
        field.set(this.mc.thePlayer.sendQueue, value);
    }
    
    @SubscribeEvent
    public void onPlayerUseItem(final PlayerInteractEvent event) {
        for (final Module module : this.loadedModules) {
            if (module.isEnabled()) {
                module.onPlayerUseItem(event);
            }
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        for (final Module module : this.loadedModules) {
            if (module.isEnabled()) {
                module.onConfigChanged(event);
            }
        }
    }
    
    public void markBlockForUpdate(final BlockPos pos) {
    }
    
    public void notifyLightSet(final BlockPos pos) {
    }
    
    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
    }
    
    public void playSound(final String soundName, final double x, final double y, final double z, final float volume, final float pitch) {
    }
    
    public void playSoundToNearExcept(final EntityPlayer except, final String soundName, final double x, final double y, final double z, final float volume, final float pitch) {
    }
    
    public void spawnParticle(final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, final double xOffset, final double yOffset, final double zOffset, final int... p_180442_15_) {
        if (Settings.config_autofish_enable && particleID == EnumParticleTypes.WATER_WAKE.getParticleID()) {
            for (final Module module : this.loadedModules) {
                if (module.getKeyBinding().isPressed()) {
                    module.onWaterWakeDetected(xCoord, yCoord, zCoord);
                }
            }
        }
    }
    
    public void onEntityAdded(final Entity entityIn) {
        if (Settings.config_autofish_enable && entityIn instanceof EntityXPOrb) {
            for (final Module module : this.loadedModules) {
                if (module.getKeyBinding().isPressed()) {
                    module.onXpOrbAdded(entityIn.posX, entityIn.posY, entityIn.posZ);
                }
            }
        }
    }
    
    public void onEntityRemoved(final Entity entityIn) {
    }
    
    public void playRecord(final String recordName, final BlockPos blockPosIn) {
    }
    
    public void broadcastSound(final int p_180440_1_, final BlockPos p_180440_2_, final int p_180440_3_) {
    }
    
    public void playAuxSFX(final EntityPlayer player, final int sfxType, final BlockPos blockPosIn, final int p_180439_4_) {
    }
    
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) {
    }
    
    static {
        ModuleManager.os = null;
    }
    
    public enum OS
    {
        WINDOWS, 
        LINUX, 
        MAC, 
        SOLARIS;
    }
}
