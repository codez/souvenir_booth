/*
 * Created on 31.12.2007
 *
 */
package ch.codez.souvenirbooth.jai;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.CRIFRegistry;
import javax.media.jai.registry.RIFRegistry;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.model.BackgroundImage;
import ch.codez.souvenirbooth.model.Motive;

public class SnapshotRenderer {
    private static final String SNAPSHOT = "fantasy_0014_action.jpg";
    
    private static final String FOLDER = "/Users/pascal/Desktop/free";
    
    private List<Motive> motives;
    
    public static void main(String args[]) {
        registerJAIStuff();
        new SnapshotRenderer().renderFolder();
        //renderAllFor(args[0]);
    }
    
    private static void renderAllFor(int index) {
        LayerBastler bastler = new LayerBastler();
        SouvenirRenderOptions settings = SouvenirRenderOptions.getInstance();
        List<Motive> motives = Motive.loadMotives();
        Motive motive = motives.get(index);
        System.out.println("Rendering motive " + motive.getLabel());
        int i = 0;
        for (BackgroundImage background : motive.getRandomBackgrounds(10)) {
            BufferedImage image = bastler.compose(
                    settings.getPathSnapshots() + SNAPSHOT, background);
            bastler.save(image, settings.getPathSouvenirs() + 
                    "motive" + index + "-" + i++ + ".jpg" );
            System.out.println("saved snapshot" + i);
        }
        System.out.println("done"); 
    }
    
    public SnapshotRenderer() {
        this.motives = Motive.loadMotives();
    }
    
    private void renderFolder() {
        File folder = new File(FOLDER);
        for (File file : folder.listFiles(new ActionFilter())) {
            String filename = file.getName();
            filename = filename.substring(0, filename.indexOf('_'));
            System.out.println(filename);
            Motive motive = this.getMotive(filename);
            this.renderBackgrounds(motive, file.getName());
        }
        
    }
    
    public class ActionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.length() > 12) {
                return "action.jpg".equals(name.substring(name.length() - 10));
            }
            return false;
        }
    }
    
    private void renderBackgrounds(Motive motive, String snapshot) {
        String snapPath = FOLDER + "/" + snapshot;
        String souvenirPath = FOLDER + "/" + snapshot.substring(0, snapshot.length() - 4);
        LayerBastler bastler = new LayerBastler();
        char index = 'a';
        for (BackgroundImage background : motive.getRandomBackgrounds(2)) {
            BufferedImage image = bastler.compose(snapPath, background);
            bastler.save(image, souvenirPath + "-" + index++ + ".jpg" );
            System.out.println("saved snapshot " + souvenirPath + index);
        }
    }
    
    private Motive getMotive(String key) {
        for (Motive m : motives) {
            if (m.getFilePrefix().equals(key)) {
                return m;
            }
        }
        return null;
    }
    
    

    private static void registerJAIStuff() {
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        registry.registerDescriptor(new ColorEraserDescriptor());
        ColorEraserCRIF crif = new ColorEraserCRIF();
        RIFRegistry.register(registry, "ColorEraser", "ColorEraser", crif);
        CRIFRegistry.register(registry, "ColorEraser", crif);
    }
}
