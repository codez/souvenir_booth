/*
 * Created on 21.11.2007
 *
 */
package ch.codez.souvenirbooth;

import java.awt.Color;
import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.model.SnapshotState;

public class SouvenirRenderOptions {

    public final static String CONFIG_FILE = "souvenirbooth.properties";
    
    private static SouvenirRenderOptions INSTANCE = new SouvenirRenderOptions();
    
    private static Logger log = Logger.getLogger(SouvenirRenderOptions.class);

    
    private PropertiesConfiguration config;
    
    private int sequence = 0;
    
    public static SouvenirRenderOptions getInstance() {
        return INSTANCE;
    }
    
    private SouvenirRenderOptions() {
        this.initConfig();
        this.sequence = this.config.getInt("picture.lastseq", 0);
    }
    
    public int getNextSequence() {
        this.sequence++;
        this.save();
        return this.sequence;
    }
    
    public int getCamWidth() {
        return this.config.getInt("cam.width", 0);
    }
    
    public int getCamHeight() {
        return this.config.getInt("cam.height", 0);
    }
    
    public int getCamDelay() {
        return this.config.getInt("cam.delay", 0);
    }

    public int[] getCamBackgroundColor() {
        return this.getColor("cam.bgcolor", "#FFFFFF");
    }
    
    public String getCamAdapterClass() {
        return this.config.getString("cam.adapterclass", 
                "ch.codez.souvenirbooth.util.ISightCaptureAdapter");
    }
    
    public int getDirectorCountdown() {
        return this.config.getInt("director.countdown", 10);
    }
    
    public int getDirectorWorkerDelay() {
        return this.config.getInt("director.workerdelay", 15);
    }
    
    public int getDirectorVariants(SnapshotState state) {
        return this.config.getInt("director." + state + ".variants", 0);
    }
    
    public int getDirectorShottime(SnapshotState state) {
        return this.config.getInt("director." + state + ".shottime", 0);
    }
    
    public float getPictureQuality() {
        return this.config.getFloat("picture.quality");
    }
    
    public int getRenderArea() {
        return this.config.getInt("render.area", 2);
    }
    
    public int getRenderTolerance() {
        return this.config.getInt("render.tolerance", 10);
    }
    
    public int getRenderOpaqueUpTo() {
        return this.config.getInt("render.opaqueUpTo", 20);
    }
    
    public int getRenderTransparentFrom() {
        return this.config.getInt("render.transparentFrom", 60);
    }
    
    public String getPathMotives() {
        return this.getPath("path.motives");
    }
    
    public String getPathSnapshots() {
        return this.getPath("path.snapshots");
    }
    
    public String getPathSouvenirs() {
        return this.getPath("path.souvenirs");
    }
    
    public Color getLafColorHighlight() {
        int[] color = this.getColor("laf.color.highlight", "#ff70aa");
        return new Color(color[0], color[1], color[2]);
    }

    private void save() {
        this.config.setProperty("picture.lastseq", new Integer(this.sequence));
        try {
            this.config.save();
        } catch (ConfigurationException e) {
            log.warn("Could not save configuration.");
        }
    }
    
    private String getPath(String key) {
        String path = this.config.getString(key, ".");
        if (! path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }
    
    private int[] getColor(String key, String def) {
        String bg = this.config.getString(key, def);
        int color[] = new int[3];
        int index = ('#' == bg.charAt(0)) ? 1 : 0;
        for (int i = 0; i < 3; i++) {
            color[i] = Integer.parseInt(bg.substring(index, index + 2), 16);
            index += 2;
        }
        return color;
    }
    
    private void initConfig() {
        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(CONFIG_FILE);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            log.error("Configuration file " + CONFIG_FILE + " not found!", e);
            config = new PropertiesConfiguration();
        }
        this.config = config;
    }
    
    
}
