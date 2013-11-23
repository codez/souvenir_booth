/*
 * Created on 08.04.2004
 *
 * $Id: ChicaneFrame.java,v 1.6 2004/04/14 23:12:47 pascal Exp $
 */
package ch.codez.souvenirbooth.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import org.apache.log4j.Logger;

import ch.codez.souvenirbooth.SouvenirRenderOptions;
import ch.codez.souvenirbooth.controller.Director;
import ch.codez.souvenirbooth.controller.DirectorListener;
import ch.codez.souvenirbooth.controller.PaneCloseListener;
import ch.codez.souvenirbooth.model.Motive;
import ch.codez.souvenirbooth.model.SouvenirImage;


public class BoothFrame extends JFrame implements DirectorListener, PaneCloseListener {

    private final static double COLS_TO_ROWS = 5.0 / 3.0;
    
    private final static int GAP = 2;
    private final static int BORDER_WIDTH = 5;
    private final static int BUTTON_SIZE = 60;
    
    private final static String SPINNER_IMAGE = "/images/spinner.gif";
    private final static String CAMERA_IMAGE = "/images/camera.gif";
    private final static String CONTROL_BG_IMAGE = "/images/metal.jpg";
    
    private static Logger log = Logger.getLogger(BoothFrame.class);
    
    
    private List<Motive> motives;
    
    private Director director = new Director();
    
    private boolean isCountingDown = false;
    
    // notification components
    private SemiTransparentPane notifier = new SemiTransparentPane();
    private JLabel countdown = new JLabel();
    private JLabel processing = new JLabel();
    private ImagePane previewPane = new ImagePane();
    
    private MotiveComponent selected; 

    
	public BoothFrame(List<Motive> motives) {
	    super("SouvenirBooth");
	    this.motives = motives;
	    this.director.addDirectorListener(this);
	    this.init();
	}
    
