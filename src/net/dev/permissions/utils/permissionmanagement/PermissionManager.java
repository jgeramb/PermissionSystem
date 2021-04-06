package net.dev.permissions.utils.permissionmanagement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PermissionManager {

	public void updateAllPermissions() {
		for (Player all : Bukkit.getOnlinePlayers()) {
			PermissionUser pAll = new PermissionUser(all.getUniqueId());
			
			pAll.updatePermissions();
		}
	}
	
}
