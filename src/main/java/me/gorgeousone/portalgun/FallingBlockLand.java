package me.gorgeousone.portalgun;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class FallingBlockLand implements Listener {

	private BlockHandler blockHandler;

	public FallingBlockLand(BlockHandler blockHandler) {
		this.blockHandler = blockHandler;
	}

	@EventHandler
	public void onFallingBlockLand(EntityChangeBlockEvent event) {

		if(event.getEntity().getType() != EntityType.FALLING_BLOCK)
			return;

		FallingBlock fallingBlock = (FallingBlock) event.getEntity();
		Player player = blockHandler.getPlayerByLiftedBlock(fallingBlock);

		if(player == null)
			return;

		event.setCancelled(true);
		blockHandler.respawnLiftedBlock(player);
	}
}
