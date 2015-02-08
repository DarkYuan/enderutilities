package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class TileEntityEnderUtilities extends TileEntity
{
    protected String tileEntityName;
    protected int rotation;
    protected String ownerName;
    protected UUID ownerUUID;

    public TileEntityEnderUtilities(String name)
    {
        this.rotation = 0;
        this.ownerName = null;
        this.ownerUUID = null;
        this.tileEntityName = name;
    }

    public String getTEName()
    {
        return this.tileEntityName;
    }

    public void setRotation(int rot)
    {
        this.rotation = rot;
    }

    public int getRotation()
    {
        return this.rotation;
    }

    public void setOwner(EntityPlayer player)
    {
        if (player != null)
        {
            this.ownerName = player.getCommandSenderName();
            this.ownerUUID = player.getUniqueID();
        }
        else
        {
            this.ownerName = null;
            this.ownerUUID = null;
        }
    }

    public String getOwnerName()
    {
        return this.ownerName;
    }

    public UUID getOwnerUUID()
    {
        return this.ownerUUID;
    }

    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.rotation = nbt.getByte("Rotation");

        if (nbt.hasKey("OwnerName", Constants.NBT.TAG_STRING) == true)
        {
            this.ownerName = nbt.getString("OwnerName");
        }

        if (nbt.hasKey("OwnerUUIDMost", Constants.NBT.TAG_LONG) == true && nbt.hasKey("OwnerUUIDLeast", Constants.NBT.TAG_LONG) == true)
        {
            this.ownerUUID = new UUID(nbt.getLong("OwnerUUIDMost"), nbt.getLong("OwnerUUIDLeast"));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Rotation", (byte)this.rotation);

        if (this.ownerName != null)
        {
            nbt.setString("OwnerName", this.ownerName);
        }

        if (this.ownerUUID != null)
        {
            nbt.setLong("OwnerUUIDMost", this.ownerUUID.getMostSignificantBits());
            nbt.setLong("OwnerUUIDLeast", this.ownerUUID.getLeastSignificantBits());
        }
    }

    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setByte("r", (byte)(this.getRotation() & 0x07));

        if (this.ownerName != null)
        {
            nbt.setString("o", this.ownerName);
        }

        return nbt;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        if (this.worldObj != null)
        {
            return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, this.getDescriptionPacketTag(null));
        }

        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();

        if (nbt.hasKey("r") == true)
        {
            this.setRotation((byte)(nbt.getByte("r") & 0x07));
        }
        if (nbt.hasKey("o", Constants.NBT.TAG_STRING) == true)
        {
            this.ownerName = nbt.getString("o");
        }

        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(x=" + xCoord + ",y=" + yCoord + ",z=" + zCoord + ")@" + System.identityHashCode(this);
    }
}
