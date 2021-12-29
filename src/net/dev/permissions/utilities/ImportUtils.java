package net.dev.permissions.utilities;

import java.util.*;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQL;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.permissionmanagement.*;

public class ImportUtils {

	private PermissionSystem permissionSystem;
	private MySQL mysql;
	private MySQLPermissionManager mysqlPermissionManager;
	private FileUtils fileUtils;
	private PermissionConfigUtils permissionConfigUtils;
	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	
	public ImportUtils() {
		permissionSystem = PermissionSystem.getInstance();
		mysql = permissionSystem.getMySQL();
		mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		fileUtils = permissionSystem.getFileUtils();
		permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		permissionGroupManager = permissionSystem.getPermissionGroupManager();
		permissionUserManager = permissionSystem.getPermissionUserManager();
	}
	
	public void importFromSQL() {
		permissionConfigUtils.getFile().delete();
		
		permissionSystem.setPermissionConfigUtils(new PermissionConfigUtils());
		
		permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		
		mysqlPermissionManager.setValues(new HashMap<>());
		mysqlPermissionManager.collectUserData();
		mysqlPermissionManager.collectGroupData();
		
		List<PermissionGroup> groups = permissionGroupManager.getPermissionGroups();
		List<String> uuids = permissionUserManager.getPermissionPlayerUUIDs();
		
		for (PermissionGroup group : groups) {
			String groupName = group.getName();
			String prefix = group.getPrefix();
			String chatPrefix = group.getChatPrefix();
			String suffix = group.getSuffix();
			String chatSuffix = group.getChatSuffix();
			int  weight = group.getWeight();
			boolean isDefault = group.isDefault();
			String parent = group.getParent();
			List<String> permissions = group.getPermissions();
			List<String> members = group.getMemberUUIDs();
			
			fileUtils.getConfiguration().set("MySQL.Enabled", false);
			
			PermissionGroup fileGroup = new PermissionGroup(groupName);
			fileGroup.registerGroupIfNotExisting();

			if(prefix != null)
				fileGroup.setPrefix(prefix);

			if(chatPrefix != null)
				fileGroup.setChatPrefix(chatPrefix);

			if(suffix != null)
				fileGroup.setSuffix(suffix);

			if(chatSuffix != null)
				fileGroup.setChatSuffix(chatSuffix);
			
			fileGroup.setWeight(weight);
			fileGroup.setDefault(isDefault);
			fileGroup.setParent(parent);
			
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Permissions", permissions);
			permissionConfigUtils.getConfiguration().set("Groups." + groupName + ".Members", members);
			
			fileUtils.getConfiguration().set("MySQL.Enabled", true);
		}
		
		for (String uuid : uuids) {
			List<String> players = permissionConfigUtils.getConfiguration().getStringList("Players.PlayerUUIDCache");
			
			if(!(players.contains(uuid)))
				players.add(uuid);
			
			permissionConfigUtils.getConfiguration().set("Players.PlayerUUIDCache", players);
			permissionConfigUtils.saveFile();
			
			PermissionUser user = permissionUserManager.getPermissionPlayer(UUID.fromString(uuid));
			String prefix = user.getPrefix();
			String chatPrefix = user.getChatPrefix();
			String suffix = user.getSuffix();
			String chatSuffix = user.getChatSuffix();
			List<String> permissions = user.getPermissions();
			String tempGroupName = mysqlPermissionManager.getPlayerTempGroupName(uuid);
			long tempGroupTime = mysqlPermissionManager.getPlayerTempGroupTime(uuid);
			
			fileUtils.getConfiguration().set("MySQL.Enabled", false);
			
			PermissionUser fileUser = new PermissionUser(UUID.fromString(uuid));
			
			if(prefix != null)
				fileUser.setPrefix(prefix);

			if(chatPrefix != null)
				fileUser.setChatPrefix(chatPrefix);

			if(suffix != null)
				fileUser.setSuffix(suffix);

			if(chatSuffix != null)
				fileUser.setChatSuffix(chatSuffix);
			
			permissionConfigUtils.getConfiguration().set("Players." + uuid + ".Permissions", permissions);
			
			if(tempGroupName != null) {
				List<String> ranks = permissionConfigUtils.getConfiguration().getStringList("TempRanks");
				
				if(!(ranks.contains(uuid)))
					ranks.add(uuid);
				
				permissionConfigUtils.getConfiguration().set("TempRanks", ranks);
				permissionConfigUtils.getConfiguration().set("Ranks." + uuid + ".GroupName", tempGroupName);
				permissionConfigUtils.getConfiguration().set("Ranks." + uuid + ".Time", tempGroupTime);
			}
			
			fileUtils.getConfiguration().set("MySQL.Enabled", true);
			
			user.updatePermissions();
		}
		
		permissionSystem.updatePrefixesAndSuffixes();
		
		permissionConfigUtils.saveFile();
		fileUtils.saveFile();
	}

