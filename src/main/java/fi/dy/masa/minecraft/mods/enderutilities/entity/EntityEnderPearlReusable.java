package fi.dy.masa.minecraft.mods.enderutilities.entity;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;

public class EntityEnderPearlReusable extends EntityThrowable
{
	private float teleportDamage = 2.0f;

	public EntityEnderPearlReusable(World world)
	{
		super(world);
	}

	public EntityEnderPearlReusable(World world, EntityLivingBase entity)
	{
		super(world, entity);
	}

	@SideOnly(Side.CLIENT)
	public EntityEnderPearlReusable(World world, double par2, double par4, double par6)
	{
		super(world, par2, par4, par6);
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(MovingObjectPosition movingObjectPosition)
	{
		if (this.worldObj.isRemote == true)
		{
			return;
		}

		if (movingObjectPosition.entityHit != null)
		{
			movingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0f);
		}

		for (int i = 0; i < 32; ++i)
		{
			this.worldObj.spawnParticle("portal", this.posX, this.posY + this.rand.nextDouble() * 2.0d, this.posZ, this.rand.nextGaussian(), 0.0d, this.rand.nextGaussian());
		}


		if (this.getThrower() != null && this.getThrower() instanceof EntityPlayerMP)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)this.getThrower();

			if (entityplayermp.playerNetServerHandler.func_147362_b().isChannelOpen() && entityplayermp.worldObj == this.worldObj)
			{
				EnderTeleportEvent event = new EnderTeleportEvent(entityplayermp, this.posX, this.posY, this.posZ, this.teleportDamage);

				if (MinecraftForge.EVENT_BUS.post(event) == false)
				{
					/*
					if (entityplayermp.isRiding())
					{
						entityplayermp.mountEntity((Entity)null);
					}
					*/

					if (entityplayermp.isRiding() == true && entityplayermp.ridingEntity instanceof EntityLiving)
					{
						EntityLiving entity = (EntityLiving)entityplayermp.ridingEntity;
						entity.setPositionAndUpdate(this.posX, this.posY, this.posZ);
						entity.fallDistance = 0.0f;
						// TODO: Add a config option to decide if the ridingEntity should take damage
						//entity.attackEntityFrom(DamageSource.fall, this.teleportDamage);
					}
					else
					{
						entityplayermp.setPositionAndUpdate(this.posX, this.posY, this.posZ);
						entityplayermp.fallDistance = 0.0f;
						entityplayermp.attackEntityFrom(DamageSource.fall, this.teleportDamage);
					}
				}
			}
		}
/*
		// Failed attempt to fix the portal duplication bug
		if (movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
			this.worldObj.getBlock(movingObjectPosition.blockX, movingObjectPosition.blockY, movingObjectPosition.blockZ) == Blocks.portal)
		{
			//System.out.println("portal");
			return;
		}
*/
		EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ,
					new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, 0));

		Random r = new Random();
		entityitem.motionX = 0.05d * r.nextGaussian();
		entityitem.motionY = 0.05d * r.nextGaussian() + 0.2d;
		entityitem.motionZ = 0.05d * r.nextGaussian();
		entityitem.delayBeforeCanPickup = 20;
		this.worldObj.spawnEntityInWorld(entityitem);

		this.setDead();
	}
}