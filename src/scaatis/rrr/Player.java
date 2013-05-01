package scaatis.rrr;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;

import org.json.JSONObject;

import scaatis.rrr.event.CheckPointEvent;
import scaatis.rrr.event.CheckPointListener;
import scaatis.rrr.event.DestroyedEvent;
import scaatis.rrr.event.DestroyedListener;
import scaatis.rrr.event.LapCompletedEvent;
import scaatis.rrr.event.LapCompletedListener;
import scaatis.rrr.event.MineDropEvent;
import scaatis.rrr.event.MineDropListener;
import scaatis.rrr.event.MissileFireEvent;
import scaatis.rrr.event.MissileFireListener;
import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.util.Vector2D;

public class Player implements JSONable {
    public static final int   maxMissiles = 3;
    public static final int   maxBoosts   = 3;
    public static final int   maxMines    = 3;
    
    private Car               car;
    private List<CheckPoint>  checkPoints;
    private int               completedLaps;
    private String            name;
    private int               missiles;
    private int               boosts;
    private int               mines;
    private RaceCharacter     preferredCharacter;
    private RaceCharacter     character;
    private CarType           carType;
    private boolean           tiledMap;
    
    private EventListenerList listeners;
    
    public Player() {
        this(false);
    }
    
    public Player(boolean tiledMap) {
        name = null;
        preferredCharacter = null;
        carType = null;
        checkPoints = null;
        car = null;
        completedLaps = -1;
        listeners = null;
        missiles = -1;
        tiledMap = false;
        boosts = -1;
        mines = -1;
    }
    
    public Player(String name, RaceCharacter raceCharacter, CarType carType) {
        this(name, raceCharacter, carType, false);
    }
    
    public Player(String name, RaceCharacter raceCharacter, CarType carType,
            boolean tiledMap) {
        this.name = name;
        preferredCharacter = raceCharacter;
        character = null;
        this.carType = carType;
        checkPoints = new ArrayList<>();
        car = null;
        completedLaps = 0;
        listeners = new EventListenerList();
        missiles = maxMissiles;
        this.tiledMap = tiledMap;
        boosts = maxBoosts;
        mines = maxMines;
    }
    
    public void checkPointCompleted(CheckPointEvent e) {
        if (checkPoints.contains(e.getCheckPoint())) {
            if (!(e.getCheckPoint() instanceof FinishLine)
                    || e.getCheckPoint() == getLastCheckPoint()) {
                return;
            }
        } else {
            checkPoints.add(0, e.getCheckPoint());
        }
        CheckPointListener[] ls = listeners.getListeners(CheckPointListener.class);
        CheckPointEvent event = new CheckPointEvent(this, e.getCheckPoint());
        for (CheckPointListener listener : ls) {
            listener.checkPoint(event);
        }
    }
    
    public void completeLap() {
        checkPoints.clear();
        completedLaps++;
        missiles = maxMissiles;
        boosts = maxBoosts;
        mines = maxMines;
        LapCompletedListener[] ls = listeners.getListeners(LapCompletedListener.class);
        LapCompletedEvent event = new LapCompletedEvent(this);
        for (LapCompletedListener listener : ls) {
            listener.lapCompleted(event);
        }
    }
    
    public void carDestroyed(DestroyedEvent e) {
        car = null;
    }
    
    public void accelerate() {
        if (car != null) {
            car.setAccelerating(1);
        }
    }
    
    public void stopAccelerating() {
        if (car != null) {
            car.setAccelerating(0);
        }
    }
    
    public void turnLeft() {
        if (car != null) {
            car.setTurning(Direction.LEFT);
        }
    }
    
    public void turnRight() {
        if (car != null) {
            car.setTurning(Direction.RIGHT);
        }
    }
    
    public void stopTurning() {
        if (car != null) {
            car.setTurning(0);
        }
    }
    
