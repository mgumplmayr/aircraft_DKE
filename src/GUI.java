import jdk.jfr.Event;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.SystemTray;
import java.awt.TrayIcon;

public class GUI extends JFrame {

    private static int chosenMode;
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
        JButton exit = new JButton("Beenden");

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
                else Main.update();
            }
        });
        test.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                productive.setSelected(false);
                setChosenMode(0);
            }
        });
        productive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                test.setSelected(false);
                setChosenMode(1);
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
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
}

