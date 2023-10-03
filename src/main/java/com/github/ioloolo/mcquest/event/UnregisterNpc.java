package com.github.ioloolo.mcquest.event;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.util.game.QuestNpcUtil;

import dev.sergiferry.playernpc.api.NPC;

public final class UnregisterNpc extends EventBase<PlayerQuitEvent> {

	void onEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		Map<Quest, NPC.Personal> personalMap = QuestNpcUtil.getNpcList().get(player);

		if (personalMap == null) {
			return;
		}

		personalMap.forEach((quest, npc) -> {
			npc.destroy();
		});
	}
}
