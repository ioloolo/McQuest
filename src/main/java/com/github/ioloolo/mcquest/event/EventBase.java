package com.github.ioloolo.mcquest.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import lombok.Getter;

@Getter
public abstract class EventBase<T extends Event> {

	private final EventPriority priority;

	protected EventBase() {
		this(EventPriority.NORMAL);
	}

	protected EventBase(EventPriority priority) {
		this.priority = priority;
	}

	abstract void onEvent(T event);
}
