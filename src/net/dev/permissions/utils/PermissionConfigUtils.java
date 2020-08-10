package net.dev.permissions.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;

public class PermissionConfigUtils {

	private PermissionSystem permissionSystem;
	
	private File directory, file;
	private YamlConfiguration cfg;
	private int i = 0;
	
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
		
		if(ranks.size() >= 1) {
			ArrayList<String> toRemove = new ArrayList<>();
			
			for (String uuid : ranks) {
				String path = "Ranks." + uuid;
				
				int newTime = (cfg.getInt(path + ".Time") - 1);
				boolean hasGroupName = cfg.contains(path + ".GroupName");
				String groupName = cfg.getString(path + ".GroupName");
				
				if((newTime <= 0) || (hasGroupName && !(permissionSystem.getPermissionUserManager().getPermissionPlayer(UUID.fromString(uuid)).getGroupNames().contains(groupName)))) {
					toRemove.add(uuid);
					
					if(hasGroupName)
						new PermissionGroup(groupName).removeMemberWithUUID(uuid);
					
					cfg.set(path, null);
					saveFile();
					
					permissionSystem.updatePrefixesAndSuffixes();
				} else
					cfg.set(path + ".Time",  newTime);
			}
			
			i++;
			
			if(i == 10) {
				i = 0;
				
				if(toRemove.size() >= 1) {
					toRemove.forEach(uuid -> ranks.remove(uuid));
					
					cfg.set("TempRanks", ranks);
					saveFile();
					
					cfg = YamlConfiguration.loadConfiguration(file);
				}
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
