package net.dev.permissions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;
import net.dev.permissions.utils.reflect.TeamUtils;

public class PlayerJoinListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		TeamUtils teamUtils = permissionSystem.getTeamUtils();
		
		Player p = e.getPlayer();
		
		PermissionSystem.getInstance().inject(p);
		
		new PermissionUser(p.getUniqueId()).updatePermissions();
		
		permissionSystem.updatePrefixesAndSuffixes();

		if (permissionSystem.getFileUtils().getConfig().getBoolean("Settings.UsePrefixesAndSuffixes")) {
			if (permissionSystem.isEazyNickInstalled()) {
				if (!(new NickManager(p).isNicked()))
					teamUtils.addPlayerToTeam(teamUtils.getTeamName(p), p.getName());
			} else
				teamUtils.addPlayerToTeam(teamUtils.getTeamName(p), p.getName());
		}
	}

}
