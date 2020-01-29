package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PortalGun extends JavaPlugin implements Listener {

	private Player gunHolder;
	private FallingBlock heldBlock;
	private boolean blockIsHeld;
	private double holdDistance;
	private BukkitRunnable blockDrag;
	private double maxDropVelocity;


	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		maxDropVelocity = 0.5;
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
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

		if(blockIsHeld) {

			Action action = event.getAction();

			if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
				dropHeldBlock();
			}

		}else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			Block clickedBlock = event.getClickedBlock();

			if(player.isSneaking())
				player.sendMessage(clickedBlock.getBlockData().getAsString(true));

			if (clickedBlock.getState() instanceof InventoryHolder) {
				player.sendMessage(ChatColor.GRAY + "Can't move this block.");
				return;
			}

			grabBlock(player, clickedBlock);
		}
	}

	private void grabBlock(Player player, Block block) {

		new BukkitRunnable() {
			@Override
			public void run() {
				Location blockMid = block.getLocation().add(0.5, 0, 0.5);
				heldBlock = block.getWorld().spawnFallingBlock(blockMid, block.getBlockData());
				heldBlock.setGravity(false);
				heldBlock.setDropItem(false);

				gunHolder = player;
				gunHolder.playNote(gunHolder.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.A));
				block.setType(Material.AIR);
				blockIsHeld = true;

				holdDistance = blockMid.distance(gunHolder.getEyeLocation());

				blockDrag = new BukkitRunnable() {
					@Override
					public void run() {

						if(!blockIsHeld)
							return;

						if(heldBlock.isDead()) {
							respawnBlock(heldBlock);
							return;
						}

						Location playerLoc = gunHolder.getEyeLocation();
						Location blockLoc = heldBlock.getLocation();

						Vector holdRange = playerLoc.getDirection().multiply(holdDistance);
						Location blockGoalLoc = playerLoc.clone().add(holdRange).subtract(0, 0.5, 0);

						Vector velocity = blockGoalLoc.clone().subtract(blockLoc).toVector();
						velocity.multiply(0.5);

						if(velocity.getY() < 0 && fallingBlockIsAboveGround(heldBlock, 0.1))
							velocity.setY(0);

						double hitbox = 0.49000000953674;

						if(velocity.getX() < 0 && Math.abs(blockLoc.getX() % 1 - 0.49) < 0.001) {
							velocity.setX(0);
							System.out.println(Math.abs(blockLoc.getX() % 1 - 0.49));
							gunHolder.playNote(gunHolder.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
						}

						if(velocity.getZ() < 0 && Math.abs(blockLoc.getZ() % 1 - 0.49) < 0.001) {
							velocity.setZ(0);
							gunHolder.playNote(gunHolder.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
						}

						heldBlock.setVelocity(velocity);
					}
				};

				blockDrag.runTaskTimer(PortalGun.this, 0, 1);
			}
		}.runTask(this);
	}

	private void respawnBlock(FallingBlock fallingBlock) {

		Location blockLoc = fallingBlock.getLocation();
		heldBlock = blockLoc.getWorld().spawnFallingBlock(blockLoc, fallingBlock.getBlockData());
		heldBlock.setGravity(false);
		heldBlock.setDropItem(false);

		gunHolder.playNote(gunHolder.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.E));
	}

	private boolean fallingBlockIsAboveGround(FallingBlock fallingBlock, double maxDistanceToGround) {

		Location blockLoc = fallingBlock.getLocation();

		if(blockLoc.getY() % 1 > maxDistanceToGround)
			return false;


		List<Vector> relativeBlockEdges = new ArrayList<>(Arrays.asList(
				new Vector(-0.5, 0, -0.5),
				new Vector( 0.5, 0, -0.5),
				new Vector( 0.5, 0,  0.5),
				new Vector(-0.5, 0,  0.5)
		));

		for(Vector relativeEdge : relativeBlockEdges) {

			Location blockEdge = blockLoc.clone().add(relativeEdge);
			blockEdge.subtract(0, maxDistanceToGround, 0);

			if(blockEdge.getBlock().getType().isSolid())
				return  true;
		}

		return false;
	}

	private void dropHeldBlock() {

		if(!blockDrag.isCancelled())
			blockDrag.cancel();

		gunHolder.playNote(gunHolder.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.D));

		if(!heldBlock.isDead()) {
			heldBlock.setGravity(true);

			Vector velocity = heldBlock.getVelocity();

			if(velocity.length() > maxDropVelocity)
				heldBlock.setVelocity(velocity.normalize().multiply(maxDropVelocity));
		}

		gunHolder = null;
		blockIsHeld = false;
		heldBlock = null;
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event) {

		if(!event.isSneaking())
			return;

		if(event.getPlayer().equals(gunHolder) && blockIsHeld) {
			Bukkit.broadcastMessage(ChatColor.GRAY + "Block floating at " + heldBlock.getLocation().toVector().toString());
		}
	}

	@EventHandler
	public void fallingBlockLand(EntityChangeBlockEvent event) {

		if(event.getEntity().equals(heldBlock)) {

			FallingBlock fallingBlock = (FallingBlock) event.getEntity();
			event.setCancelled(true);

			respawnBlock(heldBlock);
		}
	}
}
