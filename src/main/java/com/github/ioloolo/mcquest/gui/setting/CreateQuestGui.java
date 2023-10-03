package com.github.ioloolo.mcquest.gui.setting;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.gui.GuiBase;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.bridge.SimpleLocation;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.game.QuestNpcUtil;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;

import lombok.Getter;

public final class CreateQuestGui extends GuiBase {

	private static final Storage<Quest> questStorage = Storage.of(Quest.class);

	private final Quest.Type type;

	@Getter
	private final Quest quest;

	private static final int NOW_FILTER_SLOT = 4;

	private static final int NAME_SLOT = 10;
	private static final int DESCRIPTION_SLOT = 11;
	private static final int REWARD_SLOT = 12;
	private static final int NPC_LOCATION_SLOT = 13;

	private static final int HUNT_TARGET_SLOT = 19;
	private static final int HUNT_AMOUNT_SLOT = 20;

	private static final int CREATE_QUEST_SLOT = 25;

	@Getter
	private static final Map<Player, CreateQuestGui> editName = new ConcurrentHashMap<>();

	@Getter
	private static final Map<Player, CreateQuestGui> editDescription = new ConcurrentHashMap<>();

	@Getter
	private static final Map<Player, CreateQuestGui> editReward = new ConcurrentHashMap<>();

	@Getter
	private static final Map<Player, CreateQuestGui> editHuntTarget = new ConcurrentHashMap<>();

	@Getter
	private static final Map<Player, CreateQuestGui> editHuntAmount = new ConcurrentHashMap<>();

	public CreateQuestGui(Quest.Type type) {
		super("&4&o[OP] &c&l퀘스트 &0- 퀘스트 생성", 4, SettingGui.class, (inventory, player) -> {
			// Type
			{
				inventory.setItem(NOW_FILTER_SLOT, createItem(
						switch (type) {
							case HUNT -> Material.IRON_SWORD;
							case VISIT -> Material.MINECART;
						},
						switch (type) {
							case HUNT -> "&c사냥";
							case VISIT -> "&b방문";
						} + " &f퀘스트 생성"
				));
			}

			inventory.setItem(NAME_SLOT, createItem(Material.NAME_TAG, "&l퀘스트 이름"));
			inventory.setItem(DESCRIPTION_SLOT, createItem(Material.PAPER, "&l퀘스트 설명"));
			inventory.setItem(REWARD_SLOT, createItem(Material.GOLD_INGOT, "&l보상"));
			inventory.setItem(NPC_LOCATION_SLOT, createItem(Material.COMPASS, "&lNPC 위치"));

			switch (type) {
				case HUNT -> {
					inventory.setItem(HUNT_TARGET_SLOT, createItem(Material.PIG_SPAWN_EGG, "&l사냥 동물"));
					inventory.setItem(HUNT_AMOUNT_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&l사냥 수"));
				}
			}
		});

		this.type = type;
		this.quest = new Quest();

		this.quest.setType(type);
		this.quest.setData(new HashMap<>());
	}

	@Override
	protected void onOpen(InventoryOpenEvent event) {
		refreshGui();
	}

	@Override
	protected void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		int slot = event.getSlot();

		assert item != null;

		switch (slot) {
			case NAME_SLOT -> {
				player.closeInventory();
				editName.put(player, this);

				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&e&l퀘스트 이름&f을 입력해주세요."))
								.append(ComponentUtil.footer())
								.build()
				);
			}

			case DESCRIPTION_SLOT -> {
				player.closeInventory();
				editDescription.put(player, this);

				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&e&l퀘스트 설명&f을 입력해주세요."))
								.append(ComponentUtil.footer())
								.build()
				);
			}

