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
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import view.DetectorListener;
import view.DetectorShape2D;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;

import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.detector.decode.CLASDecoder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, DetectorListener, ChangeListener, EventUtils {

    JPanel mainPanel = null;

    JMenuBar menuBar = null;

    DataSourceProcessorPane processorPane = null;

    JSplitPane splitPanel = null;

    JPanel detectorPanel = null;

    CCDetector detectorView = null;

    JTabbedPane modulePanel = null;

    String moduleSelect = null;

    private int canvasUpdateTime = 4000;

    private int analysisUpdateTime = 50000;

    private int runNumber = 0;

    private final String workDir = "/Users/devita";

    ArrayList<CalibrationModule> modules = new ArrayList();

    CLASDecoder clasDecoder = new CLASDecoder();

    public CalibrationViewer() {

        // create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu constants = new JMenu("Constants");
        menuItem = new JMenuItem("Load...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Load constants from file");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save constants to file");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        menuBar.add(constants);
        JMenu file = new JMenu("Histograms");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms to file...", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...", KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set analysis update interval...", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);

        // create detector panel
        detectorPanel = new JPanel();
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

    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if ("Set analysis update interval...".equals(e.getActionCommand())) {
            this.chooseUpdateInterval();
        }
        if (e.getActionCommand() == "Open histograms file...") {
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
        if ("Print histograms to file...".equals(e.getActionCommand())) {
            this.printHistosToFile();
        }

        if ("Save histograms...".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "LTCCCalib_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
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
        if ("Load...".equals(e.getActionCommand())) {
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
            }
        }
        if ("Save...".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String dirName = "LTCCCalib_" + this.runNumber + "_" + df.format(new Date());
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
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
                    System.out.println("Created directory: " + dirName);
                }
            }
            for (int k = 0; k < this.modules.size(); k++) {
                this.modules.get(k).saveConstants(dirName);
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
        }
        if (bank != null) {
            rNum = bank.getInt("run", 0);
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

        HipoDataEvent hipo = null;

        if (de != null) {
            this.runNumber = this.getRunNumber(de);
        }
        if (de.getType() == DataEventType.EVENT_START) {
            //System.out.println(" EVENT_START");
        } else if (de.getType() == DataEventType.EVENT_ACCUMULATE) {
            // System.out.println(" EVENT_ACCUMULATE" + i);
        } else if (de.getType() == DataEventType.EVENT_SINGLE) {
            //   System.out.println("EVENT_SINGLE from CalibrationViewer");
        } else if (de.getType() == DataEventType.EVENT_STOP) {
            // System.out.println(" EVENT_STOP else");
            // System.out.println(" Analyzed");
        }
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
            if (de instanceof EvioDataEvent) {
                this.modules.get(k).dataEventAction(de);
            } else {
                this.modules.get(k).dataEventAction(hipo);
            }
            this.modules.get(k).dataEventAction(hipo);

        }

        this.detectorView.repaint();

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

    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String data = this.workDir + "/kpp-pictures/clas12rec_run_" + this.runNumber + "_" + df.format(new Date());
        File theDir = new File(data);
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
                System.out.println("Created directory: " + data);
            }
        }
        String fileName = data + "/clas12_canvas.png";
        System.out.println(fileName);
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

}
