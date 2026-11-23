package dev.bombowy.client.mixin;

import dev.bombowy.client.BombowyClient;
import dev.bombowy.client.Modules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void bombowy$whiteNickAndBlackBomb(CallbackInfoReturnable<Text> cir) {
        BombowyClient mod = BombowyClient.getInstance();
        if (mod == null || mod.getModules() == null) return;

        Modules.ModuleEntry bombEntry = mod.getModules().getById("bomb_nickname");
        if (bombEntry == null || !bombEntry.enabled) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        String rawName = self.getGameProfile().getName();

        // Nick zawsze czysty biały (#FFFFFF) + czarna bomba w nawiasie () -> "Nick (💣)"
        MutableText whiteNick = Text.literal(rawName)
                .setStyle(Style.EMPTY.withColor(0xFFFFFF));
        MutableText blackBombParens = Text.literal(" (💣)")
                .setStyle(Style.EMPTY.withColor(0x0B0C0E));

        cir.setReturnValue(whiteNick.append(blackBombParens));
    }
}
