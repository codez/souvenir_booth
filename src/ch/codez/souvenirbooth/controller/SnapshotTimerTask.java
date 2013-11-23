/*
 * Created on 02.12.2007
 *
 */
package ch.codez.souvenirbooth.controller;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.model.BackgroundImage;
import ch.codez.souvenirbooth.model.Motive;
import ch.codez.souvenirbooth.model.SnapshotState;
import ch.codez.souvenirbooth.model.SouvenirImage;
import ch.codez.souvenirbooth.util.CameraAdapter;

public class SnapshotTimerTask extends DelayedTask {
    
    private static Logger log = Logger.getLogger(SnapshotTimerTask.class);
    
    private Motive motive;
    
    private BackgroundImage[] backgrounds;
    
    private int sequenceNumber;
    
    private SnapshotState state;
    
    private Director director;
   
    public SnapshotTimerTask(Director director, Motive motive, int seq, SnapshotState state, int variants) {
        super(getSnapshotDelay(state));
        this.motive = motive;
        this.backgrounds = motive.getRandomBackgrounds(variants);
        this.sequenceNumber = seq;
        this.state = state;
        this.director = director;
    }
    
    public void run() {
        if (this.takeSnapshot()) {
            log.debug("Took snapshot '" + this.state);
            this.createImageVariants();
        }
        this.director.snapshotReady();
    }
   
    private boolean takeSnapshot() {
        CameraAdapter cam = CameraAdapter.getInstance();
        return cam.takeSnapshot(getSnapshotFilename());
    }
    
    private String getSnapshotFilename() {
        return this.motive.getSnapshotFilename(
                this.sequenceNumber, this.state);
    }
    
    private static long getSnapshotDelay(SnapshotState state) {
        SouvenirRenderOptions settings = SouvenirRenderOptions.getInstance();
        return settings.getDirectorCountdown() * 1000 + 
               settings.getDirectorShottime(state) - settings.getCamDelay();
    }
    
    private void createImageVariants() {
        char variant = 'a';
        SouvenirWorker worker = SouvenirWorker.getInstance();
        for (BackgroundImage background : this.backgrounds) {
            SouvenirImage image = new SouvenirImage(background, 
                    this.sequenceNumber, this.state, variant++);
            worker.addSouvenirImage(image);
            log.debug("Created variant " + this.state + "-" + (char)(variant-1));
        }
    }

}
