package com.github.ioloolo.mcquest.util.game;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class QuestStatusUtil {

	private static final Storage<QuestStatus> storage = Storage.of(QuestStatus.class);

	public static void start(Player player, Quest quest) {
		if (quest.getType().equals(Quest.Type.VISIT)) {
			clear(player, quest);
			return;
		}

		Scheduler.Async.run(() -> {
			storage.update(player.getUniqueId(), (status) -> {
				status.setNowQuest(quest.toReference());
				status.setHuntCount(0);
			});

			SidebarUtil.show(player);
			QuestNpcUtil.changState(player, quest, Quest.Status.STARTED);
		});

		player.playSound(player.getLocation(), "entity.player.levelup", 1, 2);

		player.showTitle(Title.title(
				ComponentUtil.color(""),
				ComponentUtil.color("&e&l퀘스트&f를 받았습니다!"),
				Title.Times.times(
						Duration.of(500, ChronoUnit.MILLIS),
						Duration.of(2000, ChronoUnit.MILLIS),
						Duration.of(500, ChronoUnit.MILLIS)
				)
		));

		player.sendMessage(
				Component.text()
						.append(ComponentUtil.header())
						.append(ComponentUtil.content("&e&l퀘스트&7&o(" + quest.getName() + ")&f를 받았습니다!"))
						.append(Component.newline())
						.append(ComponentUtil.content(quest.getDescription()))
						.append(Component.newline())
						.append(ComponentUtil.content("&f퀘스트를 완료하면 &e" + Formatter.number(quest.getReward()) + "원&f을 받을 수 있습니다!"))
						.append(ComponentUtil.footer())
						.build()
		);
	}

	public static void clear(Player player) {
		Scheduler.Async.run(() -> {
			Quest quest = storage.get(player.getUniqueId()).orElseThrow().getNowQuestOriginal();

			if (quest == null) return;

			clear(player, quest);
		});
	}

	public static void clear(Player player, Quest quest) {
		Scheduler.Async.run(() -> {
			storage.update(player.getUniqueId(), (status) -> {
				status.setHuntCount(0);
				status.setNowQuest(null);
				status.setMoney(status.getMoney() + quest.getReward());

				status.setCompletedQuests(new ArrayList<>(status.getCompletedQuests()) {{
					add(quest.toReference());
				}});
			});

			SidebarUtil.show(player);
			QuestNpcUtil.changState(player, quest, Quest.Status.COMPLETED);
		});

		player.playSound(player.getLocation(), "entity.player.levelup", 1, 2);

		player.showTitle(Title.title(
				ComponentUtil.color(""),
				ComponentUtil.color("&e&l퀘스트&f를 완료했습니다!"),
				Title.Times.times(
						Duration.of(500, ChronoUnit.MILLIS),
						Duration.of(2000, ChronoUnit.MILLIS),
						Duration.of(500, ChronoUnit.MILLIS)
				)
		));

		player.sendMessage(
				Component.text()
						.append(ComponentUtil.header())
						.append(ComponentUtil.content("&e&l퀘스트&7&o(" + quest.getName() + ")&f를 완료했습니다!"))
						.append(Component.newline())
						.append(ComponentUtil.content("&f보상으로 &e" + Formatter.number(quest.getReward()) + "원&f을 받았습니다!"))
						.append(ComponentUtil.footer())
						.build()
		);
	}
}
