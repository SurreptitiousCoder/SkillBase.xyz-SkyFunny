package net.skillbase.fsbm.modules;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.realms.RealmsMth;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SugarCaneFarmer extends Module
{
    private boolean forward;
    private long stuckTimer;
    private long cooldown;
    private int farmingMode;
    private final KeyBinding keyBindingRight;
    private final KeyBinding keyBindingAttack;
    private final KeyBinding keyBindingBack;
    private final KeyBinding keyBindingLeft;
    private final KeyBinding keyBindingUse;
    private float pointYaw;
    private boolean setupDone;
    private boolean firstUpdate;
    private final boolean debug = false;
    private boolean verified;
    GuiButton sugarCane;
    GuiButton melons;
    GuiButton other;
    GuiCheckBox holdPCM;
    GuiCheckBox holdLCM;
    
    public SugarCaneFarmer() {
        this.forward = false;
        this.stuckTimer = 0L;
        this.cooldown = 0L;
        this.farmingMode = 0;
        this.keyBindingRight = FMLClientHandler.instance().getClient().gameSettings.keyBindRight;
        this.keyBindingAttack = FMLClientHandler.instance().getClient().gameSettings.keyBindAttack;
        this.keyBindingBack = FMLClientHandler.instance().getClient().gameSettings.keyBindBack;
        this.keyBindingLeft = FMLClientHandler.instance().getClient().gameSettings.keyBindLeft;
        this.keyBindingUse = FMLClientHandler.instance().getClient().gameSettings.keyBindUseItem;
        this.pointYaw = 0.0f;
        this.setupDone = false;
        this.firstUpdate = false;
        this.verified = false;
    }
    
    public String createSha1(final File file) throws IOException {
        final InputStream fis = new FileInputStream(file);
        final String digest = DigestUtils.sha1Hex(fis);
        fis.close();
        return digest;
    }
    
    @Override
    public void setup(final String name, final boolean loadedBefore) {
        super.setup(name, loadedBefore);
        this.isGui = true;
        this.verified = true;
        System.out.println("Loaded: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void onEnable() {
        this.setupDone = false;
        this.pointYaw = RealmsMth.wrapDegrees(this.mc.thePlayer.rotationYaw + 180.0f);
    }
    
    @Override
    public void onDisable() {
        this.cooldown = 0L;
        this.stuckTimer = 0L;
        KeyBinding.setKeyBindState(this.keyBindingBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingUse.getKeyCode(), false);
    }
    
    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) {
        if (!this.verified) {
            return;
        }
        if (!this.setupDone) {
            return;
        }
        if (!this.firstUpdate) {
            this.sendDebug("Keyboard packet");
            KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
            KeyBinding.setKeyBindState(this.keyBindingAttack.getKeyCode(), this.holdLCM.isChecked());
            KeyBinding.setKeyBindState(this.keyBindingUse.getKeyCode(), this.holdPCM.isChecked());
            this.firstUpdate = true;
        }
        if (this.holdPCM.isChecked() && !this.keyBindingAttack.isKeyDown()) {
            this.sendDebug("Keyboard packet");
            KeyBinding.setKeyBindState(this.keyBindingUse.getKeyCode(), true);
        }
        if (this.holdLCM.isChecked() && !this.keyBindingAttack.isKeyDown()) {
            this.sendDebug("Keyboard packet");
            KeyBinding.setKeyBindState(this.keyBindingAttack.getKeyCode(), true);
        }
        if (this.farmingMode == 0) {
            if (this.forward && !this.keyBindingBack.isKeyDown()) {
                this.sendDebug("Keyboard packet");
                KeyBinding.setKeyBindState(this.keyBindingBack.getKeyCode(), true);
            }
            else if (!this.forward && !this.keyBindingRight.isKeyDown()) {
                this.sendDebug("Keyboard packet");
                KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
            }
            if (Math.abs(this.mc.thePlayer.motionX) <= 0.1 && Math.abs(this.mc.thePlayer.motionZ) <= 0.1 && this.cooldown == 0L) {
                ++this.stuckTimer;
                if (this.stuckTimer > 10L) {
                    if (this.forward) {
                        this.sendDebug("Keyboard packet");
                        KeyBinding.setKeyBindState(this.keyBindingBack.getKeyCode(), false);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
                    }
                    else {
                        this.sendDebug("Keyboard packet");
                        KeyBinding.setKeyBindState(this.keyBindingBack.getKeyCode(), true);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
                    }
                    this.mc.thePlayer.setSprinting(true);
                    this.forward = !this.forward;
                    this.stuckTimer = 0L;
                    this.cooldown = 60L;
                }
                return;
            }
        }
        else if (this.farmingMode == 1) {
            if (this.forward && !this.keyBindingLeft.isKeyDown()) {
                KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), true);
            }
            else if (!this.forward && !this.keyBindingRight.isKeyDown()) {
                KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
            }
            if (Math.abs(this.mc.thePlayer.motionX) <= 0.1 && Math.abs(this.mc.thePlayer.motionZ) <= 0.1 && this.cooldown == 0L) {
                ++this.stuckTimer;
                if (this.stuckTimer > 10L) {
                    if (this.forward) {
                        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), false);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
                    }
                    else {
                        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), true);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
                    }
                    this.mc.thePlayer.setSprinting(true);
                    this.forward = !this.forward;
                    this.stuckTimer = 0L;
                    this.cooldown = 60L;
                }
                return;
            }
        }
        else {
            if (this.forward && !this.keyBindingBack.isKeyDown()) {
                this.sendDebug("Keyboard packet");
                KeyBinding.setKeyBindState(this.keyBindingBack.getKeyCode(), true);
            }
            else if (!this.forward && !this.keyBindingRight.isKeyDown()) {
                this.sendDebug("Keyboard packet");
                KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
            }
            if (Math.abs(this.mc.thePlayer.motionX) <= 0.1 && Math.abs(this.mc.thePlayer.motionZ) <= 0.1 && this.cooldown == 0L) {
                ++this.stuckTimer;
                this.mc.thePlayer.rotationYaw = this.pointYaw;
                if (this.stuckTimer > 10L) {
                    if (this.forward) {
                        this.sendDebug("Keyboard packet");
                        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), false);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
                    }
                    else {
                        this.sendDebug("Keyboard packet");
                        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), true);
                        KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
                    }
                    this.mc.thePlayer.setSprinting(true);
                    this.pointYaw = RealmsMth.wrapDegrees(this.pointYaw + 180.0f);
                    this.forward = !this.forward;
                    this.stuckTimer = 0L;
                    this.cooldown = 60L;
                }
                return;
            }
        }
        if (this.cooldown > 0L) {
            --this.cooldown;
        }
    }
    
    private void sendDebug(final Object msg) {
    }
    

    @Override
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("fsbm.sugarcanefarmer.toggle", 36, "fsbm.category");
    }
    
    public void initGui() {
        super.initGui();
        this.sugarCane = new GuiButton(0, this.width / 2, 70, "Sugar Cane");
        this.other = new GuiButton(1, this.width / 2, 90, "Normal (Like netherwarts)");
        this.melons = new GuiButton(2, this.width / 2, 110, "Melons/Pumpkins (BETA TEST)");
        this.melons.xPosition -= this.melons.width / 2;
        this.sugarCane.xPosition -= this.sugarCane.width / 2;
        this.other.xPosition -= this.other.width / 2;
        this.holdPCM = new GuiCheckBox(3, this.width / 2, 140, "Hold PCM", false);
        this.holdLCM = new GuiCheckBox(4, this.width / 2 + 60, 140, "Hold LCM", true);
        this.buttonList.add(this.sugarCane);
        this.buttonList.add(this.other);
        this.buttonList.add(this.melons);
        this.buttonList.add(this.holdLCM);
        this.buttonList.add(this.holdPCM);
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.mc.fontRendererObj, "Select farming mode", this.width / 2, 60, 16777215);
    }
    
    protected void actionPerformed(final GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button instanceof GuiCheckBox) {
            return;
        }
        if (button.id == this.sugarCane.id) {
            this.farmingMode = 0;
        }
        else if (button.id == this.other.id) {
            this.farmingMode = 1;
        }
        else {
            this.farmingMode = 2;
        }
        this.setupDone = true;
        this.mc.thePlayer.closeScreen();
    }
    
    public void onGuiClosed() {
        super.onGuiClosed();
        this.enabled = this.setupDone;
    }
    
    public boolean doesGuiPauseGame() {
        return true;
    }
}
