package com.github.ioloolo.mcquest.event;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.game.SidebarUtil;

public final class CheckPlayerDocument extends EventBase<PlayerJoinEvent> {

	private static final Storage<QuestStatus> storage = Storage.of(QuestStatus.class);

	private CheckPlayerDocument() {
		super(EventPriority.MONITOR);
	}

	void onEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		Scheduler.Async.run(() -> {
			if (storage.get(player.getUniqueId()).isPresent()) return;

			storage.save(
					QuestStatus.builder()
							.id(player.getUniqueId())
							.nowQuest(null)
							.money(1000)
							.completedQuests(List.of())
							.build()
			);

			SidebarUtil.show(player);
		});
	}
}
