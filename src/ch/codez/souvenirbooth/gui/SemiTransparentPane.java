/*
 * Created on 09.12.2007
 *
 */
package ch.codez.souvenirbooth.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.codez.souvenirbooth.controller.PaneCloseListener;


public class SemiTransparentPane extends JPanel {

    public final static Dimension SCREEN_SIZE = 
                    Toolkit.getDefaultToolkit().getScreenSize();
    public final static Dimension HALF_SCREEN_SIZE = 
                    new Dimension((int)(SCREEN_SIZE.getWidth() * 0.66), 
                                  (int)(SCREEN_SIZE.getHeight() * 0.66));
    public final static Dimension NOTIFICATION_SIZE = new Dimension(350, 250);
    
    protected final static int BORDER_WIDTH = 20;
    protected final static double ARC_SIZE = 0.1;
    
    //protected final static Color BG_COLOR = new Color(30, 30, 30, 230);
    protected final static Color BG_COLOR = new Color(50, 50, 50, 230);
    protected final static Color FONT_COLOR = new Color(255, 255, 255);
    
    private final static long SCALE_TIME = 500;
    private final static int FLASH_TIME = 20;
    
    
    private Set<PaneCloseListener> listeners = new HashSet<PaneCloseListener>();
    
    private JLabel closer = new JLabel("[haut]");
    
    private String closeText = "haut";
    
    private boolean flashing;
    
    public SemiTransparentPane() {
        this.init();
    }
    
    public void showContent(JComponent component, Dimension size) {
        this.removeAll();
        this.add(component, BorderLayout.CENTER);
        this.add(this.closer, BorderLayout.SOUTH);
        this.requestFocus();
        this.setVisible(true);
        if (size != null) {
            this.setSize(size);
        }
        this.validate();
    }
    
    public void setCloseText(String text) {
        if (text == null) {
            this.closer.setVisible(false);
        } else {
            this.closer.setVisible(true);
            this.closer.setText("["+ text + "]");
            this.closeText = text;
        }
    }
    
    public synchronized void flashOff() {
        this.flashing = true;
        this.setSize(SCREEN_SIZE);
        this.repaint();
        try {
            Thread.sleep(FLASH_TIME);
        } catch (InterruptedException e) { }
        this.setVisible(false);
        this.flashing = false;
    }
    
    public void scaleTo(Dimension endSize) {
        if (endSize == null) {
            endSize = HALF_SCREEN_SIZE;
        }
        boolean scaling = true;
        long start = System.currentTimeMillis();
        Dimension startSize = this.getSize();
        double scaleX = endSize.getWidth() - startSize.getWidth();
        double scaleY = endSize.getHeight() - startSize.getHeight();
        while (scaling) {
            long current = System.currentTimeMillis() - start;
            if (current > SCALE_TIME) {
                current = SCALE_TIME;
                scaling = false;
            }
            double factor = current / (double)SCALE_TIME;
            int w = (int)(startSize.getWidth() + factor * scaleX);
            int h = (int)(startSize.getHeight() + factor * scaleY);
            
            this.setSize(w, h);
            this.repaint();
            Thread.yield();
        }
    }
    
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.setMaximumSize(new Dimension(width, height));
        this.setLocation((int)(SCREEN_SIZE.getWidth() - width) / 2,
                         (int)(SCREEN_SIZE.getHeight() - height) / 2);
        this.validate();
        this.getParent().validate();
    }
    
    public void setSize(Dimension dim) {
        this.setSize((int)dim.getWidth(), (int)dim.getHeight());
    }
    
    public void addCloseListener(PaneCloseListener l) {
        listeners.add(l);
    }
    
    public void removeCloseListener(PaneCloseListener l) {
        listeners.remove(l);
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.flashing) {
            this.paintFlash(g);
        } else {
            this.paintBackground(g);
        }
    }
    
    protected void paintBackground(Graphics g) {
        int w = this.getWidth();
        int h = this.getHeight();
        g.setColor(BG_COLOR);
        g.fillRoundRect(0, 0, w, h, 
                        (int)(w * ARC_SIZE), 
                        (int)(h * ARC_SIZE));
    }
      
    private void paintFlash(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int)SCREEN_SIZE.getWidth(), 
                         (int)SCREEN_SIZE.getHeight());
    }

    private void init() {
        this.setVisible(false);
        this.setOpaque(false);
        this.setFocusable(true);
        this.setLayout(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
        this.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, 
                BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        this.addKeyListener(new EscapeKeyListener());
        this.initCloser();
    }
    
    private void initCloser() {
        this.closer.setHorizontalAlignment(JLabel.CENTER);
        this.closer.setForeground(FONT_COLOR);
        this.closer.addMouseListener(new CloseClickListener());
        this.add(this.closer, BorderLayout.SOUTH);
    }
    
    protected void close() {
        this.setVisible(false);
        for (PaneCloseListener l : this.listeners) {
            l.paneClosed();
        }
    }
    
    private class EscapeKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                SemiTransparentPane.this.close();
            } 
        }
    }
    
    private class CloseClickListener extends MouseAdapter {
        
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                SemiTransparentPane.this.close();
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            SemiTransparentPane.this.closer.setText(
                    "<html>[<u>" + SemiTransparentPane.this.closeText + 
                    "</u>]</html>");
        }
        
        public void mouseExited(MouseEvent e) {
            SemiTransparentPane.this.closer.setText(
                    "[" + SemiTransparentPane.this.closeText + "]");
        }
        
        
    }
    
}
