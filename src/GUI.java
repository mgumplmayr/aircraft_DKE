import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    private static boolean first = true;
    private static boolean createFile = true;
    private static boolean executeRules = true;
    private static Mode chosenMode = Mode.NONE;

    public enum Mode {
        NONE, TEST, PRODUCTION
    }
    public GUI() {
        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
            UIManager.put( "Button.arc", 10 );
            UIManager.put( "Component.arc", 5 );
            UIManager.put( "ProgressBar.arc", 5 );
            UIManager.put( "TextComponent.arc", 5 );
            UIManager.put( "Component.focusWidth", 1.5 );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }

        setTitle("Aircraft Manager");
        setSize(570,180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container central = getContentPane();
        central.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,2));
        JPanel secondPane = new JPanel();
        //secondPane.setLayout(new GridLayout(5,2));

        JButton test = new JButton("Test");
        JButton productive = new JButton("Productive");

        JButton startFuseki = new JButton("Start Fuseki-Server");
        JButton importStaticData = new JButton("Import Static Data");
        JButton changeIdentifier = new JButton("ChangeIdentifier");
        JButton predictPosition = new JButton("Predict Positions");
        JButton update = new JButton("Refresh States");
        JButton openQuery = new JButton("Open Query");
        JButton log = new JButton("Log");

        JCheckBox file = new JCheckBox("create RDF-File?", true);
        JCheckBox rules = new JCheckBox("Execute SHACL-Rules?", true);


        panel.add(productive);
        panel.add(test);

        secondPane.add(startFuseki);
        secondPane.add(importStaticData);
        secondPane.add(update);
        secondPane.add(openQuery);
        secondPane.add(log);
        secondPane.add(changeIdentifier);
        secondPane.add(predictPosition);

        JPanel controls = new JPanel();
        GridLayout gridLayout = new GridLayout(2,3);
        gridLayout.setHgap(10);
        controls.setLayout(gridLayout);

        JSpinner velocityThreshold = new JSpinner(new SpinnerNumberModel(5d,0,99,1));
        JSpinner directionThreshold = new JSpinner(new SpinnerNumberModel(5d,0,99,1));
        JSpinner heightThreshold = new JSpinner(new SpinnerNumberModel(5d,0,99,1));

        controls.add(new Label("Velocity Threshold:"));
        controls.add(new Label("Direction Threshold:"));
        controls.add(new Label("Height Threshold:"));
        controls.add(velocityThreshold);
        controls.add(directionThreshold);
        controls.add(heightThreshold);
        secondPane.add(controls);


        central.add(panel,BorderLayout.CENTER);
        JPanel boxes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boxes.add(file);
        boxes.add(rules);
        central.add(boxes,BorderLayout.SOUTH);

        importStaticData.addActionListener(e -> {
            if(getFirst()){
                first = false;
                Main.loadStaticData();
                Main.validateStaticData();
                if(createFile) Main.writeRDF();
                Main.uploadStaticGraph();
            }
        });


        update.addActionListener(e -> {
            if(chosenMode == Mode.NONE);
            else {
                Main.update();
                if (isExecuteRules()) {
                    double t1 = (double) velocityThreshold.getValue();
                    double t2 = (double) velocityThreshold.getValue();
                    double t3 = (double) velocityThreshold.getValue();
                    Main.executeRules((float)t1,(float)t2, (float)t3);
                }
                if (isCreatFiles()){
                    Main.writeRDF();
                    PositionPredictor.writeRDF();
                    ChangeIdentifier.writeRDF();
                }
            }

        });
        test.addActionListener(e -> {
            if(first) {
                productive.setSelected(false);
                setChosenMode(Mode.TEST);
                central.removeAll();
                central.add(secondPane, BorderLayout.CENTER);
                central.add(boxes, BorderLayout.SOUTH);
                revalidate();
                repaint();
            } else
                test.setSelected(false);
        });

        productive.addActionListener(e -> {
            if(first) {
                test.setSelected(false);
                setChosenMode(Mode.PRODUCTION);
                central.removeAll();
                central.add(secondPane, BorderLayout.CENTER);
                central.add(boxes, BorderLayout.SOUTH);
                revalidate();
                repaint();
            } else productive.setSelected(false);
        });

        startFuseki.addActionListener(e -> Main.initiateFuseki());

        changeIdentifier.addActionListener(e -> {
            ChangeIdentifier.executeRule(5,3,1);
            if (isCreatFiles()) ChangeIdentifier.writeRDF();
        });
        predictPosition.addActionListener(e -> {
            PositionPredictor.executeRule();
            if (isCreatFiles()) PositionPredictor.writeRDF();
        });

        openQuery.addActionListener(e -> Main.openDatasetQuery());

        file.addActionListener(e -> setCreateFile(!isCreatFiles()));
        rules.addActionListener(e -> setExecuteRules(!isExecuteRules()));

        log.addActionListener(e -> new logscreen()); //todo https://stackoverflow.com/questions/14706674/system-out-println-to-jtextarea
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

    public static boolean isCreatFiles() {
        return createFile;
    }
    public static boolean isExecuteRules() {
        return executeRules;
    }

    public static void setCreateFile(boolean createFile) {
        GUI.createFile = createFile;
    }
    public static void setExecuteRules(boolean executeRules) {
        GUI.executeRules = executeRules;
    }

    class logscreen extends JFrame {
        public logscreen(){
            setTitle("Log");
            setSize(500,500);

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);


            JTextArea text = new JTextArea();
            String temp = String.valueOf(Main.log);
            System.out.println(temp);
            text.append(temp);

            JScrollPane panel = new JScrollPane(text,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            this.add(panel);


            setVisible(true);
        }

    }
}


