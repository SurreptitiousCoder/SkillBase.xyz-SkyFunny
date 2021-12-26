package net.skillbase.fsbm.modules;

import org.apache.commons.codec.digest.*;
import java.io.*;
import net.minecraft.client.*;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.client.gui.*;
import java.util.*;
import java.nio.file.*;
import java.util.function.*;
import net.minecraft.client.settings.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.*;

public class AntiAFK extends Module
{
    private final Random random;
    private boolean changed;
    private float savedYaw;
    private float savedPitch;
    private int emergencyTimer;
    private boolean rightPressed;
    private boolean leftPressed;
    private boolean forwardPressed;
    private boolean backPressed;
    private boolean attackPressed;
    private boolean usePressed;
    private boolean isOnIsland;
    private boolean emergency;
    private boolean emergencyChatUsed;
    private final String[] wut;
    private boolean verified;
    private long evacuateTimer;
    private boolean isInLimbo;
    
    public AntiAFK() {
        this.random = new Random();
        this.changed = true;
        this.savedYaw = 0.0f;
        this.savedPitch = 0.0f;
        this.emergencyTimer = 0;
        this.rightPressed = false;
        this.leftPressed = false;
        this.forwardPressed = false;
        this.backPressed = false;
        this.attackPressed = false;
        this.usePressed = false;
        this.isOnIsland = false;
        this.emergency = false;
        this.emergencyChatUsed = false;
        this.wut = new String[] { "wut?", "what just hapend", "bruh what", "pls stop", "admins i'm not a bot" };
        this.verified = false;
        this.evacuateTimer = 0L;
        this.isInLimbo = false;
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
        this.verified = true;
        System.out.println("Loaded: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void onEnable() {
        this.savedYaw = this.mc.thePlayer.rotationYaw;
        this.savedPitch = this.mc.thePlayer.rotationPitch;
    }
    
    @Override
    public void onDisable() {
        this.mc.thePlayer.rotationYaw = this.savedYaw;
        this.mc.thePlayer.rotationPitch = this.savedPitch;
    }
    
    private boolean isPlayerHoldingFishingRod(final Minecraft mc) {
        return !mc.isGamePaused() && mc.thePlayer != null && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod;
    }
    
    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) throws InterruptedException {
        if (!this.verified) {
            return;
        }
        if (this.emergency) {
            this.disableKeys();
            ++this.emergencyTimer;
            if (this.emergencyTimer > 20) {
                if (this.emergencyChatUsed) {
                    for (final Module module : this.moduleManager.loadedModules) {
                        module.setEnabled(false);
                    }
                    this.mc.theWorld.sendQuittingDisconnectingPacket();
                    this.mc.loadWorld(null);
                    this.mc.displayGuiScreen(this);
                    this.emergency = false;
                }
                else {
                    this.mc.thePlayer.sendChatMessage(this.wut[this.random.nextInt(this.wut.length)]);
                    this.emergencyChatUsed = true;
                }
                this.emergencyTimer = 0;
            }
            return;
        }
        if (this.evacuateTimer > 0L) {
            this.disableKeys();
            final long evacuateTimer = this.evacuateTimer + 1L;
            this.evacuateTimer = evacuateTimer;
            if (evacuateTimer > 400L) {
                this.mc.thePlayer.sendChatMessage("/is");
                this.isOnIsland = true;
                this.evacuateTimer = 0L;
                this.reanableKeys();
            }
            return;
        }
        final BlockPos pos = new BlockPos(this.mc.thePlayer.getPosition().getImmutable().add(0, -1, 0));
        final IBlockState blockState = this.mc.theWorld.getBlockState(pos);
        final String blockName = String.valueOf(Block.blockRegistry.getNameForObject(blockState.getBlock()));
        if (blockName.contains("bedrock") && !this.emergency) {
            this.emergency = true;
            return;
        }
        if (this.isInLimbo) {
            Thread.sleep(500L);
            this.mc.thePlayer.sendChatMessage("/is");
            this.isInLimbo = false;
            this.mc.thePlayer.sendChatMessage("/is");
            this.isOnIsland = true;
            this.evacuateTimer = 0L;
            this.reanableKeys();
        }
        boolean usingAutoFor = false;
        for (final Module module2 : this.moduleManager.loadedModules) {
            final String moduleName = module2.getClass().getSimpleName();
            if (moduleName.equals("AutoForager") || moduleName.contains("SeaCreatures")) {
                if (module2.isEnabled()) {
                    usingAutoFor = true;
                    break;
                }
                break;
            }
        }
        if (!usingAutoFor) {
            if (this.changed) {
                this.mc.thePlayer.rotationYaw = this.savedYaw;
                this.mc.thePlayer.rotationPitch = this.savedPitch;
                this.changed = false;
            }
            else {
                final int delimiter = this.isPlayerHoldingFishingRod(this.mc) ? 1 : 2;
                this.mc.thePlayer.rotationYaw = this.savedYaw + this.random.nextFloat() / delimiter * (this.random.nextBoolean() ? 1 : -1);
                this.mc.thePlayer.rotationPitch = this.savedPitch + this.random.nextFloat() / delimiter * (this.random.nextBoolean() ? 1 : -1);
                this.changed = true;
            }
        }
    }
    
