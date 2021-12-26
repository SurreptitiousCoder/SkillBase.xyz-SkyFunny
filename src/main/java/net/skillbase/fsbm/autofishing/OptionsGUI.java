package net.skillbase.fsbm.autofishing;

import net.minecraftforge.fml.client.config.*;
import net.minecraft.client.gui.*;
import net.minecraftforge.common.config.*;
import net.skillbase.fsbm.*;

public class OptionsGUI extends GuiConfig
{
    public OptionsGUI(final GuiScreen parent) {
        super(parent, new ConfigElement(FSBM.getConfig().getCategory("general")).getChildElements(), "fsbm", false, false, "SkyFunny");
        this.titleLine2 = GuiConfig.getAbridgedConfigPath("Configuration");
    }
    
    public void onGuiClosed() {
        Settings.syncConfig();
    }
}
