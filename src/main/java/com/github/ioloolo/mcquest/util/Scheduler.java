package com.github.ioloolo.mcquest.util;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import com.github.ioloolo.mcquest.McQuest;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Scheduler {

	public static final class Sync {

		public static void run(Runnable runnable) {
			Bukkit.getScheduler().runTask(McQuest.getInstance(), runnable);
		}

		public static void repeat(Runnable runnable, long delay, long period) {
			Bukkit.getScheduler().runTaskTimer(McQuest.getInstance(), runnable, delay, period);
		}

		public static void later(Runnable runnable, long delay) {
			Bukkit.getScheduler().runTaskLater(McQuest.getInstance(), runnable, delay);
		}
	}

	public static final class Async {

		public static void run(Runnable runnable) {
			Bukkit.getScheduler().runTaskAsynchronously(McQuest.getInstance(), (task) -> runnable.run());
		}

		public static void repeat(Runnable runnable, long delay, long period) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(McQuest.getInstance(), (task) -> runnable.run(), delay, period);
		}

		public static void later(Runnable runnable, long delay) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(McQuest.getInstance(), (task) -> runnable.run(), delay);
		}
	}
}
