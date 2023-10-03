package com.github.ioloolo.mcquest.gui.common;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.gui.GuiBase;
import com.github.ioloolo.mcquest.gui.setting.SettingGui;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.game.QuestNpcUtil;
import com.github.ioloolo.mcquest.util.game.SidebarUtil;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class MainGui extends GuiBase {

	private static final Cache<UUID, Boolean> CHECK_QUIT_QUEUE = CacheBuilder.newBuilder()
			.expireAfterWrite(1, java.util.concurrent.TimeUnit.MINUTES)
			.build();

	private static final Storage<QuestStatus> questStatusStorage = Storage.of(QuestStatus.class);

	private static final int PLAYER_HEAD_SLOT = 4;
	private static final int NOW_QUEST_SLOT = 10;
	private static final int COMPLETED_QUEST_SLOT = 11;
	private static final int SETTING_SLOT = 16;

	public MainGui() {
		super("&c&l퀘스트 &0- 메인 메뉴", 3, (inventory, player) -> {
			// Player Head
			{
				ItemStack itemStack = createItem(Material.PLAYER_HEAD, "&a&l%s&f님의 &e정보".formatted(player.getName()));
				SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
				itemMeta.setOwningPlayer(player);
				itemStack.setItemMeta(itemMeta);

				inventory.setItem(PLAYER_HEAD_SLOT, itemStack);
			}

			// Now Quest
			{
				ItemStack itemStack = createItem(Material.BOOK, "&b진행중&f인 &e&l퀘스트");
				inventory.setItem(NOW_QUEST_SLOT, itemStack);
			}

			// Clear Quest
			{
				inventory.setItem(COMPLETED_QUEST_SLOT, createItem(Material.BOOKSHELF, "&a완료&f한 &e&l퀘스트"));
			}

			// [OP]: Setting
			{
				if (player.isOp()) {
					inventory.setItem(SETTING_SLOT, createItem(Material.COMPASS, "&c[OP] &a&l설정"));
				}
			}
		});
	}

	@Override
	@SuppressWarnings("DataFlowIssue")
	protected void onOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		Inventory inventory = event.getInventory();

		Scheduler.Async.run(() -> {
			QuestStatus questStatus = questStatusStorage.get(player.getUniqueId()).orElseThrow();
			Quest quest = questStatus.getNowQuestOriginal();

			lore(inventory.getItem(PLAYER_HEAD_SLOT), "  - &l보유금: &f" + Formatter.number(questStatus.getMoney()) + "원");

			boolean existNowQuest = quest != null;

			inventory.getItem(NOW_QUEST_SLOT).setType(existNowQuest ? Material.ENCHANTED_BOOK : Material.BOOK);

			if (existNowQuest) {
				lore(inventory.getItem(NOW_QUEST_SLOT),
						"  - &l이름: &f" + quest.getName(),
						"  - &l설명: &f" + quest.getDescription(),
						"  - &l보상: &f" + Formatter.number(quest.getReward()) + "원",
						"",
						"&e퀘스트&f를 &c취소&f하려면 &a&l[클릭] &f해주세요."
				);
			} else {
				lore(inventory.getItem(NOW_QUEST_SLOT), "&c진행중인 퀘스트가 없습니다");
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
			case NOW_QUEST_SLOT -> {
				if (item.getType() == Material.ENCHANTED_BOOK) {
					player.closeInventory();

					UUID uuid = UUID.randomUUID();
					CHECK_QUIT_QUEUE.put(uuid, true);

					player.sendMessage(
							Component.text()
									.append(ComponentUtil.header())
									.append(ComponentUtil.content("&f퀘스트를 정말 &c&l취소&f하시겠습니까?"))
									.append(Component.newline())
									.append(
											Component.text()
													.append(ComponentUtil.color(" ".repeat(12)))

													.append(ComponentUtil.color("&a[예]").clickEvent(ClickEvent.callback((e) -> {
														if (CHECK_QUIT_QUEUE.getIfPresent(uuid) == null) return;

														CHECK_QUIT_QUEUE.invalidate(uuid);

														Scheduler.Async.run(() -> {
															Quest quest = questStatusStorage.get(player.getUniqueId())
																	.orElseThrow()
																	.getNowQuestOriginal();

															questStatusStorage.update(player.getUniqueId(), (status) -> {
																status.setNowQuest(null);
																status.setHuntCount(0);
															});

															Scheduler.Sync.run(() -> {
																SidebarUtil.show(player);
																QuestNpcUtil.changState(player, quest, Quest.Status.NOT_STARTED);
															});
														});

														player.sendMessage(ComponentUtil.color("&a퀘스트를 취소하였습니다."));
													})))

													.append(Component.text(" ".repeat(2)))

													.append(ComponentUtil.color("&c[아니요]").clickEvent(ClickEvent.callback((e) -> {
														if (CHECK_QUIT_QUEUE.getIfPresent(uuid) == null) return;

														CHECK_QUIT_QUEUE.invalidate(uuid);

														player.sendMessage(ComponentUtil.color("&c퀘스트 취소를 취소하였습니다."));
													})))

													.append(Component.newline())
									)
									.append(ComponentUtil.footer())
									.build()
					);
				}
			}
			case COMPLETED_QUEST_SLOT -> new CompletedQuestList().open(player);
			case SETTING_SLOT -> {
				if (player.isOp()) {
					new SettingGui().open(player);
				}
			}
		}
	}
}
