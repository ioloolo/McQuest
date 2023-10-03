package com.github.ioloolo.mcquest.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ReflectionUtil {

	@SneakyThrows(ClassNotFoundException.class)
	public static Class<?> clazz(String name) {
		return Class.forName(name);
	}

	@SneakyThrows(NoSuchMethodException.class)
	public static Method method(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method method = clazz.getDeclaredMethod(name, parameterTypes);
		method.setAccessible(true);
		return method;
	}

	@SneakyThrows({InvocationTargetException.class, IllegalAccessException.class})
	public static void invoke(Method method, Object instance, Object... args) {
		method.invoke(instance, args);
	}

	@SneakyThrows({InstantiationException.class, IllegalAccessException.class, InvocationTargetException.class})
	public static Object newInstance(Class<?> clazz, Object... args) {
		Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor.newInstance(args);
	}

	@SneakyThrows(NoSuchFieldException.class)
	public static Field field(Class<?> clazz, String name) {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	@SneakyThrows({IllegalAccessException.class})
	public static Object getFieldData(Field field, Object instance) {
		return field.get(instance);
	}
}
