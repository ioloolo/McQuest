package com.github.ioloolo.mcquest.gui;

import static com.github.ioloolo.mcquest.util.mc.ItemUtil.*;

import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import com.github.ioloolo.mcquest.McQuest;
import com.github.ioloolo.mcquest.util.ReflectionUtil;
import com.github.ioloolo.mcquest.util.mc.ComponentUtil;

public abstract class GuiBase {

	protected Inventory inventory;

	protected GuiBase(String title, int rows, BiConsumer<Inventory, Player> draw) {
		this(title, rows, null, draw);
	}

	protected GuiBase(String title, int rows, Class<? extends GuiBase> previous, BiConsumer<Inventory, Player> draw) {
		inventory = Bukkit.createInventory(null, rows * 9, ComponentUtil.color(title));

		Bukkit
				.getPluginManager()
				.registerEvent(
						InventoryOpenEvent.class,
						new Listener() {},
						EventPriority.NORMAL,
						(listener, rawEvent) -> {
							InventoryOpenEvent event = (InventoryOpenEvent) rawEvent;

							Inventory inv = event.getInventory();

							if (!inv.equals(inventory)) return;

							for (int i = 0; i < 9*rows; i++) {
								inv.setItem(i, BORDER.get());
							}

							for (int i = 1; i < rows-1; i++) {
								for (int j = 1; j < 9-1; j++) {
									inv.setItem(i*9 + j, EMPTY_CONTENT.get());
								}
							}

							if (previous != null) {
								inv.setItem(0, BACK.get());
							}

							draw.accept(inv, (Player) event.getPlayer());

							onOpen(event);
						},
						McQuest.getInstance()
				);

		Bukkit
				.getPluginManager()
				.registerEvent(
						InventoryClickEvent.class,
						new Listener() {},
						EventPriority.NORMAL,
						(listener, rawEvent) -> {
							InventoryClickEvent event = (InventoryClickEvent) rawEvent;

							if (!event.getInventory().equals(inventory)) return;

							event.setCancelled(true);

							if (previous != null && event.getSlot() == 0) {
								ReflectionUtil.invoke(
										ReflectionUtil.method(GuiBase.class, "open", Player.class),
										ReflectionUtil.newInstance(previous),
										event.getWhoClicked()
								);
								return;
							}

							ReflectionUtil.invoke(
									ReflectionUtil.method(this.getClass(), "onClick", InventoryClickEvent.class),
									this,
									event
							);
						},
						McQuest.getInstance()
				);
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}

	protected void onOpen(InventoryOpenEvent event) {};
	protected abstract void onClick(InventoryClickEvent event);
}
