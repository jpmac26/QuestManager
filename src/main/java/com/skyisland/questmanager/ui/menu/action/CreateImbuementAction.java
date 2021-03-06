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

package com.skyisland.questmanager.ui.menu.action;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.skyisland.questmanager.QuestManagerPlugin;
import com.skyisland.questmanager.effects.ChargeEffect;
import com.skyisland.questmanager.magic.ImbuementSet;
import com.skyisland.questmanager.magic.spell.effect.ImbuementEffect;
import com.skyisland.questmanager.player.QuestPlayer;

/**
 * Takes items (presumably from the imbuement table menu) and makes them into an imbuement, and registers
 * such to the player
 *
 */
public class CreateImbuementAction implements MenuAction, FillableInventoryAction {

	private QuestPlayer player;
	
	private ItemStack holder;
	
	private Material[] componentTypes;
	
	private static final ChargeEffect SUCCESS_EFFECT = new ChargeEffect(Effect.FLYING_GLYPH);
	
	private static final Sound SUCCESS_SOUND = Sound.ENTITY_PLAYER_LEVELUP;
	
	private static final ChargeEffect FAIL_EFFECT = new ChargeEffect(Effect.CRIT);
	
	private static final Sound FAIL_SOUND = Sound.BLOCK_ANVIL_PLACE;
	
	private static final String FAIL_MESSAGE = ChatColor.RED + "Your imbuement failed to show any signs of magic";
	
	private static final String SUCCESS_MESSAGE = ChatColor.GREEN + "Your imbuement succeeded with the following effects:";
	
	public CreateImbuementAction(QuestPlayer player, ItemStack holder) {
		this.player = player;
		this.holder = holder;
		this.componentTypes = null;
	}
	
	public void setComponentTypes(Material[] types) {
		componentTypes = types;
	}
	
	@Override
	public void onAction() {
		/*
		 * Create imbuement based on rules and stuff
		 */
		ImbuementSet set = QuestManagerPlugin.questManagerPlugin.getImbuementHandler()
				.getCombinedEffects(player, componentTypes);
		
		if (set == null || set.getEffectMap().isEmpty()) {
			//failure
			player.performImbuement(holder, null);
			
			if (!player.getPlayer().isOnline()) {
				return;
			}
			
			FAIL_EFFECT.play(player.getPlayer().getPlayer(), null);
			player.getPlayer().getPlayer().getWorld().playSound(player.getPlayer().getPlayer().getLocation(),
				FAIL_SOUND, 1, 1);
			player.getPlayer().getPlayer().sendMessage(FAIL_MESSAGE);
			return;
		}
		
		player.performImbuement(holder, set);

		if (!player.getPlayer().isOnline()) {
			return;
		}
		
		Player p = player.getPlayer().getPlayer();
		
		
		SUCCESS_EFFECT.play(p, null);
		p.getWorld().playSound(p.getLocation(), SUCCESS_SOUND, 1, 1);
		p.sendMessage(SUCCESS_MESSAGE);
		for (Entry<ImbuementEffect, Double> effect : set.getEffectMap().entrySet()) {
			p.sendMessage(ChatColor.AQUA + "" + ((int) (effect.getValue() * 100)) + "%"
					+ ChatColor.BLACK + " - " + ChatColor.GOLD
					+ effect.getKey().getDisplayName()); 
		}
		
		
		
	}

	@Override
	public void provideItems(ItemStack[] objects) {
		//throw into set, eliminate dups
		Set<Material> types = new HashSet<>();
		for (ItemStack item : objects) {
			if (item == null) {
				continue;
			}
			types.add(item.getType());
		}
		
		Material[] ret = new Material[types.size()];
		setComponentTypes(types.toArray(ret));
	}
}
