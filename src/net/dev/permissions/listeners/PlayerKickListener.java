package net.dev.permissions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.Utils;

public class PlayerKickListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent e) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utils utils = permissionSystem.getUtils();
		
		Player p = e.getPlayer();

		if (utils.getAttachments().containsKey(p.getUniqueId())) {
			p.removeAttachment(utils.getAttachments().get(p.getUniqueId()));
			utils.getAttachments().remove(p.getUniqueId());
		}

		if (permissionSystem.getFileUtils().getConfig().getBoolean("Settings.UsePrefixesAndSuffixes"))
			permissionSystem.getTeamUtils().removePlayerFromTeams(p.getName());
		
		if(utils.getDebugging().contains(p))
			utils.getDebugging().remove(p);
	}

}