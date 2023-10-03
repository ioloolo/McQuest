package com.github.ioloolo.mcquest.event;

import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.TextComponent;

import com.github.ioloolo.mcquest.gui.setting.CreateQuestGui;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;
import com.github.ioloolo.mcquest.util.other.QuaFunction;
import com.github.ioloolo.mcquest.util.other.TriFunction;

import io.papermc.paper.event.player.AsyncChatEvent;

public final class CheckQuestPropertyChange extends EventBase<AsyncChatEvent> {

	private static final QuaFunction<
				Player,
				Supplier<Map<Player, CreateQuestGui>>,
				TriFunction<Player, Map<Player, CreateQuestGui>, String, Boolean>,
				String,
				Boolean> task = (player, mapSupplier, subTask, content) -> {

		Map<Player, CreateQuestGui> map = mapSupplier.get();

		if (map.containsKey(player)) {
			Scheduler.Sync.run(() -> {
				if (subTask.apply(player, map, content)) {
					map.get(player).open(player);
					map.remove(player);
				}
			});

			return true;
		}

		return false;
	};

	void onEvent(AsyncChatEvent event) {
		Player player = event.getPlayer();
		String message = ((TextComponent) event.message()).content();

		boolean result = false;

		result |= task.apply(
				player,
				CreateQuestGui::getEditName,
				(p, map, content) -> {
					map.get(p).getQuest().setName(content);
					return true;
				},
				message
		);

		result |= task.apply(
				player,
				CreateQuestGui::getEditDescription,
				(p, map, content) -> {
					map.get(p).getQuest().setDescription(content);
					return true;
				},
				message
		);

		result |= task.apply(
				player,
				CreateQuestGui::getEditReward,
				(p, map, content) -> {
					try {
						int reward = Integer.parseInt(content);

						if (reward < 0) {
							p.sendMessage(ComponentUtil.color("&c보상은 0원보다 작을 수 없습니다."));
							return false;
						}

						map.get(p).getQuest().setReward(reward);

						return true;
					} catch (NumberFormatException e) {
						p.sendMessage(ComponentUtil.color("&c보상은 숫자여야 합니다."));
					}

					return false;
				},
				message
		);

		result |= task.apply(
				player,
				CreateQuestGui::getEditHuntTarget,
				(p, map, content) -> {
					try {
						EntityType type = EntityType.valueOf(content.toUpperCase());
						map.get(p).getQuest().getData().put("target", type);

						return true;
					} catch (IllegalArgumentException e) {
						p.sendMessage(ComponentUtil.color("&c존재하지 않는 퀘스트 타입입니다."));
					}

					return false;
				},
				message
		);

		result |= task.apply(
				player,
				CreateQuestGui::getEditHuntAmount,
				(p, map, content) -> {
					try {
						int amount = Integer.parseInt(content);

						if (amount < 0) {
							p.sendMessage(ComponentUtil.color("&c사냥 수는 0마리보다 작을 수 없습니다."));
							return false;
						}

						map.get(p).getQuest().getData().put("amount", amount);

						return true;
					} catch (NumberFormatException e) {
						p.sendMessage(ComponentUtil.color("&c사냥 수는 숫자여야 합니다."));
					}

					return false;
				},
				message
		);

		if (result) {
			event.setCancelled(true);
		}
	}
}
