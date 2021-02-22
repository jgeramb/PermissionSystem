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
import net.dev.permissions.utils.*;
import net.dev.permissions.utils.fetching.UUIDFetching;
import net.dev.permissions.utils.permissionmanagement.*;

public class RankCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utils utils = permissionSystem.getUtils();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		PermissionConfigUtils permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		PermissionUserManager permissionUserManager = permissionSystem.getPermissionUserManager();
		UUIDFetching uuidFetching = permissionSystem.getUUIDFetching();
		
		String prefix = utils.getPrefix();
		
		if(sender instanceof Player) {
			Player p = (Player) sender;
			
			if(p.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE)) || p.hasPermission(new Permission("permissions.rank", PermissionDefault.FALSE))) {
				if(args.length == 2) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						for (PermissionGroup group : permissionUserManager.getPermissionPlayer(args[0]).getGroups())
							group.removeMember(args[0], false);
						
						permissionGroup.addMember(args[0], true);
						
						Player t = Bukkit.getPlayer(args[0]);
						
						if((t != null) && fileUtils.getConfig().getBoolean("Settings.RankKick"))
							t.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfig().getString("Settings.RankKickMessage").replace("%getPrefix()%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", p.getName()).replace("%expiryTime%", fileUtils.getConfig().getString("Settings.RankKickExpiryNever"))));
							
						p.sendMessage(prefix + "§eThe group of the player §a" + args[0] + " §ewas set to §d" + permissionGroup.getName() + "§7!");
					} else
						p.sendMessage(prefix + "§cThe group §e" + args[1] + " §cdoes not exist§7!");
				} else if(args.length >= 4) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						int value = 0;
						Player t  = Bukkit.getPlayer(args[0]);
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[0]);
						UUID uuid;
						String name;
						
						if(t != null) {
							uuid = t.getUniqueId();
							name = t.getName();
						} else {
							name = args[0];
							uuid = uuidFetching.fetchUUID(name);
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
								group.removeMember(name, false);
							
							permissionGroup.addMember(name, true);
							
							if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
								permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(uuid.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000));
							else {
								List<String> ranks = permissionConfigUtils.getConfig().getStringList("TempRanks");
								
								if(!(ranks.contains(uuid.toString())))
									ranks.add(uuid.toString());
								
								permissionConfigUtils.getConfig().set("TempRanks", ranks);
								permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
								permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".Time", System.currentTimeMillis() + (time * 1000));
								permissionConfigUtils.saveFile();
							}
							
							String[] expiryTime = utils.getFormattedTime(time);
							
							if((t != null) && fileUtils.getConfig().getBoolean("Settings.RankKick"))
								t.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfig().getString("Settings.RankKickMessage").replace("%prefix%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", p.getName()).replace("%expiryTime%", fileUtils.getConfig().getString("Settings.RankKickExpiryFormat").replace("%years%", expiryTime[0]).replace("%months%", expiryTime[1]).replace("%days%", expiryTime[2]).replace("%hours%", expiryTime[3]).replace("%minutes%", expiryTime[4]).replace("%seconds%", expiryTime[5]))));
								
							p.sendMessage(prefix + "§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
						} else
							p.sendMessage(prefix + "§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
					} else
						p.sendMessage(prefix + "§cThe group §e" + args[1] + " §cdoes not exist§7!");
				} else
					p.sendMessage(prefix + "§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
			} else
				p.sendMessage(utils.getNoPerm());
		} else if(args.length == 2) {
			PermissionGroup permissionGroup = new PermissionGroup(args[1]);
			
			if(permissionGroup.exists()) {
				for (PermissionGroup group : permissionUserManager.getPermissionPlayer(args[0]).getGroups())
					group.removeMember(args[0], false);
				
				permissionGroup.addMember(args[0], true);
				
				Player t = Bukkit.getPlayer(args[0]);
				
				if((t != null) && fileUtils.getConfig().getBoolean("Settings.RankKick"))
					t.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfig().getString("Settings.RankKickMessage").replace("%getPrefix()%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", "CONSOLE").replace("%expiryTime%", fileUtils.getConfig().getString("Settings.RankKickExpiryNever"))));
					
				utils.sendConsole("§eThe group of the player §a" + args[0] + " §ewas set to §d" + permissionGroup.getName() + "§7!");
			} else
				utils.sendConsole("§cThe group §e" + args[1] + " §cdoes not exist§7!");
		} else if(args.length >= 4) {
			PermissionGroup permissionGroup = new PermissionGroup(args[1]);
			
			if(permissionGroup.exists()) {
				int value = 0;
				Player t  = Bukkit.getPlayer(args[0]);
				PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[0]);
				UUID uuid;
				String name;
				
				if(t != null) {
					uuid = t.getUniqueId();
					name = t.getName();
				} else {
					name = args[0];
					uuid = uuidFetching.fetchUUID(name);
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
						group.removeMember(name, false);
					
					permissionGroup.addMember(name, true);
					
					if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
						permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(uuid.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000));
					else {
						List<String> ranks = permissionConfigUtils.getConfig().getStringList("TempRanks");
						
						if(!(ranks.contains(uuid.toString())))
							ranks.add(uuid.toString());
						
						permissionConfigUtils.getConfig().set("TempRanks", ranks);
						permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
						permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".Time", System.currentTimeMillis() + (time * 1000));
						permissionConfigUtils.saveFile();
					}
					
					String[] expiryTime = utils.getFormattedTime(time);
					
					if((t != null) && fileUtils.getConfig().getBoolean("Settings.RankKick"))
						t.kickPlayer(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfig().getString("Settings.RankKickMessage").replace("%prefix%", prefix).replace("%rankName%", permissionGroup.getName()).replace("%rankSetter%", "CONSOLE").replace("%expiryTime%", fileUtils.getConfig().getString("Settings.RankKickExpiryFormat").replace("%years%", expiryTime[0]).replace("%months%", expiryTime[1]).replace("%days%", expiryTime[2]).replace("%hours%", expiryTime[3]).replace("%minutes%", expiryTime[4]).replace("%seconds%", expiryTime[5]))));
						
					utils.sendConsole("§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
				} else
					utils.sendConsole("§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
			} else
				utils.sendConsole("§cThe group §e" + args[1] + " §cdoes not exist§7!");
		} else
			utils.sendConsole("§e/rank «playername» «groupname» («value» «seconds|minutes|hours|days|months|years»)");
		
		return true;
	}

}
