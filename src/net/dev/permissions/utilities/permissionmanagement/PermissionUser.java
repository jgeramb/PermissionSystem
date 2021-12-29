package net.dev.permissions.utilities.permissionmanagement;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.mojang.UUIDFetching;

public class PermissionUser {

	private PermissionSystem permissionSystem;
	private Utilities utilities;
	private FileUtils fileUtils;
	private PermissionConfigUtils permissionConfigUtils;
	private MySQLPermissionManager mysqlPermissionManager;
	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	private UUIDFetching uuidFetching;
	
	private String uuid;
	
	public PermissionUser(UUID uuid) {
		this.uuid = uuid.toString();
		
		this.permissionSystem = PermissionSystem.getInstance();
		this.utilities = permissionSystem.getUtils();
		this.fileUtils = permissionSystem.getFileUtils();
		this.permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		this.mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		this.permissionGroupManager = permissionSystem.getPermissionGroupManager();
		this.permissionUserManager = permissionSystem.getPermissionUserManager();
		this.uuidFetching = permissionSystem.getUUIDFetching();
		
		registerPlayerIfNotExisting();
	}
	
	public boolean exists() {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.isUUIDInCache(uuid);
		
		return (permissionConfigUtils.getConfiguration().get("Players." + uuid) != null);
	}
	
	public void addPermission(String permission) {
		List<String> list;
		
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getPlayerPermissions(uuid);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Players." + uuid + ".Permissions");
		
		if(!(list.contains(permission.toLowerCase())))
			list.add(permission.toLowerCase());
		
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eAdded permission §a\"" + permission + "\" §eto player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
			
		updatePermissions();
	}
	
	public void removePermission(String permission) {
		List<String> list;
		
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getPlayerPermissions(uuid);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Players." + uuid + ".Permissions");
				
		if(list.contains(permission.toLowerCase()))
			list.remove(permission.toLowerCase());
		
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eRemoved permission §a\"" + permission + "\" §efrom player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
		
		updatePermissions();
	}
	
	public void clearPermissions() {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, new ArrayList<>().toString());
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Permissions", new ArrayList<String>());
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eCleared permissions of player §a" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
		
