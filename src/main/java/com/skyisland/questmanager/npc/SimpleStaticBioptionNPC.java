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

package com.skyisland.questmanager.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * An npc that doesn't move rather then being reset
 *
 */
public abstract class SimpleStaticBioptionNPC extends SimpleBioptionNPC {

	protected SimpleStaticBioptionNPC(Location startingLoc) {
		super(startingLoc);
	}
	
	@Override
	/**
	 * Render this NPC imobile using slowness instead of teleporting them
	 */
	public boolean tick() {
		Entity e = getEntity();
		
		if (e == null) {
			return false;
		}
		
		Location loc = e.getLocation();
		if (!loc.getWorld().isChunkLoaded(loc.getBlockX() / 16, loc.getBlockZ() / 16)) {
			return false;
		}
		
		if (e instanceof LivingEntity) {
			((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999999, 10, false, false), true);
		}
		
		return false;
	}
	
	@Override
	/**
	 * Sets the entity, making it immobile in the process
	 */
	public void setEntity(Entity entity) {
		super.setEntity(entity);
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999999, 10, false, false), true);
		}
	}
}
