package com.github.ioloolo.mcquest.command;

import org.bukkit.entity.Player;

public abstract class CommandBase {

	protected String command;
	protected String[] aliases;

	protected CommandBase(String command, String... aliases) {
		this.command = command;
		this.aliases = aliases;
	}

	abstract void onCommand(Player player, String[] args);
}