		updatePermissions();
	}
	
	public List<String> getPermissions() {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.getPlayerPermissions(uuid);
		
		return permissionConfigUtils.getConfiguration().getStringList("Players." + uuid + ".Permissions");
	}
	
	public void updatePermissions() {
		if(permissionUserManager.getPlayerByUUID(uuid) != null) {
			Player player = permissionUserManager.getPlayerByUUID(uuid);
			player.getEffectivePermissions().clear();
			
			PermissionAttachment pa = utilities.getAttachments().containsKey(player.getUniqueId()) ? utilities.getAttachments().get(player.getUniqueId()) : player.addAttachment(permissionSystem);

			for(PermissionAttachmentInfo info : player.getEffectivePermissions()) {
				if(player.hasPermission(info.getPermission()))
					pa.setPermission(info.getPermission(), false);
			}
			
			for(String permission : getPermissions())
				pa.setPermission(permission, true);
			
			for(PermissionGroup permissionGroup : getGroups()) {
				PermissionGroup group = permissionGroup;
				String parent = "";
				
				while((parent = group.getParent()) != null) {
					group = new PermissionGroup(parent);
					
					if(group.exists()) {
						for(String permission : group.getPermissions())
							pa.setPermission(permission, true);
						
						pa.setPermission("group." + group.getName().toLowerCase(), true);
					} else
						break;
				}
				
				for(String permission : permissionGroup.getPermissions())
					pa.setPermission(permission, true);
				
				pa.setPermission("group." + permissionGroup.getName().toLowerCase(), true);
			}
			
			player.recalculatePermissions();
		}
	}
	
	public void registerPlayerIfNotExisting() {
		if(!(exists())) {
			if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
				mysqlPermissionManager.saveToUUIDCache(uuid);
				mysqlPermissionManager.setPlayerPermissions(uuid, "[]");
				mysqlPermissionManager.setValues(new HashMap<>());
				mysqlPermissionManager.collectUserData();
				mysqlPermissionManager.collectGroupData();
			} else {
				List<String> players = permissionConfigUtils.getConfiguration().getStringList("Players.PlayerUUIDCache");
				
				if(!(players.contains(uuid)))
					players.add(uuid);
				
				permissionConfigUtils.getConfiguration().set("Players.PlayerUUIDCache", players);
				permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Permissions", new ArrayList<String>());
				permissionConfigUtils.saveFile();
			}
		
			setPrefix("");
			setChatPrefix("");
			setSuffix("");
			setChatSuffix("");
			
			utilities.sendDebugMessage("§ePlayer §a" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas registered§7!");
		}
		
		if(getGroups().isEmpty() && !(permissionGroupManager.getPermissionGroups().isEmpty())) {
			PermissionGroup group = new PermissionGroup("DEFAULT");
			
			for (PermissionGroup g : permissionGroupManager.getPermissionGroups()) {
				if(g.isDefault())
					group = g;
			}
			
			if(group.exists())
				group.addMemberWithUUID(uuid);
		}
	}
	
	public List<String> getGroupNames() {
		List<String> permissionGroups = new ArrayList<>();
		
		for(PermissionGroup permissionGroup : permissionGroupManager.getPermissionGroups()) {
			if(permissionGroup.getMemberUUIDs().contains(uuid))
				permissionGroups.add(permissionGroup.getName());
		}
		
		return permissionGroups;
	}
	
	public List<PermissionGroup> getGroups() {
		List<PermissionGroup> permissionGroups = new ArrayList<>();
		
		for(PermissionGroup permissionGroup : permissionGroupManager.getPermissionGroups()) {
			if(permissionGroup.getMemberUUIDs().contains(uuid))
				permissionGroups.add(permissionGroup);
		}
		
		return permissionGroups;
	}

	public void setPrefix(String value) {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPrefix(uuid, value);
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Options.Prefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aPrefix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setChatPrefix(String value) {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerChatPrefix(uuid, value);
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Options.ChatPrefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aChatPrefix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setSuffix(String value) {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerSuffix(uuid, value);
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Options.Suffix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aSuffix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setChatSuffix(String value) {
		if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerChatSuffix(uuid, value);
		else {
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Options.ChatSuffix", value);
			permissionConfigUtils.saveFile();
		}
	
		utilities.sendDebugMessage("§eThe value of the option §aChatSuffix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public String getPrefix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerPrefix(uuid) : permissionConfigUtils.getConfiguration().getString("Players." + uuid + ".Options.Prefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getChatPrefix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerChatPrefix(uuid) : permissionConfigUtils.getConfiguration().getString("Players." + uuid + ".Options.ChatPrefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getSuffix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerSuffix(uuid) : permissionConfigUtils.getConfiguration().getString("Players." + uuid + ".Options.Suffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getChatSuffix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerChatSuffix(uuid) : permissionConfigUtils.getConfiguration().getString("Players." + uuid + ".Options.ChatSuffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getGroupPrefix() {
		String prefix = "";
		
		for(PermissionGroup permissionGroup : getGroups()) {
			String s = permissionGroup.getPrefix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupChatPrefix() {
		String prefix = "";
		
		for(PermissionGroup permissionGroup : getGroups()) {
			String s = permissionGroup.getChatPrefix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupSuffix() {
		String prefix = "";
		
		for(PermissionGroup permissionGroup : getGroups()) {
			String s = permissionGroup.getSuffix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupChatSuffix() {
		String prefix = "";
		
		for(PermissionGroup permissionGroup : getGroups()) {
			String s = permissionGroup.getChatSuffix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}

	public PermissionGroup getHighestGroup() {
		PermissionGroup group = null;
		int lowestWeight = Integer.MAX_VALUE;
		
		for (PermissionGroup permissionGroup : getGroups()) {
			if(permissionGroup.getWeight() < lowestWeight) {
				lowestWeight = permissionGroup.getWeight();
				group = permissionGroup;
			}
		}
			
		return group;
	}
	
}