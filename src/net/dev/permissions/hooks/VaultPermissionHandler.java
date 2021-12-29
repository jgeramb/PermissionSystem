package net.dev.permissions.hooks;

import java.util.ArrayList;
import java.util.Optional;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.permissionmanagement.*;
import net.milkbowl.vault.permission.Permission;

public class VaultPermissionHandler extends Permission {

	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	
	public VaultPermissionHandler(PermissionSystem permissionSystem) {
		this.permissionGroupManager = permissionSystem.getPermissionGroupManager();
		this.permissionUserManager = permissionSystem.getPermissionUserManager();
	}
	
	@Override
	public String getName() {
		return "PermissionSystem";
	}
	
	@Override
	public boolean hasGroupSupport() {
		return true;
	}

	@Override
	public boolean hasSuperPermsCompat() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String[] getGroups() {
		ArrayList<String> list = new ArrayList<>();
		
		permissionGroupManager.getPermissionGroups().forEach(permissionGroup -> list.add(permissionGroup.getName()));
		
		return list.toArray(new String[0]);
	}

	@Override
	public boolean groupAdd(String world, String group, String permission) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent()) {
			optionalPermissionGroup.get().addPermission(permission);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean groupRemove(String world, String group, String permission) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent()) {
			optionalPermissionGroup.get().removePermission(permission);
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean groupHas(String world, String group, String permission) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent())
			return optionalPermissionGroup.get().getPermissions().contains(permission.toLowerCase());
		
		return false;
	}

	@Override
	public String[] getPlayerGroups(String world, String player) {
		ArrayList<String> list = new ArrayList<>();
		
		permissionUserManager.getPermissionPlayer(player).getGroups().forEach(permissionGroup -> list.add(permissionGroup.getName()));
		
		return list.toArray(new String[0]);
	}

	@Override
	public String getPrimaryGroup(String world, String player) {
		return permissionUserManager.getPermissionPlayer(player).getHighestGroup().getName();
	}

	@Override
	public boolean playerAdd(String world, String player, String permission) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null) {
			permissionUser.addPermission(permission);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean playerRemove(String world, String player, String permission) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null) {
			permissionUser.removePermission(permission);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean playerHas(String world, String player, String permission) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			return permissionUser.getPermissions().contains(permission.toLowerCase());
		
		return false;
	}

	@Override
	public boolean playerAddGroup(String world, String player, String group) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent()) {
			optionalPermissionGroup.get().addMember(player, true);
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean playerRemoveGroup(String world, String player, String group) {
		Optional<PermissionGroup> optionalPermissionGroup = permissionGroupManager.getPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group)).findFirst();
		
		if(optionalPermissionGroup.isPresent()) {
			optionalPermissionGroup.get().removeMember(player, true);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean playerInGroup(String world, String player, String group) {
		PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(player);
		
		if(permissionUser != null)
			return permissionUser.getGroups().stream().anyMatch(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(group));
		
		return false;
	}
	
}
