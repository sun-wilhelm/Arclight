package io.izzel.arclight.common.mixin.core.entity.monster;

import io.izzel.arclight.common.bridge.entity.monster.EndermanEntityBridge;
import io.izzel.arclight.common.mixin.core.entity.CreatureEntityMixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.network.datasync.DataParameter;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends CreatureEntityMixin implements EndermanEntityBridge {

    // @formatter:off
    @Shadow private int targetChangeTime;
    @Shadow @Final private static DataParameter<Boolean> SCREAMING;
    @Shadow @Final private static DataParameter<Boolean> field_226535_bx_;
    @Shadow @Final private static AttributeModifier ATTACKING_SPEED_BOOST;
    // @formatter:on

    @Override
    public void bridge$updateTarget(LivingEntity livingEntity) {
        IAttributeInstance iattributeinstance = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (livingEntity == null) {
            this.targetChangeTime = 0;
            this.dataManager.set(SCREAMING, false);
            this.dataManager.set(field_226535_bx_, false);
            iattributeinstance.removeModifier(ATTACKING_SPEED_BOOST);
        } else {
            this.targetChangeTime = this.ticksExisted;
            this.dataManager.set(SCREAMING, true);
            if (!iattributeinstance.hasModifier(ATTACKING_SPEED_BOOST)) {
                iattributeinstance.applyModifier(ATTACKING_SPEED_BOOST);
            }
        }
    }

    @Override
    public boolean setGoalTarget(LivingEntity livingEntity, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        if (!super.setGoalTarget(livingEntity, reason, fireEvent)) {
            return false;
        }
        bridge$updateTarget(getAttackTarget());
        return true;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void setAttackTarget(@Nullable LivingEntity entity) {
        if (this.getAttackTarget() == entity) {
            return;
        }
        this.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.UNKNOWN, true);
        arclight$targetSuccess = new AtomicBoolean();
        super.setAttackTarget(entity);
        boolean ret = arclight$targetSuccess.get();
        arclight$targetSuccess = null;
        if (ret) {
            bridge$updateTarget(getAttackTarget());
        }
    }
}
