package net.dev.permissions.utilities.permissionmanagement;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.mojang.UUIDFetching;

public class PermissionGroup {

	private PermissionSystem permissionSystem;
	private Utilities utilities;
	private FileUtils fileUtils;
	private PermissionConfigUtils permissionConfigUtils;
	private MySQLPermissionManager mysqlPermissionManager;
	private UUIDFetching uuidFetching;
	
	private String groupName;
	
	public PermissionGroup(String groupName) {
		this.groupName = groupName.toUpperCase();
		
		this.permissionSystem = PermissionSystem.getInstance();
		this.utilities = permissionSystem.getUtils();
		this.fileUtils = permissionSystem.getFileUtils();
		this.permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		this.mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		this.uuidFetching = permissionSystem.getUUIDFetching();
	}

	public void deleteGroup() {
		if (exists()) {
			if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
				mysqlPermissionManager.removeGroup(groupName);
			else {
				List<String> groupNames = permissionConfigUtils.getConfiguration().getStringList("Groups.GroupNames");

				if (groupNames.contains(groupName))
					groupNames.remove(groupName);

				permissionConfigUtils.getConfiguration().set("Groups.GroupNames", groupNames);

				String path = "Groups." + groupName;

				permissionConfigUtils.getConfiguration().set(path, null);
				permissionConfigUtils.saveFile();
			}
			
			utilities.sendDebugMessage("§eGroup §a" + groupName + " §ewas deleted§7!");
		}
	}

	public boolean exists() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.isGroupRegistered(groupName);

