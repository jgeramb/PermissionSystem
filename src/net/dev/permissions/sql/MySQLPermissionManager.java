package net.dev.permissions.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLPermissionManager {

	private MySQL mysql;
	private HashMap<SQLProperty, Object> values = new HashMap<>();
	
	public MySQLPermissionManager(MySQL mysql) {
		this.mysql = mysql;
	}
	
	//UUID-Cache management
	
	public List<String> getUUIDCache() {
		if(values.containsKey(SQLProperty.UUIDCACHE))
			return ((List<String>) values.get(SQLProperty.UUIDCACHE));
		
		ArrayList<String> list = new ArrayList<>();
		
		if(mysql.isConnected()) {
			try {
				ResultSet rs = mysql.getResult("SELECT * FROM UUIDCache");
				
				while (rs.next())
					list.add(rs.getString("uuid"));
				
				rs.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		values.put(SQLProperty.UUIDCACHE, list);
		
		return list;
	}
	
	public boolean isUUIDInCache(String uuid) {
		if(values.containsKey(SQLProperty.UUIDCACHE))
			return ((List<String>) values.get(SQLProperty.UUIDCACHE)).contains(uuid);
		else
			return getUUIDCache().contains(uuid);
	}

	public void saveToUUIDCache(String uuid) {
		if(mysql.isConnected()) {
			if(!(isUUIDInCache(uuid)))
				mysql.update("INSERT INTO UUIDCache (uuid) VALUES ('" + uuid + "')");
		}		
	}

	//Player management
	
	public void collectUserData() {
		if(mysql.isConnected()) {
			values.put(SQLProperty.USER_PERMISSIONS, new HashMap<String, List<String>>());
			values.put(SQLProperty.USER_PREFIX, new HashMap<String, String>());
			values.put(SQLProperty.USER_SUFFIX, new HashMap<String, String>());
			values.put(SQLProperty.USER_CHAT_PREFIX, new HashMap<String, String>());
			values.put(SQLProperty.USER_CHAT_SUFFIX, new HashMap<String, String>());
			values.put(SQLProperty.USER_TEMP_GROUP_NAME, new HashMap<String, String>());
			values.put(SQLProperty.USER_TEMP_GROUP_TIME, new HashMap<String, Long>());
			
			try {
				ResultSet rs = mysql.getResult("SELECT * FROM PermissionUsers");
				
				while(rs.next()) {
					String uuid = rs.getString("uuid");
					List<String> permissions = new ArrayList<>();
					String permissionString = rs.getString("permissions");
					
					if(!(permissionString.equalsIgnoreCase("[]"))){
						for (String string : permissionString.replace("[", "").replace("]", "").split(", "))
							permissions.add(string.trim());
					}
					
					((HashMap<String, List<String>>) values.get(SQLProperty.USER_PERMISSIONS)).put(uuid, permissions);
					((HashMap<String, String>) values.get(SQLProperty.USER_PREFIX)).put(uuid, rs.getString("prefix"));
					((HashMap<String, String>) values.get(SQLProperty.USER_SUFFIX)).put(uuid, rs.getString("suffix"));
					((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_PREFIX)).put(uuid, rs.getString("chat_prefix"));
					((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_SUFFIX)).put(uuid, rs.getString("chat_suffix"));
					((HashMap<String, String>) values.get(SQLProperty.USER_TEMP_GROUP_NAME)).put(uuid, rs.getString("temp_group_name"));
					((HashMap<String, Long>) values.get(SQLProperty.USER_TEMP_GROUP_TIME)).put(uuid, rs.getLong("temp_group_time"));
				}
				
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getPlayerPermissions(String uuid) {
		return (values.containsKey(SQLProperty.USER_PERMISSIONS) ? ((HashMap<String, List<String>>) values.get(SQLProperty.USER_PERMISSIONS)).get(uuid) : new ArrayList<>());
	}

	public String getPlayerPrefix(String uuid) {
		return (values.containsKey(SQLProperty.USER_PREFIX) ? ((HashMap<String, String>) values.get(SQLProperty.USER_PREFIX)).get(uuid) : null);
	}
	
	public String getPlayerChatPrefix(String uuid) {
		return (values.containsKey(SQLProperty.USER_CHAT_PREFIX) ? ((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_PREFIX)).get(uuid) : null);
	}

	public String getPlayerSuffix(String uuid) {
		return (values.containsKey(SQLProperty.USER_SUFFIX) ? ((HashMap<String, String>) values.get(SQLProperty.USER_SUFFIX)).get(uuid) : null);
	}

	public String getPlayerChatSuffix(String uuid) {
		return (values.containsKey(SQLProperty.USER_CHAT_SUFFIX) ? ((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_SUFFIX)).get(uuid) : null);
	}
	
	public String getPlayerTempGroupName(String uuid) {
		return (values.containsKey(SQLProperty.USER_TEMP_GROUP_NAME) ? ((HashMap<String, String>) values.get(SQLProperty.USER_TEMP_GROUP_NAME)).get(uuid) : null);
	}
	
	public long getPlayerTempGroupTime(String uuid) {
		return (values.containsKey(SQLProperty.USER_TEMP_GROUP_TIME) ? ((HashMap<String, Long>) values.get(SQLProperty.USER_TEMP_GROUP_TIME)).get(uuid) : 0L);
	}
	
	public void setPlayerPermissions(String uuid, String permissions) {
		if(mysql.isConnected()) {
			if(!(isUUIDInCache(uuid)))
				mysql.update("INSERT INTO PermissionUsers (uuid, prefix, suffix, chat_prefix, chat_suffix, permissions) VALUES ('" + uuid + "', '', '', '', '', '" + permissions + "')");
			else
				mysql.update("UPDATE PermissionUsers SET permissions = '" + permissions + "' WHERE UUID = '" + uuid + "'");
		}
		
		if(values.containsKey(SQLProperty.USER_PERMISSIONS)) {
			List<String> list = new ArrayList<>();
			
			if(!(permissions.equalsIgnoreCase("[]"))){
				for (String string : permissions.replace("[", "").replace("]", "").split(", "))
					list.add(string.trim());
			}
			
			((HashMap<String, List<String>>) values.get(SQLProperty.USER_PERMISSIONS)).put(uuid, list);
		}
	}

	public void setPlayerPrefix(String uuid, String value) {
		if(mysql.isConnected()) {
			if(isUUIDInCache(uuid))
				mysql.update("UPDATE PermissionUsers SET prefix = '" + value + "' WHERE UUID = '" + uuid + "'");
		}	
		
		if(values.containsKey(SQLProperty.USER_PREFIX))
			((HashMap<String, String>) values.get(SQLProperty.USER_PREFIX)).put(uuid, value);
	}

	public void setPlayerChatPrefix(String uuid, String value) {
		if(mysql.isConnected()) {
			if(isUUIDInCache(uuid))
				mysql.update("UPDATE PermissionUsers SET chat_prefix = '" + value + "' WHERE UUID = '" + uuid + "'");
		}	
		
		if(values.containsKey(SQLProperty.USER_CHAT_PREFIX))
			((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_PREFIX)).put(uuid, value);
	}

	public void setPlayerSuffix(String uuid, String value) {
		if(mysql.isConnected()) {
			if(isUUIDInCache(uuid))
				mysql.update("UPDATE PermissionUsers SET suffix = '" + value + "' WHERE UUID = '" + uuid + "'");
		}
		
		if(values.containsKey(SQLProperty.USER_SUFFIX))
			((HashMap<String, String>) values.get(SQLProperty.USER_SUFFIX)).put(uuid, value);
	}
	
	public void setPlayerChatSuffix(String uuid, String value) {
		if(mysql.isConnected()) {
			if(isUUIDInCache(uuid))
				mysql.update("UPDATE PermissionUsers SET chat_suffix = '" + value + "' WHERE UUID = '" + uuid + "'");
		}
		
		if(values.containsKey(SQLProperty.USER_CHAT_SUFFIX))
			((HashMap<String, String>) values.get(SQLProperty.USER_CHAT_SUFFIX)).put(uuid, value);
	}
	
	public void setPlayerTempGroup(String uuid, String groupName, long time) {
		if(mysql.isConnected()) {
			if(isUUIDInCache(uuid))
				mysql.update("UPDATE PermissionUsers SET temp_group_name = " + ((groupName != null) ? ("'" + groupName + "'") : "NULL") + ", temp_group_time = " + time + " WHERE UUID = '" + uuid + "'");
		}
		
		if(values.containsKey(SQLProperty.USER_TEMP_GROUP_NAME))
			((HashMap<String, String>) values.get(SQLProperty.USER_TEMP_GROUP_NAME)).put(uuid, groupName);
		
		if(values.containsKey(SQLProperty.USER_TEMP_GROUP_TIME))
			((HashMap<String, Long>) values.get(SQLProperty.USER_TEMP_GROUP_TIME)).put(uuid, time);
	}
	
	//Group management
	
	public void collectGroupData() {
		if(mysql.isConnected()) {
			values.put(SQLProperty.GROUP_PERMISSIONS, new HashMap<String, List<String>>());
			values.put(SQLProperty.GROUP_MEMBERS, new HashMap<String, List<String>>());
			values.put(SQLProperty.GROUP_PREFIX, new HashMap<String, String>());
			values.put(SQLProperty.GROUP_SUFFIX, new HashMap<String, String>());
			values.put(SQLProperty.GROUP_CHAT_PREFIX, new HashMap<String, String>());
			values.put(SQLProperty.GROUP_CHAT_SUFFIX, new HashMap<String, String>());
			values.put(SQLProperty.GROUP_PARENT, new HashMap<String, String>());
			values.put(SQLProperty.GROUP_DEFAULT, new HashMap<String, Boolean>());
			values.put(SQLProperty.GROUP_WEIGHT, new HashMap<String, Integer>());
			
			try {
				ResultSet rs = mysql.getResult("SELECT * FROM PermissionGroups");
				List<String> groupNames = new ArrayList<>();
				
				while(rs.next()) {
					String groupName = rs.getString("name");
					List<String> permissions = new ArrayList<>();
					List<String> members = new ArrayList<>();
					String permissionsString = rs.getString("permissions");
					String membersString = rs.getString("members");
					
					if(!(permissionsString.equalsIgnoreCase("[]"))) {
						for (String string : permissionsString.replace("[", "").replace("]", "").split(", "))
							permissions.add(string.trim());
					}
					
					if(!(membersString.equalsIgnoreCase("[]"))) {
						for (String string : membersString.replace("[", "").replace("]", "").split(", "))
							members.add(string.trim());
					}
					
					((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_PERMISSIONS)).put(groupName, permissions);
					((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_MEMBERS)).put(groupName, members);
					((HashMap<String, String>) values.get(SQLProperty.GROUP_PREFIX)).put(groupName, rs.getString("prefix"));
					((HashMap<String, String>) values.get(SQLProperty.GROUP_SUFFIX)).put(groupName, rs.getString("suffix"));
					((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_PREFIX)).put(groupName, rs.getString("chat_prefix"));
					((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_SUFFIX)).put(groupName, rs.getString("chat_suffix"));
					((HashMap<String, String>) values.get(SQLProperty.GROUP_PARENT)).put(groupName, rs.getString("parent"));
					((HashMap<String, Boolean>) values.get(SQLProperty.GROUP_DEFAULT)).put(groupName, rs.getBoolean("is_default"));
					((HashMap<String, Integer>) values.get(SQLProperty.GROUP_WEIGHT)).put(groupName, rs.getInt("weight"));
					
					groupNames.add(groupName);
				}
				
				values.put(SQLProperty.GROUP_NAMES, groupNames);
				
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getGroupNames() {
		return (values.containsKey(SQLProperty.GROUP_NAMES) ? ((List<String>) values.get(SQLProperty.GROUP_NAMES)) : new ArrayList<>());
	}

	public List<String> getGroupPermissions(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_PERMISSIONS) ? ((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_PERMISSIONS)).get(groupName) : new ArrayList<>());
	}
	
	public List<String> getGroupMembers(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_MEMBERS) ? ((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_MEMBERS)).get(groupName) : new ArrayList<>());
	}

	public String getGroupPrefix(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_PREFIX) ? ((HashMap<String, String>) values.get(SQLProperty.GROUP_PREFIX)).get(groupName) : null);
	}
	
	public String getGroupChatPrefix(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_CHAT_PREFIX) ? ((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_PREFIX)).get(groupName) : null);
	}

	public String getGroupSuffix(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_SUFFIX) ? ((HashMap<String, String>) values.get(SQLProperty.GROUP_SUFFIX)).get(groupName) : null);
	}

	public String getGroupChatSuffix(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_CHAT_SUFFIX) ? ((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_SUFFIX)).get(groupName) : null);
	}

	public String getGroupParent(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_PARENT) ? ((HashMap<String, String>) values.get(SQLProperty.GROUP_PARENT)).get(groupName) : null);
	}
	
	public boolean isDefaultGroup(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_DEFAULT) ? ((HashMap<String, Boolean>) values.get(SQLProperty.GROUP_DEFAULT)).get(groupName) : false);
	}
	
	public int getGroupWeight(String groupName) {
		return (values.containsKey(SQLProperty.GROUP_WEIGHT) ? ((HashMap<String, Integer>) values.get(SQLProperty.GROUP_WEIGHT)).get(groupName) : 999);
	}

	public boolean isGroupRegistered(String groupName) {
		return getGroupNames().contains(groupName);
	}
	
	public void removeGroup(String groupName) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("DELETE FROM PermissionGroups WHERE name = '" + groupName + "'");
		}
		
		((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_PERMISSIONS)).remove(groupName);
		((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_MEMBERS)).remove(groupName);
		((HashMap<String, String>) values.get(SQLProperty.GROUP_PREFIX)).remove(groupName);
		((HashMap<String, String>) values.get(SQLProperty.GROUP_SUFFIX)).remove(groupName);
		((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_PREFIX)).remove(groupName);
		((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_SUFFIX)).remove(groupName);
		((HashMap<String, String>) values.get(SQLProperty.GROUP_PARENT)).remove(groupName);
		((HashMap<String, Boolean>) values.get(SQLProperty.GROUP_DEFAULT)).remove(groupName);
		((HashMap<String, Integer>) values.get(SQLProperty.GROUP_WEIGHT)).remove(groupName);
	}
	
	public void setGroupPermissions(String groupName, String permissions) {
		if(mysql.isConnected()) {
			if(!(isGroupRegistered(groupName)))
				mysql.update("INSERT INTO PermissionGroups (name, prefix, suffix, chat_prefix, chat_suffix, is_default, parent, weight, members, permissions) VALUES ('" + groupName + "', '', '', '', '', false, '', 1, '[]', '" + permissions + "')");
			else
				mysql.update("UPDATE PermissionGroups SET permissions = '" + permissions + "' WHERE name = '" + groupName + "'");
		}
		
		if(values.containsKey(SQLProperty.GROUP_PERMISSIONS)) {
			List<String> list = new ArrayList<>();
			
			if(!(permissions.equalsIgnoreCase("[]"))){
				for (String string : permissions.replace("[", "").replace("]", "").split(", "))
					list.add(string.trim());
			}
			
			((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_PERMISSIONS)).put(groupName, list);
		}
	}
	
	public void setGroupPrefix(String groupName, String value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET prefix = '" + value + "' WHERE name = '" + groupName + "'");
		}
		
		if(values.containsKey(SQLProperty.GROUP_PREFIX))
			((HashMap<String, String>) values.get(SQLProperty.GROUP_PREFIX)).put(groupName, value);
	}
	
	public void setGroupChatPrefix(String groupName, String value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET chat_prefix = '" + value + "' WHERE name = '" + groupName + "'");
		}	
		
		if(values.containsKey(SQLProperty.GROUP_CHAT_PREFIX))
			((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_PREFIX)).put(groupName, value);
	}

	public void setGroupSuffix(String groupName, String value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET suffix = '" + value + "' WHERE name = '" + groupName + "'");
		}	
		
		if(values.containsKey(SQLProperty.GROUP_SUFFIX))
			((HashMap<String, String>) values.get(SQLProperty.GROUP_SUFFIX)).put(groupName, value);
	}

	public void setGroupChatSuffix(String groupName, String value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET chat_suffix = '" + value + "' WHERE name = '" + groupName + "'");
		}	

		if(values.containsKey(SQLProperty.GROUP_CHAT_SUFFIX))
			((HashMap<String, String>) values.get(SQLProperty.GROUP_CHAT_SUFFIX)).put(groupName, value);
	}

	public void setGroupMembers(String groupName, String members) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET members = '" + members + "' WHERE name = '" + groupName + "'");
		}	
		
		if(values.containsKey(SQLProperty.GROUP_MEMBERS)) {
			List<String> list = new ArrayList<>();
			
			if(!(members.equalsIgnoreCase("[]"))){
				for (String string : members.replace("[", "").replace("]", "").split(", "))
					list.add(string.trim());
			}
			
			((HashMap<String, List<String>>) values.get(SQLProperty.GROUP_MEMBERS)).put(groupName, list);
		}
	}
	
	public void setGroupParent(String groupName, String value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET parent = '" + value + "' WHERE name = '" + groupName + "'");
		}	
		
		if(values.containsKey(SQLProperty.GROUP_PARENT))
			((HashMap<String, String>) values.get(SQLProperty.GROUP_PARENT)).put(groupName, value);
	}
	
	public void setIsDefault(String groupName, boolean value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET is_default = " + value + " WHERE name = '" + groupName + "'");
		}
		
		if(values.containsKey(SQLProperty.GROUP_DEFAULT))
			((HashMap<String, Boolean>) values.get(SQLProperty.GROUP_DEFAULT)).put(groupName, value);
	}

	public void setGroupWeight(String groupName, int value) {
		if(mysql.isConnected()) {
			if(isGroupRegistered(groupName))
				mysql.update("UPDATE PermissionGroups SET weight = '" + value + "' WHERE name = '" + groupName + "'");
		}
		
		if(values.containsKey(SQLProperty.GROUP_WEIGHT))
			((HashMap<String, Integer>) values.get(SQLProperty.GROUP_WEIGHT)).put(groupName, value);
	}

	public void setValues(HashMap<SQLProperty, Object> values) {
		this.values = values;
	}
	
}