			case REWARD_SLOT -> {
				player.closeInventory();
				editReward.put(player, this);

				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&e&l퀘스트 보상&f을 입력해주세요."))
								.append(ComponentUtil.footer())
								.build()
				);
			}

			case NPC_LOCATION_SLOT -> {
				player.closeInventory();

				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&e&lNPC 위치&f를 설정해주세요."))
								.append(Component.newline())
								.append(
										Component.text()
												.append(ComponentUtil.color(" ".repeat(4)))
												.append(ComponentUtil.color("설정할 위치에 서서 "))
												.append(ComponentUtil.color("&a&l[클릭] ").clickEvent(ClickEvent.callback((e) -> {
													quest.setLocation(SimpleLocation.from(player.getLocation()));
													player.openInventory(inventory);
												})))
												.append(ComponentUtil.color("&f해주세요."))
								)
								.append(ComponentUtil.footer())
								.build()
				);
			}

			case CREATE_QUEST_SLOT -> {
				if (!isValid()) return;

				Scheduler.Async.run(() -> {
					questStorage.save(quest);

					player.sendMessage(
							Component.text()
									.append(ComponentUtil.header())
									.append(ComponentUtil.content("&e&l퀘스트&f가 생성되었습니다."))
									.append(ComponentUtil.footer())
									.build()
					);

					QuestNpcUtil.showAll(quest);
				});
			}
		}

		if (type.equals(Quest.Type.HUNT)) {
			switch (slot) {
				case HUNT_TARGET_SLOT -> {
					player.closeInventory();
					editHuntTarget.put(player, this);

					player.sendMessage(
							Component.text()
									.append(ComponentUtil.header())
									.append(ComponentUtil.content("&e&l사냥 동물&f을 입력해주세요."))
									.append(ComponentUtil.footer())
									.build()
					);
				}

				case HUNT_AMOUNT_SLOT -> {
					player.closeInventory();
					editHuntAmount.put(player, this);

					player.sendMessage(
							Component.text()
									.append(ComponentUtil.header())
									.append(ComponentUtil.content("&e&l사냥 수&f를 입력해주세요."))
									.append(ComponentUtil.footer())
									.build()
					);
				}
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void refreshGui() {
		boolean isValidName = quest.getName() != null && !quest.getName().isEmpty();
		lore(inventory.getItem(NAME_SLOT), "  - &l이름: &e" + (isValidName ? quest.getName() : "&c없음"));

		boolean isValidDescription = quest.getDescription() != null && !quest.getDescription().isEmpty();
		lore(inventory.getItem(DESCRIPTION_SLOT), "  - &l설명: &e" + (isValidDescription ? quest.getDescription() : "&c없음"));

		boolean isValidReward = quest.getReward() >= 0;
		lore(inventory.getItem(REWARD_SLOT), "  - &l보상: &e" + (isValidReward ? Formatter.number(quest.getReward()) : "&c없음"));

		boolean isValidLocation = quest.getLocation() != null;
		lore(inventory.getItem(NPC_LOCATION_SLOT),
				isValidLocation
						? new String[] {
						"  - &l월드: &e" + quest.getLocation().getWorld(),
						"  - &l위치: &e(%.1f, %.1f, %.1f)".formatted(
								quest.getLocation().getX(),
								quest.getLocation().getY(),
								quest.getLocation().getZ()
						)}
						: new String[] {"  - &c없음"}
				);

		if (type.equals(Quest.Type.HUNT)) {
			Map<String, Object> data = quest.getData();

			boolean isValidTarget = data.containsKey("target") && data.get("target") instanceof EntityType;
			lore(inventory.getItem(HUNT_TARGET_SLOT), "  - &l사냥 동물: &e" + (isValidTarget ? ((EntityType) data.get("target")).name() : "&c없음"));

			boolean isValidAmount = data.containsKey("amount") && data.get("amount") instanceof Integer && (int) data.get("amount") > 0;
			lore(inventory.getItem(HUNT_AMOUNT_SLOT), "  - &l사냥 수: &e" + (isValidAmount ? Formatter.number((int) data.get("amount")) : "&c없음"));
		}

		inventory.setItem(CREATE_QUEST_SLOT, createItem(Material.CRAFTING_TABLE, "&l퀘스트 생성"));

		if (!isValid()) {
			highlight(inventory.getItem(CREATE_QUEST_SLOT));
			lore(inventory.getItem(CREATE_QUEST_SLOT), "&c퀘스트 생성을 위해선 모든 정보가 필요합니다.");
		}
	}

	private boolean isValid() {
		boolean valid = true;

		valid &= quest.getName() != null && !quest.getName().isEmpty();
		valid &= quest.getDescription() != null && !quest.getDescription().isEmpty();
		valid &= quest.getReward() >= 0;
		valid &= quest.getLocation() != null;

		if (type.equals(Quest.Type.HUNT)) {
			Map<String, Object> data = quest.getData();

			valid &= data.containsKey("target") && data.get("target") instanceof EntityType;
			valid &= data.containsKey("amount") && data.get("amount") instanceof Integer && (int) data.get("amount") > 0;
		}

		return valid;
	}
}
