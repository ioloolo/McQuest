package com.github.ioloolo.mcquest.command;

import org.bukkit.entity.Player;

import com.github.ioloolo.mcquest.gui.common.MainGui;

public final class MyQuestCommand extends CommandBase {

	private MyQuestCommand() {
		super("quest", "퀘스트");
	}

	@Override
	void onCommand(Player player, String[] args) {
		new MainGui().open(player);
	}
}
