package com.redlimerl.detailab.mixins;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.Collection;

@Mixin(AttributeInstance.class)
public interface EntityAttributeInstanceInvoker {
	@Invoker("getModifiersOrEmpty")
	public Collection<AttributeModifier> invokeGetModifiersOrEmpty(AttributeModifier.Operation operation);
}
