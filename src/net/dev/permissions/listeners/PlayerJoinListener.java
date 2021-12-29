package net.dev.permissions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.nms.ScoreboardTeamHandler;
import net.dev.permissions.utilities.permissionmanagement.PermissionUser;

public class PlayerJoinListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		ScoreboardTeamHandler scoreboardTeamHandler = permissionSystem.getScoreboardTeamHandler();
		
		Player player = event.getPlayer();
		
		PermissionSystem.getInstance().inject(player);
		
		new PermissionUser(player.getUniqueId()).updatePermissions();
		
		permissionSystem.updatePrefixesAndSuffixes();

		if (permissionSystem.getFileUtils().getConfiguration().getBoolean("Settings.UsePrefixesAndSuffixes")) {
			if (permissionSystem.isEazyNickInstalled()) {
				if (!(new NickManager(player).isNicked()))
					scoreboardTeamHandler.addPlayerToTeam(scoreboardTeamHandler.getTeamName(player), player.getName());
			} else
				scoreboardTeamHandler.addPlayerToTeam(scoreboardTeamHandler.getTeamName(player), player.getName());
		}
	}

}
