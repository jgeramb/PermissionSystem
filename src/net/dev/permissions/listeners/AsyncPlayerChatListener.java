package net.dev.permissions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.FileUtils;
import net.dev.permissions.utilities.permissionmanagement.PermissionUser;

public class AsyncPlayerChatListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		
		if (fileUtils.getConfiguration().getBoolean("Settings.ReplaceChatFormat")) {
			Player player = event.getPlayer();
			PermissionUser permissionUser = new PermissionUser(player.getUniqueId());

			String prefix = permissionUser.getGroupChatPrefix() + permissionUser.getChatPrefix();
			String suffix = permissionUser.getChatSuffix() + permissionUser.getGroupChatSuffix();

			if (permissionSystem.isEazyNickInstalled()) {
				if (new NickManager(player).isNicked()) {
					prefix = "";
					suffix = "";
				}
			}
			
			event.setFormat(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfiguration().getString("Settings.ChatFormat")).replace("%prefix%", prefix).replace("%suffix%", suffix).replace("%displayName%", player.getDisplayName()).replace("%name%", player.getName()).replace("%message%", event.getMessage().replaceAll("%", "%%")));
		}
	}

}