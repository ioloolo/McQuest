package com.github.ioloolo.mcquest.util.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.ioloolo.mcquest.McQuest;
import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;

import dev.sergiferry.playernpc.api.NPC;
import dev.sergiferry.playernpc.api.NPCLib;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class QuestNpcUtil {

	private static final Storage<Quest> storage = Storage.of(Quest.class);

	private static final NPCLib npcLib = NPCLib.getInstance();

	@Getter
	private static final Map<Player, Map<Quest, NPC.Personal>> npcList = new HashMap<>();

	public static void show(Player player) {
		Scheduler.Async.run(() -> storage.get().forEach(quest -> show(player, quest)));
	}

	public static void show(Player player, Quest quest) {
		Scheduler.Async.run(() -> {
			Map<Quest, NPC.Personal> personalMap = npcList.get(player);

			if (personalMap.containsKey(quest)) {
				return;
			}

			QuestStatus questStatus = Storage.of(QuestStatus.class).get(player.getUniqueId()).orElseThrow();

			Scheduler.Sync.run(() -> {
				personalMap.put(quest, npcLib.generatePersonalNPC(player, McQuest.getInstance(), UUID.randomUUID().toString(), quest.getLocation().real()));

				String typeStr = switch (quest.getType()) {
					case HUNT -> "&c&l사냥";
					case VISIT -> "&b&l방문";
				};

				List<String> name = new ArrayList<>(List.of("&7[&e&l퀘스트 &f- " + typeStr + "&7]", quest.getName()));

				if (questStatus.getCompletedQuests().contains(quest.toReference())) {
					name.add("&a&l완료");
				} else if (questStatus.getNowQuest() != null && questStatus.getNowQuest().equals(quest.toReference())) {
					name.add("&e&l진행중");
				} else {
					name.add("&c&l미수락");
				}

				personalMap.get(quest).setText(name);
				personalMap.get(quest).setGazeTrackingType(NPC.GazeTrackingType.PLAYER);
				personalMap.get(quest).setNameTag(new NPC.NameTag(""));

				personalMap.get(quest).setCustomData(
						McQuest.getInstance(),
						"quest",
						quest.getId().toString()
				);

				personalMap.get(quest).setSkin(new NPC.Skin.SignedTexture(
						"ewogICJ0aW1lc3RhbXAiIDogMTY5NjMzODg5ODExOSwKICAicHJvZmlsZUlkIiA6ICI1OGQ2YmFhYTQ5ZjA0Nzg2OGE1MzE1ZWViYmU0YWIwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJfemFpZGdyaWRfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M1MmQ3ZjE4N2ExMDliZDFhZjExNWU5YTM3YzZhOGZlZTM5ZGEwMTU4ZTU3Y2NkMGRiZGFiZWYwMTI4YzM0ZjMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
						"ax9RhIkeHehC15aSyx/V8+DK5/Eeq/zcRPrVC00kalR0dbpugDH0mGgd1ZH/0+Fntc0xW6oJX7NQ4e4ScDdujvFq8//QEIPW6XJtDuDv2AUPaHgDpAtd4tp/WLenxNB0BOvZK31ypWizay4zJpmTyXykqlMqN/2RNmd6uTIGiiarU6FK3WNDuscCqNbLdThfp+8STbzAyaA3a053stmAICzKawE5cnoe9Ad4fYHuHEFnmqhwP+oTaXewn8Mslkppsi/6EZiMSJqZiW53oKOq49DpazZnXQ3rwfZTLKs4S6IE4/vu15knU60rbK79RMHwA8CPX5crJuPwCk+Gf1bLZkEiMx1zNTCMXpWgovXlpzL81FnsgzzcYg3/6iqaoEhj1Z7GJA0KHdxYt1pwhB1MWBhrz3znTX4sJW6kbvTXS3obQLBOtzXxgjJ49IgJOpLFl8OHvNFIfBWmQg+ACbVz8PQ3XmVcpp75Q6upRYbIWt1XQaPdsqbc3ubHCpXI0Nn5hE/aHL9oUWn9Vc9bPtXwVqN3XwOKF/uv5E+Lo4VoeNilew3G+SWndh7kTxuHWAvz+TQGGRLfFQ5+pGfQYZd5Rsd6dh7BbhtlpV4LmXKA2w7D7PIwL/+tBYAqd81K/ywdkXceXU870vd74iKC1+3lRLh/CmAs+Duw2mZUDPNNiEI="
				));

				personalMap.get(quest).create();

				personalMap.get(quest).show();
			});
		});
	}

	public static void showAll() {
		Bukkit.getOnlinePlayers().forEach(QuestNpcUtil::show);
	}

	public static void showAll(Quest quest) {
		Bukkit.getOnlinePlayers().forEach(player -> show(player, quest));
	}

	public static void changState(Player player, Quest quest, Quest.Status status) {
		NPC.Personal npc = npcList.get(player).get(quest);

		String typeStr = switch (quest.getType()) {
			case HUNT -> "&c&l사냥";
			case VISIT -> "&b&l방문";
		};

		List<String> name = new ArrayList<>(List.of("&7[&e&l퀘스트 &f- " + typeStr + "&7]", quest.getName()));

		switch (status) {
			case STARTED -> {
				name.add("&e&l진행중");
			}
			case COMPLETED -> {
				name.add("&a&l완료");
			}
			case NOT_STARTED -> {
				name.add("&c&l미수락");
			}
		}

		npc.setText(name);

		npc.simpleUpdateText();
	}
}