	public void importFromFile() {
		permissionConfigUtils.reloadConfig();
		
		mysql.update("TRUNCATE PermissionUsers");
		mysql.update("TRUNCATE PermissionGroups");
		mysql.update("TRUNCATE UUIDCache");
		
		mysqlPermissionManager.setValues(new HashMap<>());
		
		fileUtils.getConfiguration().set("MySQL.Enabled", false);
		fileUtils.saveFile();
		
		List<PermissionGroup> groups = permissionGroupManager.getPermissionGroups();
		List<String> uuids = permissionUserManager.getPermissionPlayerUUIDs();
		
		for (PermissionGroup group : groups) {
			fileUtils.getConfiguration().set("MySQL.Enabled", false);
			
			String groupName = group.getName();
			String prefix = group.getPrefix();
			String chatPrefix = group.getChatPrefix();
			String suffix = group.getSuffix();
			String chatSuffix = group.getChatSuffix();
			int  weight = group.getWeight();
			boolean isDefault = group.isDefault();
			String parent = group.getParent();
			List<String> permissions = group.getPermissions();
			List<String> members = group.getMemberUUIDs();
			
			fileUtils.getConfiguration().set("MySQL.Enabled", true);
			
			PermissionGroup sqlGroup = new PermissionGroup(groupName);
			sqlGroup.registerGroupIfNotExisting();
			
			if(prefix != null)
				sqlGroup.setPrefix(prefix);
			
			if(chatPrefix != null)
				sqlGroup.setChatPrefix(chatPrefix);
			
			if(suffix != null)
				sqlGroup.setSuffix(suffix);
			
			if(chatSuffix != null)
				sqlGroup.setChatSuffix(chatSuffix);
			
			sqlGroup.setWeight(weight);
			sqlGroup.setDefault(isDefault);
			sqlGroup.setParent(parent);
			
			mysqlPermissionManager.setGroupMembers(groupName, members.toString());
			mysqlPermissionManager.setGroupPermissions(groupName, permissions.toString());
		}
		
		for (String uuid : uuids) {
			fileUtils.getConfiguration().set("MySQL.Enabled", false);
			
			PermissionUser user = permissionUserManager.getPermissionPlayer(UUID.fromString(uuid));
			String prefix = user.getPrefix();
			String chatPrefix = user.getChatPrefix();
			String suffix = user.getSuffix();
			String chatSuffix = user.getChatSuffix();
			List<String> permissions = user.getPermissions();
			String tempGroupName = null;
			long tempGroupTime = 0L;
			
			if(permissionConfigUtils.getConfiguration().getStringList("TempRanks").contains(uuid)) {
				tempGroupName = permissionConfigUtils.getConfiguration().getString("Ranks." + uuid + ".GroupName");
				tempGroupTime = permissionConfigUtils.getConfiguration().getLong("Ranks." + uuid + ".Time");
			}
			
			fileUtils.getConfiguration().set("MySQL.Enabled", true);
			
			PermissionUser sqlUser = new PermissionUser(UUID.fromString(uuid));
			
			if(prefix != null)
				sqlUser.setPrefix(prefix);
			
			if(chatPrefix != null)
				sqlUser.setChatPrefix(chatPrefix);
			
			if(suffix != null)
				sqlUser.setSuffix(suffix);
			
			if(chatSuffix != null)
				sqlUser.setChatSuffix(chatSuffix);
			
			mysqlPermissionManager.setPlayerPermissions(uuid, permissions.toString());
			mysqlPermissionManager.saveToUUIDCache(uuid);
			
			if(tempGroupName != null)
				mysqlPermissionManager.setPlayerTempGroup(uuid, tempGroupName, tempGroupTime);
			
			user.updatePermissions();
		}
		
		if (fileUtils.getConfiguration().getBoolean("Settings.UsePrefixesAndSuffixes"))
			permissionSystem.updatePrefixesAndSuffixes();
		
		fileUtils.saveFile();
	}

}
