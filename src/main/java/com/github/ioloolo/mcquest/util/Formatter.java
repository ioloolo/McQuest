package com.github.ioloolo.mcquest.util;

import java.text.DecimalFormat;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Formatter {

	public static String number(int number) {
		return new DecimalFormat("#,###").format(number);
	}
}
