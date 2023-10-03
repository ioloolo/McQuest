package com.github.ioloolo.mcquest.event;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.game.QuestStatusUtil;
import com.github.ioloolo.mcquest.util.game.SidebarUtil;

public final class CheckHuntCount extends EventBase<EntityDeathEvent> {

	private static final Storage<QuestStatus> storage = Storage.of(QuestStatus.class);

	void onEvent(EntityDeathEvent event) {
		Player killer = event.getEntity().getKiller();

		if (killer == null) return;

		Scheduler.Async.run(() -> {
			QuestStatus questStatus = storage.get(killer.getUniqueId()).orElseThrow();
			Quest quest = questStatus.getNowQuestOriginal();

			if (quest == null) return;

			if (quest.getType() != Quest.Type.HUNT) return;

			int count = questStatus.getHuntCount();

			EntityType target = EntityType.valueOf(quest.getData().get("target").toString());
			int amount = Integer.parseInt(quest.getData().get("amount").toString());

			if (!event.getEntityType().equals(target)) return;

			if (count + 1 >= amount) {
				QuestStatusUtil.clear(killer);
			} else {
				storage.update(killer.getUniqueId(), (status) -> status.setHuntCount(status.getHuntCount() + 1));
			}

			SidebarUtil.show(killer);
		});
	}
}
