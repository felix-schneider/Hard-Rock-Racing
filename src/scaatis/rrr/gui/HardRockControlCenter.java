package scaatis.rrr.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import scaatis.rrr.Connection;
import scaatis.rrr.Player;

public class HardRockControlCenter {
    
    private JButton                              abortButton;
    private JList<Map.Entry<Connection, Player>> connections;
    private JFrame                               frame;
    private JButton                              kickButton;
    private JButton                              quitButton;
    private JButton                              raceStateButton;
    
    public HardRockControlCenter() {
        initializeStuff();
        frame.setVisible(true);
    }
    
    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
    
    public JButton getAbortButton() {
        return abortButton;
    }
    
    public JList<Map.Entry<Connection, Player>> getConnections() {
        return connections;
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public JButton getKickButton() {
        return kickButton;
    }
    
    public JButton getQuitButton() {
        return quitButton;
    }
    
    public JButton getRaceStateButton() {
        return raceStateButton;
    }
    
    private void initializeStuff() {
        frame = new JFrame();
        frame.setTitle("Hard Rock Racing Server");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setBounds(100, 100, 390, 225);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        JPanel topHalf = new JPanel();
        topHalf.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        topHalf.setLayout(new BorderLayout());
        frame.add(topHalf, BorderLayout.NORTH);
        
        connections = new JList<>();
        connections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connections.setLayoutOrientation(JList.VERTICAL);
        connections.setVisibleRowCount(5);
        connections.setCellRenderer(new CellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(connections);
        scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        topHalf.add(scrollPane, BorderLayout.CENTER);
        
        kickButton = new JButton("Kick", null);
        topHalf.add(kickButton, BorderLayout.EAST);
        
        JPanel bottomHalf = new JPanel();
        bottomHalf.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        bottomHalf.setLayout(new FlowLayout(FlowLayout.LEADING));
        frame.add(bottomHalf);
        
        raceStateButton = new JButton("Racestate", null);
        bottomHalf.add(raceStateButton);
        
        abortButton = new JButton("Abort Race", null);
        bottomHalf.add(abortButton);
        
        quitButton = new JButton("Quit", null);
        bottomHalf.add(quitButton);
    }
}
