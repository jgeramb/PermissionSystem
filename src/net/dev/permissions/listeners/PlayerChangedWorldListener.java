package net.dev.permissions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import net.dev.permissions.utils.permissionmanagement.PermissionUser;

public class PlayerChangedWorldListener implements Listener {

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		new PermissionUser(e.getPlayer().getUniqueId()).updatePermissions();
	}

}
