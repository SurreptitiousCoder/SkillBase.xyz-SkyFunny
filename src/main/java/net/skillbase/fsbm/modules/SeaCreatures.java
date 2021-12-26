package net.skillbase.fsbm.modules;

import net.minecraft.entity.*;
import net.minecraft.client.settings.*;
import net.minecraftforge.fml.client.*;
import org.apache.commons.codec.digest.*;
import java.io.*;
import net.minecraftforge.fml.common.*;
import java.lang.reflect.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.skillbase.fsbm.util.*;
import net.minecraft.client.entity.*;
import java.util.*;
import java.nio.file.*;
import java.util.function.*;
import net.minecraft.realms.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.monster.*;
import net.minecraft.util.*;

public class SeaCreatures extends Module
{
    private Entity target;
    private long cooldown;
    private final KeyBinding keyBindForward;
    private final KeyBinding keyBindingRight;
    private final KeyBinding keyBindingLeft;
    private final KeyBinding keyBindingJump;
    private long noAimTimer;
    private boolean canAttack;
    private long healTimer;
    private long stuckTimer;
    private final Random random;
    private boolean verified;
    private boolean workingOnFinding;
    
    public SeaCreatures() {
        this.target = null;
        this.cooldown = 0L;
        this.keyBindForward = FMLClientHandler.instance().getClient().gameSettings.keyBindForward;
        this.keyBindingRight = FMLClientHandler.instance().getClient().gameSettings.keyBindRight;
        this.keyBindingLeft = FMLClientHandler.instance().getClient().gameSettings.keyBindLeft;
        this.keyBindingJump = FMLClientHandler.instance().getClient().gameSettings.keyBindJump;
        this.noAimTimer = 0L;
        this.canAttack = false;
        this.healTimer = 0L;
        this.stuckTimer = 0L;
        this.random = new Random();
        this.verified = false;
        this.workingOnFinding = false;
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
        KeyBinding.setKeyBindState(this.keyBindingJump.getKeyCode(), true);
    }
    
