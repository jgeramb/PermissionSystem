package net.dev.permissions.nms;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.permissions.*;

public class PermissibleBaseOverride extends PermissibleBase {

	public PermissibleBaseOverride(ServerOperator serverOperator) {
		super(serverOperator);
	}

	@Override
	public boolean hasPermission(String inName) {
		if((hasPerm(inName) || hasPerm("*") || hasPerm("'*'")) && !(hasPerm("-" + inName)))
			return true;

		if (super.hasPermission("-" + inName))
			return false;
		
		if (super.hasPermission("*") || super.hasPermission("'*'"))
			return true;
		
		if(inName.contains(".")) {
			String[] splitPermission = inName.split("\\.");
			
			for (int i = 1; i < splitPermission.length; i++) {
				String tempPermissionPart = "";
				
				for (int j = 0; j < i; j++)
					tempPermissionPart += splitPermission[j] + ".";
					
				if(hasPerm(tempPermissionPart + "*"))
					return true;
			}
		}

		return super.hasPermission(inName);
	}

	@Override
	public boolean hasPermission(Permission perm) {
		if(hasPermission(perm.getName()))
			return true;

		return super.hasPermission(perm);
	}

	private boolean hasPerm(String inName) {
		if (inName == null)
			throw new IllegalArgumentException("Permission name cannot be null");

		Map<String, PermissionAttachmentInfo> permissions = new HashMap<>();

		try {
			Field permissionsField = PermissibleBase.class.getDeclaredField("permissions");
			permissionsField.setAccessible(true);
			permissions = (Map<String, PermissionAttachmentInfo>) permissionsField.get(this);
			permissionsField.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String name = inName.toLowerCase();

		if (isPermissionSet(name))
			return permissions.get(name).getValue();
		else {
			Permission perm = Bukkit.getServer().getPluginManager().getPermission(name);

			if (perm != null)
				return perm.getDefault().getValue(false);
			else
				return Permission.DEFAULT_PERMISSION.getValue(false);
		}
	}

}