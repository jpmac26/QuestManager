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

package com.skyisland.questmanager.magic.spell;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.skyisland.questmanager.magic.MagicUser;

public abstract class TargetSpell extends Spell {
	
	protected TargetSpell(int cost, int difficulty, String name, String description) {
		super(cost, difficulty, name, description);
	}
	
	protected abstract void onBlockHit(MagicUser caster, Location loc);
	
	protected abstract void onEntityHit(MagicUser caster, LivingEntity target);
}
