package com.monstersknow.core.ai;

import com.monstersknow.core.entity.Entity;

final class AiEntityViewImpl implements AiEntityView {
    private final Entity entity;

    AiEntityViewImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String getId() { return entity.getId(); }

    @Override
    public String getName() { return entity.getName(); }

    @Override
    public String getType() { return entity.getType(); }

    @Override
    public int getCurrentHealth() { return entity.getCurrentHealth(); }

    @Override
    public int getMaxHealth() { return entity.getStats().getMaxHealth(); }

    @Override
    public Entity.Position getPosition() { return entity.getPosition(); }

    @Override
    public boolean isHidden() { return entity.isHidden(); }

    @Override
    public boolean isAlive() { return entity.isAlive(); }
}
