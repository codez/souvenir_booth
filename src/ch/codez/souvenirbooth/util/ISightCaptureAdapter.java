/*
 * Created on 03.12.2007
 *
 */
package ch.codez.souvenirbooth.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class ISightCaptureAdapter extends CameraAdapter {

    private static Logger log = Logger.getLogger(ISightCaptureAdapter.class);
    
    private final String COMMAND;
    
    public ISightCaptureAdapter() {
        String dir = System.getProperty("user.dir");
        COMMAND = dir + File.separator + "isightcapture ";
    }
    
    public boolean takeSnapshot(String filename) {
        String command = COMMAND + "-t tiff " + filename;
        try {
            Process proc = Runtime.getRuntime().exec(command);
            this.logOutput(proc);
            return true;
        } catch (IOException e) {
            log.error("Could not capture snapshot using command " + command, e);
        }
        return false;
    }

    public BufferedImage takeSnapshot() {
        return null;
    }

    public boolean selftest() {
        try {
            Process proc = Runtime.getRuntime().exec(COMMAND);
            this.logOutput(proc);
            return true;
        } catch (IOException e) { }
        return false;          
    }
    
    private void logOutput(Process proc) {
        if (log.isDebugEnabled()) {
            this.logStream(proc.getInputStream());
            this.logStream(proc.getErrorStream());
        }
    }

    private void logStream(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }
        } catch (IOException e) {
            log.debug("Could not debug stream", e);
        }
    }
}
