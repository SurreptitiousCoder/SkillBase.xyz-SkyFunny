package net.skillbase.fsbm.modules;

import net.minecraftforge.fml.common.gameevent.*;
import net.minecraft.entity.player.*;
import net.minecraft.client.settings.*;

public class AutoHelmet extends Module
{
    private boolean hasBonzoMask;
    private boolean hasSpiritMask;
    private int bonzoCooldown;
    private int spiritCooldown;
    
    public AutoHelmet() {
        this.hasBonzoMask = false;
        this.hasSpiritMask = false;
        this.bonzoCooldown = 0;
        this.spiritCooldown = 0;
    }
    
    @Override
    public void onUpdate(final TickEvent.ClientTickEvent event) throws Exception {
        if (this.mc.thePlayer.getHealth() < 2.0f) {
            System.out.println(this.mc.thePlayer.inventory.armorInventory[0].getTooltip(this.mc.thePlayer, true));
        }
    }
    

    
    @Override
    protected KeyBinding registerKeybinding() {
        return null;
    }
}
