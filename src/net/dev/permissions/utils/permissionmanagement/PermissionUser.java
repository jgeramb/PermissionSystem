package net.dev.permissions.utils.permissionmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utils.FileUtils;
import net.dev.permissions.utils.PermissionConfigUtils;
import net.dev.permissions.utils.Utils;
import net.dev.permissions.utils.fetching.UUIDFetching;

public class PermissionUser {

	private PermissionSystem permissionSystem;
	private Utils utils;
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
		this.utils = permissionSystem.getUtils();
		this.fileUtils = permissionSystem.getFileUtils();
		this.permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		this.mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		this.permissionGroupManager = permissionSystem.getPermissionGroupManager();
		this.permissionUserManager = permissionSystem.getPermissionUserManager();
		this.uuidFetching = permissionSystem.getUUIDFetching();
		
		registerPlayerIfNotExisting();
	}
	
	public boolean exists() {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.isUUIDInCache(uuid);
		
		return (permissionConfigUtils.getConfig().get("Players." + uuid) != null);
	}
	
	public void addPermission(String permission) {
		List<String> list;
		
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getPlayerPermissions(uuid);
		else
			list = permissionConfigUtils.getConfig().getStringList("Players." + uuid + ".Permissions");
		
		if(!(list.contains(permission.toLowerCase())))
			list.add(permission.toLowerCase());
		
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, list.toString());
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eAdded permission §a\"" + permission + "\" §eto player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
			
		updatePermissions();
	}
	
	public void removePermission(String permission) {
		List<String> list;
		
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getPlayerPermissions(uuid);
		else
			list = permissionConfigUtils.getConfig().getStringList("Players." + uuid + ".Permissions");
				
		if(list.contains(permission.toLowerCase()))
			list.remove(permission.toLowerCase());
		
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, list.toString());
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eRemoved permission §a\"" + permission + "\" §efrom player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
		
		updatePermissions();
	}
	
	public void clearPermissions() {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPermissions(uuid, new ArrayList<>().toString());
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Permissions", new ArrayList<String>());
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eCleared permissions of player §a" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + "§7!");
		
		updatePermissions();
	}
	
	public List<String> getPermissions() {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.getPlayerPermissions(uuid);
		
		return permissionConfigUtils.getConfig().getStringList("Players." + uuid + ".Permissions");
	}
	
	public void updatePermissions() {
		if(permissionUserManager.getPlayerByUUID(uuid) != null) {
			Player p = permissionUserManager.getPlayerByUUID(uuid);
			p.getEffectivePermissions().clear();
			
			PermissionAttachment pa = utils.getAttachments().containsKey(p.getUniqueId()) ? utils.getAttachments().get(p.getUniqueId()) : p.addAttachment(permissionSystem);

			for(PermissionAttachmentInfo info : p.getEffectivePermissions()) {
				if(p.hasPermission(info.getPermission()))
					pa.setPermission(info.getPermission(), false);
			}
			
			for(String permission : getPermissions())
				pa.setPermission(permission, true);
			
			for(PermissionGroup pg : getGroups()) {
				PermissionGroup group = pg;
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
				
				for(String permission : pg.getPermissions())
					pa.setPermission(permission, true);
				
				pa.setPermission("group." + pg.getName().toLowerCase(), true);
			}
			
			p.recalculatePermissions();
		}
	}
	
	public void registerPlayerIfNotExisting() {
		if(!(exists())) {
			if(fileUtils.getConfig().getBoolean("MySQL.Enabled")) {
				mysqlPermissionManager.saveToUUIDCache(uuid);
				mysqlPermissionManager.setPlayerPermissions(uuid, "[]");
				mysqlPermissionManager.setValues(new HashMap<>());
				mysqlPermissionManager.collectUserData();
				mysqlPermissionManager.collectGroupData();
			} else {
				List<String> players = permissionConfigUtils.getConfig().getStringList("Players.PlayerUUIDCache");
				
				if(!(players.contains(uuid)))
					players.add(uuid);
				
				permissionConfigUtils.getConfig().set("Players.PlayerUUIDCache", players);
				permissionConfigUtils.getConfig().set("Players." + uuid + ".Permissions", new ArrayList<String>());
				permissionConfigUtils.saveFile();
			}
		
			setPrefix("");
			setChatPrefix("");
			setSuffix("");
			setChatSuffix("");
			
			utils.sendDebugMessage("§ePlayer §a" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas registered§7!");
		}
		
		if(getGroups().isEmpty()) {
			PermissionGroup group = new PermissionGroup("DEFAULT");
			
			for (PermissionGroup g : permissionGroupManager.getPermissionGroups()) {
				if(g.isDefault())
					group = g;
			}
			
			group.registerGroupIfNotExisting();
			group.addMemberWithUUID(uuid);
		}
	}
	
	public List<String> getGroupNames() {
		List<String> permissionGroups = new ArrayList<>();
		
		for(PermissionGroup pg : permissionGroupManager.getPermissionGroups()) {
			if(pg.getMemberUUIDs().contains(uuid))
				permissionGroups.add(pg.getName());
		}
		
		return permissionGroups;
	}
	
	public List<PermissionGroup> getGroups() {
		List<PermissionGroup> permissionGroups = new ArrayList<>();
		
		for(PermissionGroup pg : permissionGroupManager.getPermissionGroups()) {
			if(pg.getMemberUUIDs().contains(uuid))
				permissionGroups.add(pg);
		}
		
		return permissionGroups;
	}

	public void setPrefix(String value) {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerPrefix(uuid, value);
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Options.Prefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eThe value of the option §aPrefix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setChatPrefix(String value) {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerChatPrefix(uuid, value);
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Options.ChatPrefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eThe value of the option §aChatPrefix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setSuffix(String value) {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerSuffix(uuid, value);
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Options.Suffix", value);
			permissionConfigUtils.saveFile();
		}
		
		utils.sendDebugMessage("§eThe value of the option §aSuffix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setChatSuffix(String value) {
		if(fileUtils.getConfig().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setPlayerChatSuffix(uuid, value);
		else {
			permissionConfigUtils.getConfig().set("Players." + uuid + ".Options.ChatSuffix", value);
			permissionConfigUtils.saveFile();
		}
	
		utils.sendDebugMessage("§eThe value of the option §aChatSuffix §eof player §d" + uuidFetching.fetchName(UUID.fromString(uuid)) + "/" + uuid + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public String getPrefix() {
		String s = fileUtils.getConfig().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerPrefix(uuid) : permissionConfigUtils.getConfig().getString("Players." + uuid + ".Options.Prefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getChatPrefix() {
		String s = fileUtils.getConfig().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerChatPrefix(uuid) : permissionConfigUtils.getConfig().getString("Players." + uuid + ".Options.ChatPrefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getSuffix() {
		String s = fileUtils.getConfig().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerSuffix(uuid) : permissionConfigUtils.getConfig().getString("Players." + uuid + ".Options.Suffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getChatSuffix() {
		String s = fileUtils.getConfig().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getPlayerChatSuffix(uuid) : permissionConfigUtils.getConfig().getString("Players." + uuid + ".Options.ChatSuffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : null);
	}
	
	public String getGroupPrefix() {
		String prefix = "";
		
		for(PermissionGroup pg : getGroups()) {
			String s = pg.getPrefix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupChatPrefix() {
		String prefix = "";
		
		for(PermissionGroup pg : getGroups()) {
			String s = pg.getChatPrefix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupSuffix() {
		String prefix = "";
		
		for(PermissionGroup pg : getGroups()) {
			String s = pg.getSuffix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}
	
	public String getGroupChatSuffix() {
		String prefix = "";
		
		for(PermissionGroup pg : getGroups()) {
			String s = pg.getChatSuffix();
			
			if(s != null)
				prefix += s;
		}
		
		return prefix;
	}

	public PermissionGroup getHighestGroup() {
		PermissionGroup group = null;
		int lowestWeight = 999999999;
		
		for (PermissionGroup pg : getGroups()) {
			if(pg.getWeight() < lowestWeight) {
				lowestWeight = pg.getWeight();
				group = pg;
			}
		}
			
		return group;
	}
	
}