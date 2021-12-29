package net.dev.permissions.hooks;

import java.util.Optional;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.permissionmanagement.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class VaultChatHandler extends Chat {

	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	
	public VaultChatHandler(PermissionSystem permissionSystem, Permission permission) {
		super(permission);
		
		this.permissionGroupManager = permissionSystem.getPermissionGroupManager();
		this.permissionUserManager = permissionSystem.getPermissionUserManager();
	}

	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	//Group prefixes & suffixes
	
	@Override
	public void setGroupPrefix(String world, String group, String chatPrefix) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent())
			optionalPermissionGroup.get().setChatPrefix(chatPrefix);
	}

	@Override
	public void setGroupSuffix(String world, String group, String chatSuffix) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent())
			optionalPermissionGroup.get().setChatSuffix(chatSuffix);
	}
	
	@Override
	public String getGroupPrefix(String world, String group) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent())
			return optionalPermissionGroup.get().getChatPrefix();
		
		return "";
	}

	@Override
	public String getGroupSuffix(String world, String group) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent())
			return optionalPermissionGroup.get().getChatSuffix();
		
		return "";
	}
	
	//Player playerrefixes & suffixes


	@Override
	public void setPlayerPrefix(String world, String player, String chatPrefix) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			permissionUser.setChatPrefix(chatPrefix);
	}

	@Override
	public void setPlayerSuffix(String world, String player, String chatSuffix) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			permissionUser.setChatSuffix(chatSuffix);
	}
	
	@Override
	public String getPlayerPrefix(String world, String player) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			return permissionUser.getChatPrefix();
		
		return "";
	}

	@Override
	public String getPlayerSuffix(String world, String player) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			return permissionUser.getChatSuffix();
		
		return "";
	}
	
	//Group info nodes

	@Override
	public void setGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
	}

	@Override
	public void setGroupInfoDouble(String world, String group, String node, double defaultValue) {
	}

	@Override
	public void setGroupInfoInteger(String world, String group, String node, int defaultValue) {
	}

	@Override
	public void setGroupInfoString(String world, String group, String node, String defaultValue) {
	}
	
	@Override
	public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
		return defaultValue;
	}

	@Override
	public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
		return defaultValue;
	}

	@Override
	public String getGroupInfoString(String world, String group, String node, String defaultValue) {
		return defaultValue;
	}

	//Player info nodes
	
	@Override
	public void setPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
	}

	@Override
	public void setPlayerInfoDouble(String world, String player, String node, double defaultValue) {
	}

	@Override
	public void setPlayerInfoInteger(String world, String player, String node, int defaultValue) {
	}

	@Override
	public void setPlayerInfoString(String world, String player, String node, String defaultValue) {
	}
	
	@Override
	public boolean getPlayerInfoBoolean(String world, String group, String node, boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double getPlayerInfoDouble(String world, String group, String node, double defaultValue) {
		return defaultValue;
	}

	@Override
	public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
		return defaultValue;
	}

	@Override
	public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
		return defaultValue;
	}
	
}