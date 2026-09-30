package aubymori.oldverticalbobbing.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import aubymori.oldverticalbobbing.CameraPitching;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;
    @Shadow @Final Camera mainCamera;
    @Shadow @Final GameRenderState gameRenderState;

    private float partialTick;

    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    private void onRenderLevel(CallbackInfo ci) {
        if (this.gameRenderState.levelRenderState.cameraRenderState.entityRenderState.isPlayer) {
            this.partialTick = this.gameRenderState.levelRenderState.cameraRenderState.cameraEntityPartialTicks;
        }
    }

    @Inject(
        method = "bobView",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void onBobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo callback) {
        if (cameraState.entityRenderState.isPlayer) {
            Player player = (Player)this.minecraft.getCameraEntity();
            CameraPitching cameraPitching = (CameraPitching)player;
            assert cameraPitching != null;
            float pitch = Mth.lerp(this.partialTick, cameraPitching.getPrevCameraPitch(), cameraPitching.getCameraPitch());

            poseStack.rotateDegrees(Axis.XP, pitch);
        }
    }
}
