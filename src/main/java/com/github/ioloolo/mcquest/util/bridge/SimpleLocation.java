package com.github.ioloolo.mcquest.util.bridge;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class SimpleLocation {

	private String world;
	private double x;
	private double y;
	private double z;

	public static SimpleLocation from(Location location) {
		return SimpleLocation.builder()
				.world(location.getWorld().getName())
				.x(location.getX())
				.y(location.getY())
				.z(location.getZ())
				.build();
	}

	public Location real() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
}
