package net.dev.permissions.utilities.mojang;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.FileUtils;

public class UUIDFetching {

	private FileUtils fileUtils;
	private UUIDFetcher_1_7 uuidFetcher_1_7;
	private UUIDFetcher_1_8_R1 uuidFetcher_1_8_R1;
	private UUIDFetcher uuidFetcher;
	private String version;
	
	public UUIDFetching() {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		
		this.fileUtils = permissionSystem.getFileUtils();
		this.uuidFetcher_1_7 = permissionSystem.getUUIDFetcher_1_7();
		this.uuidFetcher_1_8_R1 = permissionSystem.getUUIDFetcher_1_8_R1();
		this.uuidFetcher = permissionSystem.getUUIDFetcher();
		this.version = permissionSystem.getReflectionHelper().getVersion();
	}
	
	public UUID fetchUUID(String name) {
		if(fileUtils.getConfiguration().getBoolean("Settings.MojangFetching"))
			return (version.equals("1_7_R4") ? uuidFetcher_1_7.getUUID(name) : (version.equals("1_8_R1") ? uuidFetcher_1_8_R1.getUUID(name) : uuidFetcher.getUUID(name)));
		else {
			Optional<OfflinePlayer> offlinePlayerOptional = Arrays.asList(Bukkit.getOfflinePlayers()).stream().filter(currentOfflinePlayer -> currentOfflinePlayer.getName().equals(name)).findFirst();
			
			if(offlinePlayerOptional.isPresent())
				return offlinePlayerOptional.get().getUniqueId();
		}
		
		return null;
	}
	
	public String fetchName(UUID uuid) {
		if(fileUtils.getConfiguration().getBoolean("Settings.MojangFetching"))
			return (version.equals("1_7_R4") ? uuidFetcher_1_7.getName(uuid) : (version.equals("1_8_R1") ? uuidFetcher_1_8_R1.getName(uuid) : uuidFetcher.getName(uuid)));
		else {
			Optional<OfflinePlayer> offlinePlayerOptional = Arrays.asList(Bukkit.getOfflinePlayers()).stream().filter(currentOfflinePlayer -> currentOfflinePlayer.getUniqueId().equals(uuid)).findFirst();
			
			if(offlinePlayerOptional.isPresent())
				return offlinePlayerOptional.get().getName();
		}
		
		return null;
	}
	
}
