package net.skillbase.fsbm.modules;

import net.minecraft.client.gui.*;
import net.minecraft.client.settings.*;
import net.minecraft.client.*;
import net.skillbase.fsbm.*;
import net.minecraftforge.fml.client.registry.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.fml.client.event.*;

public abstract class Module extends GuiScreen
{
    private KeyBinding keyBinding;
    protected Minecraft mc;
    protected boolean enabled;
    protected String name;
    public boolean isGui;
    protected boolean errorOnEnable;
    protected String errorMessage;
    protected boolean disabled;
    protected ModuleManager moduleManager;
    
    public Module() {
        this.enabled = false;
        this.isGui = false;
        this.errorOnEnable = false;
        this.errorMessage = null;
        this.disabled = false;
    }
    
    public void setup(final String name, final boolean newKey) {
        if (newKey) {
            this.keyBinding = FSBM.moduleNameToKeybindingMap.get(name);
        }
        else {
            this.keyBinding = this.registerKeybinding();
            if (this.keyBinding != null) {
                ClientRegistry.registerKeyBinding(this.keyBinding);
            }
        }
        this.mc = Minecraft.getMinecraft();
        this.name = name;
        this.init();
    }
    
    public void initMM(final ModuleManager mm) {
        if (this.moduleManager != null) {
            return;
        }
        this.moduleManager = mm;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void disable() {
        this.disabled = true;
    }
    
    public void init() {
    }
    
    public void init(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public void onEnable() {
    }
    
    public void onDisable() {
    }
    
    public abstract void onUpdate(final TickEvent.ClientTickEvent p0) throws Exception;
    

    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) throws Exception {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!this.enabled) {
            return;
        }
        if (this.mc.thePlayer == null || this.mc.theWorld == null) {
            return;
        }
        if (this.disabled) {
            return;
        }
        this.onUpdate(event);
    }
    
    protected abstract KeyBinding registerKeybinding();
    
    public KeyBinding getKeyBinding() {
        return this.keyBinding;
    }
    
    public void toggle() {
        if (this.errorOnEnable && this.errorMessage != null) {
            this.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(this.errorMessage));
        }
        else {
            if (!this.disabled) {
                this.enabled = !this.enabled;
            }
            if (this.enabled) {
                if (this.isGui) {
                    this.mc.displayGuiScreen((GuiScreen)this);
                }
                this.onEnable();
            }
            else {
                this.onDisable();
            }
            if (this.mc.thePlayer == null || this.mc.theWorld == null) {
                return;
            }
            if (this.name == null) {
                this.name = this.getClass().getSimpleName();
            }
            this.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentTranslation("fsbm.module." + (this.enabled ? "enabled" : "disabled"), new Object[] { new ChatComponentText(this.name) }));
        }
    }
    
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public final void errorEnabling(final String error) {
        this.setEnabled(false);
        this.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(error));
    }
    
    public final void onError(final String error) {
        this.errorOnEnable = true;
        this.errorMessage = error;
    }
    
    public void onChat(final ClientChatReceivedEvent event) {
    }
    
    public void onSound(final PlaySoundEvent event) {
    }
    
    public void onWorldLoad(final WorldEvent.Load event) {
    }
    
    public void onPlayerUseItem(final PlayerInteractEvent event) {
    }
    
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
    }
    
    public void onWaterWakeDetected(final double x, final double y, final double z) {
    }
    
    public void onXpOrbAdded(final double x, final double y, final double z) {
    }
}
