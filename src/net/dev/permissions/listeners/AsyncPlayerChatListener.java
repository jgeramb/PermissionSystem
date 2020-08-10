package net.dev.permissions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.FileUtils;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;

public class AsyncPlayerChatListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		
		if (fileUtils.getConfig().getBoolean("Settings.ReplaceChatFormat")) {
			Player p = e.getPlayer();
			PermissionUser pp = new PermissionUser(p.getUniqueId());

			String prefix = pp.getGroupChatPrefix() + pp.getChatPrefix();
			String suffix = pp.getChatSuffix() + pp.getGroupChatSuffix();

			if (permissionSystem.isEazyNickInstalled()) {
				if (new NickManager(p).isNicked()) {
					prefix = "";
					suffix = "";
				}
			}
			
			e.setFormat(ChatColor.translateAlternateColorCodes('&', fileUtils.getConfig().getString("Settings.ChatFormat")).replace("%prefix%", prefix).replace("%suffix%", suffix).replace("%displayName%", p.getDisplayName()).replace("%name%", p.getName()).replace("%message%", e.getMessage().replaceAll("%", "%%")));
		}
	}

}