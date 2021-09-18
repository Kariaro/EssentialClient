package essentialclient.mixins.missingTools;

import essentialclient.gui.clientrule.ClientRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends MiningToolItem {

    //Client implementation of missingTools
    protected PickaxeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Tag<Block> tag, Item.Settings settings) {
        super(attackDamage, attackSpeed, material, tag, settings);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        Material material = state.getMaterial();
        if (ClientRules.MISSING_TOOLS.getBoolean() && material == Material.GLASS)
            return miningSpeed;
        return super.getMiningSpeedMultiplier(stack, state);
    }
}