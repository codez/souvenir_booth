/*
 * Created on 02.12.2007
 *
 */
package ch.codez.souvenirbooth.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.model.Motive;


public class MotiveComponent extends ImagePane {
    
    private final static int BORDER_WIDTH = 2;
    
    //private final static Color COLOR_SELECTED = new Color(150, 150, 255);
    //private final static Color COLOR_SELECTED = new Color(100, 100, 100);
    private final static Color COLOR_DESELECTED = new JLabel().getBackground();
    
    private Motive motive;
    
    public MotiveComponent(Motive m) {
        super(false);
        this.motive = m;
        this.init();
    }
    
    private void init() {
        ImageIcon icon = new ImageIcon(motive.getFilename()); 
        this.setImage(icon.getImage());
        
        this.setForeground(Color.white);
        Border border = BorderFactory.createEmptyBorder(BORDER_WIDTH, 
                BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
        border = BorderFactory.createTitledBorder(border, this.motive.getLabel(), 
                TitledBorder.CENTER, TitledBorder.ABOVE_BOTTOM, this.getFont(), Color.WHITE);
        this.setBorder(border);
    }

    public Motive getMotive() {
        return this.motive;
    }

    public void select() {
        this.setBackground(SouvenirRenderOptions.getInstance().getLafColorHighlight());
    }
    
    public void deselect() {
        this.setBackground(COLOR_DESELECTED);
    }
    
}