		return (permissionConfigUtils.getConfiguration().get("Groups." + groupName) != null);
	}

	public void addPermission(String permission) {
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupPermissions(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Permissions");

		if (!(list.contains(permission.toLowerCase())))
			list.add(permission.toLowerCase());

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupPermissions(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eAdded permission §a\"" + permission + "\" §eto group §d" + groupName + "§7!");

		updatePermissions();
	}

	public void removePermission(String permission) {
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupPermissions(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Permissions");

		if (list.contains(permission.toLowerCase()))
			list.remove(permission.toLowerCase());

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupPermissions(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Permissions", list);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eRemoved permission §a\"" + permission + "\" §efrom group §d" + groupName + "§7!");

		updatePermissions();
	}

	public void clearPermissions() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupPermissions(groupName, new ArrayList<>().toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Permissions", new ArrayList<String>());
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eCleared permissions of group §a" + groupName + "§7!");

		updatePermissions();
	}

	public List<String> getPermissions() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.getGroupPermissions(groupName);

		return permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Permissions");
	}

	public void updatePermissions() {
		for (Player player : getOnlineMembers()) {
			PermissionUser permissionUser = new PermissionUser(player.getUniqueId());
			
			permissionUser.updatePermissions();
		}
	}

	public void addMember(String name, boolean update) {
		Player player = Bukkit.getPlayer(name);
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupMembers(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Members");

		if (player != null) {
			String uuid = player.getUniqueId().toString();

			if (!(list.contains(uuid)))
				list.add(uuid);
		} else {
			String uuid = uuidFetching.fetchUUID(name).toString();

			if (!(list.contains(uuid)))
				list.add(uuid);
		}

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupMembers(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", list);
			permissionConfigUtils.saveFile();
		}

		if (update) {
			utilities.sendDebugMessage("§ePlayer §a" + name + " §ewas added to group §d" + groupName + "§7!");
			
			updatePermissions();
		}
	}

	public void removeMember(String name, boolean update) {
		Player player = Bukkit.getPlayer(name);
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupMembers(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Members");

		if (player != null) {
			String uuid = player.getUniqueId().toString();

			if (list.contains(uuid))
				list.remove(uuid);
		} else {
			String uuid = uuidFetching.fetchUUID(name).toString();

			if (list.contains(uuid))
				list.remove(uuid);
		}

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupMembers(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", list);
			permissionConfigUtils.saveFile();
		}

		if (update) {
			utilities.sendDebugMessage("§ePlayer §a" + name + " §ewas removed from group §d" + groupName + "§7!");
			
			updatePermissions();
		}
	}

	public void addMemberWithUUID(String uuid) {
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupMembers(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Members");

		if (!(list.contains(uuid)))
			list.add(uuid);

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupMembers(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", list);
			permissionConfigUtils.saveFile();
		}

		updatePermissions();
	}

	public void removeMemberWithUUID(String uuid) {
		List<String> list;

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			list = mysqlPermissionManager.getGroupMembers(groupName);
		else
			list = permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Members");


		if (list.contains(uuid))
			list.remove(uuid);

		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupMembers(groupName, list.toString());
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", list);
			permissionConfigUtils.saveFile();
		}

		updatePermissions();
	}

	public List<String> getMemberUUIDs() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.getGroupMembers(groupName);

		return permissionConfigUtils.getConfiguration().getStringList("Groups." + groupName + ".Members");
	}

	public List<Player> getOnlineMembers() {
		List<Player> list = new ArrayList<>();

		for (String uuid : getMemberUUIDs()) {
			Player player = Bukkit.getPlayer(UUID.fromString(uuid));

			if (player != null)
				list.add(player);
		}

		return list;
	}

	public void registerGroupIfNotExisting() {
		if(!(exists())) {
			if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
				mysqlPermissionManager.setGroupPermissions(groupName, new ArrayList<>().toString());
				mysqlPermissionManager.setGroupMembers(groupName, "[]");
				mysqlPermissionManager.setValues(new HashMap<>());
				mysqlPermissionManager.collectGroupData();
			} else {
				List<String> groupNames = permissionConfigUtils.getConfiguration().getStringList("Groups.GroupNames");
				
				if (!(groupNames.contains(groupName)))
					groupNames.add(groupName);
				
				permissionConfigUtils.getConfiguration().set("Groups.GroupNames", groupNames);
				permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Permissions", new ArrayList<String>());
				permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", new ArrayList<String>());
				permissionConfigUtils.saveFile();
			}
			
			setPrefix("");
			setChatPrefix("");
			setSuffix("");
			setChatSuffix("");
			setDefault(permissionSystem.getPermissionGroupManager().getPermissionGroups().isEmpty());
			setWeight(0);
			
			utilities.sendDebugMessage("§eGroup §a" + groupName + " §ewas created§7!");
		}
	}

	public void setDefault(boolean value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setIsDefault(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.IsDefaultGroup", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aIsDefaultGroup §eof group §d" + groupName + " §ewas updated§8: §d" + value);
	}

	public void setPrefix(String value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupPrefix(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.Prefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aPrefix §eof group §d" + groupName + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}

	public void setChatPrefix(String value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupChatPrefix(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.ChatPrefix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aChatPrefix §eof group §d" + groupName + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}

	public void setSuffix(String value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupSuffix(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.Suffix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aSuffix §eof group §d" + groupName + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void setChatSuffix(String value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupChatSuffix(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.ChatSuffix", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aChatSuffix §eof group §d" + groupName + " §ewas updated§8: §f" + ChatColor.translateAlternateColorCodes('&', value));	}

	public void setWeight(int value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupWeight(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Options.Weight", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe value of the option §aWeight §eof group §d" + groupName + " §ewas updated§8: §d" + value);
	}
	
	public void setParent(String value) {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			mysqlPermissionManager.setGroupParent(groupName, value);
		else {
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Parent", value);
			permissionConfigUtils.saveFile();
		}
		
		utilities.sendDebugMessage("§eThe parent of the group §a" + groupName + " §ewas updated§8: §d" + value);
	}

	public String getPrefix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getGroupPrefix(groupName) : permissionConfigUtils.getConfiguration().getString("Groups." + groupName + ".Options.Prefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : "");
	}
	
	public String getChatPrefix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getGroupChatPrefix(groupName) : permissionConfigUtils.getConfiguration().getString("Groups." + groupName + ".Options.ChatPrefix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : "");
	}

	public String getSuffix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getGroupSuffix(groupName) : permissionConfigUtils.getConfiguration().getString("Groups." + groupName + ".Options.Suffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : "");
	}

	public String getChatSuffix() {
		String s = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getGroupChatSuffix(groupName) : permissionConfigUtils.getConfiguration().getString("Groups." + groupName + ".Options.ChatSuffix");
		
		return ((s != null) ? ChatColor.translateAlternateColorCodes('&', s) : "");
	}

	public int getWeight() {
		int weight = fileUtils.getConfiguration().getBoolean("MySQL.Enabled") ? mysqlPermissionManager.getGroupWeight(groupName) : permissionConfigUtils.getConfiguration().getInt("Groups." + groupName + ".Options.Weight");
		
		if(weight > 999)
			weight = 999;
		
		return weight;
	}
	
	public String getParent() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.getGroupParent(groupName);

		return permissionConfigUtils.getConfiguration().getString("Groups." + groupName + ".Parent");
	}
	
	public boolean isDefault() {
		if (fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
			return mysqlPermissionManager.isDefaultGroup(groupName);

		return permissionConfigUtils.getConfiguration().getBoolean("Groups." + groupName + ".Options.IsDefaultGroup");
	}

	public String getName() {
		return groupName;
	}

}