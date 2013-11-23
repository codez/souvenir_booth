/*
 * Created on 21.11.2007
 *
 */
package ch.codez.souvenirbooth.jai;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.registry.RenderedRegistryMode;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.model.BackgroundImage;

public class LayerBastler {

    private static Logger log = Logger.getLogger(LayerBastler.class);

    
    public BufferedImage compose(String snapshotFile, BackgroundImage bgImage) {
        PlanarImage background = this.loadFile(bgImage.getFilename());
        PlanarImage snapshot = this.loadFile(snapshotFile);
        background = this.scaleBackground(background, bgImage, snapshot.getBounds().getSize());
        snapshot = this.position(snapshot, bgImage, background.getBounds().getSize());
        return this.renderComposite(snapshot, background);
    }
    
    public void save(BufferedImage image, String file)  {
        try {
        BufferedOutputStream out = new BufferedOutputStream(new
                                        FileOutputStream(file));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(.9f, false);
        //TODO: save photo meta data (date, time)
        encoder.setJPEGEncodeParam(param);
        encoder.encode(image);
        out.close(); 
        } catch (IOException e) {
            
        }
    }
    
    private PlanarImage scaleBackground(PlanarImage background, BackgroundImage bgImage, Dimension size) {
        double factor = this.getScaleFactor(background.getWidth(), background.getHeight(), size);
        factor /= bgImage.getScale();
        
        log.debug("background scale factor: " + factor);
        
        ParameterBlockJAI pb = new ParameterBlockJAI("scale",
                RenderedRegistryMode.MODE_NAME);
        
        pb.setSource("source0", background); 
        
        pb.setParameter("xScale", new Float(factor));
        pb.setParameter("yScale", new Float(factor));
        
        return JAI.create("scale", pb);
    }
    
    private PlanarImage position(PlanarImage snapshot, BackgroundImage bgImage, Dimension size) {
        int marginWidth = (int)(size.getWidth() - snapshot.getWidth()) / 2;
        int marginHeight = (int)(size.getHeight() - snapshot.getHeight()) / 2;
        int left = (int)(bgImage.getLeft() / 100.0 * size.getWidth());
        int up = (int)(bgImage.getUp() / 100.0 * size.getHeight());
        
        log.debug("margin width: " + marginWidth + ", height: " + marginHeight +
                  ", left: "+ left + ", up: " + up);
        
        // translate
        ParameterBlockJAI pb = new ParameterBlockJAI("translate",
                RenderedRegistryMode.MODE_NAME);
        pb.setSource("source0", snapshot); 
        pb.setParameter("xTrans", new Float(marginWidth - left));
        pb.setParameter("yTrans", new Float(marginHeight - up));
        snapshot = JAI.create("translate", pb);
        
        // add border
        pb = new ParameterBlockJAI("border",
                RenderedRegistryMode.MODE_NAME);
        pb.setSource("source0", snapshot); 
        pb.setParameter("leftPad", marginWidth - left);
        pb.setParameter("rightPad", marginWidth + left);
        pb.setParameter("topPad", marginHeight - up);
        pb.setParameter("bottomPad", marginHeight + up);
        pb.setParameter("type", ColorEraserOperator.getConstantExtender(
              SouvenirRenderOptions.getInstance().getCamBackgroundColor()));
        return JAI.create("border", pb);
    }
    
    private double getScaleFactor(int width, int height, Dimension innerBounds) {
        double factor = innerBounds.getWidth() / (double)width;
        if (height * factor < innerBounds.getHeight()) {
            factor = innerBounds.getHeight() / (double)height;
        }
        return factor;
    }
    
    public BufferedImage renderComposite(PlanarImage snapshot, PlanarImage background) {
        log.debug("snapshot size: " + snapshot.getWidth() + "x" + snapshot.getHeight());
        log.debug("background size: " + background.getWidth() + "x" + background.getHeight());
        PlanarImage mask = this.getMask(snapshot);
        PlanarImage composite = this.getComposite(background, snapshot, mask);
        return composite.getAsBufferedImage();
    }
    
    
    private PlanarImage loadFile(String filename) {
        ParameterBlockJAI pb = new ParameterBlockJAI("fileload",
                RenderedRegistryMode.MODE_NAME);
        pb.setParameter("filename", filename);
        PlanarImage image = JAI.create("fileload", filename);

        log.debug("loaded " + filename + " - " + image.getWidth() + "x" + image.getHeight());
        return image;
    }
    
    
    private PlanarImage getMask(PlanarImage snapshot) {
        SouvenirRenderOptions options = SouvenirRenderOptions.getInstance();
        
        ParameterBlockJAI pb = new ParameterBlockJAI("ColorEraser",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", snapshot);
        
        pb.setParameter("color", options.getCamBackgroundColor());
        pb.setParameter("areaSize", options.getRenderArea());
        pb.setParameter("tolerance", options.getRenderTolerance());
        pb.setParameter("minPercentageForTransparency", options.getRenderOpaqueUpTo());
        pb.setParameter("maxPercentageForOpaquenes", options.getRenderTransparentFrom());
        
        return JAI.create("ColorEraser", pb, null);
    }
    
    private PlanarImage getComposite(PlanarImage background, PlanarImage snapshot, PlanarImage mask) {
        ParameterBlockJAI pb = new ParameterBlockJAI("composite",
                                    RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", snapshot);
        pb.setSource("source1", background);
        
        pb.setParameter("source1Alpha", mask);
        pb.setParameter("alphaPremultiplied", false);
        
        return JAI.create("composite", pb);
    }
    
}
