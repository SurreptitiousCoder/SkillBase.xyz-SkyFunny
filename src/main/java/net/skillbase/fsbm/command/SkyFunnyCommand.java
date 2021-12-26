package net.skillbase.fsbm.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.skillbase.fsbm.FSBM;
import net.skillbase.fsbm.modules.Module;
import org.lwjgl.input.Keyboard;

public class SkyFunnyCommand extends CommandBase
{
    public String getCommandName() {
        return "skyfunny";
    }
    
    public String getCommandUsage(final ICommandSender sender) {
        return "/skyfunny";
    }
    
    public void processCommand(final ICommandSender sender, final String[] args) {
        sender.addChatMessage(new ChatComponentText("You are using SkyFunny-1.3"));
        if (sender instanceof EntityPlayerSP) {
            final Minecraft mc = Minecraft.getMinecraft();
            final StringBuilder sb = new StringBuilder();
            for (final Module module : FSBM.getInstance().getModuleManager().getLoadedModules()) {
                sb.append("    ").append(module.getName()).append(" (KeyBind: ").append(Keyboard.getKeyName(module.getKeyBinding().getKeyCode())).append(")\n");
            }
            mc.thePlayer.addChatMessage(new ChatComponentText("You have " + FSBM.getInstance().getModuleManager().getLoadedModules().size() + " mods!\n" + sb));
            final IChatComponent comp = new ChatComponentText("Buy new mods: https://discord.gg/NcpmYQGjgu");
            final ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/NcpmYQGjgu") {
                public Action getAction() {
                    return Action.OPEN_URL;
                }
            });
            comp.setChatStyle(style);
            mc.thePlayer.addChatMessage(comp);
        }
    }
    
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
