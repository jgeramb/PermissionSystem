package net.dev.permissions.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.mojang.UUIDFetching;
import net.dev.permissions.utilities.permissionmanagement.*;

public class RankCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utilities utilities = permissionSystem.getUtils();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		PermissionConfigUtils permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		PermissionUserManager permissionUserManager = permissionSystem.getPermissionUserManager();
		UUIDFetching uuidFetching = permissionSystem.getUUIDFetching();
		
		String prefix = utilities.getPrefix();
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			
			if(player.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE)) || player.hasPermission(new Permission("permissions.rank", PermissionDefault.FALSE))) {
				if(args.length == 2) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						for (PermissionGroup group : permissionUserManager.getPermissionPlayer(args[0]).getGroups())
							group.removeMember(args[0], false);
						
						permissionGroup.addMember(args[0], true);
						
						Player targetPlayer = Bukkit.getPlayer(args[0]);
						
						if((targetPlayer != null) && fileUtils.getConfiguration().getBoolean("Settings.RankKick"))
							targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfiguration().getString("Settings.RankKickMessage").replace("%getPrefix()%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", player.getName()).replace("%expiryTime%", fileUtils.getConfiguration().getString("Settings.RankKickExpiryNever"))));
							
						player.sendMessage(prefix + "§eThe group of the player §a" + args[0] + " §ewas set to §d" + permissionGroup.getName() + "§7!");
					} else
						player.sendMessage(prefix + "§cThe group §e" + args[1] + " §cdoes not exist§7!");
				} else if(args.length >= 4) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						int value = 0;
						Player targetPlayer  = Bukkit.getPlayer(args[0]);
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[0]);
						UUID targetUniqueId;
						String targetName;
						
						if(targetPlayer != null) {
							targetUniqueId = targetPlayer.getUniqueId();
							targetName = targetPlayer.getName();
						} else {
							targetName = args[0];
							targetUniqueId = uuidFetching.fetchUUID(targetName);
						}
						
						try {
							value = Integer.parseInt(args[2]);
						} catch (Exception e) {
						}
						
						if(args[3].equalsIgnoreCase("seconds") || args[3].equalsIgnoreCase("minutes") || args[3].equalsIgnoreCase("hours") || args[3].equalsIgnoreCase("days") || args[3].equalsIgnoreCase("months") || args[3].equalsIgnoreCase("years")) {
							String unit = args[3].toUpperCase();
							int time = value;
							
							if(unit.equalsIgnoreCase("minutes"))
								time *= 60;
							else if(unit.equalsIgnoreCase("hours"))
								time *= 60 * 60;
							else if(unit.equalsIgnoreCase("days"))
								time *= 60 * 60 * 24;
							else if(unit.equalsIgnoreCase("months"))
								time *= 60 * 60 * 24 * 30;
							else if(unit.equalsIgnoreCase("years"))
								time *= 60 * 60 * 24 * 30 * 12;
							
							for (PermissionGroup group : permissionUser.getGroups())
								group.removeMember(targetName, false);
							
							permissionGroup.addMember(targetName, true);
							
							if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
								permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(targetUniqueId.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000L));
							else {
								List<String> ranks = permissionConfigUtils.getConfiguration().getStringList("TempRanks");
								
								if(!(ranks.contains(targetUniqueId.toString())))
									ranks.add(targetUniqueId.toString());
								
								permissionConfigUtils.getConfiguration().set("TempRanks", ranks);
								permissionConfigUtils.getConfiguration().set("Ranks." + targetUniqueId.toString() + ".GroupName", permissionGroup.getName());
								permissionConfigUtils.getConfiguration().set("Ranks." + targetUniqueId.toString() + ".Time", System.currentTimeMillis() + (time * 1000L));
								permissionConfigUtils.saveFile();
							}
							
							String[] expiryTime = utilities.getFormattedTime(time);
							
							if((targetPlayer != null) && fileUtils.getConfiguration().getBoolean("Settings.RankKick"))
								targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfiguration().getString("Settings.RankKickMessage").replace("%prefix%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", player.getName()).replace("%expiryTime%", fileUtils.getConfiguration().getString("Settings.RankKickExpiryFormat").replace("%years%", expiryTime[0]).replace("%months%", expiryTime[1]).replace("%days%", expiryTime[2]).replace("%hours%", expiryTime[3]).replace("%minutes%", expiryTime[4]).replace("%seconds%", expiryTime[5]))));
								
							player.sendMessage(prefix + "§eThe group of the player §a" + targetName + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
						} else
							player.sendMessage(prefix + "§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
					} else
						player.sendMessage(prefix + "§cThe group §e" + args[1] + " §cdoes not exist§7!");
				} else
					player.sendMessage(prefix + "§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
			} else
				player.sendMessage(utilities.getNoPerm());
		} else if(args.length == 2) {
			PermissionGroup permissionGroup = new PermissionGroup(args[1]);
			
			if(permissionGroup.exists()) {
				for (PermissionGroup group : permissionUserManager.getPermissionPlayer(args[0]).getGroups())
					group.removeMember(args[0], false);
				
				permissionGroup.addMember(args[0], true);
				
				Player targetPlayer = Bukkit.getPlayer(args[0]);
				
				if((targetPlayer != null) && fileUtils.getConfiguration().getBoolean("Settings.RankKick"))
					targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfiguration().getString("Settings.RankKickMessage").replace("%getPrefix()%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", "CONSOLE").replace("%expiryTime%", fileUtils.getConfiguration().getString("Settings.RankKickExpiryNever"))));
					
				utilities.sendConsole("§eThe group of the player §a" + args[0] + " §ewas set to §d" + permissionGroup.getName() + "§7!");
			} else
				utilities.sendConsole("§cThe group §e" + args[1] + " §cdoes not exist§7!");
		} else if(args.length >= 4) {
			PermissionGroup permissionGroup = new PermissionGroup(args[1]);
			
			if(permissionGroup.exists()) {
				int value = 0;
				Player targetPlayer  = Bukkit.getPlayer(args[0]);
				PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[0]);
				UUID targetUniqueId;
				String targetName;
				
				if(targetPlayer != null) {
					targetUniqueId = targetPlayer.getUniqueId();
					targetName = targetPlayer.getName();
				} else {
					targetName = args[0];
					targetUniqueId = uuidFetching.fetchUUID(targetName);
				}
				
				try {
					value = Integer.parseInt(args[2]);
				} catch (Exception e) {
				}
				
				if(args[3].equalsIgnoreCase("seconds") || args[3].equalsIgnoreCase("minutes") || args[3].equalsIgnoreCase("hours") || args[3].equalsIgnoreCase("days") || args[3].equalsIgnoreCase("months") || args[3].equalsIgnoreCase("years")) {
					String unit = args[3].toUpperCase();
					int time = value;
					
					if(unit.equalsIgnoreCase("minutes"))
						time *= 60;
					else if(unit.equalsIgnoreCase("hours"))
						time *= 60 * 60;
					else if(unit.equalsIgnoreCase("days"))
						time *= 60 * 60 * 24;
					else if(unit.equalsIgnoreCase("months"))
						time *= 60 * 60 * 24 * 30;
					else if(unit.equalsIgnoreCase("years"))
						time *= 60 * 60 * 24 * 30 * 12;
					
					for (PermissionGroup group : permissionUser.getGroups())
						group.removeMember(targetName, false);
					
					permissionGroup.addMember(targetName, true);
					
					if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
						permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(targetUniqueId.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000L));
					else {
						List<String> ranks = permissionConfigUtils.getConfiguration().getStringList("TempRanks");
						
						if(!(ranks.contains(targetUniqueId.toString())))
							ranks.add(targetUniqueId.toString());
						
						permissionConfigUtils.getConfiguration().set("TempRanks", ranks);
						permissionConfigUtils.getConfiguration().set("Ranks." + targetUniqueId.toString() + ".GroupName", permissionGroup.getName());
						permissionConfigUtils.getConfiguration().set("Ranks." + targetUniqueId.toString() + ".Time", System.currentTimeMillis() + (time * 1000L));
						permissionConfigUtils.saveFile();
					}
					
					String[] expiryTime = utilities.getFormattedTime(time);
					
					if((targetPlayer != null) && fileUtils.getConfiguration().getBoolean("Settings.RankKick"))
						targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfiguration().getString("Settings.RankKickMessage").replace("%prefix%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", "CONSOLE").replace("%expiryTime%", fileUtils.getConfiguration().getString("Settings.RankKickExpiryFormat").replace("%years%", expiryTime[0]).replace("%months%", expiryTime[1]).replace("%days%", expiryTime[2]).replace("%hours%", expiryTime[3]).replace("%minutes%", expiryTime[4]).replace("%seconds%", expiryTime[5]))));
						
					utilities.sendConsole("§eThe group of the player §a" + targetName + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
				} else
					utilities.sendConsole("§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
			} else
				utilities.sendConsole("§cThe group §e" + args[1] + " §cdoes not exist§7!");
		} else
			utilities.sendConsole("§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
		
		return true;
	}

}
