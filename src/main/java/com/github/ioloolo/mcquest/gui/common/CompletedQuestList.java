package com.github.ioloolo.mcquest.gui.common;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import java.util.concurrent.ExecutionException;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.gui.GuiBase;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;

public final class CompletedQuestList extends GuiBase {

	private static final Storage<QuestStatus> questStatusStorage = Storage.of(QuestStatus.class);

	private static final int NOW_PAGE_SLOT = 49;
	private static final int NEXT_PAGE_SLOT = 53;
	private static final int PREVIOUS_PAGE_SLOT = 45;

	private final int page;

	public CompletedQuestList() {
		this(1);
	}

	public CompletedQuestList(int page) {
		super("&c&l퀘스트 &0- 완료한 퀘스트", 6, MainGui.class, (inventory, player) -> {
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

		this.page = page;
	}

	@Override
	protected void onOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		Inventory inventory = event.getInventory();

		Scheduler.Async.run(() -> {
			QuestStatus questStatus = questStatusStorage.get(player.getUniqueId()).orElseThrow();

			// This Page
			{
				final int[] i = {10};
				questStatus.getCompletedQuests()
						.stream()
						.skip((page - 1) * 28L)
						.limit(28)
						.forEach(documentReference -> {
							Quest quest;

							try {
								quest = documentReference.get().get().toObject(Quest.class);
							} catch (InterruptedException | ExecutionException e) {
								throw new RuntimeException(e);
							}

							assert quest != null;

							inventory.setItem(i[0], createItem(
									switch (quest.getType()) {
										case HUNT -> Material.IRON_SWORD;
										case VISIT -> Material.MINECART;
									},
									quest.getName(),
									"  - &l이름: &f" + quest.getName(),
									"  - &l설명: &f" + quest.getDescription(),
									"  - &l보상: &f" + Formatter.number(quest.getReward()) + "원"
							));

							if (i[0] == 16 || i[0] == 25 || i[0] == 34)
								i[0] += 2;

							++i[0];
						});
			}

			// Next Page
			{
				int maxPage = (int) Math.ceil(questStatus.getCompletedQuests().size() / 28.0);

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
			case NEXT_PAGE_SLOT -> {
				if (item.getType() == Material.ARROW) {
					new CompletedQuestList(page + 1).open(player);
				}
			}
			case PREVIOUS_PAGE_SLOT -> {
				if (item.getType() == Material.ARROW) {
					new CompletedQuestList(page - 1).open(player);
				}
			}
		}
	}
}
