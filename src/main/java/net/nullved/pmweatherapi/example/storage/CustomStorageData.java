package net.nullved.pmweatherapi.example.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.storage.data.StorageData;

public class CustomStorageData extends StorageData {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("example", "custom_sd");
    private final String string;

    public CustomStorageData(BlockPos pos, String string) {
        super(pos);
        this.string = string;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = super.serializeToNBT();
        tag.putString("string", string);
        return tag;
    }

    public static CustomStorageData deserializeFromNBT(CompoundTag tag, int version) {
        BlockPos pos = deserializeBlockPos(tag);
        if (pos == null) throw new IllegalArgumentException("Could not read BlockPos in CustomStorageData!");

        return new CustomStorageData(pos, tag.getString("string"));
    }
}