    public void fireMissile() {
        if (car != null && missiles > 0) {
            missiles--;
            Missile missile = new Missile(this);
            // activate listeners
            MissileFireListener[] ls = listeners.getListeners(MissileFireListener.class);
            MissileFireEvent event = new MissileFireEvent(this, missile);
            for (MissileFireListener listener : ls) {
                listener.missileFired(event);
            }
        }
    }
    
    public void boost() {
        if (car != null && boosts > 0) {
            boosts--;
            car.boost();
        }
    }
    
    public void dropMine() {
        if (car != null && mines > 0) {
            mines--;
            Vector2D offset = new Vector2D.Polar(car.getFacing() + Math.PI,
                    Car.hitbox.getWidth());
            Mine mine = new Mine(offset.applyTo(car.getLocation()));
            // activate Listeners
            MineDropListener[] ls = listeners.getListeners(MineDropListener.class);
            MineDropEvent event = new MineDropEvent(this, mine);
            for (MineDropListener listener : ls) {
                listener.mineDropped(event);
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Car getCar() {
        return car;
    }
    
    public void setCar(Car car) {
        if (car == this.car) {
            return;
        }
        this.car = car;
        if(car == null) {
        	return;
        }
        car.addCheckPointListener(new CheckPointListener() {
            @Override
            public void checkPoint(CheckPointEvent e) {
                checkPointCompleted(e);
            }
        });
        car.addDestroyedListener(new DestroyedListener() {
            @Override
            public void destroyed(DestroyedEvent e) {
                carDestroyed(e);
                
            }
        });
    }
    
    public int getCompletedLaps() {
        return completedLaps;
    }
    
    public void clear() {
    	completedLaps = 0;
    	checkPoints.clear();
    	listeners = new EventListenerList();
    	setCar(null);
    	character = null;
    }
    
    public int getMissiles() {
        return missiles;
    }
    
    public int getBoosts() {
        return boosts;
    }
    
    public RaceCharacter getPreferredCharacter() {
        return preferredCharacter;
    }
    
    public RaceCharacter getCharacter() {
        return character;
    }
    
    public void setCharacter(RaceCharacter character) {
        this.character = character;
    }
    
    public CarType getCarType() {
        return carType;
    }
    
    public boolean isObserver() {
        return name == null;
    }
    
    public boolean transferMapTiled() {
        return tiledMap;
    }
    
    public int getNumberOfCheckPoints() {
        return checkPoints.size();
    }
    
    public CheckPoint getLastCheckPoint() {
        return checkPoints.get(0);
    }
    
    public void addMissileFireListener(MissileFireListener listener) {
        listeners.add(MissileFireListener.class, listener);
    }
    
    public void removeMissileFireListener(MissileFireListener listener) {
        listeners.remove(MissileFireListener.class, listener);
    }
    
    public void addLapCompletedListener(LapCompletedListener listener) {
        listeners.add(LapCompletedListener.class, listener);
    }
    
    public void removeLapCompletedListener(LapCompletedListener listener) {
        listeners.remove(LapCompletedListener.class, listener);
    }
    
    public void addCheckPointListener(CheckPointListener listener) {
        listeners.add(CheckPointListener.class, listener);
    }
    
    public void removeCheckPointListener(CheckPointListener listener) {
        listeners.remove(CheckPointListener.class, listener);
    }
    
    public void addMineDropListener(MineDropListener listener) {
        listeners.add(MineDropListener.class, listener);
    }
    
    public void removeMineDropListener(MineDropListener listener) {
        listeners.remove(MineDropListener.class, listener);
    }
    
    @Override
    public String toString() {
        if (isObserver()) {
            return "Observer";
        }
        RaceCharacter ch = character;
        if (ch == null) {
            ch = preferredCharacter;
        }
        return name + "(" + ch.getName() + "), in a " + carType.getName();
    }
    
    @Override
    public JSONObject toJSON() {
        JSONObject message = new JSONObject();
        message.put("message", "player");
        message.put("name", name);
        RaceCharacter ch = character;
        if (ch == null) {
            ch = preferredCharacter;
        }
        message.put("character", ch.getName());
        message.put("cartype", carType.getName());
        return message;
    }
}
