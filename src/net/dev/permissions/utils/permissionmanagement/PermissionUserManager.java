package net.dev.permissions.utils.permissionmanagement;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dev.permissions.PermissionSystem;

public class PermissionUserManager {

	private PermissionSystem permissionSystem;
	
	public PermissionUserManager() {
		this.permissionSystem = PermissionSystem.getInstance();
	}
	
	public PermissionUser getPermissionPlayer(String name) {
		Player p = Bukkit.getPlayer(name);
		
		return ((p != null) ? new PermissionUser(p.getUniqueId()) : new PermissionUser(permissionSystem.getUUIDFetching().fetchUUID(name)));
	}
	
	public PermissionUser getPermissionPlayer(UUID uuid) {
		return (new PermissionUser(uuid));
	}
	
	public Player getPlayerByUUID(String uuid) {
		return Bukkit.getPlayer(UUID.fromString(uuid));
	}

	public List<String> getPermissionPlayerUUIDs() {
		if(permissionSystem.getFileUtils().getConfig().getBoolean("MySQL.Enabled"))
			return permissionSystem.getMySQLPermissionManager().getUUIDCache();
		
		return (permissionSystem.getPermissionConfigUtils().getConfig().getStringList("Players.PlayerUUIDCache"));
	}
	
}