    public void initGui() {
        final GuiButton button = new GuiButton(0, this.width / 2, this.height / 2 + 55, "Return to the multiplayer menu");
        button.xPosition -= button.width / 2;
        this.buttonList.add(button);
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.mc.fontRendererObj, "Admin protection ", this.width / 2, this.height / 2 - 30, 16711680);
        this.drawCenteredString(this.mc.fontRendererObj, "AntiAFK disconnected you from the server, because admins teleported you", this.width / 2, this.height / 2 - 15, 16711680);
        this.drawCenteredString(this.mc.fontRendererObj, "to the macro test box", this.width / 2, this.height / 2, 16711680);
        this.drawCenteredString(this.mc.fontRendererObj, "If you are still got banned, most likely admins have 100% evidence that you was macroing", this.width / 2, this.height / 2 + 40, 8553607);
        this.drawCenteredString(this.mc.fontRendererObj, "You are safe!", this.width / 2, this.height / 2 + 25, 65280);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    protected void actionPerformed(final GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
    }

    public void reanableKeys() {
        this.isOnIsland = true;
        this.evacuateTimer = 0L;
        if (this.rightPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindRight.getKeyCode(), true);
        }
        if (this.leftPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindLeft.getKeyCode(), true);
        }
        if (this.forwardPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), true);
        }
        if (this.backPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindBack.getKeyCode(), true);
        }
        if (this.attackPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindAttack.getKeyCode(), true);
        }
        if (this.usePressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
        }
    }
    
    public void disableKeys() {
        if (this.rightPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindRight.getKeyCode(), false);
        }
        if (this.leftPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindLeft.getKeyCode(), false);
        }
        if (this.forwardPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);
        }
        if (this.backPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindBack.getKeyCode(), false);
        }
        if (this.attackPressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }
        if (this.usePressed) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }
    
    @Override
    public void onChat(final ClientChatReceivedEvent event) {
        final String text = event.message.getUnformattedText().toLowerCase();
        if (text.contains("evacuating to hub") && this.enabled) {
            this.saveBinds();
            ++this.evacuateTimer;
        }
        else if (text.contains("you were spawned in limbo") && this.enabled) {
            this.saveBinds();
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mc.thePlayer.sendChatMessage("/lobby skyblock");
            try {
                Thread.sleep(2000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mc.thePlayer.sendChatMessage("/play skyblock");
            ++this.evacuateTimer;
        }
        else if (text.contains("couldn't warp you!") && this.isOnIsland) {
            this.isOnIsland = false;
            this.saveBinds();
            ++this.evacuateTimer;
        }
        else if (text.contains("please don't spam the command")) {
            FMLCommonHandler.instance().exitJava(0, true);
        }
        else if (text.contains("an exception occured in your connection")) {
            this.mc.thePlayer.sendChatMessage("/play skyblock");
            this.saveBinds();
            ++this.evacuateTimer;
        }
    }
    
    @Override
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("fsbm.antiafk.toggle", 37, "fsbm.category");
    }
    
    public void saveBinds() {
        this.rightPressed = this.mc.gameSettings.keyBindRight.isKeyDown();
        this.leftPressed = this.mc.gameSettings.keyBindLeft.isKeyDown();
        this.forwardPressed = this.mc.gameSettings.keyBindForward.isKeyDown();
        this.backPressed = this.mc.gameSettings.keyBindBack.isKeyDown();
        this.attackPressed = this.mc.gameSettings.keyBindAttack.isKeyDown();
        this.usePressed = this.mc.gameSettings.keyBindUseItem.isKeyDown();
    }
}
