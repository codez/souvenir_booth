/*
 * Created on 30.11.2007
 *
 */
package ch.codez.souvenirbooth.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.model.Motive;
import ch.codez.souvenirbooth.model.SnapshotState;
import ch.codez.souvenirbooth.util.CameraAdapter;

public class Director {
    
    private static Logger log = Logger.getLogger(Director.class);
    
    private static SouvenirRenderOptions SETTINGS = SouvenirRenderOptions.getInstance();
    
    
    List<Timer> timers = new ArrayList<Timer>();
    
    RandomCountdownTimerTask countdown = null;
    
    Set<DirectorListener> listeners = new HashSet<DirectorListener>();

    
    public void andAction(Motive motive) {
        // stop worker & perform cleanup
        this.cancel();
        SouvenirWorker.getInstance().stop();
        
        int seq = SETTINGS.getNextSequence();
        List<DelayedTask> tasks = new ArrayList<DelayedTask>(4);
        
        log.debug("Directing motive " + motive.getFilePrefix() + ", sequence " + seq);
        
        // create snapshot tasks
        this.addSnapshotTask(tasks, motive, seq, SnapshotState.pre);
        this.addSnapshotTask(tasks, motive, seq, SnapshotState.action);
        this.addSnapshotTask(tasks, motive, seq, SnapshotState.post);
        
        // create worker task
        CountdownTimerTask counter = new CountdownTimerTask();
        if ( CameraAdapter.getInstance().isRandom() ) {
            this.countdown = new RandomCountdownTimerTask();
            counter = this.countdown;
        } else {
            tasks.add(new WorkerTimerTask());
        }

        // schedule tasks as gleichzeitig as possible
        Timer countdown = new Timer(true);
        for (DelayedTask task : tasks) {
            this.timers.add(task.schedule());
        }
        
        //countdown
        countdown.scheduleAtFixedRate(counter, 0, 1000);
        this.timers.add(countdown);
    }
    
    public void snapshotReady() {
        if (this.countdown != null) {
            this.countdown.stop();
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {}
            SouvenirWorker.getInstance().start();
        }
    }
    
    public void cancel() {
        for (Timer timer : timers) {
            timer.cancel();
        }
        timers.clear();
        this.countdown = null;
        log.debug("Director cancelled");
    }
    
    public void addDirectorListener(DirectorListener l) {
        this.listeners.add(l);
        SouvenirWorker.getInstance().addDirectorListener(l);
    }
    
    public void removeDirectorListener(DirectorListener l) {
        this.listeners.remove(l);
        SouvenirWorker.getInstance().removeDirectorListener(l);
    }
    
    private void addSnapshotTask(List<DelayedTask> tasks, Motive motive, int seq, SnapshotState state) {
        int variants = SETTINGS.getDirectorVariants(state);
        if (variants > 0) {
            log.debug("Created snapshot task for state " + state);
            tasks.add(new SnapshotTimerTask(this, motive, seq, state, variants));
        }
    }
    
    public class WorkerTimerTask extends DelayedTask {
        public WorkerTimerTask() {
            super(Director.SETTINGS.getDirectorWorkerDelay() * 1000);
        }
        
        public void run() {
            SouvenirWorker.getInstance().start();
        }
    }

    public class CountdownTimerTask extends TimerTask {
        protected int i = Director.SETTINGS.getDirectorCountdown();
        public void run() {
            for (DirectorListener l : Director.this.listeners) {
                l.countDownAt(i--);
            }
            if (i < 0) {
                log.debug("Countdown terminated");
                for (DirectorListener l : Director.this.listeners) {
                    l.processing();
                }
                this.cancel();
            }
        }
    }
    
    public class RandomCountdownTimerTask extends CountdownTimerTask {
        
        protected boolean stopped = false;
        
        public void run() {
            super.run();
            this.i = (int)(Math.random() * 59 + 1);
        }
        
        public void stop() {
            this.i = 0;
        }
    }
}
