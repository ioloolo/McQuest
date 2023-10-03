package com.github.ioloolo.mcquest.util.mc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ComponentUtil {

	public static TextComponent color(String string) {
		return Component.text(ColorUtil.color(string));
	}

	public static TextComponent header() {
		return Component.text()
				.append(Component.newline())
				.append(ComponentUtil.color("&b" + "=".repeat(27)))
				.append(Component.newline())
				.append(Component.newline())
				.build();
	}

	public static TextComponent footer() {
		return Component.text()
				.append(Component.newline())
				.append(ComponentUtil.color("&b" + "=".repeat(27)))
				.append(Component.newline())
				.build();
	}

	public static TextComponent content(String text) {
		return Component.text()
				.append(ComponentUtil.color(" ".repeat(4)))
				.append(ComponentUtil.color(text))
				.append(Component.newline())
				.build();
	}
}
