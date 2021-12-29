package net.dev.permissions.utilities;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.configuration.file.YamlConfiguration;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.permissionmanagement.PermissionGroup;

public class PermissionConfigUtils {

	private PermissionSystem permissionSystem;
	
	private File file;
	private YamlConfiguration configuration;
	
	public PermissionConfigUtils() {
		permissionSystem = PermissionSystem.getInstance();
		
		File directory = new File("plugins/" + permissionSystem.getDescription().getName() + "/");
		file = new File(directory, "permissions.yml");
		
		if(!(directory.exists()))
			directory.mkdir();
		
		if(!(file.exists())) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}
		
		configuration = YamlConfiguration.loadConfiguration(file);
		
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
	}
	
	public void updateTempRanks() {
		List<String> ranks = configuration.getStringList("TempRanks");
		
		for (String uuid : new ArrayList<>(ranks)) {
			String path = "Ranks." + uuid;
			
			boolean hasGroupName = configuration.contains(path + ".GroupName");
			String groupName = configuration.getString(path + ".GroupName");
			
			if((configuration.getLong(path + ".Time") <= System.currentTimeMillis()) || (hasGroupName && !(permissionSystem.getPermissionUserManager().getPermissionPlayer(UUID.fromString(uuid)).getGroupNames().contains(groupName)))) {
				ranks.remove(uuid);
				
				if(hasGroupName)
					new PermissionGroup(groupName).removeMemberWithUUID(uuid);
				
				configuration.set(path, null);
				configuration.set("TempRanks", ranks);
				saveFile();
				
				permissionSystem.updatePrefixesAndSuffixes();
			}
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public YamlConfiguration getConfiguration() {
		return configuration;
	}
	
}
