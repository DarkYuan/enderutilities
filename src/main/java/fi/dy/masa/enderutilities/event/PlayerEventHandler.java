package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;

public class PlayerEventHandler
{
	@SubscribeEvent
	public void onStartStracking(PlayerEvent.StartTracking event)
	{
		if (event.entity != null && event.target != null && event.entity.worldObj.isRemote == false)
		{
			// Remount the entity if the player starts tracking an entity he is supposed to be riding already
			if (event.entity.ridingEntity == event.target)
			{
				event.entity.mountEntity(event.target);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerOpenContainerEvent event)
	{
		if (event == null || event.entityPlayer == null)
		{
			return;
		}

		EntityPlayer player = event.entityPlayer;
		ItemStack stack = player.getCurrentEquippedItem();
		if (stack != null && stack.getItem() != null)
		{
			if (stack.getItem() == EnderUtilitiesItems.enderBag)
			{
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt != null && nbt.getBoolean("IsOpen") == true)
				{
					if (player.openContainer != player.inventoryContainer)
					{
						// Allow access from anywhere with the Ender Bag (bypassing the distance checks)
						event.setResult(Result.ALLOW);
					}
					// Ender Bag: Player has just closed the remote container
					else
					{
						nbt.removeTag("ChunkLoadingRequired");
						nbt.setBoolean("IsOpenDummy", true);
						stack.setTagCompound(nbt);
						//player.inventory.markDirty();
					}
				}
			}
		}
	}
}