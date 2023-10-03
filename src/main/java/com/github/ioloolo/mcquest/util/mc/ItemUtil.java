package com.github.ioloolo.mcquest.util.mc;

import java.util.Arrays;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ItemUtil {

	public static final Supplier<ItemStack> BORDER = () -> createItem(Material.BLACK_STAINED_GLASS_PANE, "");

	public static final Supplier<ItemStack> EMPTY_CONTENT= () -> createItem(Material.WHITE_STAINED_GLASS_PANE, "");

	public static final Supplier<ItemStack> BACK = () -> createItem(Material.BARRIER, "뒤로 가기");

	public static ItemStack createItem(Material material, String name, String... lore) {
		ItemStack itemStack = new ItemStack(material, 1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.displayName(ComponentUtil.color(name));
		itemMeta.lore(Arrays.stream(lore).map(ComponentUtil::color).map(Component::asComponent).toList());
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack createItem(Material material, String name, int amount, String... lore) {
		ItemStack item = createItem(material, name, lore);
		item.setAmount(amount);
		return item;
	}

	public static void highlight(ItemStack itemStack) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
		itemStack.setItemMeta(itemMeta);
	}

	public static void lore(ItemStack itemStack, String... lore) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.lore(Arrays.stream(lore).map(ComponentUtil::color).map(TextComponent::asComponent).toList());
		itemStack.setItemMeta(itemMeta);
	}
}
