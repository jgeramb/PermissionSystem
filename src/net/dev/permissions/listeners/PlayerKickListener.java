package net.dev.permissions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerKickEvent;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.Utilities;

public class PlayerKickListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utilities utilities = permissionSystem.getUtils();
		
		Player player = event.getPlayer();

		if (utilities.getAttachments().containsKey(player.getUniqueId())) {
			player.removeAttachment(utilities.getAttachments().get(player.getUniqueId()));
			utilities.getAttachments().remove(player.getUniqueId());
		}

		if (permissionSystem.getFileUtils().getConfiguration().getBoolean("Settings.UsePrefixesAndSuffixes"))
			permissionSystem.getScoreboardTeamHandler().removePlayerFromTeams(player.getName());
		
		if(utilities.getDebugging().contains(player))
			utilities.getDebugging().remove(player);
	}

}