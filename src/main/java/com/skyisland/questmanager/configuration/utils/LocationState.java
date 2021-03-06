/*
 *  QuestManager: An RPG plugin for the Bukkit API.
 *  Copyright (C) 2015-2016 Github Contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skyisland.questmanager.configuration.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

/**
 * Convenience class for saving and loading location data from config 
 *
 */
public class LocationState implements ConfigurationSerializable {
	
	Location location;
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(LocationState.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(LocationState.class);
	}
	

	private enum aliases {
		BUKKIT("org.bukkit.Location"),
		LOCATIONUPPER("LOCATION"),
		LOCATIONLOWER("location"),
		LOCATIONFORMAL("Location"),
		DEFAULT(LocationState.class.getName()),
		SIMPLE("LocationState");
		
		private String alias;
		
		aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	/**
	 * Stores fields and their config keys
		 *
	 */
	private enum fields {
		X("x"),
		Y("y"),
		Z("z"),
		PITCH("pitch"),
		YAW("yaw"),
		WORLD("world");
		
		private String key;
		
		fields(String key) {
			this.key = key;
		}
		
		/**
		 * Returns the configuration key mapped to this field
		 */
		public String getKey() {
			return this.key;
		}
	}
	
	/**
	 * Creates a LocationState with the information from the passed location.
	 */
	public LocationState(Location location) {
		this.location = location;
	}
	
	/**
	 * Serializes the wrapped location to a format that's able to be saved to a configuration file.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> config = new HashMap<>(6);
		config.put(fields.X.getKey(), location.getX());
		config.put(fields.Y.getKey(), location.getY());
		config.put(fields.Z.getKey(), location.getZ());
		config.put(fields.PITCH.getKey(), location.getPitch());
		config.put(fields.YAW.getKey(), location.getYaw());
		config.put(fields.WORLD.getKey(), location.getWorld().getName());
		return config;
	}
	
	/**
	 * Uses the passed configuration map to instantiate a new location (and wrapper).
	 */
	public static LocationState valueOf(Map<String, Object> configMap) {
		World world = Bukkit.getWorld((String) configMap.get(fields.WORLD.getKey()));
		
		if (world == null) {
			Bukkit.getLogger().info("Unable to create LocationState from passed map!");
			return null;
		}
		
		double x,y,z;
		float pitch, yaw;
		x = (double) configMap.get(fields.X.getKey());
		y = (double) configMap.get(fields.Y.getKey());
		z = (double) configMap.get(fields.Z.getKey());
		pitch = (float) ((double) configMap.get(fields.PITCH.getKey()));
		yaw = (float) ((double) configMap.get(fields.YAW.getKey()));
		
		return new LocationState(
				new Location(
						world,
						x,
						y,
						z,
						yaw,
						pitch));
	}
	
	/**
	 * Return the location wrapped by this class
	 */
	public Location getLocation() {
		return location;
	}
}
