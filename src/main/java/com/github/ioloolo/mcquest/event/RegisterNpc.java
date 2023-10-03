package com.github.ioloolo.mcquest.event;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.ioloolo.mcquest.util.game.QuestNpcUtil;

public final class RegisterNpc extends EventBase<PlayerJoinEvent> {

	void onEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		QuestNpcUtil.getNpcList().put(player, new HashMap<>());
		QuestNpcUtil.show(player);
	}
}
