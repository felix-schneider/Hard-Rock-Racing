package scaatis.rrr;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import scaatis.rrr.gui.QueuedAction;
import scaatis.rrr.gui.HardRockControlCenter;
import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.FinishLine;

public class HardRockRacing {

    public static final boolean                 debug           = true;
    public static final int                     fps             = 30;
    public static final int                     laps            = 4;
    public static final int                     pauseAfterRace  = 10;
    public static final int                     pauseBeforeRace = 5;

    private HardRockControlCenter               controlCenter;
    private int                                 curIndex;
    private Track                               currentTrack;
    private List<Player>                        finished;
    private List<String>                        maps;
    private List<Mine>                          mines;
    private List<Missile>                       missiles;
    private double                              pause;
    private HardRockProtocol                    protocol;
    private ConcurrentLinkedQueue<QueuedAction> queuedActions;
    private List<Player>                        racers;
    private boolean                             running;
    private double                              serverTime;
    private RaceState                           state;

    private Collection<GameObject>              toBeDestroyed;
    private ConcurrentLinkedQueue<GameObject>   newProjectiles;

    public HardRockRacing(List<String> maps) {
        running = false;
        pause = 0;
        this.maps = maps;
        curIndex = 0;
        currentTrack = null;
        if (maps.size() == 0) {
            throw new IllegalArgumentException("No map rotation!");
        }
        racers = new ArrayList<>();
        missiles = new ArrayList<>();
        mines = new ArrayList<>();
        state = RaceState.WAITING;
        serverTime = 0;
        toBeDestroyed = new ArrayList<>();
        finished = new ArrayList<>();
        queuedActions = new ConcurrentLinkedQueue<>();
        protocol = new HardRockProtocol(this);
        controlCenter = new HardRockControlCenter();
        newProjectiles = new ConcurrentLinkedQueue<>();
        initializeGUI();
    }

