package com.github.ioloolo.mcquest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.ioloolo.mcquest.util.game.SidebarUtil;

public final class ShowSidebar extends EventBase<PlayerJoinEvent> {

	void onEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		SidebarUtil.show(player);
	}
}
