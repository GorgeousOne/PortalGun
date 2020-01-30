package me.gorgeousone.portalgun;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BlockInteract implements Listener {

	private BlockHandler blockHandler;

	public BlockInteract(BlockHandler blockHandler) {
		this.blockHandler = blockHandler;
	}

	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {

		Player player = event.getPlayer();

		if (event.getHand() != EquipmentSlot.HAND)
			return;

		ItemStack heldItem = event.getItem();

		if (heldItem == null || heldItem.getType() != Material.DIAMOND_HORSE_ARMOR)
			return;

		event.setCancelled(true);

		if (blockHandler.isLiftingBlock(player)) {

			Action action = event.getAction();

			if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
				blockHandler.dropLiftedBlock(player);

		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			Block clickedBlock = event.getClickedBlock();

			if (clickedBlock.getState() instanceof InventoryHolder) {
				player.sendMessage(ChatColor.GRAY + "Can't move this block.");
				return;
			}

			blockHandler.liftBlock(player, clickedBlock);
		}
	}
}
