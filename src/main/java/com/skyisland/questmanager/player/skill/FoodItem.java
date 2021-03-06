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

package com.skyisland.questmanager.player.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.skyisland.questmanager.QuestManagerPlugin;

public class FoodItem extends QualityItem {
		
	public static final int DEFAULT_FOOD_LEVEL = 4;
	
	private int foodLevel;
	
	public FoodItem(ItemStack item, int foodLevel) {
		super(item);
		this.foodLevel = foodLevel;
	}
	
	public static FoodItem wrapItem(ItemStack input) {
		QualityItem qItem = new QualityItem(input.clone());
		ItemStack item = qItem.getUnderlyingItem();
		
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()
				|| !item.getItemMeta().getLore().get(0).toLowerCase().contains("food level: ")) {
			return new FoodItem(item, DEFAULT_FOOD_LEVEL);
		}
		
		String line = ChatColor.stripColor(item.getItemMeta().getLore().get(0));
		line = line.toLowerCase().substring(line.indexOf("food level: ") + 12).trim();
		int foodLevel = DEFAULT_FOOD_LEVEL;
		try {
			foodLevel = Integer.parseInt(line);
		} catch (Exception e) {
			e.printStackTrace();
			QuestManagerPlugin.logger.info("Just pretending it said " + DEFAULT_FOOD_LEVEL);
		}
		
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.remove(0);
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		FoodItem food = new FoodItem(item, foodLevel);
		food.setQuality(qItem.getQuality());
		
		return food;
	}
	
	/**
	 * Gets a formateed itemstack that has food level in the lore
	 */
	public ItemStack getItem() {
		if (getUnderlyingItem() == null) {
			return null;
		}

		ItemStack ret = getUnderlyingItem().clone();
		String line = ChatColor.DARK_GRAY + "Food Level: " + ChatColor.DARK_PURPLE + foodLevel;
		
		ItemMeta meta = ret.getItemMeta();
		List<String> lore;
		if (meta.getLore() != null && !meta.getLore().isEmpty()) {
			lore = new ArrayList<>(meta.getLore().size());
			lore.add(line);
			lore.addAll(meta.getLore());
		} else {
			lore = new ArrayList<>(1);
			lore.add(line);
		}
		
		meta.setLore(lore);
		
		ret.setItemMeta(meta);
		
		ItemStack swap = getUnderlyingItem();
		this.setItem(ret);
		ret = super.getItem();
		this.setItem(swap);
		
		return ret;
	}

	public int getFoodLevel() {
		return foodLevel;
	}
	
	@Override
	public FoodItem clone() {
		FoodItem ret = new FoodItem(getUnderlyingItem().clone(), foodLevel);
		ret.setQuality(this.getQuality());
		return ret;
	}

	public int setFoodLevel(double calculateFoodLevel) {
		return foodLevel;
	}
}
