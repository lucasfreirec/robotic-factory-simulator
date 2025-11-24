package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Robot extends Component {
    
    private static final long serialVersionUID = -1218857231970296747L;

    private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

    private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

    private Battery battery;
    
    private int speed;
    
    private List<Component> targetComponents;
    
    @JsonIgnore
    private transient Iterator<Component> targetComponentsIterator;
    
    @JsonIgnore
    private Component currTargetComponent;
    
    @JsonIgnore
    private transient Iterator<Position> currentPathPositionsIter;
    
    @JsonIgnore
    private transient boolean blocked;
    
    @JsonIgnore
    private Position memorizedTargetPosition;
    
    @JsonIgnore
    private FactoryPathFinder pathFinder;

    public Robot() {
        super(null, null, null);
        this.battery = null;
        this.targetComponents = new ArrayList<>();
    }

    public Robot(final Factory factory,
                 final FactoryPathFinder pathFinder,
                 final CircularShape shape,
                 final Battery battery,
                 final String name ) {
        super(factory, shape, name);
        
        this.pathFinder = pathFinder;
        
        this.battery = battery;
        
        targetComponents = new ArrayList<>();
        currTargetComponent = null;
        currentPathPositionsIter = null;
        speed = 5;
        blocked = false;
        memorizedTargetPosition = null;
    }
    
    @Override
    public String toString() {
        return super.toString() + " battery=" + battery + "]";
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(final int speed) {
        this.speed = speed;
    }
    
    @JsonIgnore
    public Position getMemorizedTargetPosition() {
        return memorizedTargetPosition;
    }
    
    public List<Component> getTargetComponents() {
        if (targetComponents == null) {
            targetComponents = new ArrayList<>();
        }
        
        return targetComponents;
    }
    
    public boolean addTargetComponent(final Component targetComponent) {
        return getTargetComponents().add(targetComponent);
    }
    
    public boolean removeTargetComponent(final Component targetComponent) {
        return getTargetComponents().remove(targetComponent);
    }
    
    @JsonIgnore
    @Override
    public boolean isMobile() {
        return true;
    }

    @Override
    public boolean behave() {
        if (getTargetComponents().isEmpty()) {
            return false;
        }
        
        if (currTargetComponent == null || hasReachedCurrentTarget() || currentPathPositionsIter == null) { 
            currTargetComponent = nextTargetComponentToVisit();
            
            computePathToCurrentTargetComponent();
        }

        return moveToNextPathPosition() != 0;
    }
        
    private Component nextTargetComponentToVisit() {
        if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
            targetComponentsIterator = getTargetComponents().iterator();
        }
        
        return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
    }

    private int moveToNextPathPosition() {
        final Motion motion = computeMotion();

        int displacement = motion == null ? 0 : getFactory().moveComponent(motion, this);

        if (displacement != 0) {
            notifyObservers();
        }
        else if (isLivelyLocked()) {
            final Position freeNeighbouringPosition = findFreeNeighbouringPosition();

            if (freeNeighbouringPosition != null) {
                this.memorizedTargetPosition = freeNeighbouringPosition;
                displacement = moveToNextPathPosition();
                computePathToCurrentTargetComponent();
            }
        }

        return displacement;
    }

    private Position findFreeNeighbouringPosition() {
        int[] dy = {-1, 0, 1, 0};
        int[] dx = {0, 1, 0, -1};

        Position currentPosition = getPosition();
        Random rand = new Random();
        int offset = rand.nextInt(4);

        Position newPosition = new Position(currentPosition.getxCoordinate()+dx[offset]*getSpeed(),
                currentPosition.getyCoordinate()+dy[offset]*getSpeed());

        final PositionedShape shape = new RectangularShape(newPosition.getxCoordinate(),
                newPosition.getyCoordinate(),
                2,
                2);
        if (getFactory().hasObstacleAt(shape) || getFactory().hasMobileComponentAt(shape, this)) return null;

        return newPosition;
    }
    
    private void computePathToCurrentTargetComponent() {
        if (pathFinder != null) {
            final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
            currentPathPositionsIter = currentPathPositions.iterator();
        }
    }
    
    private Motion computeMotion() {
        if (currentPathPositionsIter == null || !currentPathPositionsIter.hasNext()) {
            blocked = true;
            return null;
        }
        
        final Position targetPosition = getTargetPosition();
        final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
                                                          targetPosition.getyCoordinate(),
                                                          2,
                                                          2);
        
        if (getFactory().hasMobileComponentAt(shape, this)) {
            this.memorizedTargetPosition = targetPosition;
            return null;
        }

        this.memorizedTargetPosition = null;
        return new Motion(getPosition(), targetPosition);
    }
    
    private Position getTargetPosition() {
        return this.memorizedTargetPosition == null ? currentPathPositionsIter.next() : this.memorizedTargetPosition;
    }
    
    @JsonIgnore
    public boolean isLivelyLocked() {
        if (memorizedTargetPosition == null) {
            return false;
        }
            
        final Component otherComponent = getFactory().getMobileComponentAt(memorizedTargetPosition, this);

        if (otherComponent instanceof Robot)  {
            return getPosition().equals(((Robot) otherComponent).getMemorizedTargetPosition());
        }
        
        return false;
    }

    private boolean hasReachedCurrentTarget() {
        return currTargetComponent != null && getPositionedShape().overlays(currTargetComponent.getPositionedShape());
    }
    
    @JsonIgnore
    @Override
    public boolean canBeOverlayed(final PositionedShape shape) {
        return true;
    }
    
    @JsonIgnore
    @Override
    public Style getStyle() {
        return blocked ? BLOCKED_STYLE : STYLE;
    }
    
    public void setPathFinder(FactoryPathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }
}