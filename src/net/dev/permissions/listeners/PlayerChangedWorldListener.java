package net.dev.permissions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import net.dev.permissions.utilities.permissionmanagement.PermissionUser;

public class PlayerChangedWorldListener implements Listener {

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		new PermissionUser(event.getPlayer().getUniqueId()).updatePermissions();
	}

}
