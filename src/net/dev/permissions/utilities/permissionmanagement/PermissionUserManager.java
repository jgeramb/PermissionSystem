package net.dev.permissions.utilities.permissionmanagement;

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
		Player player = Bukkit.getPlayer(name);
		
		return ((player != null) ? new PermissionUser(player.getUniqueId()) : new PermissionUser(permissionSystem.getUUIDFetching().fetchUUID(name)));
	}
	
	public PermissionUser getPermissionPlayer(UUID uuid) {
		return (new PermissionUser(uuid));
	}
	
	public Player getPlayerByUUID(String uuid) {
		return Bukkit.getPlayer(UUID.fromString(uuid));
	}

	public List<String> getPermissionPlayerUUIDs() {
		if(permissionSystem.getFileUtils().getConfiguration().getBoolean("MySQL.Enabled"))
			return permissionSystem.getMySQLPermissionManager().getUUIDCache();
		
		return (permissionSystem.getPermissionConfigUtils().getConfiguration().getStringList("Players.PlayerUUIDCache"));
	}
	
}
