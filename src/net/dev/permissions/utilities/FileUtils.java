package net.dev.permissions.utilities;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import net.dev.permissions.PermissionSystem;

public class FileUtils {

	private File file;
	private YamlConfiguration configuration;
	private PermissionSystem permissionSystem;
	
	public FileUtils() {
		permissionSystem = PermissionSystem.getInstance();
		
		PluginDescriptionFile desc = permissionSystem.getDescription();
		
		File directory = new File("plugins/" + desc.getName() + "/");
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
		
		configuration = YamlConfiguration.loadConfiguration(file);
		
		configuration.addDefault("Messages.Prefix", "&8» &3System &7┃ ");
		configuration.addDefault("Messages.NoPerm", "&cYou are not allowed to perform this command&7!");
		configuration.addDefault("Settings.ReplaceChatFormat", true);
		configuration.addDefault("Settings.ChatFormat", "%prefix%%name%%suffix%&7: &f%message%");
		configuration.addDefault("Settings.UsePrefixesAndSuffixes", true);
		configuration.addDefault("Settings.RankKick", true);
		configuration.addDefault("Settings.RankKickMessage", "&7You received the rank &3%rankName% &7from the player &e%rankSetter%\n&7Expiry&8: &3%expiryTime%");
		configuration.addDefault("Settings.RankKickExpiryFormat", "%years% years %months% months %days% days %hours% hours %minutes% minutes %seconds% seconds");
		configuration.addDefault("Settings.RankKickExpiryNever", "&c&lNever");
		configuration.addDefault("Settings.MojangFetching", true);
		configuration.addDefault("MySQL.Enabled", false);
		configuration.addDefault("MySQL.host", "localhost");
		configuration.addDefault("MySQL.port", "3306");
		configuration.addDefault("MySQL.database", "private");
		configuration.addDefault("MySQL.user", "root");
		configuration.addDefault("MySQL.password", "password");
		configuration.addDefault("WebServer.Enabled", false);
		configuration.addDefault("WebServer.DeleteFiles", true);
		configuration.addDefault("WebServer.Port", 125);
		configuration.options().copyDefaults(true);
		saveFile();
	}
	
	public void saveFile() {
		try {
			configuration.save(file);
		} catch (IOException e) {
		}
	}
	
	public void reloadConfig() {
		configuration = YamlConfiguration.loadConfiguration(file);
		
		Utilities utilities = permissionSystem.getUtils();
		
		utilities.setPrefix(ChatColor.translateAlternateColorCodes('&', configuration.getString("Messages.Prefix")));
		utilities.setNoPerm(ChatColor.translateAlternateColorCodes('&', configuration.getString("Messages.NoPerm")));
	}
	
	public File getFile() {
		return file;
	}
	
	public YamlConfiguration getConfiguration() {
		return configuration;
	}
	
}
