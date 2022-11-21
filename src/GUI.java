import jdk.jfr.Event;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.URI;

public class GUI extends JFrame {
    private static boolean first = true;
    private static int chosenMode = -1;
    public GUI() {

        setTitle("Fusekimanager");
        setSize(500,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container panel = getContentPane();
        panel.setLayout(new GridLayout(2,3));

        JLabel mode= new JLabel("Modus:",SwingConstants.CENTER);
        mode.setForeground(Color.BLUE);
        JRadioButton test = new JRadioButton("Test",false);
        test.setHorizontalTextPosition(SwingConstants.CENTER);
        test.setVerticalTextPosition(JRadioButton.TOP);
        JRadioButton productive = new JRadioButton("Produktion",false);
        productive.setHorizontalTextPosition(SwingConstants.CENTER);
        productive.setVerticalTextPosition(JRadioButton.TOP);

        JLabel action = new JLabel("Aktionen:",SwingConstants.CENTER);
        action.setForeground(Color.BLUE);
        JButton updateButton = new JButton("Aktualisieren");
        JButton exit = new JButton("Fuseki öffnen");

        panel.add(mode);
        panel.add(test);
        panel.add(productive);
        panel.add(action);
        panel.add(updateButton);
        panel.add(exit);


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(getChosenMode() != 0 && getChosenMode() != 1) return;
                else {
                    first = false;
                    Main.update();
                }
            }
        });
        test.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(first) {
                    productive.setSelected(false);
                    setChosenMode(0);
                } else
                    test.setSelected(false);
            }
        });
        productive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(first) {
                    test.setSelected(false);
                    setChosenMode(1);
                } else productive.setSelected(false);
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop desktop = java.awt.Desktop.getDesktop();
                    URI oURL = new URI("http://localhost:3030/#/dataset/aircraft/query/");
                    desktop.browse(oURL);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        //TODO SysTrayIcon für schnelles updaten
        /*if(java.awt.SystemTray.isSupported()){
            SystemTray tray = SystemTray.getSystemTray();
            try {
                tray.add(new TrayIcon(getIconImage()));
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }

        }*/
    }


    public static int getChosenMode() {
        return chosenMode;
    }

    public void setChosenMode(int chosenMode) {
        this.chosenMode = chosenMode;
    }

    public static boolean getFirst(){
        return first;
    }
}