    public void dropPlayer(Player player) {
        boolean removed = racers.remove(player);
        if (!removed) {
            removed = finished.remove(player);
        }
        if (!removed) {
            return;
        }
        for (Missile missile : missiles) {
            if (missile.getShooter() == player) {
                missile.destroy();
            }
        }
        if (racers.isEmpty()) {
            setRaceState(RaceState.WAITING);
        }
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public List<Mine> getMines() {
        return mines;
    }

    public List<Missile> getMissiles() {
        return missiles;
    }

    public List<Player> getRacers() {
        return racers;
    }

    public RaceState getState() {
        return state;
    }

    public double getTimer() {
        return serverTime;
    }

    public void queueAction(QueuedAction action) {
        queuedActions.add(action);
    }

    public void setRaceState(RaceState raceState) {
        while (state != raceState && state != RaceState.WAITING) {
            advanceGameState();
        }
    }

    public void start() {
        running = true;
        protocol.start();
        long timeA = System.nanoTime();
        long timeB = timeA;
        HardRockProtocol.log(this, "Game ready, now waiting for players.");
        while (running) {
            try {
                Thread.sleep((int) (1000 / ((double) fps) - (timeB - timeA) / 1e6));
            } catch (InterruptedException e) {
            } catch (IllegalArgumentException e) {
                HardRockProtocol.log(this, "Cannot keep up!");
            }
            while (!queuedActions.isEmpty()) {
                queuedActions.poll().perform();
            }
            protocol.update();
            timeB = System.nanoTime();
            double delta = (timeB - timeA) / 1e9;
            timeA = System.nanoTime();
            update(delta);
            timeB = System.nanoTime();
        }
    }

    public void startPreRace(List<Player> racers) {
        if (state != RaceState.WAITING) {
            return;
        }
        // set to PRERACE
        // load track
        try {
            currentTrack = TrackLoader.loadTrack(maps.get(curIndex));
        } catch (IOException e) {
            stop();
            throw new RuntimeException(e);
        }
        curIndex = (curIndex + 1) % maps.size();
        if (racers.size() > 4 || racers.isEmpty()) {
            stop();
            throw new IllegalArgumentException("Trying to start race with " + racers.size() + " players!");
        }
        for (Player player : racers) {
            this.racers.add(player);
            player.addMineDropListener(new MineDropListener() {

                @Override
                public void mineDropped(MineDropEvent event) {
                    mineDrop(event);
                }
            });
            player.addMissileFireListener(new MissileFireListener() {

                @Override
                public void missileFired(MissileFireEvent e) {
                    missileFire(e);
                }
            });
            player.addCheckPointListener(new CheckPointListener() {

                @Override
                public void checkPoint(CheckPointEvent e) {
                    checkPointReached(e);
                }
            });
            player.addLapCompletedListener(new LapCompletedListener() {

                @Override
                public void lapCompleted(LapCompletedEvent e) {
                    lapComplete(e);
                }
            });
        }
        pause = pauseBeforeRace;
        state = RaceState.PRERACE;
        protocol.sendGameStart();
        HardRockProtocol.log(this, "Prerace wait started, now waiting for " + pauseBeforeRace + " seconds.");
    }

    public void stop() {
        running = false;
        protocol.stop();
    }

    protected void updateGUI() {
        if (state == RaceState.WAITING) {
            controlCenter.getRaceStateButton().setEnabled(false);
        } else {
            controlCenter.getRaceStateButton().setEnabled(true);
        }
        if (state == RaceState.RACE) {
            controlCenter.getAbortButton().setEnabled(true);
        } else {
            controlCenter.getAbortButton().setEnabled(false);
        }
        controlCenter.getRaceStateButton().setText(state.toString());
        Vector<Map.Entry<Connection, Player>> data = new Vector<>(protocol.getConnectionEntries());
        controlCenter.getConnections().setListData(data);
        controlCenter.getFrame().repaint();
    }

    private void advanceGameState() {
        switch (state) {
        case PRERACE:
            startRace();
            break;
        case RACE:
            finishRace();
            break;
        case POSTRACE:
            cleanup();
        default:
            break;
        }
        updateGUI();
    }

    private void checkPointReached(CheckPointEvent event) {
        if (!(event.getSource() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getSource();
        if (player.getNumberOfCheckPoints() == currentTrack.getCheckpoints().size()
                && event.getCheckPoint() instanceof FinishLine) {
            player.completeLap();
        }
    }

    private void cleanup() {
        // set to WAITING
        clearFinished();
        state = RaceState.WAITING;
        HardRockProtocol.log(this, "Cleaning up after race, now waiting for players.");
    }

    private void clearFinished() {
        for (Player player : finished) {
            player.clear();
        }
        finished.clear();
    }

    private boolean collide(Collides a, Collides b) {
        Area intersect = a.getArea();
        intersect.intersect(b.getArea());
        return !intersect.isEmpty();
    }

    private void collisions() {
        for (Missile missile : missiles) {
            for (Player player : racers) {
                if (player == missile.getShooter() || player.getCar() == null) {
                    continue;
                }
                if (collide(missile, player.getCar())) {
                    missile.collideWith(player.getCar());
                }
            }
        }
        for (Mine mine : mines) {
            for (Player player : racers) {
                if (player.getCar() != null && collide(mine, player.getCar())) {
                    mine.collideWith(player.getCar());
                }
            }
        }
        for (Player player : racers) {
            if (player.getCar() == null) {
                continue;
            }
            if (collide(player.getCar(), currentTrack)) {
                player.getCar().collideWith(currentTrack);
            }
            for (CheckPoint checkpoint : currentTrack.getCheckpoints()) {
                if (collide(player.getCar(), checkpoint)) {
                    player.getCar().collideWith(checkpoint);
                }
            }
            for (Player player2 : racers) {
                if (player2.getCar() != null && collide(player.getCar(), player2.getCar())) {
                    Car.collide(player.getCar(), player2.getCar());
                }
            }
        }
    }

    private void destroy(DestroyedEvent e) {
        if (e.getSource() instanceof GameObject) {
            toBeDestroyed.add((GameObject) e.getSource());
        }
        if (e.getSource() instanceof Car) {
            protocol.sendDestroyed((Car) e.getSource());
        }
    }

    private void finishRace() {
        // set to POSTRACE
        pause = pauseAfterRace;
        // despawn all players, in case they have not finished
        for (Player player : racers) {
            finished.add(player);
        }
        for (Player player : finished) {
            player.setCar(null);
        }
        currentTrack = null;
        racers.clear();
        missiles.clear();
        mines.clear();
        newProjectiles.clear();
        state = RaceState.POSTRACE;
        protocol.sendRaceOver(finished);
        HardRockProtocol.log(this, "Race finished, placings were " + finished.toString() +
                " now waiting for " + pauseAfterRace + " seconds.");
    }

    private void initializeGUI() {
        controlCenter.getFrame().addWindowListener(new QueuedAction(this) {
            @Override
            public void perform() {
                controlCenter.close();
                stop();
            }
        });
        controlCenter.getAbortButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                setRaceState(RaceState.WAITING);
            }
        });
        controlCenter.getKickButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                Map.Entry<Connection, Player> selectedEntry;
                selectedEntry = controlCenter.getConnections().getSelectedValue();
                if (selectedEntry == null) {
                    return;
                }
                protocol.connectionLost(selectedEntry.getKey());
                selectedEntry.getKey().close();
            }
        });
        controlCenter.getQuitButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                controlCenter.close();
                stop();
            }
        });
        controlCenter.getRaceStateButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                advanceGameState();
            }
        });
        updateGUI();
    }

    private void lapComplete(LapCompletedEvent e) {
        if (!(e.getSource() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getSource();
        protocol.sendLapComplete(player);
        HardRockProtocol.log(this, "Player " + player.getName() +
                " has completed lap " + player.getCompletedLaps());
    }

    private void mineDrop(MineDropEvent event) {
        event.getMine().addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
        newProjectiles.add(event.getMine());
    }

    private void missileFire(MissileFireEvent event) {
        event.getMissile().addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
        newProjectiles.add(event.getMissile());
    }

    private void spawn(Player player) {
        Point2D spawnloc = player.getLastCheckPoint().getSpawnLocation();
        spawn(player, spawnloc, player.getLastCheckPoint().getOrientation());
    }

    private void spawn(Player player, Point2D location, Direction orientation) {
        Car car = new Car(location, orientation);
        player.setCar(car);
        car.addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
    }

    private void startRace() {
        // set to RACE
        serverTime = 0;
        for (int i = 0; i < racers.size(); i++) {
            spawn(racers.get(i), currentTrack.getFinishLine().getStartLocation(i + 1), currentTrack.getStartDirection());
        }
        newProjectiles.clear();
        state = RaceState.RACE;
        HardRockProtocol.log(this, "The stage is set, the green flag drops!");
    }

    private void update(double delta) {
        if (state == RaceState.PRERACE || state == RaceState.POSTRACE) {
            pause -= delta;
            if (pause <= 0) {
                advanceGameState();
            }
        } else if (state == RaceState.RACE) {
            serverTime += delta;
            for (Player player : racers) {
                if (player.getCar().isDestroyed()) {
                    spawn(player);
                }
                player.getCar().update(delta);
            }
            for (Missile missile : missiles) {
                missile.update(delta);
            }
            collisions();
            protocol.sendGameState();
            Iterator<Player> racerIterator = racers.iterator();
            while (racerIterator.hasNext()) {
                Player player = racerIterator.next();
                if (player.getCompletedLaps() == laps) {
                    racerIterator.remove();
                    finished.add(player);
                }
            }
            if (racers.isEmpty()) {
                advanceGameState();
            }
            while (!newProjectiles.isEmpty()) {
                GameObject go = newProjectiles.poll();
                if (go instanceof Missile) {
                    missiles.add((Missile) go);
                } else if (go instanceof Mine) {
                    mines.add((Mine) go);
                }
            }
            for (GameObject go : toBeDestroyed) {
                missiles.remove(go);
                mines.remove(go);
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<String> tracks = new ArrayList<>();
        tracks.add("res/basictrack");
        HardRockRacing racing = new HardRockRacing(tracks);
        racing.start();
    }
}
