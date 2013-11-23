/*
 * Created on 13.11.2007
 *
 */
package ch.codez.souvenirbooth;

import java.io.File;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.CRIFRegistry;
import javax.media.jai.registry.RIFRegistry;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.gui.BoothFrame;
import ch.codez.souvenirbooth.jai.ColorEraserCRIF;
import ch.codez.souvenirbooth.jai.ColorEraserDescriptor;
import ch.codez.souvenirbooth.model.Motive;
import ch.codez.souvenirbooth.util.CameraAdapter;

public class Main {

    private static Logger log = Logger.getLogger(Main.class);

    
    public static void main(String[] args) throws Exception {
        assertSettings();
        setOSXOptions();
        registerJAIStuff();
        createPictureDirectories();
        
        BoothFrame frame = new BoothFrame(Motive.loadMotives());
        frame.runFullScreen();
    }
    
    private static void setOSXOptions() {
        //System.setProperty("apple.awt.fakefullscreen", "true");
        //System.setProperty("apple.awt.fullscreenusefade", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    
    private static void registerJAIStuff() {
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        registry.registerDescriptor(new ColorEraserDescriptor());
        ColorEraserCRIF crif = new ColorEraserCRIF();
        RIFRegistry.register(registry, "ColorEraser", "ColorEraser", crif);
        CRIFRegistry.register(registry, "ColorEraser", crif);
    }
    
    private static void createPictureDirectories() {
        SouvenirRenderOptions settings = SouvenirRenderOptions.getInstance();
        assertDirectoryExistance(settings.getPathMotives());
        assertDirectoryExistance(settings.getPathSnapshots());
        assertDirectoryExistance(settings.getPathSouvenirs());
    }
    
    private static void assertDirectoryExistance(String path) {
        File dir = new File(path);
        if (! dir.exists()) {
            boolean success = dir.mkdirs();
            if (! success) {
                log.error("Could not create directory " + path);
            } else {
                log.info("Created directory " + path);
            }
        }
    }
    
    private static void assertSettings() {
        assertExistingProperties();
        assertExistingMotives();
        assertCamAdapterWorking();
    }
    
    private static void assertExistingProperties() {
        File properties = new File(SouvenirRenderOptions.CONFIG_FILE);
        if (! properties.exists()) {
            System.out.println("No settings file defined (" + properties.getName() + ").");
        }
    }
    
    private static void assertExistingMotives() {
        File motives = new File(Motive.DEFINITION_FILE);
        if (! motives.exists()) {
            System.out.println("No motive file defined (" + motives.getName() + ").");
        }
    }

    private static void assertCamAdapterWorking() {
        if (CameraAdapter.getInstance() == null) {
            System.out.println("No camera adapter defined!");
            System.exit(1);
            if (!CameraAdapter.getInstance().selftest()) {
                System.out.println("Camera adapter not working!");
                System.exit(1);
            }
        }
    }
}
