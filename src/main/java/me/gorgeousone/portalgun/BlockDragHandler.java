package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the dragging of lifted blocks continuously.
 */
public class BlockDragHandler {

	private PortalMain main;
	private BlockHandler blockHandler;
	private BukkitRunnable blockDragTask;
	
	public BlockDragHandler(PortalMain main, BlockHandler blockHandler) {
		this.main = main;
		this.blockHandler = blockHandler;

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

					if (liftedBlock.isDead())
						blockHandler.respawnLiftedBlock(player);

					Vector newVelocity = calculateDragVelocity(player, liftedBlock);
					Location blockLoc = liftedBlock.getLocation();

					//stops blocks from landing again and again when pushed towards the ground
					if (newVelocity.getY() < 0 && isAboveGround(liftedBlock))
						newVelocity.setY(0);

//					//stops blocks from glitching into walls
//					if (newVelocity.getX() < 0 && Math.abs(blockLoc.getX() % 1 - 0.49) < 0.001) {
//						newVelocity.setX(0);
//						player.playNote(player.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
//					}
//
//					if (newVelocity.getZ() < 0 && Math.abs(blockLoc.getZ() % 1 - 0.49) < 0.001) {
//						newVelocity.setZ(0);
//						player.playNote(player.getLocation(), Instrument.BANJO, Note.natural(0, Note.Tone.C));
//					}

					liftedBlock.setVelocity(newVelocity);
				}
			}
		};

		blockDragTask.runTaskTimer(main, 0, 1);
	}

	//calculates the velocity for the block so it floats towards the location the player is looking at
	private Vector calculateDragVelocity(Player player, FallingBlock liftedBlock) {

		//calculates the location in front of the player
		double holdingDistance = blockHandler.getHoldingDistance(player);
		Location playerLoc = player.getEyeLocation();
		Location targetLoc = playerLoc.clone().add(playerLoc.getDirection().multiply(holdingDistance));
		
		//calculates the velocity the block will be dragged with towards the target location
		Location blockLoc = liftedBlock.getLocation();
		double dragSpeedFactor = 0.5;
		return targetLoc.clone().subtract(blockLoc).toVector().multiply(dragSpeedFactor);
	}

	
	private boolean isAboveGround(FallingBlock fallingBlock) {

		Location blockLoc = fallingBlock.getLocation();

		if (blockLoc.getY() % 1 > 0.1)
			return false;

		List<Vector> relativeBlockEdges = new ArrayList<>(Arrays.asList(
				new Vector(-0.5, 0, -0.5),
				new Vector(0.5, 0, -0.5),
				new Vector(0.5, 0, 0.5),
				new Vector(-0.5, 0, 0.5)
		));

		for (Vector relativeEdge : relativeBlockEdges) {

			Location blockEdge = blockLoc.clone().add(relativeEdge);
			blockEdge.subtract(0, 0.1, 0);

			if (blockEdge.getBlock().getType().isSolid())
				return true;
		}

		return false;
	}
}
