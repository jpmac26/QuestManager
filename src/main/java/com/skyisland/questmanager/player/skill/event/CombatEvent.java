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

package com.skyisland.questmanager.player.skill.event;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.skyisland.questmanager.configuration.PluginConfiguration;
import com.skyisland.questmanager.player.PlayerOptions;
import com.skyisland.questmanager.player.QuestPlayer;

/**
 * Event called when some sort of combat action is happening in a 
 * {@link PluginConfiguration#getWorlds() QuestWorld}
 * by a {@link QuestPlayer QuestPlayer}
 *
 */
public class CombatEvent extends Event {
	
	public static final String MISS_MESSAGE = ChatColor.YELLOW + "You missed!" + ChatColor.RESET;
	
	public static final String NO_DAMAGE_MESSAGE = ChatColor.YELLOW + "Your attack had no effect!" + ChatColor.RESET;
	
	public static final Sound MISS_SOUND = Sound.ENTITY_ENDERDRAGON_FLAP;
	
	public static final Sound NO_DAMAGE = Sound.ITEM_SHIELD_BLOCK;
	
	public static final String DAMAGE_MESSAGE = ChatColor.DARK_GRAY + "Your attack did "
			+ ChatColor.RED + "%.2f damage" + ChatColor.DARK_GRAY + " to "
			+ ChatColor.GRAY + "%s" + ChatColor.RESET;
	
	public static void doMiss(QuestPlayer misser, Location targetLocation) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT))
			misser.getPlayer().getPlayer().sendMessage(MISS_MESSAGE);
		targetLocation.getWorld().spigot().playEffect(targetLocation, Effect.SMOKE);
		targetLocation.getWorld().playSound(targetLocation, MISS_SOUND, 1f, 1.85f);
	}

	public static void doNoDamage(QuestPlayer misser, Location targetLocation) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT))
			misser.getPlayer().getPlayer().sendMessage(NO_DAMAGE_MESSAGE);
		targetLocation.getWorld().spigot().playEffect(targetLocation, Effect.VILLAGER_THUNDERCLOUD);
		targetLocation.getWorld().playSound(targetLocation, NO_DAMAGE, 1f, 1f);
	}
	
	public static void doHit(QuestPlayer misser, Location targetLocation, double damage, String target) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)) {
			String modmsg = String.format(DAMAGE_MESSAGE, damage, target);
			
			misser.getPlayer().getPlayer().sendMessage(modmsg);
		}
		
		//TODO effects?
	}

	private static final HandlerList HANDLERS = new HandlerList();
		
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	private QuestPlayer player;
	
	private LivingEntity target;
	
	private ItemStack weapon;
	
	private ItemStack otheritem;
	
	/**
	 * The base amount of damage the player is going to do
	 */
	private double damage;
	
	/**
	 * Damage modification from skills
	 */
	private double modifiedDamage;
	
	/**
	 * Multiplicitive damage modification from skills
	 */
	private double efficiency;
	
	private boolean isMiss;
	
	public CombatEvent(QuestPlayer player, LivingEntity target, ItemStack weapon, ItemStack otheritem, double damage) {
		this.player = player;
		this.target = target;
		this.damage = damage;
		this.weapon = weapon;
		this.otheritem = otheritem;
		this.modifiedDamage = 0.0;
		this.efficiency = 1.0;
		isMiss = false;
	}

	public QuestPlayer getPlayer() {
		return player;
	}

	public void setPlayer(QuestPlayer player) {
		this.player = player;
	}

	public LivingEntity getTarget() {
		return target;
	}
	
	public ItemStack getWeapon() {
		return weapon;
	}
	
	public ItemStack getOtherItem() {
		return otheritem;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	/**
	 * Gets the base amount of damage being done. 
	 */
	public double getDamage() {
		return damage;
	}

	/**
	 * Sets the base amount of damage being done.
	 * This is not intended to be changed for simple 'bonus damage' adjustments. For that, see
	 * {@link #setModifiedDamage(double)}
	 */
	public void setDamage(double damage) {
		this.damage = damage;
	}

	/**
	 * Returns the current amount of bonus damage being dealt to the target. This includes negative amounts
	 * for damage penalties.
	 */
	public double getModifiedDamage() {
		return modifiedDamage;
	}

	/**
	 * Sets the damage modifier for the event. This includes bonuses and penalties to damage
	 */
	public void setModifiedDamage(double modifiedDamage) {
		this.modifiedDamage = modifiedDamage;
	}

	/**
	 * Gets the current efficiency of the event.
	 * This is a multiplicitive bonus/penalty done after modifications
	 */
	public double getEfficiency() {
		return efficiency;
	}

	/**
	 * Sets the efficiency the event will be executed at.
	 * This is the final modification done, and is multiplicitive (e.g. efficiency 1 does 100% of damage)
	 */
	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}

	public boolean isMiss() {
		return isMiss;
	}

	public void setMiss(boolean isMiss) {
		this.isMiss = isMiss;
	}
	
	/**
	 * Performs all calculations with given parameters to determine the final damage dealt.
	 * Damage will not be negative. This calculation also does not consider whether or not the attack missed.
	 * @return the damage that would be dealt, or 0 if the damage would be negative
	 */
	public double getFinalDamage() {
		double calc = damage + modifiedDamage;
		calc *= efficiency;
		
		return Math.max(calc, 0.0);
	}
}
