package net.dev.permissions.utils.permissionmanagement;

import java.util.ArrayList;
import java.util.List;

import net.dev.permissions.PermissionSystem;

public class PermissionGroupManager {

	public List<PermissionGroup> getPermissionGroups() {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		
		List<String> groupNames;
		
		if(permissionSystem.getFileUtils().getConfig().getBoolean("MySQL.Enabled"))
			groupNames = permissionSystem.getMySQLPermissionManager().getGroupNames();
		else
			groupNames = permissionSystem.getPermissionConfigUtils().getConfig().getStringList("Groups.GroupNames");
		
		List<PermissionGroup> permissionGroups = new ArrayList<>();
		
		for(String groupName : groupNames)
			permissionGroups.add( new PermissionGroup(groupName));
		
		return permissionGroups;
	}
	
}