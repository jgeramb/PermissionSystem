package net.dev.permissions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.Utils;

public class DebugPermsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Utils utils = PermissionSystem.getInstance().getUtils();
		
		if(sender instanceof Player) {
			Player p = (Player) sender;
			String prefix = utils.getPrefix();
			
			if(p.hasPermission(new Permission("permissions.debug", PermissionDefault.FALSE)) || p.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE))) {
				if(utils.getDebugging().contains(p)) {
					utils.getDebugging().remove(p);
					
					p.sendMessage(prefix + "§cYou are no longer in debug mode§7!");
				} else {
					utils.getDebugging().add(p);
					
					p.sendMessage(prefix + "§eYou are now in debug mode§7!");
				}
			} else
				p.sendMessage(utils.getNoPerm());
		}
		
		return true;
	}

}
