package org.sobadfish.chessgame.entitys;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class ChessChoseEntity extends Entity implements CustomEntity {

    public ChessChoseEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setScale(0.2f);
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return ChessEntityManager.DEF_CHOSE;
    }

    @Override
    public int getNetworkId() {
        return getEntityDefinition().getRuntimeId();
    }

    public boolean attack(EntityDamageEvent source) {
        EntityDamageEvent.DamageCause cause = source.getCause();
        return cause == EntityDamageEvent.DamageCause.VOID;
    }

    @Override
    public void saveNBT() {

    }
}
