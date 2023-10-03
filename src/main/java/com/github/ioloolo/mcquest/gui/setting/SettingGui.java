package com.github.ioloolo.mcquest.gui.setting;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.gui.GuiBase;
import com.github.ioloolo.mcquest.gui.common.MainGui;

public final class SettingGui extends GuiBase {

	private static final int VISIT_QUEST_LIST_SLOT = 10;
	private static final int HUNT_QUEST_LIST_SLOT = 11;

	public SettingGui() {
		super("&4&o[OP] &c&l퀘스트 &0- 설정", 3, MainGui.class, (inventory, player) -> {
			inventory.setItem(VISIT_QUEST_LIST_SLOT, createItem(Material.MINECART, "&b&l방문 퀘스트 &f목록"));
			inventory.setItem(HUNT_QUEST_LIST_SLOT, createItem(Material.IRON_SWORD, "&c&l사냥 퀘스트 &f목록"));
		});
	}

	@Override
	protected void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		int slot = event.getSlot();

		assert item != null;

		switch (slot) {
			case VISIT_QUEST_LIST_SLOT -> new FilteredQuestList(Quest.Type.VISIT).open(player);
			case HUNT_QUEST_LIST_SLOT -> new FilteredQuestList(Quest.Type.HUNT).open(player);
		}
	}
}
