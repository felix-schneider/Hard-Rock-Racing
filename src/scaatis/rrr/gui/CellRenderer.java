package scaatis.rrr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import scaatis.rrr.Connection;
import scaatis.rrr.Player;

public class CellRenderer extends JPanel implements
        ListCellRenderer<Map.Entry<Connection, Player>> {
    
    private static final long serialVersionUID = 1L;
    private JLabel            label;
    
    public CellRenderer() {
        label = new JLabel();
        setLayout(new BorderLayout());
        add(label, BorderLayout.WEST);
    }
    
    @Override
    public Component getListCellRendererComponent(
            JList<? extends Entry<Connection, Player>> list,
            Entry<Connection, Player> value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        StringBuilder builder = new StringBuilder();
        if (value.getValue().getName() != null) {
            builder.append(value.getValue().getName());
        } else {
            builder.append("Observer");
        }
        builder.append(" (");
        builder.append(value.getKey().getAddress().toString());
        builder.append(")");
        label.setText(builder.toString());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        return this;
    }
    
}
