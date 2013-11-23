/*
 * Created on 21.11.2007
 *
 */
package ch.codez.souvenirbooth.jai;

import javax.media.jai.RasterAccessor;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.model.Motive;

public class RasterIterator {

    private static Logger log = Logger.getLogger(RasterIterator.class);
    
    // array indexes
    private static final int SRC = 0;
    private static final int DST = 1;
    
    // Raster accessors
    private RasterAccessor src;
    private RasterAccessor dst;
    
    
    private int[] pixelStrides;
    private int[] scanlineStrides;
    private int[] numBands = new int[2];
    
    public RasterIterator(RasterAccessor src, RasterAccessor dst) {
        this.src = src;
        this.dst = dst;
        
        this.numBands        = new int[] { src.getNumBands(),
                                           dst.getNumBands() };
        this.pixelStrides    = new int[] { src.getPixelStride(), 
                                           dst.getPixelStride() };
        this.scanlineStrides = new int[] { src.getScanlineStride(), 
                                           dst.getScanlineStride() };
    }

    
    public void iterate(PixelAreaOperator transformer) {
        int dwidth = dst.getWidth();
        int dheight = dst.getHeight();

        byte srcDataArrays[][] = src.getByteDataArrays();
        byte dstDataArrays[][] = dst.getByteDataArrays();
        
        int bandOffsets[][] = new int[][] { src.getBandOffsets(), 
                                            dst.getBandOffsets() };
  
        int maxBands = Math.max(this.numBands[SRC], this.numBands[DST]);    
        int scanlineOffsets[][] = new int[2][maxBands];
        int pixelOffsets[][] = new int[2][maxBands];
        
        for (int k = 0; k < this.numBands[SRC]; k++) {
            scanlineOffsets[SRC][k] = bandOffsets[SRC][k];
        }
        scanlineOffsets[DST][0] = bandOffsets[DST][0];
        for (int j = 0; j < dheight; j++) {
            transformer.nextLine();
            //log.debug("Rendering line " + j + " of " + dheight);
            
            // copy offsets
            for (int k = 0; k < this.numBands[SRC]; k++) {
                pixelOffsets[SRC][k] = scanlineOffsets[SRC][k];
            }
            pixelOffsets[DST][0] = scanlineOffsets[DST][0];
            
            for (int i = 0; i < dwidth; i++) {
                dstDataArrays[0][pixelOffsets[DST][0]] = 
                    transformer.doSomethingNasty(srcDataArrays, pixelOffsets[SRC], numBands[SRC]);
                
                // increment x
                for (int k = 0; k < this.numBands[SRC]; k++) {
                    pixelOffsets[SRC][k] += pixelStrides[SRC];
                }
                pixelOffsets[DST][0] += pixelStrides[DST];
            }
            // increment y
            for (int k = 0; k < this.numBands[SRC]; k++) {
                scanlineOffsets[SRC][k] += scanlineStrides[SRC];
            }
            scanlineOffsets[DST][0] += scanlineStrides[DST];
        }
    }
    
}
