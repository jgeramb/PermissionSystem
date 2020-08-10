package net.dev.permissions.utils.reflect;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

public class ReflectUtils {

	public void setField(Class<?> clazz, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field f = clazz.getDeclaredField(fieldName);
		
		f.setAccessible(true);
		f.set(clazz, value);
		f.setAccessible(false);
	}
	
	public void setField(Object obj, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field f = obj.getClass().getDeclaredField(fieldName);
		
		f.setAccessible(true);
		f.set(obj, value);
		f.setAccessible(false);
	}

	public Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + getVersion() + "." + className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}
	
}
