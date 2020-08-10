package net.dev.permissions.utils.reflect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;

public class PermissibleBaseOverride extends PermissibleBase {

	public PermissibleBaseOverride(ServerOperator op) {
		super(op);
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
		if((hasPerm(perm.getName()) || hasPerm("*") || hasPerm("'*'")) && !(hasPerm("-" + perm.getName())))
			return true;
		
		if (super.hasPermission("-" + perm.getName()))
			return false;

		if (super.hasPermission("*") || super.hasPermission("'*'"))
			return true;
		
		if(perm.getName().contains(".")) {
			String[] splitPermission = perm.getName().split("\\.");
			
			for (int i = 1; i < splitPermission.length; i++) {
				String tempPermissionPart = "";
				
				for (int j = 0; j < i; j++)
					tempPermissionPart += splitPermission[j] + ".";
					
				if(hasPerm(tempPermissionPart + "*"))
					return true;
			}
		}

		return super.hasPermission(perm);
	}

	private boolean hasPerm(String inName) {
		if (inName == null) {
			throw new IllegalArgumentException("Permission name cannot be null");
		}

		Map<String, PermissionAttachmentInfo> permissions = new HashMap<String, PermissionAttachmentInfo>();

		try {
			Field f = PermissibleBase.class.getDeclaredField("permissions");
			f.setAccessible(true);
			permissions = (Map<String, PermissionAttachmentInfo>) f.get(this);
			f.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
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