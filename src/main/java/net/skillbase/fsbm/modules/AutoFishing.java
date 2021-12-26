package net.skillbase.fsbm.modules;

import org.apache.commons.codec.digest.*;
import java.io.*;
import net.minecraftforge.client.event.sound.*;
import net.skillbase.fsbm.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.fml.client.event.*;
import net.skillbase.fsbm.autofishing.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.*;
import net.minecraft.entity.projectile.*;
import net.minecraftforge.fml.common.gameevent.*;
import java.util.*;
import java.nio.file.*;
import java.util.function.*;
import net.skillbase.fsbm.util.*;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.util.*;
import net.minecraft.server.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;

public class AutoFishing extends Module
{
    private boolean notificationShownToPlayer;
    private long castScheduledAt;
    private long startedReelDelayAt;
    private long startedCastDelayAt;
    private boolean isFishing;
    private long closeWaterWakeDetectedAt;
    private long xpLastAddedAt;
    private long closeBobberSplashDetectedAt;
    private final Random random;
    private long lastFishEntityServerY;
    private long hookFirstInWaterAt;
    private boolean verified;
    
    public AutoFishing() {
        this.notificationShownToPlayer = false;
        this.castScheduledAt = 0L;
        this.startedReelDelayAt = 0L;
        this.startedCastDelayAt = 0L;
        this.isFishing = false;
        this.closeWaterWakeDetectedAt = 0L;
        this.xpLastAddedAt = 0L;
        this.closeBobberSplashDetectedAt = 0L;
        this.random = new Random();
        this.lastFishEntityServerY = 0L;
        this.hookFirstInWaterAt = 0L;
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
        this.verified = true;
        System.out.println("Loaded: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void onSound(final PlaySoundEvent event) {
        if (Settings.config_autofish_enable && "random.splash".equals(event.name)) {
            this.onBobberSplashDetected(event.sound.getXPosF(), event.sound.getYPosF(), event.sound.getZPosF());
        }
    }
    
    @Override
    public void onPlayerUseItem(final PlayerInteractEvent event) {
        if (Settings.config_autofish_enable && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.world.isRemote) {
            this._onPlayerUseItem();
        }
    }
    
    @Override
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals("fsbm")) {
            Settings.syncConfig();
        }
    }
    
    @Override
    public void toggle() {
        if (this.mc.currentScreen instanceof OptionsGUI) {
            return;
        }
        this.mc.displayGuiScreen(new OptionsGUI(this.mc.currentScreen));
    }
    
    @Override
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("fsbm.autofishing.options", 24, "fsbm.category");
    }
    
    public void onBobberSplashDetected(final float x, final float y, final float z) {
        if (this.playerHookInWater((EntityPlayer)this.mc.thePlayer)) {
            final EntityFishHook hook = this.mc.thePlayer.fishEntity;
            final double xzDistanceFromHook = hook.getDistanceSq((double)x, hook.posY, (double)z);
            if (xzDistanceFromHook <= 2.8) {
                this.closeBobberSplashDetectedAt = this.mc.theWorld.getTotalWorldTime();
            }
        }
    }
    
    public void _onPlayerUseItem() {
        if (this.playerIsHoldingRod()) {
            if (!this.rodIsCast()) {
                this.resetReelDelay();
                this.resetCastSchedule();
                this.resetBiteTracking();
                this.isFishing = true;
                this.startCastDelay();
            }
            else {
                this.isFishing = false;
                this.resetCastDelay();
            }
        }
    }
    
    @Override
    public void onWaterWakeDetected(final double x, final double y, final double z) {
        if (!this.enabled) {
            return;
        }
        if (this.mc != null && this.playerHookInWater((EntityPlayer)this.mc.thePlayer)) {
            final EntityFishHook hook = this.mc.thePlayer.fishEntity;
            final double distanceFromHook = new BlockPos(x, y, z).distanceSq(hook.posX, hook.posY, hook.posZ);
            if (distanceFromHook <= 1.0 && this.closeWaterWakeDetectedAt <= 0L) {
                this.closeWaterWakeDetectedAt = this.mc.theWorld.getTotalWorldTime();
            }
        }
    }
    
    @Override
    public void onXpOrbAdded(final double x, final double y, final double z) {
        if (!this.enabled()) {
            return;
        }
        if (this.mc.thePlayer != null) {
            final double distanceFromPlayer = this.mc.thePlayer.getPosition().distanceSq(x, y, z);
            if (distanceFromPlayer < 2.0) {
                this.xpLastAddedAt = this.mc.theWorld.getTotalWorldTime();
            }
        }
    }
    
    public boolean enabled() {
        return Settings.config_autofish_enable;
    }
    
    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) {
        if (!this.verified) {
            return;
        }
        if (!this.enabled()) {
            return;
        }
        if (!this.mc.isGamePaused() && this.mc.thePlayer != null) {
            if (!this.notificationShownToPlayer) {
                this.showNotificationToPlayer();
            }
            if (this.playerIsHoldingRod() || this.waitingToRecast()) {
                if ((this.hookHasBeenInWaterLongEnough() && !this.isDuringReelDelay() && this.isFishBiting()) || this.somethingSeemsWrong()) {
                    System.out.println("reel in");
                    this.startReelDelay();
                    this.reelIn();
                    this.scheduleNextCast();
                }
                else if (this.isTimeToCast()) {
                    System.out.println("Time to cast");
                    if (!this.rodIsCast()) {
                        if (this.needToSwitchRods()) {
                            this.tryToSwitchRods();
                        }
                        if (this.playerCanCast()) {
                            this.startFishing();
                        }
                    }
                    this.resetReelDelay();
                    this.resetCastSchedule();
                    this.resetBiteTracking();
                }
                if (Settings.config_autofish_entityClearProtect) {
                    this.checkForEntityClear();
                }
                this.checkForMissedBite();
            }
            else {
                this.isFishing = false;
            }
        }
    }

    private boolean isFishBiting() {
        final EntityPlayer serverPlayerEntity = this.getServerPlayerEntity();
        if (serverPlayerEntity != null) {
            try {
                return this.isFishBiting_fromServerEntity(serverPlayerEntity);
            }
            catch (Exception e) {
                return this.isFishBiting_fromClientWorld();
            }
        }
        return this.isFishBiting_fromClientWorld();
    }
    
    private boolean isFishBiting_fromServerEntity(final EntityPlayer serverPlayerEntity) throws NumberFormatException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final EntityFishHook serverFishEntity = serverPlayerEntity.fishEntity;
        final int ticksCatchable = ReflectionUtils.getPrivateIntFieldFromObject(serverFishEntity, "ticksCatchable", "field_146045_ax");
        return ticksCatchable > 0;
    }
    
    private boolean isFishBiting_fromClientWorld() {
        return this.isFishBiting_fromMovement() || this.isFishBiting_fromBobberSound() || this.isFishBiting_fromWaterWake() || this.isFishBiting_fromAll();
    }
    
    private boolean isFishBiting_fromBobberSound() {
        return Settings.config_autofish_aggressiveBiteDetection && this.closeBobberSplashDetectedAt > 0L;
    }
    
    private boolean isFishBiting_fromWaterWake() {
        return Settings.config_autofish_aggressiveBiteDetection && this.closeWaterWakeDetectedAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.closeWaterWakeDetectedAt + 30L;
    }
    
    private boolean isFishBiting_fromMovement() {
        final EntityFishHook fishEntity = this.mc.thePlayer.fishEntity;
        if (fishEntity != null && Math.abs(fishEntity.motionX) < 0.01 && Math.abs(fishEntity.motionZ) < 0.01) {
            final long calculatedServerY = fishEntity.serverPosY - this.lastFishEntityServerY;
            this.lastFishEntityServerY = fishEntity.serverPosY;
            return fishEntity.motionY < -0.02 || calculatedServerY < -500L;
        }
        return false;
    }
    
    private boolean isFishBiting_fromAll() {
        final EntityFishHook fishEntity = this.mc.thePlayer.fishEntity;
        return fishEntity != null && fishEntity.motionX == 0.0 && fishEntity.motionZ == 0.0 && fishEntity.motionY < -0.008 && (this.recentCloseBobberSplash() || this.recentCloseWaterWake());
    }
    
    private boolean isDuringReelDelay() {
        return this.startedReelDelayAt != 0L && this.mc.theWorld.getTotalWorldTime() < this.startedReelDelayAt + 15L;
    }
    
    private boolean isDuringCastDelay() {
        return this.startedCastDelayAt != 0L && this.mc.theWorld.getTotalWorldTime() < this.startedCastDelayAt + 20L;
    }
    
    private boolean playerHookInWater() {
        if (this.playerHookInWater((EntityPlayer)this.mc.thePlayer)) {
            if (this.hookFirstInWaterAt == 0L) {
                this.hookFirstInWaterAt = this.mc.theWorld.getTotalWorldTime();
            }
            return true;
        }
        return false;
    }
    
    private boolean playerHookInWater(final EntityPlayer player) {
        return player != null && player.fishEntity != null && player.fishEntity.isInWater();
    }
    
    private boolean hookHasBeenInWaterLongEnough() {
        return this.playerHookInWater() && this.mc.theWorld.getTotalWorldTime() > this.hookFirstInWaterAt + 40L;
    }
    
    private boolean playerIsHoldingRod() {
        final ItemStack heldItem = this.mc.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemFishingRod && heldItem.getItemDamage() <= heldItem.getMaxDamage();
    }
    
    private boolean recentCloseBobberSplash() {
        return this.closeBobberSplashDetectedAt > 0L && this.mc.theWorld.getTotalWorldTime() < this.closeBobberSplashDetectedAt + 20L;
    }
    
    private boolean recentCloseWaterWake() {
        return this.closeWaterWakeDetectedAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.closeWaterWakeDetectedAt + 30L;
    }
    
    private boolean somethingSeemsWrong() {
        if (this.rodIsCast() && !this.isDuringCastDelay() && !this.isDuringReelDelay() && this.hookShouldBeInWater()) {
            if ((this.playerHookInWater((EntityPlayer)this.mc.thePlayer) || Settings.config_autofish_handleProblems) && this.waitedLongEnough()) {
                return true;
            }
            if (Settings.config_autofish_handleProblems) {
                if (this.hookedAnEntity()) {
                    return true;
                }
                if (!this.playerHookInWater((EntityPlayer)this.mc.thePlayer)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hookedAnEntity() {
        return this.mc.thePlayer.fishEntity != null && this.mc.thePlayer.fishEntity.caughtEntity != null;
    }
    
    private boolean waitedLongEnough() {
        return this.startedCastDelayAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.startedCastDelayAt + 1800L;
    }
    
    private boolean hookShouldBeInWater() {
        return this.startedCastDelayAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.startedCastDelayAt + 80L;
    }
    
    private boolean rodIsCast() {
        return this.playerIsHoldingRod() && this.mc.thePlayer.fishEntity != null;
    }
    
    private boolean needToSwitchRods() {
        return Settings.config_autofish_multirod && !this.playerCanCast();
    }
    
    private boolean isTimeToCast() {
        return this.castScheduledAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.castScheduledAt + Settings.config_autofish_recastDelay * 20L;
    }
    
    private boolean waitingToRecast() {
        return this.castScheduledAt > 0L;
    }
    
    private boolean playerCanCast() {
        if (!this.playerIsHoldingRod()) {
            return false;
        }
        final ItemStack heldItem = this.mc.thePlayer.getHeldItem();
        return !Settings.config_autofish_preventBreak || heldItem.getMaxDamage() - heldItem.getItemDamage() > 2;
    }
    
    private void showNotificationToPlayer() {
        this.notificationShownToPlayer = true;
    }
    
    private void reelIn() {
        this.playerUseRod();
    }
    
    private void startFishing() {
        this.playerUseRod();
        this.startCastDelay();
    }
    
    private void resetCastSchedule() {
        this.castScheduledAt = 0L;
    }
    
    private void resetCastDelay() {
        this.startedCastDelayAt = 0L;
    }
    
    private void scheduleNextCast() {
        this.castScheduledAt = this.mc.theWorld.getTotalWorldTime();
    }
    
    private void startReelDelay() {
        this.startedReelDelayAt = this.mc.theWorld.getTotalWorldTime();
    }
    
    private void startCastDelay() {
        this.startedCastDelayAt = this.mc.theWorld.getTotalWorldTime();
    }
    
    private void resetReelDelay() {
        this.startedReelDelayAt = 0L;
    }
    
    public void triggerBites() {
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            for (final WorldServer worldServer : server.worldServers) {
                for (final EntityPlayer player : worldServer.playerEntities) {
                    if (this.playerHookInWater(player)) {
                        final int ticks = 40 + MathHelper.getRandomIntegerInRange(this.random, 0, 40);
                        this.setTicksCatchableDelay(player.fishEntity, ticks);
                    }
                }
            }
        }
    }
    
    private void triggerBite() {
        final EntityPlayer serverPlayerEntity = this.getServerPlayerEntity();
        if (serverPlayerEntity != null) {
            final EntityFishHook serverFishEntity = serverPlayerEntity.fishEntity;
            final int ticks = 40 + MathHelper.getRandomIntegerInRange(this.random, 0, 40);
            this.setTicksCatchableDelay(serverFishEntity, ticks);
        }
    }
    
    private void setTicksCatchableDelay(final EntityFishHook hook, final int ticks) {
        final String forgeFieldName = "ticksCatchableDelay";
        final String vanillaFieldName = "field_146038_az";
        try {
            final int currentTicksCatchableDelay = ReflectionUtils.getPrivateIntFieldFromObject(hook, forgeFieldName, vanillaFieldName);
            if (currentTicksCatchableDelay <= 0) {
                try {
                    ReflectionUtils.setPrivateIntFieldOfObject(hook, forgeFieldName, vanillaFieldName, ticks);
                }
                catch (Exception ex) {}
            }
        }
        catch (Exception ex2) {}
    }
    
    private EntityPlayer getServerPlayerEntity() {
        if (this.mc.getIntegratedServer() == null || this.mc.getIntegratedServer().getEntityWorld() == null) {
            return null;
        }
        return this.mc.getIntegratedServer().getEntityWorld().getPlayerEntityByName(this.mc.thePlayer.getName());
    }
    
    private void playerUseRod() {
        this.mc.playerController.sendUseItem(this.mc.thePlayer, (World)this.mc.theWorld, this.mc.thePlayer.getHeldItem());
        this._onPlayerUseItem();
    }
    
    private void resetBiteTracking() {
        this.hookFirstInWaterAt = 0L;
        this.lastFishEntityServerY = 0L;
        this.xpLastAddedAt = 0L;
        this.closeWaterWakeDetectedAt = 0L;
        this.closeBobberSplashDetectedAt = 0L;
    }
    
    private void tryToSwitchRods() {
        final InventoryPlayer inventory = this.mc.thePlayer.inventory;
        for (int i = 0; i < 9; ++i) {
            final ItemStack curItemStack = inventory.mainInventory[i];
            if (curItemStack != null && curItemStack.getItem() instanceof ItemFishingRod && (!Settings.config_autofish_preventBreak || curItemStack.getMaxDamage() - curItemStack.getItemDamage() > 2)) {
                inventory.currentItem = i;
                break;
            }
        }
    }
    
    private void checkForEntityClear() {
        if (this.isFishing && !this.isDuringCastDelay() && this.mc.thePlayer.fishEntity == null) {
            this.isFishing = false;
            this.startFishing();
        }
    }
    
    private void checkForMissedBite() {
        if (this.playerHookInWater((EntityPlayer)this.mc.thePlayer) && this.closeBobberSplashDetectedAt > 0L && this.mc.theWorld.getTotalWorldTime() > this.closeBobberSplashDetectedAt + 45L) {
            this.resetBiteTracking();
        }
    }
}
