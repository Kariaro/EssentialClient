package essentialclient.mixins.core;

import essentialclient.EssentialClient;
import essentialclient.clientrules.AnnounceAFK;
import essentialclient.commands.TravelCommand;
import essentialclient.gui.clientrule.ClientRule;
import essentialclient.gui.clientrule.ClientRuleHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @Inject(method = "<init>",at = @At("RETURN"))
    private void loadMe(CallbackInfo ci)
    {
        EssentialClient.noop();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (TravelCommand.enabled)
            TravelCommand.tickTravel();
        if (ClientRule.clientRulesMap.get("announceAFK") != null && ClientRuleHelper.getNumber("announceAFK") > 0)
            AnnounceAFK.tickAFK(player);
    }
}
