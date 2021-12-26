package net.skillbase.fsbm.modules;

import net.minecraft.util.*;
import net.skillbase.fsbm.util.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraft.inventory.*;
import net.minecraft.client.settings.*;

import java.util.concurrent.*;

import net.minecraft.network.play.client.*;
import net.minecraft.network.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.client.gui.*;
import net.minecraft.item.*;
import net.minecraft.init.*;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.function.*;
import java.util.*;

import net.minecraftforge.fml.common.*;
import org.apache.commons.codec.digest.*;

import java.io.*;

public class AutoForager extends Module {
    private float savedYaw;
    private float savedPitch;
    private final double firstBlockYaw = 8.0;
    private final double secondBlockYaw = 14.0;
    private final double thirdBlockYaw = -10.0;
    private final double fourthBlockYaw = -13.0;
    private final double firstBlockPitch = 28.0;
    private final double secondBlockPitch = 38.0;
    private final double thirdBlockPitch = 28.0;
    private final double fourthBlockPitch = 38.0;
    private int actionTimer;
    private short currentBlock;
    private int nextDelay;
    private boolean needsToBreakBlock;
    private boolean needsToBone;
    private boolean haveSaplings;
    private boolean haveBone;
    private final int initialDelay = 4;
    private final boolean noVersion;
    private boolean verified;

    public AutoForager() {
        this.savedYaw = 0.0f;
        this.savedPitch = 0.0f;
        this.actionTimer = 0;
        this.currentBlock = 0;
        this.nextDelay = 0;
        this.needsToBreakBlock = false;
        this.needsToBone = false;
        this.haveSaplings = true;
        this.haveBone = true;
        this.noVersion = false;
    }

    private void setBoneLocation() {
        this.yaw(-8.0);
        this.pitch(17.0);
    }

