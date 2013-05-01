package scaatis.rrr.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import scaatis.rrr.HardRockRacing;

public abstract class QueuedAction implements ActionListener, WindowListener {
    
    private HardRockRacing game;
    
    public QueuedAction(HardRockRacing game) {
        this.game = game;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        game.queueAction(this);
    }
    
    @Override
    public void windowActivated(WindowEvent e) {}
    
    @Override
    public void windowClosed(WindowEvent e) {}
    
    @Override
    public void windowClosing(WindowEvent e) {
        game.queueAction(this);
    }
    
    @Override
    public void windowDeactivated(WindowEvent e) {}
    
    @Override
    public void windowDeiconified(WindowEvent e) {}
    
    @Override
    public void windowIconified(WindowEvent e) {}
    
    @Override
    public void windowOpened(WindowEvent e) {}
    
    public abstract void perform();
}
