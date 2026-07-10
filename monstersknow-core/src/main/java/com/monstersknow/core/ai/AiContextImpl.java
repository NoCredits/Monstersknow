package com.monstersknow.core.ai;

import com.monstersknow.core.entity.CombatState;
import com.monstersknow.core.entity.Entity;
import com.monstersknow.core.entity.Weapon;
import java.util.List;

final class AiContextImpl implements AiContext {
    private final CombatState state;
    private final Entity self;

    AiContextImpl(CombatState state, Entity self) {
        this.state = state;
        this.self = self;
    }

    @Override
    public String getSelfId() { return self.getId(); }

    @Override
    public String getSelfName() { return self.getName(); }

    @Override
    public int getCurrentHealth() { return self.getCurrentHealth(); }

    @Override
    public int getMaxHealth() { return self.getStats().getMaxHealth(); }

    @Override
    public Entity.Position getSelfPosition() { return self.getPosition(); }

    @Override
    public boolean isHidden() { return self.isHidden(); }

    @Override
    public int getArrowsRemaining() { return self.getArrowsRemaining(); }

    @Override
    public boolean hasRangedWeapon() { return self.getRangedWeapon() != null; }

    @Override
    public boolean hasMeleeWeapon() { return self.getMeleeWeapon() != null; }

    @Override
    public double getRangedWeaponRange() {
        Weapon weapon = self.getRangedWeapon();
        return weapon != null ? weapon.getMaxRange() : 0.0;
    }

    @Override
    public double getMeleeWeaponRange() {
        Weapon weapon = self.getMeleeWeapon();
        return weapon != null ? weapon.getMaxRange() : 0.0;
    }

    @Override
    public double getFacingAngle() { return self.getFacingAngle(); }

    @Override
    public AiEntityView getNearestEnemy() {
        Entity nearest = state.getNearestEnemy(self);
        return nearest != null ? new AiEntityViewImpl(nearest) : null;
    }

    @Override
    public boolean hasAnyLivingEnemy() { return state.hasAnyLivingEnemy(self); }

    @Override
    public List<AiEntityView> getEnemiesWithinDistance(double distance) {
        return state.getEnemiesWithinDistance(self, distance).stream()
                .<AiEntityView>map(AiEntityViewImpl::new)
                .toList();
    }

    @Override
    public boolean isNearCover() { return state.isNearCover(self); }

    @Override
    public boolean hasLineOfSightTo(String entityId) {
        return state.getAllEntities().stream()
                .filter(e -> e.getId().equals(entityId))
                .findFirst()
                .map(target -> state.hasLineOfSight(self, target))
                .orElse(false);
    }

    @Override
    public int getTurnNumber() { return state.getTurnNumber(); }
}
