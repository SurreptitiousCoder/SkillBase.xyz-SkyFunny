package net.skillbase.fsbm.command;

import net.minecraft.client.entity.*;
import net.skillbase.fsbm.*;
import net.minecraft.util.*;
import net.minecraft.command.*;

public class UpdateCommand extends CommandBase
{
    public String getCommandName() {
        return "skyupdate";
    }
    
    public String getCommandUsage(final ICommandSender sender) {
        return "skyupdate";
    }
    
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (sender instanceof EntityPlayerSP) {
            boolean silent = (args.length == 2 || args.length == 1) && (Boolean.parseBoolean(args[0]) || Boolean.parseBoolean(args[1]));
            FSBM.loadedBefore = true;
            FSBM.getInstance().unloadManager();
            try {
                FSBM.getInstance().load();
            }
            catch (Exception e) {
                sender.addChatMessage((IChatComponent)new ChatComponentText("Can't update: " + e));
                e.printStackTrace();
                return;
            }
            if (!silent) {
                sender.addChatMessage((IChatComponent)new ChatComponentText("Done updating!"));
            }
        }
    }
    
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
