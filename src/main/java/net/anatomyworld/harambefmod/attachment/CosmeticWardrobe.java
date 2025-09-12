package net.anatomyworld.harambefmod.attachment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public final class CosmeticWardrobe {
    @Nullable private ResourceLocation head, chest, legs, feet;

    public @Nullable ResourceLocation head()  { return head;  }
    public @Nullable ResourceLocation chest() { return chest; }
    public @Nullable ResourceLocation legs()  { return legs;  }
    public @Nullable ResourceLocation feet()  { return feet;  }

    public void setHead(@Nullable ResourceLocation id)  { this.head = id; }
    public void setChest(@Nullable ResourceLocation id) { this.chest = id; }
    public void setLegs(@Nullable ResourceLocation id)  { this.legs = id; }
    public void setFeet(@Nullable ResourceLocation id)  { this.feet = id; }

    public void set(int slot, @Nullable ResourceLocation id) {
        switch (slot) {
            case 0 -> head = id;
            case 1 -> chest = id;
            case 2 -> legs = id;
            case 3 -> feet = id;
        }
    }

    public static final MapCodec<CosmeticWardrobe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("head").forGetter(w -> Optional.ofNullable(w.head)),
            ResourceLocation.CODEC.optionalFieldOf("chest").forGetter(w -> Optional.ofNullable(w.chest)),
            ResourceLocation.CODEC.optionalFieldOf("legs").forGetter(w -> Optional.ofNullable(w.legs)),
            ResourceLocation.CODEC.optionalFieldOf("feet").forGetter(w -> Optional.ofNullable(w.feet))
    ).apply(i, (h,c,l,f)->{
        var w=new CosmeticWardrobe();
        w.head = h.orElse(null); w.chest = c.orElse(null); w.legs = l.orElse(null); w.feet = f.orElse(null);
        return w;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticWardrobe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), (CosmeticWardrobe w)->Optional.ofNullable(w.head),
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), (CosmeticWardrobe w)->Optional.ofNullable(w.chest),
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), (CosmeticWardrobe w)->Optional.ofNullable(w.legs),
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), (CosmeticWardrobe w)->Optional.ofNullable(w.feet),
                    (h,c,l,f)->{
                        var w=new CosmeticWardrobe();
                        w.head=h.orElse(null); w.chest=c.orElse(null); w.legs=l.orElse(null); w.feet=f.orElse(null);
                        return w;
                    });
}
