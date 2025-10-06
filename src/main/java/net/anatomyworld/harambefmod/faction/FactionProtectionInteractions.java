package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class FactionProtectionInteractions {
    private FactionProtectionInteractions() {}

    private static Faction playerFaction(Player p) {
        Team t = p.getTeam();
        return (t == null) ? null : Faction.fromTeamName(t.getName()); // 1.21: getName() -> String
    }

    /** If the player has one of the protection effects, return that effect’s owning faction. */
    private static Faction effectFaction(Player p) {
        if (p.getEffect((net.minecraft.core.Holder) ModMobEffects.BELMONT_PROTECTION) != null) return Faction.BELMONT;
        if (p.getEffect((net.minecraft.core.Holder) ModMobEffects.DYNASTY_PROTECTION)  != null) return Faction.DYNASTY;
        if (p.getEffect((net.minecraft.core.Holder) ModMobEffects.IMPERIUM_PROTECTION) != null) return Faction.IMPERIUM;
        if (p.getEffect((net.minecraft.core.Holder) ModMobEffects.MISCHIEF_PROTECTION) != null) return Faction.MISCHIEF;
        return null;
    }

    /** True when the player is under a foreign protection effect (the effect’s faction ≠ player’s team faction). */
    private static boolean isForeignProtected(Player p) {
        Faction ef = effectFaction(p);
        if (ef == null) return false;
        Faction pf = playerFaction(p);
        return pf == null || pf != ef;
    }

    private static void denied(Player p) {
        p.displayClientMessage(
                Component.literal("This area is protected by another faction.").withStyle(ChatFormatting.RED),
                true
        );
    }

    /* Containers, doors/trapdoors/gates, levers/buttons/note blocks, etc. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        Level level = e.getLevel();
        if (level.isClientSide) return;

        Player player = e.getEntity();
        if (!isForeignProtected(player)) return;

        BlockPos pos = e.getPos();
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();

        // GUI blocks without BE:
        boolean isStaticMenuBlock =
                b instanceof CraftingTableBlock
                        || b instanceof StonecutterBlock
                        || b instanceof LoomBlock
                        || b instanceof AnvilBlock
                        || b instanceof GrindstoneBlock
                        || b instanceof CartographyTableBlock
                        || b instanceof SmithingTableBlock
                        || b instanceof EnchantingTableBlock;

        // Blocks with BE that opens a Menu (chests/barrels/furnaces/shulkers/etc.)
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasMenuProviderBE = be instanceof MenuProvider;

        // Door-like
        boolean isDoorLike =
                b instanceof DoorBlock
                        || b instanceof TrapDoorBlock
                        || b instanceof FenceGateBlock;

        // Redstone toggles / utility
        boolean isRedstoneToggle =
                b instanceof LeverBlock
                        || b instanceof ButtonBlock
                        || b instanceof NoteBlock;

        boolean interactive = isStaticMenuBlock || hasMenuProviderBE || isDoorLike || isRedstoneToggle;
        if (!interactive) return;

        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.FAIL);
        denied(player);
    }

    /* Melee attacks (no entity killing in foreign zones while effect is present). */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        Player player = e.getEntity();
        if (player.level().isClientSide) return;
        if (!isForeignProtected(player)) return;

        Entity target = e.getTarget();
        if (target == null) return;

        e.setCanceled(true);
        denied(player);
    }
}
