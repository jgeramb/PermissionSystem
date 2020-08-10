package net.dev.permissions.utils.fetching;

import java.util.UUID;

import net.dev.permissions.PermissionSystem;

public class UUIDFetching {

	private UUIDFetcher_1_7 uuidFetcher_1_7;
	private UUIDFetcher_1_8_R1 uuidFetcher_1_8_R1;
	private UUIDFetcher uuidFetcher;
	private String version;
	
	public UUIDFetching() {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		
		this.uuidFetcher_1_7 = permissionSystem.getUUIDFetcher_1_7();
		this.uuidFetcher_1_8_R1 = permissionSystem.getUUIDFetcher_1_8_R1();
		this.uuidFetcher = permissionSystem.getUUIDFetcher();
		this.version = permissionSystem.getReflectUtils().getVersion();
	}
	
	public UUID fetchUUID(String name) {
		return (version.equals("v1_7_R4") ? uuidFetcher_1_7.getUUID(name) : (version.equals("v1_8_R1") ? uuidFetcher_1_8_R1.getUUID(name) : uuidFetcher.getUUID(name)));
	}
	
	public String fetchName(UUID uuid) {
		return (version.equals("v1_7_R4") ? uuidFetcher_1_7.getName(uuid) : (version.equals("v1_8_R1") ? uuidFetcher_1_8_R1.getName(uuid) : uuidFetcher.getName(uuid)));
	}
	
}
