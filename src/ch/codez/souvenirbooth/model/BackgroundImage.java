/*
 * Created on 30.11.2007
 *
 */
package ch.codez.souvenirbooth.model;


public class BackgroundImage {
    
    private Motive motive;
    
    private String file;
    
    private double scale;
    
    private double up;
    
    private double left;
   
    public BackgroundImage(String filename, double scale, double up, double left) {
        this.file = Motive.getPath() + filename;
        this.scale = scale;
        this.up = up;
        this.left = left;
    }
    
    public Motive getMotive() {
        return this.motive;
    }
    
    public void setMotive(Motive motive) {
        this.motive = motive;
    }
    
    public String getFilePrefix() {
        return this.motive.getFilePrefix();
    }
    
    public String getFilename() {
        return this.file;
    }
    
    public double getScale() {
        return this.scale;
    }
    
    public double getUp() {
        return this.up;
    }
    
    public double getLeft() {
        return this.left;
    }
    
}
