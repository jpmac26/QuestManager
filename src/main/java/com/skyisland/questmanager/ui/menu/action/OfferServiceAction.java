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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.skyisland.questmanager.npc.utils.ServiceOffer;
import com.skyisland.questmanager.player.QuestPlayer;
import com.skyisland.questmanager.ui.ChatMenu;
import com.skyisland.questmanager.ui.menu.SimpleChatMenu;
import com.skyisland.questmanager.ui.menu.message.Message;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Trades an itemstack for some currency
 *
 */
public class OfferServiceAction implements MenuAction {
	
	private ServiceOffer offer;
	
	private QuestPlayer player;
	
	private Message denial;
	
	public OfferServiceAction(ServiceOffer offer, QuestPlayer player, Message denialMessage) {
		this.offer = offer;
		this.player = player;
		this.denial = denialMessage;
	}

	@Override
	public void onAction() {
		if (!player.getPlayer().isOnline()) {
			System.out.println("Very bad Service error!!!!!!!!!!!!!");
			return;
		}

		Player p = player.getPlayer().getPlayer();
		
		//check if they have the required item
		if (!hasItem(p.getInventory(), offer.getItem())) {
			deny();
			return;
		}
		
		//had the item
		
		//play exp sound, give money,
		//deduct required items
		
		removeItem(p.getInventory(), offer.getItem());

		player.addMoney(offer.getPrice());
		
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		
		(new TitleObject(ChatColor.GOLD + "Sale",
				" "))
		.setFadeIn(20).setFadeOut(20).setStay(40).send(p);

		
	}
	
	/**
	 * Checks whether the passed inventory has enough of the provided item.
	 * This method checks the name of the item when calculating how much they have
	 */
	private boolean hasItem(Inventory inv, ItemStack searchItem) {
		int count = 0;
		String itemName = null;
		
		if (searchItem.hasItemMeta() && searchItem.getItemMeta().hasDisplayName()) {
			itemName = searchItem.getItemMeta().getDisplayName();
		}
		
		for (ItemStack item : inv.all(searchItem.getType()).values()) {
			if ((itemName == null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())) || 
					(item.hasItemMeta() && item.getItemMeta().getDisplayName() != null 
					  && item.getItemMeta().getDisplayName().equals(itemName))) {
				count += item.getAmount();
			}
		}

		return count >= searchItem.getAmount();
	}
	
	/**
	 * Removes the passed item from the player's inventory.
	 * This method also uses item lore to make sure the correct items are removed
	 */
	private void removeItem(Inventory inv, ItemStack searchItem) {
		//gotta go through and find ones that match the name
		int left = searchItem.getAmount();
		String itemName = null;
		ItemStack item;
		
		if (searchItem.hasItemMeta() && searchItem.getItemMeta().hasDisplayName()) {
			itemName = searchItem.getItemMeta().getDisplayName();
		}
		
		for (int i = 0; i <= 35; i++) {
			item = inv.getItem(i);
			if (item != null && item.getType() == searchItem.getType())
			if (  (itemName == null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()))
				|| (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals(itemName))	
					) {
				//deduct from this item stack as much as we can, up to 'left'
				//but if there's more than 'left' left, just remove it
				int amt = item.getAmount();
				if (amt <= left) {
					//gonna remove entire stack
					item.setType(Material.AIR);
					item.setAmount(0);
					item.setItemMeta(null);
				} else {
					item.setAmount(amt - left);
				}
				
				inv.setItem(i, item);
				left-=amt;
				
				if (left <= 0) {
					break;
				}
			}
		}
	}
	
	private void deny() {
		ChatMenu menu = new SimpleChatMenu(denial.getFormattedMessage());
		
		menu.show(player.getPlayer().getPlayer());
	}
}
