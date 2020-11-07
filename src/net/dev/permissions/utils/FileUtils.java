package net.dev.permissions.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import net.dev.permissions.PermissionSystem;

public class FileUtils {

	private File directory, file;
	private YamlConfiguration cfg;
	private PermissionSystem permissionSystem;
	
	public FileUtils() {
		permissionSystem = PermissionSystem.getInstance();
		
		PluginDescriptionFile desc = permissionSystem.getDescription();
		
		directory = new File("plugins/" + desc.getName() + "/");
		file = new File(directory, "config.yml");
		
		if(!(directory.exists()))
			directory.mkdir();
		
		if(!(file.exists())) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
			
			Bukkit.getOnlinePlayers().forEach(all -> all.kickPlayer("§3" + desc.getName() + " §ewas enabled for the first time§7!\n\n§aThank you for using it§7!"));
		}
		
		cfg = YamlConfiguration.loadConfiguration(file);
		
		cfg.addDefault("Messages.Prefix", "&8» &3System &7┃ ");
		cfg.addDefault("Messages.NoPerm", "&cYou are not allowed to perform this command&7!");
		cfg.addDefault("Settings.ReplaceChatFormat", true);
		cfg.addDefault("Settings.ChatFormat", "%prefix%%name%%suffix%&7: &f%message%");
		cfg.addDefault("Settings.UsePrefixesAndSuffixes", true);
		cfg.addDefault("Settings.RankKick", true);
		cfg.addDefault("Settings.RankKickMessage", "%prefix%\n\n&7You received the rank &3%rankName% &7from the player &e%rankSetter%\n&7Expiry&8: &3%expiryTime%");
		cfg.addDefault("Settings.RankKickExpiryFormat", "%years% years %months% months %days% days %hours% hours %minutes% minutes %seconds% seconds");
		cfg.addDefault("Settings.RankKickExpiryNever", "&c&lNever");
		cfg.addDefault("MySQL.Enabled", false);
		cfg.addDefault("MySQL.host", "localhost");
		cfg.addDefault("MySQL.port", "3306");
		cfg.addDefault("MySQL.database", "private");
		cfg.addDefault("MySQL.user", "root");
		cfg.addDefault("MySQL.password", "password");
		cfg.addDefault("WebServer.Enabled", false);
		cfg.addDefault("WebServer.DeleteFiles", true);
		cfg.addDefault("WebServer.Port", 125);
		cfg.options().copyDefaults(true);
		saveFile();
	}
	
	public void saveFile() {
		try {
			cfg.save(file);
		} catch (IOException e) {
		}
	}
	
	public void reloadConfig() {
		cfg = YamlConfiguration.loadConfiguration(file);
		
		Utils utils = permissionSystem.getUtils();
		
		utils.setPrefix(ChatColor.translateAlternateColorCodes('&', cfg.getString("Messages.Prefix")));
		utils.setNoPerm(ChatColor.translateAlternateColorCodes('&', cfg.getString("Messages.NoPerm")));
	}
	
	public File getFile() {
		return file;
	}
	
	public YamlConfiguration getConfig() {
		return cfg;
	}
	
}
