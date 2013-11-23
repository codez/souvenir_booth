/*
 * Created on 30.11.2007
 *
 */
package ch.codez.souvenirbooth.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.jai.LayerBastler;
import ch.codez.souvenirbooth.model.SouvenirImage;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class SouvenirWorker implements Runnable {

    private static Logger log = Logger.getLogger(SouvenirWorker.class);

    private static final long SLEEP_INTERVAL = 2000;
    
    private static SouvenirWorker INSTANCE = new SouvenirWorker();
    
    
    private Queue<SouvenirImage> tasks = new PriorityBlockingQueue<SouvenirImage>();
    
    private LayerBastler bastler = new LayerBastler();
    
    private Thread workThread;
    
    private boolean running = false;
    
    private boolean first, further;

    Set<DirectorListener> listeners = new HashSet<DirectorListener>();
    
    private SouvenirRenderOptions settings = SouvenirRenderOptions.getInstance();
    

    public static SouvenirWorker getInstance() {
        return INSTANCE;
    }
    
    public void start() {
        this.running = true;
        log.debug("told to start");
        this.workLoop();
    }
    
    public void stop() {
        if (this.running) {
            log.debug("told to stop.");
        }
        this.running = false;  
    }
    
    public synchronized void addSouvenirImage(SouvenirImage image) {
        this.tasks.offer(image);
    }
    
    public void addDirectorListener(DirectorListener l) {
        this.listeners.add(l);
    }
    
    public void removeDirectorListener(DirectorListener l) {
        this.listeners.remove(l);
    }
    
    public void run() {
        SouvenirImage image = null;
        while (this.running && (image = this.tasks.poll()) != null) {
            this.first = true;
            this.processImage(image);
            if (this.first && ! further) {
                this.notifyFirstReady(image);
                further = true;
            }
            try {
                Thread.sleep(SLEEP_INTERVAL);  
            } catch (InterruptedException e) {  }
        }
        if (this.first && ! further) {
            this.notifyFirstReady(image);
        }
        this.finish();
    }
    
    private synchronized void workLoop() {
        // will be set again in run() if not yet running
        this.first = false;
        this.further = false;
        
        if (this.workThread == null) {
            this.workThread = new Thread(this);
            this.workThread.start();
            log.debug("started.");
        }
    }
    
    private void finish() {
        this.stop();
        log.debug("finished");
        this.workThread = null;
    }
    
    private void notifyFirstReady(SouvenirImage image) {
        for (DirectorListener l : this.listeners) {
            l.ready(image);
        }
        log.debug("first done");
        this.first = false;
    }
    
    private void processImage(SouvenirImage image) {
        String filename = image.getFilename();
        String name = filename.substring(filename.lastIndexOf(File.separatorChar));
        log.debug("Rendering souvenir " + name);
        
        try {
            BufferedImage souvenir = bastler.compose(image.getSnapshotFilename(), 
                                                     image.getBackground());
            this.save(souvenir, filename);
            log.debug("Souvenir " + name + " saved.");
        } catch (IOException e) {
            log.error("Could not save souvenir to file " + name, e);
        } catch (IllegalArgumentException iae) {
            log.error("Could not compose souvenir " + name, iae);
        }
    }
    
    private void save(BufferedImage image, String file) throws IOException  {
        BufferedOutputStream out = new BufferedOutputStream(new
                                        FileOutputStream(file));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(settings.getPictureQuality(), false);
        //TODO: save photo meta data (date, time)
        encoder.setJPEGEncodeParam(param);
        encoder.encode(image);
        out.close(); 
    }
}
