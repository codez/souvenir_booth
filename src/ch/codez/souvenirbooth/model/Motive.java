/*
 * Created on 30.11.2007
 *
 */
package ch.codez.souvenirbooth.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.SouvenirRenderOptions;

public class Motive {
    
    public static final String DEFINITION_FILE = "motives.xml";
    
    private static String EXTENSION_SNAPSHOT = "jpg";

    private static Logger log = Logger.getLogger(Motive.class);

    private String file;
    private String label;
    private String filePrefix;
    private Set<BackgroundImage> backgrounds = new HashSet<BackgroundImage>();
    
    public Motive(String filename, String label, String fileprefix) {
        this.file = getPath() + filename;
        this.label = label;
        this.filePrefix = fileprefix;
    }
    
    public BackgroundImage getRandomBackgroundImage() {
        int random = (int)(Math.random() * this.backgrounds.size());
        int count = 0;
        for (BackgroundImage background : this.backgrounds) {
            if (count++ == random) {
                return background;
            }
        }
        log.error("No backgrounds defined for Motive " + label);
        return null;
    }
    
    public BackgroundImage[] getRandomBackgrounds(int count) {
        int[] indizes = this.getRandomIndizes(count);
        BackgroundImage[] backgrounds = new BackgroundImage[indizes.length];
        int bg = 0;
        int index = 0;
        for (BackgroundImage background : this.backgrounds) {
            if (bg++ == indizes[index]) {
                backgrounds[index++] = background;
                if (index == indizes.length) {
                    break;
                }
            }
        }
        return backgrounds;
    }
    
    public int countBackgrounds() {
        return this.backgrounds.size();
    }
    
    public void addBackgroundImage(BackgroundImage background) {
        background.setMotive(this);
        this.backgrounds.add(background);
    }
    
    
    public String getSnapshotFilename(int sequenceNumber, SnapshotState state) {
        return SouvenirRenderOptions.getInstance().getPathSnapshots() +
                  this.createSnapshotFilename(sequenceNumber, state);
    }

    public String getFilename() {
        return this.file;
    }
    
    public String getLabel() {
        return this.label;
    }

    public String getFilePrefix() {
        return this.filePrefix;
    }
    
    private String createSnapshotFilename(int sequenceNumber, SnapshotState state) {
        return String.format("%s_%04d_%s.%s", 
                             this.getFilePrefix(),
                             sequenceNumber,
                             state,
                             EXTENSION_SNAPSHOT);
    }
    
    public static String getPath() {
        return SouvenirRenderOptions.getInstance().getPathMotives();
    }
    
    public static List<Motive> loadMotives() {
        List<Motive> list = new ArrayList<Motive>();
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(DEFINITION_FILE);
        } catch (ConfigurationException e) {
            log.error("Could not load motives.xml");
            return list;
        }
        int motives = config.getList("motive.master.file").size();
        for (int m = 0; m < motives; m++) {
            Motive motive = loadMotive(config, m);
            int backgrounds = config.getList("motive(" + m + ").background.file").size();
            for (int b = 0; b < backgrounds; b++) {
                motive.addBackgroundImage(loadBackground(config, m, b));
            }
            if (backgrounds > 0) {
                list.add(motive);
                log.debug("Motive loaded " + motive.getLabel() + " with " + backgrounds + " backgrounds");
            }
        }
        log.debug(list.size() + " motives loaded");
        return list;
    }
    
    private static Motive loadMotive(XMLConfiguration config, int index) {
        String masterProp = "motive(" + index + ").master.";
        String filename = config.getString(masterProp + "file");
        String label = config.getString(masterProp + "label");
        String fileprefix = config.getString(masterProp + "fileprefix");
        return new Motive(filename, label, fileprefix);
    }
    
    private static BackgroundImage loadBackground(XMLConfiguration config, int motive, int index) {
        String bgProp = "motive(" + motive + ").background" + "(" + index + ").";
        String filename = config.getString(bgProp + "file");
        double scale = config.getDouble(bgProp + "scale");
        double left = config.getDouble(bgProp + "left");
        double top = config.getDouble(bgProp + "up");
        return new BackgroundImage(filename, scale, top, left);
    }
    
    private int[] getRandomIndizes(int count) {
        int max = this.countBackgrounds();
        count = Math.min(count, max);
        int[] indizes = new int[count];
        int i = 0;
        while (i < count) {
            int random = (int)(Math.random() * max);
            if (this.addIfUnique(indizes, random, i)) {
                i++;
            }
        }
        Arrays.sort(indizes);
        return indizes;
    }
        
    private boolean addIfUnique(int[] array, int element, int index) {
        boolean gotcha = true;
        for (int j = 0; gotcha && j < index; j++) {
            if (array[j] == element) {
                gotcha = false;
            }
        }
        if (gotcha) {
            array[index] = element;
        }
        return gotcha;
    }
     
}