    public void runFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        gs.setFullScreenWindow(this);
        this.validate();
    }
    
    public void paneClosed() {
        log.debug("Pane closed");
        if (this.isCountingDown) {
            this.director.cancel();
            this.isCountingDown = false;
        }
    }
    
    public void countDownAt(int i) {
        this.isCountingDown = true;
        this.countdown.setText(String.valueOf(i)); 
        this.notifier.setCloseText("haut");
        this.notifier.showContent(this.countdown, SemiTransparentPane.HALF_SCREEN_SIZE);  
        
        if (i == 0) {
            this.notifier.flashOff();
            this.isCountingDown = false;
        }
        this.validate();
    }
    
    public void processing() {
        this.notifier.showContent(this.processing, 
                SemiTransparentPane.NOTIFICATION_SIZE);
        this.notifier.setCloseText("its längt's");
    }
    
    public void ready(SouvenirImage image) {
        if (!this.isNotifying() || this.isCountingDown) {
            return;
        }
        if (image != null) {
            ImageIcon icon = new ImageIcon(image.getFilename()); 
            this.previewPane.setImage(icon.getImage());
            this.notifier.showContent(this.previewPane, null);
            this.notifier.setCloseText("zue tue");
            this.notifier.scaleTo(SemiTransparentPane.HALF_SCREEN_SIZE);
        } else {
            this.notifier.showContent(new JLabel(
                    "<html><center><font size=+1 color=white>" +
            		"Hoppla Schorsch, da isch grad öppis lätz gangä." +
            		"</font></center></html>"), 
                                      SemiTransparentPane.NOTIFICATION_SIZE);
        }
    }
    
    protected void clickMotive(MotiveComponent component) {
        if (this.isNotifying()) {
            return;
        }
        
        if (component == this.selected) {
            this.selected = null;
            component.deselect();
        } else {
            if (this.selected != null) {
                this.selected.deselect();
            }
            this.selected = component;
            this.selected.select();
        }
    }
    
    private boolean isNotifying() {
        return this.notifier.isVisible();
    }
    
    protected void init() {
       this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       this.setUndecorated(true);
       this.setResizable(false);
       this.setBackground(Color.BLACK);
       this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
       this.setVisible(true);
       
       this.initFramePanel();
       this.initControls();
       this.initCountdown();
       this.initProcessing();
       
       this.validate();
    }
    
    private void initFramePanel() {
        JLayeredPane center = new JLayeredPane();
        center.setLayout(new OverlayLayout(center));
        this.notifier.addCloseListener(this);
        center.add(this.notifier, JLayeredPane.POPUP_LAYER);
        center.add(this.initMotivePane(), JLayeredPane.DEFAULT_LAYER);
        this.getContentPane().add(center, BorderLayout.CENTER);
    }
    
    private void initControls() {
        ImageIcon bgImage = this.loadIcon(CONTROL_BG_IMAGE);
        JPanel bottomPane = new BackgroundPane(bgImage.getImage());
        bottomPane.setLayout(new BorderLayout());
        bottomPane.setBorder(BorderFactory.createCompoundBorder( 
                //BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(
                        BORDER_WIDTH, BORDER_WIDTH * 4, BORDER_WIDTH, 100)));

        bottomPane.add(this.initControlInfo(), BorderLayout.CENTER);
        bottomPane.add(this.initControlButton(), BorderLayout.EAST);
        this.getContentPane().add(bottomPane, BorderLayout.SOUTH);
    }
    
    private JComponent initControlInfo() {
        JLabel message = new JLabel("<html><font size=+1 color=black><center>" +
                "Tue bissoguet es Biudli uuswähle wo dr gfaut, di ines Koschtüm wärfä " +
                "u wedä de nache bisch ufe Chnopf da äne drückä zum es Föteli z'schiessä" +
                "</center></font></html>");

        this.setForeground(Color.white);
        message.setHorizontalAlignment(JLabel.CENTER);
        return message;
    }
    
    private JButton initControlButton() {
        ImageIcon icon = this.loadIcon(CAMERA_IMAGE);
        icon = new ImageIcon(icon.getImage().getScaledInstance(
                        BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH));
        JButton button = new RoundButton(new CountdownAction(icon));
        button.setForeground(SouvenirRenderOptions.getInstance().getLafColorHighlight());
        return button;
    }
    
    private JComponent initMotivePane() {
        int count = this.motives.size();
        if (count == 0) {
            log.warn("No motives defined");
            return new JLabel("No motives defined");
        }
        
        int cols = (int)Math.ceil(Math.sqrt(count * COLS_TO_ROWS));
        int rows = (int)Math.ceil(count / (double)cols);
        JPanel motivePane = new JPanel(new GridLayout(rows, cols, GAP, GAP));
        
        MouseListener listener = new MotiveMouseListener();
        for (Motive motive : this.motives) {
            MotiveComponent component = new MotiveComponent(motive);
            component.addMouseListener(listener);
            motivePane.add(component);
        }
        return motivePane;
    }
    
    private void initCountdown() {
        this.countdown.setHorizontalAlignment(JLabel.CENTER);
        Font font = this.countdown.getFont().deriveFont(Font.BOLD, 
                (int)SemiTransparentPane.HALF_SCREEN_SIZE.getHeight() / 2);
        this.countdown.setFont(font);
        this.countdown.setForeground(Color.WHITE);
    }
    
    private void initProcessing() {
        ImageIcon icon = this.loadIcon(SPINNER_IMAGE);
        processing.setIcon(icon);
        processing.setOpaque(false);
        processing.setIconTextGap(20);

        processing.setHorizontalAlignment(JLabel.CENTER);
        processing.setHorizontalTextPosition(JLabel.CENTER);
        processing.setVerticalTextPosition(JLabel.BOTTOM);
        processing.setForeground(Color.WHITE);
        processing.setText("<html><center><font color=white size=+1>" +
                           "Dis Föteli wird grad zämepoue.<br>" +
                           "Ds geit öpe es Zytli.." +
                           "</font></center></html>");
    }
    
    private ImageIcon loadIcon(String file) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(file));
        log.debug("Image " + file + " loaded with status " + icon.getImageLoadStatus());
        return icon;
    }
    
    private class MotiveMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            BoothFrame.this.clickMotive((MotiveComponent)e.getComponent());
        }
    }
    
    private class CountdownAction extends AbstractAction {
        public CountdownAction(Icon icon) {
            super(null, icon);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (BoothFrame.this.isNotifying()) {
                return;
            }
            if (BoothFrame.this.selected == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            Motive motive = BoothFrame.this.selected.getMotive();
            BoothFrame.this.director.andAction(motive);
        }
    }
}

