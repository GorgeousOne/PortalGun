package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockDragHandler {

	private PortalMain main;
	private BlockHandler blockHandler;
	private BukkitRunnable blockDragTask;

	private double dragSpeedFactor;

	public BlockDragHandler(PortalMain main, BlockHandler blockHandler) {
		this.main = main;
		this.blockHandler = blockHandler;

		dragSpeedFactor = 0.5;
		startBlockDragTask();
	}

	public void unload() {
		blockDragTask.cancel();
	}

	private void startBlockDragTask() {

		blockDragTask = new BukkitRunnable() {
			@Override
			public void run() {

				for(Map.Entry<UUID, FallingBlock> entry : blockHandler.getLiftedBlocks()) {

					Player player = Bukkit.getPlayer(entry.getKey());
					FallingBlock liftedBlock = entry.getValue();

					Location playerLoc = player.getEyeLocation();
					Location blockLoc = liftedBlock.getLocation();

					if (liftedBlock.isDead())
						blockHandler.respawnLiftedBlock(player);

					Vector newVelocity = calculateDragVelocity(player, liftedBlock);

					if (newVelocity.getY() < 0 && fallingBlockIsAboveGround(liftedBlock, 0.1))
						newVelocity.setY(0);

					double hitbox = 0.49000000953674;

					if (newVelocity.getX() < 0 && Math.abs(blockLoc.getX() % 1 - 0.49) < 0.001) {
						newVelocity.setX(0);
						System.out.println(Math.abs(blockLoc.getX() % 1 - 0.49));
						player.playNote(player.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
					}

					if (newVelocity.getZ() < 0 && Math.abs(blockLoc.getZ() % 1 - 0.49) < 0.001) {
						newVelocity.setZ(0);
						player.playNote(player.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
					}

					liftedBlock.setVelocity(newVelocity);
				}
			}
		};

		blockDragTask.runTaskTimer(main, 0, 1);
	}

	private Vector calculateDragVelocity(Player player, FallingBlock liftedBlock) {

		double holdingDistance = blockHandler.getHoldingDistance(player);
		Location playerLoc = player.getEyeLocation();
		Location blockLoc = liftedBlock.getLocation();
		Location targetLoc = playerLoc.clone().add(playerLoc.getDirection().multiply(holdingDistance));

		return targetLoc.clone().subtract(blockLoc).toVector().multiply(dragSpeedFactor);
	}

	private boolean fallingBlockIsAboveGround(FallingBlock fallingBlock, double maxDistanceToGround) {

		Location blockLoc = fallingBlock.getLocation();

		if (blockLoc.getY() % 1 > maxDistanceToGround)
			return false;


		List<Vector> relativeBlockEdges = new ArrayList<>(Arrays.asList(
				new Vector(-0.5, 0, -0.5),
				new Vector(0.5, 0, -0.5),
				new Vector(0.5, 0, 0.5),
				new Vector(-0.5, 0, 0.5)
		));

		for (Vector relativeEdge : relativeBlockEdges) {

			Location blockEdge = blockLoc.clone().add(relativeEdge);
			blockEdge.subtract(0, maxDistanceToGround, 0);

			if (blockEdge.getBlock().getType().isSolid())
				return true;
		}

		return false;
	}
}
