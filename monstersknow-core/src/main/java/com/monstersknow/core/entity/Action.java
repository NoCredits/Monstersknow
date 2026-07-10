package com.monstersknow.core.entity;

/**
 * Represents an action that an entity can take during combat.
 */
public class Action {
    public enum ActionType {
        ATTACK_RANGED,
        ATTACK_MELEE,
        MOVE,
        MOVE_TOWARDS,
        MOVE_AWAY,
        HIDE,
        DISENGAGE,
        DASH,
        FLEE,
        SEARCH,
        IDLE
    }

    private ActionType type;
    private String targetId;
    private Entity.Position targetPosition;
    private double distance;
    private Action bonusAction;
    private Double desiredFacingAngle;

    private Action(ActionType type) {
        this.type = type;
    }

    /**
     * Attach a bonus action to be resolved right after this action, for
     * creatures with a feature like Nimble Escape (Hide/Disengage as a
     * bonus action). Only HIDE and DISENGAGE are meaningful as bonus actions.
     */
    public Action withBonusAction(Action bonusAction) {
        this.bonusAction = bonusAction;
        return this;
    }

    public Action getBonusAction() { return bonusAction; }

    /**
     * Attach a facing update (radians, atan2 convention) to apply this turn,
     * independent of the action itself - e.g. keeping eyes on a target while
     * moving/attacking, or sweeping a field of view while searching.
     */
    public Action withFacing(double radians) {
        this.desiredFacingAngle = radians;
        return this;
    }

    public Double getDesiredFacingAngle() { return desiredFacingAngle; }

    // Factory methods
    public static Action attackRanged(String targetId) {
        Action action = new Action(ActionType.ATTACK_RANGED);
        action.targetId = targetId;
        return action;
    }

    public static Action attackMelee(String targetId) {
        Action action = new Action(ActionType.ATTACK_MELEE);
        action.targetId = targetId;
        return action;
    }

    public static Action move(Entity.Position position) {
        Action action = new Action(ActionType.MOVE);
        action.targetPosition = position;
        return action;
    }

    public static Action moveTowards(Entity.Position target, double distance) {
        Action action = new Action(ActionType.MOVE_TOWARDS);
        action.targetPosition = target;
        action.distance = distance;
        return action;
    }

    public static Action moveAway(Entity.Position target, double distance) {
        Action action = new Action(ActionType.MOVE_AWAY);
        action.targetPosition = target;
        action.distance = distance;
        return action;
    }

    public static Action hide() {
        return new Action(ActionType.HIDE);
    }

    public static Action disengage() {
        return new Action(ActionType.DISENGAGE);
    }

    public static Action dash(Entity.Position away, double distance) {
        Action action = new Action(ActionType.DASH);
        action.targetPosition = away;
        action.distance = distance;
        return action;
    }

    public static Action flee(Entity.Position threatPosition) {
        Action action = new Action(ActionType.FLEE);
        action.targetPosition = threatPosition;
        return action;
    }

    public static Action idle() {
        return new Action(ActionType.IDLE);
    }

    public static Action search() {
        return new Action(ActionType.SEARCH);
    }

    // Getters
    public ActionType getType() { return type; }
    public String getTargetId() { return targetId; }
    public Entity.Position getTargetPosition() { return targetPosition; }
    public double getDistance() { return distance; }
}
