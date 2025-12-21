package com.bekkouame1024.mod.crosshaircustomizer.mixin.client;

import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.utils.CustomCrosshairRenderer;
import com.bekkouame1024.mod.crosshaircustomizer.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//? if =1.21.4 {
/*import java.util.function.Function;
import net.minecraft.client.render.RenderLayer;
*///?}

//? if >=1.21.6 <=1.21.8 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?}

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    
    //? if >=1.21 <=1.21.1 {
    /*@WrapOperation(
            method = "renderCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V")
    )
    private void drawCustomCrosshairWrap(DrawContext context, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        HitResult hitResult = client.crosshairTarget;

        String currentCrosshair = config.currentCrosshair;
        if(currentCrosshair == null) {
            original.call(context, texture, x, y, width, height);
            return;
        }
        
        if (texture.equals(Identifier.ofVanilla("hud/crosshair"))){
            Identifier crosshairToDraw = Identifier.of(currentCrosshair);

            if (hitResult instanceof EntityHitResult entityHit) {
                Entity entity = entityHit.getEntity();

                if (entity instanceof PlayerEntity && config.currentTargetCrosshair != null) {
                    crosshairToDraw = Identifier.of(config.currentTargetCrosshair);
                }
            }
            
            CustomCrosshairRenderer.drawCrosshair(
                    context,
                    crosshairToDraw,
                    x + width / 2,
                    y + height / 2,
                    15,
                    15
            );
        }
    }
    *///?}
    
    //? if =1.21.4 {
    /*@WrapOperation(
            method = "renderCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V")
    )
    private void drawCustomCrosshairWrap(DrawContext context, Function<Identifier, RenderLayer> renderLayers, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        HitResult hitResult = client.crosshairTarget;

        String currentCrosshair = config.currentCrosshair;
        if(currentCrosshair == null) {
            original.call(context, renderLayers, texture, x, y, width, height);
            return;
        }

        if (texture.equals(Identifier.ofVanilla("hud/crosshair"))){
            Identifier crosshairToDraw = Identifier.of(currentCrosshair);

            if (hitResult instanceof EntityHitResult entityHit) {
                Entity entity = entityHit.getEntity();

                if (entity instanceof PlayerEntity && config.currentTargetCrosshair != null) {
                    crosshairToDraw = Identifier.of(config.currentTargetCrosshair);
                }
            }

            CustomCrosshairRenderer.drawCrosshair(
                    context,
                    renderLayers,
                    crosshairToDraw,
                    x + width / 2,
                    y + height / 2,
                    15,
                    15
            );
        }
    }
    *///?}

    //? if >=1.21.6 <=1.21.8 {
    @WrapOperation(
            method = "renderCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V")
    )
    private void drawCustomCrosshairWrap(DrawContext context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        HitResult hitResult = client.crosshairTarget;
        
        String currentCrosshair = config.currentCrosshair;
        if(currentCrosshair == null) {
            original.call(context, pipeline, texture, x, y, width, height);
            return;
        }
        
        if (texture.equals(Identifier.ofVanilla("hud/crosshair"))){
            Identifier crosshairToDraw = Identifier.of(currentCrosshair);
            
            if (hitResult instanceof EntityHitResult entityHit) {
                Entity entity = entityHit.getEntity();
                
                if (entity instanceof PlayerEntity && config.currentTargetCrosshair != null) {
                    crosshairToDraw = Identifier.of(config.currentTargetCrosshair);
                }
            }
            
            CustomCrosshairRenderer.drawCrosshair(
                    context,
                    pipeline,
                    crosshairToDraw,
                    x + width / 2,
                    y + height / 2,
                    15,
                    15
            );
        }
    }
    //?}
}