    @Override
    public void onDisable() {
        this.target = null;
        KeyBinding.setKeyBindState(this.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(this.keyBindingJump.getKeyCode(), false);
    }
    
    private void rightClick() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            final Method method = this.mc.getClass().getDeclaredMethod("rightClickMouse", (Class<?>[])new Class[0]);
            method.setAccessible(true);
            method.invoke(this.mc, new Object[0]);
        }
        catch (Exception exception) {
            final Method method2 = this.mc.getClass().getDeclaredMethod("func_147121_ag", (Class<?>[])new Class[0]);
            method2.setAccessible(true);
            method2.invoke(this.mc, new Object[0]);
        }
    }
    
    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) throws Exception {
        if (!this.verified) {
            return;
        }
        if (this.target != null) {
            if (this.healTimer > 0L) {
                ++this.healTimer;
            }
            if (this.healTimer > 150L) {
                this.healTimer = 0L;
            }
            if (!this.target.isEntityAlive()) {
                this.target = null;
                return;
            }
            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null && Math.abs(this.mc.thePlayer.motionX) == 0.0 && Math.abs(this.mc.thePlayer.motionZ) == 0.0 && this.cooldown == 0L) {
                ++this.stuckTimer;
                KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), true);
                if (this.stuckTimer > 30L) {
                    KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
                    this.mc.thePlayer.setSprinting(true);
                    this.stuckTimer = 0L;
                }
                return;
            }
            final Pair<Float, Float> yawAndPitch = this.getRotationsToEntity(this.target, this.mc.thePlayer.rotationYaw, this.mc.thePlayer.rotationPitch);
            this.mc.thePlayer.rotationYaw = yawAndPitch.getKey() + (this.canAttack ? (this.random.nextInt(10) * (this.random.nextBoolean() ? 1 : -1)) : 0);
            this.mc.thePlayer.rotationPitch = yawAndPitch.getValue() + (this.canAttack ? (this.random.nextInt(10) * (this.random.nextBoolean() ? 1 : -1)) : 0);
            final long noAimTimer = this.noAimTimer + 1L;
            this.noAimTimer = noAimTimer;
            if (noAimTimer > 20L) {
                this.rightClick();
                this.noAimTimer = 0L;
                this.mc.thePlayer.inventory.currentItem = 1;
                this.rightClick();
                this.mc.thePlayer.inventory.currentItem = 0;
            }
            if (Math.abs(this.mc.thePlayer.motionX) == 0.0 && Math.abs(this.mc.thePlayer.motionZ) == 0.0) {
                this.workingOnFinding = true;
            }
            if (this.target.getDistanceToEntity((Entity)this.mc.thePlayer) <= 3.0f) {
                this.canAttack = true;
                if (this.cooldown >= 8L && this.mc.thePlayer.getCurrentEquippedItem() != null) {
                    if (this.mc.thePlayer.getCurrentEquippedItem().getDisplayName() != null) {
                        if (this.mc.thePlayer.getCurrentEquippedItem().getDisplayName().toLowerCase().contains("flower of truth")) {
                            this.rightClick();
                        }
                    }
                }
                if (this.cooldown >= this.getRandomNumberUsingNextInt(2, 8)) {
                    if (this.mc.objectMouseOver.entityHit != null) {
                        this.noAimTimer = 0L;
                        try {
                            final Method method = this.mc.getClass().getDeclaredMethod("clickMouse", (Class<?>[])new Class[0]);
                            method.setAccessible(true);
                            method.invoke(this.mc, new Object[0]);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                            final Method method2 = this.mc.getClass().getDeclaredMethod("func_147116_af", (Class<?>[])new Class[0]);
                            method2.setAccessible(true);
                            method2.invoke(this.mc, new Object[0]);
                        }
                    }
                    this.cooldown = 0L;
                }
                else {
                    ++this.cooldown;
                }
            }
            else {
                KeyBinding.setKeyBindState(this.keyBindForward.getKeyCode(), true);
                KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), this.random.nextBoolean());
                this.mc.thePlayer.setSprinting(true);
                this.canAttack = false;
            }
        }
        else {
            KeyBinding.setKeyBindState(this.keyBindForward.getKeyCode(), false);
            KeyBinding.setKeyBindState(this.keyBindingRight.getKeyCode(), false);
            KeyBinding.setKeyBindState(this.keyBindingLeft.getKeyCode(), false);
            this.getClosest();
            if (this.target == null) {
                final EntityPlayerSP thePlayer = this.mc.thePlayer;
                thePlayer.rotationYaw += this.random.nextFloat() / 2.0f * (this.random.nextBoolean() ? 1 : -1);
                final EntityPlayerSP thePlayer2 = this.mc.thePlayer;
                thePlayer2.rotationPitch += this.random.nextFloat() / 2.0f * (this.random.nextBoolean() ? 1 : -1);
            }
        }
    }
    

    
    private Pair<Float, Float> getRotationsToEntity(final Entity entity, final float lastYaw, final float lastPitch) {
        final double x = entity.posX - this.mc.thePlayer.posX;
        final double y = entity.posY - (this.mc.thePlayer.posY + this.mc.thePlayer.getEyeHeight());
        final double z = entity.posZ - this.mc.thePlayer.posZ;
        final double distance = MathHelper.sqrt_double(x * x + z * z);
        final int modifier = (this.mc.thePlayer.getHealth() <= 6.0 || this.healTimer > 0L) ? -1 : 1;
        if (this.mc.thePlayer.getHealth() <= 5.0) {
            ++this.healTimer;
        }
        final float afterYaw = (float)(MathHelper.atan2(z, x) * 57.29577951308232 - 90.0) * modifier;
        final float afterPitch = this.workingOnFinding ? -77.0f : ((float)(-(MathHelper.atan2(y, distance) * 57.29577951308232)));
        this.workingOnFinding = false;
        final float pitch = afterPitch - 10.0f;
        float baseSpeed = 20.0f;
        if (this.isRoughlyEqual(lastYaw, afterYaw / 2.0f, 7.0f)) {
            baseSpeed += this.getRandomNumberUsingNextInt(10, 20);
        }
        return new Pair<Float, Float>(this.updateRotation(lastYaw, afterYaw, baseSpeed), this.updateRotation(lastPitch, pitch, baseSpeed));
    }
    
    private float updateRotation(final float lastYaw, final float yaw, final float speed) {
        float f = RealmsMth.wrapDegrees(yaw - lastYaw);
        if (f > speed) {
            f = speed;
        }
        if (f < -speed) {
            f = -speed;
        }
        return lastYaw + f;
    }
    
    public int getRandomNumberUsingNextInt(final int min, final int max) {
        return this.random.nextInt(max - min) + min;
    }
    
    public boolean isRoughlyEqual(final float x, final float y, final float rough) {
        return Math.abs(x - y) <= rough;
    }
    
    private void getClosest() {
        float min = 0.0f;
        Entity possibleTarget = null;
        if (!this.mc.theWorld.loadedEntityList.isEmpty()) {
            for (final Entity possibleTargetEntity : this.mc.theWorld.loadedEntityList) {
                if (this.isValidEntity(possibleTargetEntity) && possibleTargetEntity.isEntityAlive() && !possibleTargetEntity.isInvisible()) {
                    min = this.mc.thePlayer.getDistanceToEntity(possibleTargetEntity);
                    possibleTarget = possibleTargetEntity;
                    break;
                }
            }
        }
        for (int i = 0; i < this.mc.theWorld.loadedEntityList.size(); ++i) {
            final Entity entity = this.mc.theWorld.loadedEntityList.get(i);
            if (this.isValidEntity(entity) && entity.isEntityAlive() && !entity.isInvisible() && this.mc.thePlayer.getDistanceToEntity(entity) < min) {
                min = this.mc.thePlayer.getDistanceToEntity(entity);
                possibleTarget = entity;
            }
        }
        this.target = possibleTarget;
        if (this.target != null) {
            this.sendMessage("�a[NEW TARGET!] �7" + this.target.toString());
        }
    }
    
    private boolean isValidEntity(final Entity entity) {
        if (entity instanceof EntitySquid) {
            return true;
        }
        if (entity instanceof EntityArmorStand) {
            final EntityArmorStand mpPlayer = (EntityArmorStand)entity;
            final String finalString = " " + mpPlayer.getName() + " " + mpPlayer.getCustomNameTag();
            if (finalString.contains("&") || finalString.contains("[")) {
                return true;
            }
        }
        if (entity instanceof EntityHorse) {
            final EntityHorse horse = (EntityHorse)entity;
            return horse.getHorseVariant() != 3;
        }
        return entity instanceof EntityMob || entity instanceof EntityRabbit || entity instanceof EntityOcelot || entity instanceof EntitySlime || entity instanceof EntityIronGolem || entity.hasCustomName();
    }
    
    private void sendMessage(final String text) {
        this.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText(text));
    }
    
    @Override
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("fsbm.seacreatures.toggle", 38, "fsbm.category");
    }
}
