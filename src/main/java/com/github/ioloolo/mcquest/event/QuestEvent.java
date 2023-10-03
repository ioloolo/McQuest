package com.github.ioloolo.mcquest.event;

import java.util.UUID;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

import com.github.ioloolo.mcquest.McQuest;
import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.game.QuestStatusUtil;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;

import dev.sergiferry.playernpc.api.NPC;

public final class QuestEvent extends EventBase<NPC.Events.Interact> {

	private static final Storage<QuestStatus> storage = Storage.of(QuestStatus.class);

	void onEvent(NPC.Events.Interact event) {
		Player player = event.getPlayer();
		NPC npc = event.getNPC();
		NPC.Interact.ClickType clickType = event.getClickType();

		if (clickType.isLeftClick()) return;

		if (!npc.hasCustomData(McQuest.getInstance(), "quest")) return;

		Scheduler.Async.run(() -> {
			QuestStatus questStatus = storage.get(player.getUniqueId()).orElseThrow();
			Quest nowQuest = questStatus.getNowQuestOriginal();

			if (nowQuest != null) {
				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&c퀘스트를 이미 진행중입니다."))
								.append(ComponentUtil.footer())
				);

				return;
			}

			String questId = npc.grabCustomData(McQuest.getInstance(), "quest").orElseThrow();
			Quest quest = Storage.of(Quest.class).get(UUID.fromString(questId)).orElseThrow();

			if (questStatus.getCompletedQuests().contains(quest.toReference())) {
				player.sendMessage(
						Component.text()
								.append(ComponentUtil.header())
								.append(ComponentUtil.content("&c이미 완료한 퀘스트입니다."))
								.append(ComponentUtil.footer())
				);

				return;
			}

			QuestStatusUtil.start(player, quest);
		});
	}
}
