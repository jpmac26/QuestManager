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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import com.skyisland.questmanager.QuestManagerPlugin;
import com.skyisland.questmanager.configuration.EquipmentConfiguration;
import com.skyisland.questmanager.configuration.QuestConfiguration;
import com.skyisland.questmanager.configuration.utils.LocationState;
import com.skyisland.questmanager.fanciful.FancyMessage;
import com.skyisland.questmanager.player.QuestPlayer;
import com.skyisland.questmanager.player.utils.CompassTrackable;
import com.skyisland.questmanager.quest.Quest;
import com.skyisland.questmanager.ui.ChatMenu;
import com.skyisland.questmanager.ui.menu.BioptionChatMenu;
import com.skyisland.questmanager.ui.menu.action.QuestStartAction;
import com.skyisland.questmanager.ui.menu.message.BioptionMessage;
import com.skyisland.questmanager.ui.menu.message.Message;

/**
 * NPC that starts a quest :D
 * This simple starting VERSION mounts atop a {@link SimpleBioptionNPC}, and has all the capability
 * and limits defined therein.
 *
 */
public class SimpleQuestStartNPC extends SimpleStaticBioptionNPC implements CompassTrackable {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SimpleQuestStartNPC.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SimpleQuestStartNPC.class);
	}
	

	private enum aliases {
		FULL("com.SkyIsland.QuestManager.NPC.SimpleQuestStartNPC"),
		DEFAULT(SimpleQuestStartNPC.class.getName()),
		SHORT("SimpleQuestStartNPC"),
		INFORMAL("QSNPC");
		
		private String alias;
		
		aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}

	
	private SimpleQuestStartNPC(Location startingLoc) {
		super(startingLoc);
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>(4);
		System.out.println("point 1");
		map.put("name", name);
		map.put("type", getEntity().getType());
		map.put("location", new LocationState(getEntity().getLocation()));
		
		EquipmentConfiguration econ;
		
		if (getEntity() instanceof LivingEntity) {
			econ = new EquipmentConfiguration(
					((LivingEntity) getEntity()).getEquipment()
					);
		} else {
			econ = new EquipmentConfiguration();
		}
		
		map.put("equipment", econ);
		
		map.put("firstmessage", chat);
		map.put("duringmessage", duringMessage);
		map.put("postmessage", afterMessage);
		map.put("badrequirementmessage", altMessage);
				
		return map;
	}
	
	public static SimpleQuestStartNPC valueOf(Map<String, Object> map) {
		if (map == null || !map.containsKey("name") || !map.containsKey("type") 
				 || !map.containsKey("location") || !map.containsKey("equipment")
				  || !map.containsKey("firstmessage") || !map.containsKey("duringmessage")
				  || !map.containsKey("postmessage") || !map.containsKey("badrequirementmessage")) {
			QuestManagerPlugin.logger.warning("Invalid NPC info! "
					+ (map.containsKey("name") ? ": " + map.get("name") : ""));
			return null;
		}
		EquipmentConfiguration econ = new EquipmentConfiguration();
		try {
			YamlConfiguration tmp = new YamlConfiguration();
			tmp.createSection("key",  (Map<?, ?>) map.get("equipment"));
			econ.load(tmp.getConfigurationSection("key"));
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		LocationState ls = (LocationState) map.get("location");
		Location loc = ls.getLocation();
		
		EntityType type = EntityType.valueOf((String) map.get("type"));
		
		
		SimpleQuestStartNPC npc = new SimpleQuestStartNPC(loc);
		npc.isEnd = false;
		
		npc.name = (String) map.get("name");
		
		//load the chunk
		loc.getChunk();
		npc.setEntity(loc.getWorld().spawnEntity(loc, type));
		npc.getEntity().setCustomName((String) map.get("name"));

		if (npc.getEntity() instanceof LivingEntity) {
			EntityEquipment equipment = ((LivingEntity) npc.getEntity()).getEquipment();
			equipment.setHelmet(econ.getHead());
			equipment.setChestplate(econ.getChest());
			equipment.setLeggings(econ.getLegs());
			equipment.setBoots(econ.getBoots());
			equipment.setItemInMainHand(econ.getHeldMain());
			equipment.setItemInOffHand(econ.getHeldOff());
			
		}
		
		npc.chat = (BioptionMessage) map.get("firstmessage");
		npc.duringMessage = (Message) map.get("duringmessage");
		npc.afterMessage = (Message) map.get("postmessage");
		npc.altMessage = (Message) map.get("badrequirementmessage");
		
		
		//provide our npc's name, unless we don't have one!
		if (npc.name != null && !npc.name.equals("")) {
			FancyMessage label = new FancyMessage(npc.name);
			npc.chat.setSourceLabel(label);
			npc.duringMessage.setSourceLabel(label);
			npc.afterMessage.setSourceLabel(label);
			npc.altMessage.setSourceLabel(label);
		}
		
		return npc;
	}
	
	private QuestConfiguration quest;
	
	private boolean isEnd;
	
	private Message duringMessage;
	
	private Message afterMessage;
	
	private Message finishMessage;
	
	private Message altMessage;
	
	public void setQuestTemplate(QuestConfiguration questTemplate) {
		this.quest = questTemplate;
	}
	
	public void markAsEnd(Message finishMessage) {
		this.finishMessage = finishMessage;
		
		if (name != null && !name.equals("")) {
			this.finishMessage.setSourceLabel(
					new FancyMessage(name));
		}
		
		isEnd = true;
	}
	
	@Override
	protected void interact(Player player) {
		
		//do different things depending on if the player has or is doing the quest
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(
				player.getUniqueId());
		
		ChatMenu messageChat = null;
		boolean meetreqs = true;
		
		
		List<String> reqs = quest.getRequiredQuests();
		
		if (reqs != null && !reqs.isEmpty()) {
			//go through reqs, see if the player has those quests completed
			for (String req : reqs) {
				if (!QuestPlayer.meetsRequirement(qp, req)) {
					meetreqs = false;
					break;
				}
			}
		}
		
		if (!meetreqs) {
			//doesn't have all the required quests done yet!
			messageChat = ChatMenu.getDefaultMenu(altMessage);
		} else if (!quest.isRepeatable() && qp.hasCompleted(quest.getName())) {
			//already completed it
			messageChat = ChatMenu.getDefaultMenu(afterMessage);
		} else if (qp.isInQuest(quest.getName())) {
			//is currently in it
			
			//Is this the possible end?
			
			if (isEnd) {
				//fetch instance of quest
				Quest qInst = null;
				for (Quest q : qp.getCurrentQuests()) {
					if (q.getName().equals(quest.getName())) {
						qInst = q;
						break;
					}
				}
				
				if (qInst == null) {
					//something went wrong!
					QuestManagerPlugin.logger.warning(
							"Unable to find matching quest in SimpleQuestStartNPC!!!!!!!");
					return;
				}
				
				//perform check against completion!!!!
				if (qInst.isReady()) {	
					messageChat = ChatMenu.getDefaultMenu(finishMessage);
					qInst.completeQuest(false);
				} else {
					QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(player).updateQuestBook(true);
				}
			}
			if (messageChat == null) {
				Quest qInst = null;
				for (Quest q : qp.getCurrentQuests()) {
					if (q.getName().equals(quest.getName())) {
						qInst = q;
						break;
					}
				}
				
				messageChat = ChatMenu.getDefaultMenu(duringMessage);
				messageChat.setQuestBacker(qInst);
			}
		} else {
			messageChat = new BioptionChatMenu(chat,
					new QuestStartAction(quest, new FancyMessage(this.name).color(ChatColor.DARK_GRAY).style(ChatColor.BOLD)
							.then("\n").then(chat.getBody()), chat.getResponse1(), player),  null);			
		}

		messageChat.show(player);
	}
	
	@Override
	public Location getLocation() {
		Entity e = getEntity();
		
		if (e != null) {
			return e.getLocation();
		} else {
			return null;
		}
	}
}
