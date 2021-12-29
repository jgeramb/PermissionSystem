package net.dev.permissions.hooks;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.permissionmanagement.PermissionGroup;
import net.dev.permissions.utilities.permissionmanagement.PermissionUser;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolderExpansion extends PlaceholderExpansion {
	
	private Plugin plugin;
	
	public PlaceHolderExpansion(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utilities utilities = permissionSystem.getUtils();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		PermissionConfigUtils permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		MySQLPermissionManager mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		
		if(player != null) {
			UUID uuid = player.getUniqueId();
			PermissionUser user = new PermissionUser(uuid);
			
			if(identifier.equals("group_expiry")) {
				String timedGroup = "NONE", formattedTime = "", group = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						group = tmpGroup.getName();
						weight = tmpGroup.getWeight();
					}
				}
				
				if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
					String tempGroupName = mysqlPermissionManager.getPlayerTempGroupName(uuid.toString());
					
					if(tempGroupName != null) {
						timedGroup = tempGroupName;
						formattedTime = utilities.formatTime((mysqlPermissionManager.getPlayerTempGroupTime(uuid.toString()) - System.currentTimeMillis()) / 1000L);
					}
				} else if(permissionConfigUtils.getConfiguration().getStringList("TempRanks").contains(uuid.toString())) {
					timedGroup = permissionConfigUtils.getConfiguration().getString("Ranks." + uuid.toString() + ".GroupName");
					formattedTime = utilities.formatTime((permissionConfigUtils.getConfiguration().getLong("Ranks." + uuid.toString() + ".Time") - System.currentTimeMillis()) / 1000L);
				}
				
				return ((formattedTime.isEmpty() || !(timedGroup.equals(group))) ? "NONE" : formattedTime);
			}
			
			if(identifier.equals("highest_group") || identifier.equals("group")) {
				String group = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						group = tmpGroup.getName();
						weight = tmpGroup.getWeight();
					}
				}
				
				return group;
			}
			
			if(identifier.equals("highest_group_weight") || identifier.equals("group_weight")) {
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight)
						weight = tmpGroup.getWeight();
				}
				
				return String.valueOf(weight);
			}
			
			if(identifier.equals("highest_group_prefix") || identifier.equals("group_prefix")) {
				String prefix = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						prefix = tmpGroup.getPrefix();
						weight = tmpGroup.getWeight();
					}
				}
				
				return prefix;
			}
			
			if(identifier.equals("highest_group_suffix") || identifier.equals("group_suffix")) {
				String suffix = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						suffix = tmpGroup.getSuffix();
						weight = tmpGroup.getWeight();
					}
				}
				
				return suffix;
			}
			
			if(identifier.equals("highest_group_chat_prefix") || identifier.equals("group_chat_prefix")) {
				String prefix = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						prefix = tmpGroup.getChatPrefix();
						weight = tmpGroup.getWeight();
					}
				}
				
				return prefix;
			}
			
			if(identifier.equals("highest_group_chat_suffix") || identifier.equals("group_chat_suffix")) {
				String suffix = "";
				int weight = Integer.MAX_VALUE;
				
				for (PermissionGroup tmpGroup : user.getGroups()) {
					if(tmpGroup.getWeight() < weight) {
						suffix = tmpGroup.getChatSuffix();
						weight = tmpGroup.getWeight();
					}
				}
				
				return suffix;
			}
			
			if(identifier.equals("prefix")) {
				if(user.getPrefix() != null)
					return user.getPrefix();
				else {
					String prefix = "";
					int weight = Integer.MAX_VALUE;
					
					for (PermissionGroup tmpGroup : user.getGroups()) {
						if(tmpGroup.getWeight() < weight) {
							prefix = tmpGroup.getPrefix();
							weight = tmpGroup.getWeight();
						}
					}
					
					return prefix;
				}
			}
			
			if(identifier.equals("suffix")) {
				if(user.getSuffix() != null)
					return user.getSuffix();
				else {
					String suffix = "";
					int weight = Integer.MAX_VALUE;
					
					for (PermissionGroup tmpGroup : user.getGroups()) {
						if(tmpGroup.getWeight() < weight) {
							suffix = tmpGroup.getSuffix();
							weight = tmpGroup.getWeight();
						}
					}
					
					return suffix;
				}
			}
			
			if(identifier.equals("chat_prefix")) {
				if(user.getChatPrefix() != null)
					return user.getChatPrefix();
				else {
					String prefix = "";
					int weight = Integer.MAX_VALUE;
					
					for (PermissionGroup tmpGroup : user.getGroups()) {
						if(tmpGroup.getWeight() < weight) {
							prefix = tmpGroup.getChatPrefix();
							weight = tmpGroup.getWeight();
						}
					}
					
					return prefix;
				}
			}
			
			if(identifier.equals("chat_suffix")) {
				if(user.getChatSuffix() != null)
					return user.getChatSuffix();
				else {
					String suffix = "";
					int weight = Integer.MAX_VALUE;
					
					for (PermissionGroup tmpGroup : user.getGroups()) {
						if(tmpGroup.getWeight() < weight) {
							suffix = tmpGroup.getChatSuffix();
							weight = tmpGroup.getWeight();
						}
					}
					
					return suffix;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String getIdentifier() {
		return plugin.getDescription().getName();
	}
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}
	
	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
	}
	
}
