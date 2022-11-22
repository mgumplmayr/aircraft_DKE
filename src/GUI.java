import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class GUI extends JFrame {
    private static boolean first = true;
    private static Mode chosenMode = Mode.NONE;

    public enum Mode {
        NONE, TEST, PRODUCTION
    }
    public GUI() {

        setTitle("Fusekimanager");
        setSize(500,150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container panel = getContentPane();
        panel.setLayout(new GridLayout(1,2));
        Container secondPane = getContentPane();
        panel.setLayout(new GridLayout(1,3));

        JButton test = new JButton("test");
        JButton productive = new JButton("productive");

        JButton startFuseki = new JButton("start Fuseki-Server");
        JButton refresh = new JButton("refresh");
        JButton openQuery = new JButton("open Query");

        panel.add(test);
        panel.add(productive);

        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(chosenMode == Mode.NONE) return;
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
                    setChosenMode(Mode.TEST);
                    panel.removeAll();
                    secondPane.add(startFuseki);
                    secondPane.add(refresh);
                    secondPane.add(openQuery);
                    revalidate();
                    repaint();
                } else
                    test.setSelected(false);
            }
        });
        productive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(first) {
                    test.setSelected(false);
                    setChosenMode(Mode.PRODUCTION);
                    panel.removeAll();
                    secondPane.add(startFuseki);
                    secondPane.add(refresh);
                    secondPane.add(openQuery);
                    revalidate();
                    repaint();
                } else productive.setSelected(false);
            }
        });
        startFuseki.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.initiateFuseki();
            }
        });

        openQuery.addActionListener(new ActionListener() {
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


    public static Mode getChosenMode() {
        return chosenMode;
    }

    public void setChosenMode(Mode chosenMode) {
        this.chosenMode = chosenMode;
    }

    public static boolean getFirst(){
        return first;
    }
}

