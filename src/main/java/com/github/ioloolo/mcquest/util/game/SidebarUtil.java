package com.github.ioloolo.mcquest.util.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.kyori.adventure.text.Component;

import com.github.ioloolo.mcquest.data.Quest;
import com.github.ioloolo.mcquest.data.QuestStatus;
import com.github.ioloolo.mcquest.util.Formatter;
import com.github.ioloolo.mcquest.util.Scheduler;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.github.ioloolo.mcquest.util.mc.ColorUtil;

public final class SidebarUtil {

	private static final Storage<QuestStatus> storage = Storage.of(QuestStatus.class);

	public static void show(Player player) {
		Scheduler.Sync.run(() -> {
			ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
			Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

			Objective objective = scoreboard.getObjective("sidebar:" + player.getUniqueId());

			if (objective != null) {
				objective.unregister();
			}

			objective = scoreboard.registerNewObjective("sidebar:"+player.getUniqueId(), Criteria.DUMMY, Component.text(""));
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			Objective finalObjective = objective;
			Scheduler.Async.run(() -> {
				QuestStatus questStatus = storage.get(player.getUniqueId()).orElseThrow();
				Quest quest = questStatus.getNowQuestOriginal();

				List<String> contents = new ArrayList<>(List.of(
						"&f===== &e&lMc Quest &f=====",
						"",
						"  - &l이름: &a" + player.getName(),
						"  - &l돈: &a" + Formatter.number(questStatus.getMoney()) + "원",
						" ",
						"  - &l퀘스트: " + (quest == null ? "&c없음" : quest.getName()),
						quest != null ? "     - &l타입: &f" + quest.getType().getName() : "",
						quest != null ? "     - &l보상: &f" + Formatter.number(quest.getReward()) + "원" : "",
						quest != null && quest.getType().equals(Quest.Type.HUNT)
								? "     - &l현황: &f%d &f/ %d  &7&o(마리)&f".formatted(
								questStatus.getHuntCount(),
								Integer.parseInt(quest.getData().get("amount").toString())
						)
								: "",
						"   ",
						"&f====================="
				));
				Collections.reverse(contents);

				for (int i = 0; i < contents.size(); i++) {
					finalObjective.getScore(ColorUtil.color(contents.get(i))).setScore(i);
				}

				player.setScoreboard(scoreboard);
			});
		});
	}
}
