package Group9.agent.container;

import Group9.PiMath;
import Group9.map.area.EffectArea;
import Group9.map.area.ModifyViewEffect;
import Group9.math.Vector2;
import Group9.tree.PointContainer;
import Interop.Geometry.Distance;
import Interop.Geometry.Vector;
import Interop.Percept.Vision.FieldOfView;

import java.util.*;

public abstract class AgentContainer<T> {

    private T agent;
    private FieldOfView normalFOV;
    private PointContainer.Circle shape = null;
    private Vector2 direction = null;

    public Map<Cooldown, Integer> cooldowns = new HashMap<>();

    public AgentContainer(T agent, Vector position, Vector direction, FieldOfView normalFOV)
    {
        this.agent = agent;
        this.shape = new PointContainer.Circle(Vector2.from(position), 0.5);
        this.direction = Vector2.from(direction);
        this.normalFOV = normalFOV;

        assert (this.direction.length() - 1) < 1E-9;
    }

    public T getAgent()
    {
        return this.agent;
    }

    public PointContainer.Circle getShape()
    {
        return shape;
    }

    public Vector2 getPosition()
    {
        return this.shape.getCenter();
    }

    public Vector2 getDirection()
    {
        return direction;
    }

    public FieldOfView getFOV(Set<EffectArea> areas)
    {
        Optional<ModifyViewEffect> viewAffectedArea = areas.stream()
                .filter(a -> a instanceof ModifyViewEffect)
                .map(a -> (ModifyViewEffect) a).findAny();

        if(viewAffectedArea.isPresent())
        {
            return new FieldOfView(new Distance(viewAffectedArea.get().get(this)), normalFOV.getViewAngle());
        }

        return this.normalFOV;
    }

    public void moveTo(Vector2 position)
    {
        this.shape.translate(this.getPosition().sub(position));
    }

    public void move(double distance)
    {
        this.shape.translate(this.direction.mul(distance, distance));
    }

    /**
     * Turns the agent by a certain amount of radians and returns the updated direction.
     * @param radians
     * @return
     */
    public Vector2 rotate(double radians)
    {
        final double theta = PiMath.getDistanceBetweenAngles(this.getDirection().getClockDirection(), radians);
        final double x = direction.getX();
        final double y = direction.getY();

        this.direction = new Vector2(
                x * Math.cos(theta) - y * Math.sin(theta),
                x * Math.sin(theta) + y * Math.cos(theta)
        );
        return this.direction;
    }

    public int getCooldown(Cooldown cooldown)
    {
        return this.cooldowns.getOrDefault(cooldown, -1);
    }

    public boolean hasCooldown(Cooldown cooldown)
    {
        return this.cooldowns.containsKey(cooldown);
    }

    public void addCooldown(Cooldown cooldown, int rounds)
    {
        this.cooldowns.put(cooldown, rounds);
    }

    public void cooldown()
    {
        Iterator<Map.Entry<Cooldown, Integer>> iterator = this.cooldowns.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Cooldown, Integer> entry = iterator.next();
            if(entry.getValue() - 1 == 0)
            {
                iterator.remove();
            }
            else
            {
                this.cooldowns.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    public enum Cooldown
    {
        SPRINTING,
        PHEROMONE
    }

    public enum FOVType
    {
        NORMAL,
        SHADED,
        SENTRY
    }

    public static class DataContainer
    {
        // TODO this is supposed to contain what the agent sees at a certain point so it can be stored in the graph
        //  - it would be great if we could come up with some kind of hash function to match new data containers or subsets
        //      of them, so that we can figure out whether we have seen this space before which is kinda important because of
        //          teleports
    }

}
