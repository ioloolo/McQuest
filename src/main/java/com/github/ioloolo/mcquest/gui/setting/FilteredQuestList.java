package com.github.ioloolo.mcquest.gui.setting;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.gui.GuiBase;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.Scheduler;

public final class FilteredQuestList extends GuiBase {

	private static final Storage<Quest> questStorage = Storage.of(Quest.class);

	private static final int NOW_FILTER_SLOT = 4;

	private static final int CREATE_QUEST_SLOT = 8;

	private static final int NOW_PAGE_SLOT = 49;
	private static final int NEXT_PAGE_SLOT = 53;
	private static final int PREVIOUS_PAGE_SLOT = 45;

	private final int page;
	private final Quest.Type type;

	public FilteredQuestList(Quest.Type type) {
		this(1, type);
	}

	public FilteredQuestList(int page, Quest.Type type) {
		super("&4&o[OP] &c&l퀘스트 &0- 퀘스트 목록", 6, SettingGui.class, (inventory, player) -> {
			// Now Filter
			{
				inventory.setItem(NOW_FILTER_SLOT, createItem(
						switch (type) {
							case HUNT -> Material.IRON_SWORD;
							case VISIT -> Material.MINECART;
						},
						"&l현재 필터: " + switch (type) {
							case HUNT -> "&c사냥";
							case VISIT -> "&b방문";
						}
				));
			}

			// Create Quest
			{
				inventory.setItem(CREATE_QUEST_SLOT, createItem(Material.BOOK, "&a퀘스트 생성"));
			}

			// Now page
			{
				inventory.setItem(NOW_PAGE_SLOT, createItem(Material.PAPER, "&a현재 페이지: &f" + page, page));
			}

			// Previous Page
			{
				if (page > 1) {
					inventory.setItem(PREVIOUS_PAGE_SLOT, createItem(Material.ARROW, "&a이전 페이지"));
				}
			}
		});

		this.type = type;
		this.page = page;
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	protected void onOpen(InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();

		Scheduler.Async.run(() -> {
			List<Quest> questList = questStorage.get()
					.stream()
					.filter(quest -> quest.getType() == type)
					.toList();

			// Now Filter
			{
				lore(inventory.getItem(NOW_FILTER_SLOT), "  - &l총 갯수: &e" + questList.size() + "개");
			}

			// This Page
			{
				final int[] i = {10};
				questList.stream()
						.skip((page - 1) * 28L)
						.limit(28)
						.forEach(quest -> {
							inventory.setItem(i[0], createItem(
									switch (quest.getType()) {
										case HUNT -> Material.IRON_SWORD;
										case VISIT -> Material.MINECART;
									},
									quest.getName()
							));

							List<String> lores = new ArrayList<>(List.of(
									"  - &l이름: &f" + quest.getName(),
									"  - &l설명: &f" + quest.getDescription(),
									"  - &l보상: &f" + Formatter.number(quest.getReward()) + "원"
							));

							if (quest.getType().equals(Quest.Type.HUNT)) {
								Map<String, Object> data = quest.getData();

								lores.add("");
								lores.add("  - &l사냥 대상: &f" + data.get("target"));
								lores.add("  - &l사냥 수: &f" + data.get("amount") + "마리");
							}

							lore(inventory.getItem(i[0]), lores.toArray(String[]::new));

							if (i[0] == 16 || i[0] == 25 || i[0] == 34) i[0] += 2;
							++i[0];
						});
			}

			// Next Page
			{
				int maxPage = (int) Math.ceil(questList.size() / 28.0);

				if (maxPage > page) {
					inventory.setItem(NEXT_PAGE_SLOT, createItem(Material.ARROW, "&a다음 페이지"));
				}
			}
		});
	}

	@Override
	protected void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		int slot = event.getSlot();

		assert item != null;

		switch (slot) {
			case CREATE_QUEST_SLOT -> {
				switch (type) {
					case HUNT -> new CreateQuestGui(Quest.Type.HUNT).open(player);
					case VISIT -> new CreateQuestGui(Quest.Type.VISIT).open(player);
				}
			}
			case NEXT_PAGE_SLOT -> {
				if (item.getType() == Material.ARROW) {
					new FilteredQuestList(page + 1, type).open(player);
				}
			}
			case PREVIOUS_PAGE_SLOT -> {
				if (item.getType() == Material.ARROW) {
					new FilteredQuestList(page - 1, type).open(player);
				}
			}
		}
	}
}
