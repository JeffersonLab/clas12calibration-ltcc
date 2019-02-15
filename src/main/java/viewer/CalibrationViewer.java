package viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import modules.SPECalibration;
import modules.SPESummary;
import modules.EventUtils;
import modules.Occupancy;
import modules.LTCCPulses;

import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import view.DetectorListener;
import view.DetectorShape2D;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;

import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.detector.decode.CLASDecoder;

/**
 * @author ungaro
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, DetectorListener, ChangeListener, EventUtils {

    JPanel mainPanel = null;

    JMenuBar menuBar = null;

    DataSourceProcessorPane processorPane = null;

    JSplitPane splitPanel = null;

    CCDetector detectorView = null;

    JTabbedPane modulePanel = null;

    String moduleSelect = null;

    private int canvasUpdateTime = 4000;

    private int analysisUpdateTime = 5000;

    private boolean mode1Active = false;

    private int runNumber = -1;
    private int nProcessed = 0;

    private final String workDir = ".";

    ArrayList<CalibrationModule> modules = new ArrayList();

    CLASDecoder clasDecoder = new CLASDecoder();

    // if it's calibration than we do not need to analyse the pulse?
    JButton isItCalibration;

    public CalibrationViewer() {

        // create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // create menu bar
        menuBar = new JMenuBar();

        // Constants
        JMenu constantsMenu = new JMenu("Constants");
        constantsMenu.add(createMenuItem("Load Constants", "Load Constants from file", KeyEvent.VK_L));
        constantsMenu.add(createMenuItem("Save Constants", "Save Constants to file", KeyEvent.VK_S));
        menuBar.add(constantsMenu);

        // Settings
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.add(createMenuItem("Set analysis update interval", "Set analysis update interval", KeyEvent.VK_T));
        settingsMenu.add(createMenuItem("Switch Mode 1", "Activate/Disactive Mode 1 histos", KeyEvent.VK_M));
        menuBar.add(settingsMenu);

        // Save Data Group
        JMenu dataGroupIO = new JMenu("DataGroupIO");
        dataGroupIO.add(createMenuItem("Save Datagroup", "Save all datagroup objects like histos, etc", KeyEvent.VK_D));
        dataGroupIO.add(createMenuItem("Load Datagroup", "Load all datagroup objects like histos, etc", KeyEvent.VK_R));
        menuBar.add(dataGroupIO);

        // create detector panel
        JPanel detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new CCDetector("LTCC");
        initDetector();
        detectorPanel.add(detectorView);

        SPECalibration speC = new SPECalibration(detectorView, "SPECalibration");
        SPESummary speS = new SPESummary(detectorView, "SPESummary");
        speS.setCalibrationTable(speC.getCalibrationTable());

        Occupancy pmtOccupancy = new Occupancy(detectorView, "Occupancy");

        LTCCPulses ltccPulses = new LTCCPulses(detectorView, "Mode 1");

        // create module viewer: each of these is on one tab
        //modules.add(new TimeCalibration(detectorView, "TimeCalibration"));
        modules.add(speC);
        modules.add(speS);
        modules.add(pmtOccupancy);
        modules.add(ltccPulses);

        modulePanel = new JTabbedPane();
        for (int k = 0; k < modules.size(); k++) {
            modulePanel.add(modules.get(k).getName(), modules.get(k).getView());
            if (moduleSelect == null) {
                moduleSelect = modules.get(k).getName();
            }
        }
        modulePanel.addChangeListener(this);

        // create split panel to host detector view and canvas+constants view
        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorPanel);
        splitPanel.setRightComponent(modulePanel);
        splitPanel.setDividerLocation(0.3);
        splitPanel.setResizeWeight(0.3);

        // create data processor panel
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);
        processorPane.addEventListener(this);

        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane, BorderLayout.PAGE_END);

        this.setCanvasUpdate(canvasUpdateTime);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // System.out.println(e.getActionCommand());
        if ("Set analysis update interval".equals(e.getActionCommand())) {
            this.chooseUpdateInterval();
        }

        if ("Switch Mode 1".equals(e.getActionCommand())) {
            mode1Active = !mode1Active;
            System.out.println("Mode 1: " + mode1Active);
        }

        if ("Save Datagroup".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy");

            String fileName = "LTCCCalib-Run." + this.runNumber + "." + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();

            File workingDirectory = new File(this.workDir + "/LTCCCalib-histos");
            fc.setCurrentDirectory(workingDirectory);

            File file = new File(fileName);
            fc.setSelectedFile(file);

            int returnValue = fc.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
            }
            this.saveHistosToFile(fileName);
        }

        if ("Load Datagroup".equals(e.getActionCommand())) {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(this.workDir + "/LTCCCalib-histos");
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
            }
            if (fileName != null) {
                this.loadHistosFromFile(fileName);
            }
        }

        if ("Load Constants...".equals(e.getActionCommand())) {
            String filePath = null;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose Constants Folder...");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int returnValue = fc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                filePath = fc.getSelectedFile().getAbsolutePath();
            }
            for (int k = 0; k < this.modules.size(); k++) {
                this.modules.get(k).loadConstants(filePath);

            //    this.modules.get(k).analyze();

            }
        }

        if ("Save Constants".equals(e.getActionCommand())) {

            DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            String dirName = "LTCCCalib-" + this.runNumber + "_" + df.format(new Date());

            JFileChooser fc = new JFileChooser();

            //File workingDirectory = new File(this.workDir);
            // fc.setCurrentDirectory(workingDirectory);
            File file = new File(dirName);
            fc.setSelectedFile(file);

            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                dirName = fc.getSelectedFile().getAbsolutePath();
            }

            File theDir = new File(dirName);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try {
                    theDir.mkdir();
                    result = true;
                } catch (SecurityException se) {
                    //handle it
                }
                if (result) {
                    System.out.println("Created file: " + dirName);
                }
            }

            for (int k = 0; k < this.modules.size(); k++) {
                // how to select a particular module?
                if ("SPECalibration".equals(this.modules.get(k).getName())) {
                    this.modules.get(k).saveConstants(dirName);
                }
            }
        }
    }

    public void chooseUpdateInterval() {
        String s = (String) JOptionPane.showInputDialog(
                null,
                "GUI update interval (ms)",
                " ",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "5000");
        if (s != null) {
            int time = 5000;
            try {
                time = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if (time > 0) {
                this.setCanvasUpdate(time);
            } else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }

    private int getRunNumber(DataEvent event) {

        int rNum = this.runNumber;

        DataBank bank = null;
        if (event.hasBank("RUN::config")) {
            bank = event.getBank("RUN::config");

            if (bank != null) {
                rNum = bank.getInt("run", 0);
            }
        }
        return rNum;
    }

    public void initDetector() {
        detectorView.getView().addDetectorListener(this);
        for (String layer : detectorView.getView().getLayerNames()) {
            detectorView.getView().setDetectorListener(layer, this);
        }
        detectorView.updateBox();

        printEventUtils();

    }

    @Override
    public void dataEventAction(DataEvent de) {
        nProcessed++;
        HipoDataEvent hipo = null;

//        if (de.getType() == DataEventType.EVENT_START) {
//            //System.out.println(" EVENT_START");
//        } else if (de.getType() == DataEventType.EVENT_ACCUMULATE) {
//            // System.out.println(" EVENT_ACCUMULATE" + i);
//        } else if (de.getType() == DataEventType.EVENT_SINGLE) {
//            //   System.out.println("EVENT_SINGLE from CalibrationViewer");
//        } else if (de.getType() == DataEventType.EVENT_STOP) {
//            // System.out.println(" EVENT_STOP else");
//            // System.out.println(" Analyzed");
//        }
        // this should be taken care in each module instead?
        // each modules knows what type of data it needs
        if (de instanceof EvioDataEvent) {
            hipo = (HipoDataEvent) clasDecoder.getDataEvent(de);
            DataBank header = clasDecoder.createHeaderBank(hipo, 0, 0, (float) 0, (float) 0);
            hipo.appendBanks(header);
        } else {
            hipo = (HipoDataEvent) de;
        }

        for (int k = 0; k < this.modules.size(); k++) {
            if ("Mode 1".equals(this.modules.get(k).getName())) {
                if (mode1Active) {
                    this.modules.get(k).dataEventAction(de);
                }
            } else {
                this.modules.get(k).dataEventAction(hipo);
            }
            //           this.modules.get(k).dataEventAction(hipo);

        }

        if (nProcessed % 100 == 0) {
            this.detectorView.repaint();
        }

        if (runNumber == -1 && hipo == null) {
            runNumber = this.getRunNumber(de);
        } else {
            runNumber = this.getRunNumber(hipo);
        }

    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();

        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).readDataGroup(dir);
        }
    }

    public void timerUpdate() {
        this.detectorView.repaint();

        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).timerUpdate();
        }
    }

    public void resetEventListener() {
        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).resetEventListener();
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).processShape(dsd);
        }
    }

    @Override
    public void update(DetectorShape2D dsd) {
//        System.out.println("Changing color");
        for (int k = 0; k < this.modules.size(); k++) {
            if (this.modules.get(k).getName() == moduleSelect) {
                Color col = this.modules.get(k).getColor(dsd);
                dsd.setColor(col.getRed(), col.getGreen(), col.getBlue());
            }
        }
    }

    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }

    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for (int k = 0; k < this.modules.size(); k++) {
            this.modules.get(k).setCanvasUpdate(time);
        }
    }

    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        moduleSelect = sourceTabbedPane.getTitleAt(index);
        this.detectorView.repaint();
    }

    // main 
    public static void main(String[] args) {
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalibrationViewer viewer = new CalibrationViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(1400, 800);
        frame.setVisible(true);

    }

    public long getTriggerWord(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        return bank.getLong("trigger", 0);
    }

    private JMenuItem createMenuItem(String title, String description, int ke) {

        JMenuItem menuItem = new JMenuItem(title, ke);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(ke, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(description);
        menuItem.addActionListener(this);

        return menuItem;
    }
}
