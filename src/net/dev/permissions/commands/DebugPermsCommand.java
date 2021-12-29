package net.dev.permissions.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.Utilities;

public class DebugPermsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Utilities utilities = PermissionSystem.getInstance().getUtils();
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			String prefix = utilities.getPrefix();
			
			if(player.hasPermission(new Permission("permissions.debug", PermissionDefault.FALSE)) || player.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE))) {
				if(utilities.getDebugging().contains(player)) {
					utilities.getDebugging().remove(player);
					
					player.sendMessage(prefix + "§cYou are no longer in debug mode§7!");
				} else {
					utilities.getDebugging().add(player);
					
					player.sendMessage(prefix + "§eYou are now in debug mode§7!");
				}
			} else
				player.sendMessage(utilities.getNoPerm());
		}
		
		return true;
	}

}
