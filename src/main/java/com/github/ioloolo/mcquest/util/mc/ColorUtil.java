package com.github.ioloolo.mcquest.util.mc;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ColorUtil {

	public static String color(String string) {
		return ("&f" + string).replace('&', '§');
	}
}
