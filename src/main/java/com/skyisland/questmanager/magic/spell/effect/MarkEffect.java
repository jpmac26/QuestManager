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

package com.skyisland.questmanager.magic.spell.effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.skyisland.questmanager.magic.MagicUser;
import com.skyisland.questmanager.player.QuestPlayer;

/**
 * Sets the casters mark location
 */
public class MarkEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(MarkEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(MarkEffect.class);
	}
	

	private enum aliases {
		DEFAULT(MarkEffect.class.getName()),
		OLD("com.SkyIsland.QuestManager.Magic.Spell.Effect." + MarkEffect.class.getSimpleName()),
		LONGI("SpellMark"),
		LONG("MarkSpell"),
		SHORT("SMark");
		
		private String alias;
		
		aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static MarkEffect valueOf(Map<String, Object> map) {
		return new MarkEffect();
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		return map;
	}
	
	public MarkEffect() {
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (cause instanceof QuestPlayer) {
			((QuestPlayer) cause).mark(e.getLocation());
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		if (cause instanceof QuestPlayer) {
			((QuestPlayer) cause).mark(loc);
		}
	}
}
