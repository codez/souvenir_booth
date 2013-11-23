/*
 * Created on 30.11.2007
 *
 */
package ch.codez.souvenirbooth.model;

import java.util.Date;

import ch.codez.souvenirbooth.SouvenirRenderOptions;

public class SouvenirImage implements Comparable<SouvenirImage> {
    
    private static String EXTENSION_SOUVENIR = "jpg";
   
    
    private BackgroundImage background;
    
    private int sequenceNumber;
    
    private SnapshotState state;
    
    private char variant;
    
    private Date date;
    
   
    public SouvenirImage(BackgroundImage bgImage,
                         int seqNum, SnapshotState state) {
        this(bgImage, seqNum, state, 'a');
    }
    
    public SouvenirImage(BackgroundImage bgImage,
                         int seqNum, SnapshotState state, char variant) {
        this.background = bgImage;
        this.sequenceNumber = seqNum;
        this.state = state;
        this.variant = variant;
        this.date = new Date();
    }
    
    public String getSnapshotFilename() {
        return this.getMotive().getSnapshotFilename(this.sequenceNumber, this.state);
    }
   
    public String getFilename() {
        return getPath() + this.createFilename();
    }
    
    public Motive getMotive() {
        return this.background.getMotive();
    }
    
    public BackgroundImage getBackground() {
        return this.background;
    }
    
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public SnapshotState getState() {
        return this.state;
    }

    public char getVariant() {
        return this.variant;
    }

    public Date getDate() {
        return this.date;
    }
    
    /**
     *  002-action-a
     *  001-action-a
     *  002-action-b
     *  001-action-b
     *  001-pre-a
     */
    public int compareTo(SouvenirImage other) {
        int diff = this.getVariant() - other.getVariant();
        if (diff != 0) {
            return diff;
        }
        diff = other.getSequenceNumber() - this.getSequenceNumber();
        if (diff != 0) {
            return diff;
        }
        if (isActionNotAction(this.state, other.state)) {
            return -1;
        } else if (isActionNotAction(other.state, this.state)) {
            return 1;
        }
        return 0;
    }
    
    private boolean isActionNotAction(SnapshotState action, SnapshotState notAction) {
        return action == SnapshotState.action &&
               notAction != SnapshotState.action;
    }
    
    private static String getPath() {
        return SouvenirRenderOptions.getInstance().getPathSouvenirs();
    }
    
    private String createFilename() {
        return String.format("%s_%04d_%s-%c.%s", 
                             this.background.getFilePrefix(),
                             this.sequenceNumber,
                             this.state,
                             this.variant,
                             EXTENSION_SOUVENIR);
    }
}
