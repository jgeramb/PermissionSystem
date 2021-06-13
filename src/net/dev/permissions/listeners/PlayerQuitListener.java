package net.dev.permissions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.Utils;

public class PlayerQuitListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
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