    @Override
    public void onEnable() {
        this.haveBone = true;
        this.haveSaplings = true;
        if (!this.verified) {
            this.mc.thePlayer.addChatMessage(new ChatComponentText("Module isn't verified so you can't enable it for security reasons"));
        }
        if (this.noVersion) {
            this.mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This module requires latest version of SkyFunny loader"));
        }
        super.onEnable();
        this.savedYaw = this.mc.thePlayer.rotationYaw;
        this.savedPitch = this.mc.thePlayer.rotationPitch;
        this.needsToBreakBlock = false;
        this.needsToBone = false;
        this.currentBlock = 0;
        final Pair<Double, Double> _currentRotation = this.getBlockByNumber((short) 0);
        this.yaw(_currentRotation.getKey());
        this.pitch(_currentRotation.getValue());
    }

    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) throws Exception {
        if (!this.verified) {
            return;
        }
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        if (!this.haveBone) {
            this.mc.ingameGUI.drawCenteredString(this.mc.fontRendererObj, "NO BONEMEAL!", this.mc.displayWidth / 2, 70, 16711680);
            if (this.runScan(this.mc.thePlayer.inventoryContainer.inventorySlots, slot -> this.isBonemeal(slot.getStack()))) {
                this.haveBone = true;
            }
            return;
        }
        if (!this.haveSaplings) {
            this.mc.ingameGUI.drawCenteredString(this.mc.fontRendererObj, "NO SAPLINGS!", this.mc.displayWidth / 2, 85, 16711680);
            if (this.runScan(this.mc.thePlayer.inventoryContainer.inventorySlots, slot -> this.isSapling(slot.getStack()))) {
                this.haveSaplings = true;
            }
            return;
        }
        if (this.actionTimer < this.nextDelay) {
            ++this.actionTimer;
            return;
        }
        this.actionTimer = 0;
        if (this.nextDelay != 4) {
            this.sendDebug("Close packet: " + this.nextDelay + " " + 4);
            this.nextDelay = 4;
            if (this.mc.currentScreen != null) {
                this.mc.thePlayer.closeScreen();
                this.mc.setIngameFocus();
                this.mc.inGameHasFocus = true;
            }
        }
        if (this.needsToBreakBlock) {
            this.needsToBreakBlock = false;
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindAttack.getKeyCode(), true);

            this.moduleManager.getScheduler().schedule(() -> {
                KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindAttack.getKeyCode(), false);
                this.currentBlock = 0;
                final Pair<Double, Double> _currentRotation = this.getBlockByNumber((short) 0);
                this.yaw(_currentRotation.getKey().floatValue());
                this.pitch(_currentRotation.getValue().floatValue());
                return;
            }, 700L, TimeUnit.MILLISECONDS);
            this.nextDelay = 18;
            return;
        }
        if (this.needsToBone) {
            if (!this.isBonemeal(this.mc.thePlayer.inventory.mainInventory[1])) {
                this.getBoneSlot();
                this.nextDelay = 20;
                return;
            }
            this.rightClick();
            this.needsToBreakBlock = true;
            this.needsToBone = false;
            this.nextDelay = 5;
            this.mc.thePlayer.inventory.currentItem = 2;
        } else {
            final Pair<Double, Double> currentRotation = this.getBlockByNumber(this.currentBlock);
            this.yaw(currentRotation.getKey().floatValue());
            this.pitch(currentRotation.getValue().floatValue());
            ++this.currentBlock;
            this.mc.thePlayer.inventory.currentItem = 0;
            if (!this.isSapling(this.mc.thePlayer.inventory.mainInventory[0])) {
                this.nextDelay = 20;
                this.getSaplingSlot();
                return;
            }
            this.rightClick();
            if (this.currentBlock > 4) {
                final Pair<Double, Double> _currentRotation2 = this.getBlockByNumber((short) 3);
                this.yaw(_currentRotation2.getKey().floatValue());
                this.pitch(_currentRotation2.getValue().floatValue());
                this.mc.thePlayer.inventory.currentItem = 1;
                this.needsToBone = true;
            }
        }
    }

    private void getSaplingSlot() {
        this.sendDebug("Sapling");
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        final GuiInventory inv = new GuiInventory(this.mc.thePlayer);
        this.mc.displayGuiScreen(inv);
        boolean found = false;
        for (final Slot slot : inv.inventorySlots.inventorySlots) {
            if (this.isSapling(slot.getStack())) {
                found = true;
                this.mc.playerController.windowClick(0, slot.slotNumber, 0, 2, this.mc.thePlayer);
                break;
            }
        }
        if (found) {
            this.haveSaplings = true;
            this.nextDelay = 30;
            return;
        }
        this.haveSaplings = false;
    }

    private boolean runScan(final List<Slot> inventorySlots, final Predicate<Slot> filter) {
        return inventorySlots.stream().anyMatch(filter);
    }

    private void sendDebug(final Object msg) {
    }

    private void getBoneSlot() {
        this.sendDebug("bone");
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        final GuiInventory inv = new GuiInventory(this.mc.thePlayer);
        this.mc.displayGuiScreen(inv);
        boolean found = false;
        for (final Slot slot : inv.inventorySlots.inventorySlots) {
            if (this.isBonemeal(slot.getStack())) {
                found = true;
                this.mc.playerController.windowClick(0, slot.slotNumber, 1, 2, this.mc.thePlayer);
                break;
            }
        }
        if (found) {
            this.haveBone = true;
            this.nextDelay = 30;
            return;
        }
        this.haveBone = false;
    }

    private boolean isSapling(final ItemStack itemStack) {
        return itemStack != null && itemStack.getItem().equals(Item.getItemFromBlock(Blocks.sapling));
    }

    private boolean isBonemeal(final ItemStack itemStack) {
        return itemStack != null && itemStack.getItem().equals(Items.dye);
    }

    private void rightClick() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            final Method method = this.mc.getClass().getDeclaredMethod("rightClickMouse", new Class[0]);
            method.setAccessible(true);
            method.invoke(this.mc);
        } catch (Exception exception) {
            final Method method2 = this.mc.getClass().getDeclaredMethod("func_147121_ag", new Class[0]);
            method2.setAccessible(true);
            method2.invoke(this.mc);
        }
    }

    public Pair<Double, Double> getBlockByNumber(final short number) {
        switch (number) {
            case 0: {
                return new Pair<Double, Double>(8.0, 28.0);
            }
            case 1: {
                return new Pair<Double, Double>(14.0, 38.0);
            }
            case 2: {
                return new Pair<>(-10.0, 28.0);
            }
            case 3: {
                return new Pair<Double, Double>(-13.0, 38.0);
            }
            default: {
                return new Pair<Double, Double>((double) this.savedYaw, (double) this.savedPitch);
            }
        }
    }

    @Override
    public void setup(final String name, final boolean loadedBefore) {
        super.setup(name, loadedBefore);
        this.verified = true;
        System.out.println("Loaded: " + this.getClass().getSimpleName());
    }

    public String createSha1(final File file) throws IOException {
        final InputStream fis = new FileInputStream(file);
        final String digest = DigestUtils.sha1Hex(fis);
        fis.close();
        return digest;
    }

    @Override
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("fsbm.autoforager.toggle", 35, "fsbm.category");
    }

    private void yaw(final double yaw) {
        this.mc.thePlayer.rotationYaw = (float) yaw;
    }

    private void pitch(final double pitch) {
        this.mc.thePlayer.rotationPitch = (float) pitch;
    }
}
