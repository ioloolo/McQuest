package com.github.ioloolo.mcquest.util.other;

@FunctionalInterface
public interface QuaFunction<T, U, V, K, R> {

	R apply(T t, U u, V v, K k);
}
