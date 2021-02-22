package net.dev.permissions.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.configuration.file.YamlConfiguration;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;

public class PermissionConfigUtils {

	private PermissionSystem permissionSystem;
	
	private File directory, file;
	private YamlConfiguration cfg;
	
	public PermissionConfigUtils() {
		permissionSystem = PermissionSystem.getInstance();
		
		directory = new File("plugins/" + permissionSystem.getDescription().getName() + "/");
		file = new File(directory, "permissions.yml");
		
		if(!(directory.exists()))
			directory.mkdir();
		
		if(!(file.exists())) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}
		
		cfg = YamlConfiguration.loadConfiguration(file);
		
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
	}
	
	public void updateTempRanks() {
		List<String> ranks = cfg.getStringList("TempRanks");
		
		for (String uuid : new ArrayList<>(ranks)) {
			String path = "Ranks." + uuid;
			
			boolean hasGroupName = cfg.contains(path + ".GroupName");
			String groupName = cfg.getString(path + ".GroupName");
			
			if((cfg.getLong(path + ".Time") <= System.currentTimeMillis()) || (hasGroupName && !(permissionSystem.getPermissionUserManager().getPermissionPlayer(UUID.fromString(uuid)).getGroupNames().contains(groupName)))) {
				ranks.remove(uuid);
				
				if(hasGroupName)
					new PermissionGroup(groupName).removeMemberWithUUID(uuid);
				
				cfg.set(path, null);
				cfg.set("TempRanks", ranks);
				saveFile();
				
				permissionSystem.updatePrefixesAndSuffixes();
			}
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public YamlConfiguration getConfig() {
		return cfg;
	}
	
}
