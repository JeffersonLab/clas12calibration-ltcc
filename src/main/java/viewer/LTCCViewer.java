package viewer;

// graphics
import java.awt.BorderLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JMenuItem;

// actions
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

// ltcc 
import modules.CalibrationData;

/**
 * @author ungaro
 */
public final class LTCCViewer implements ActionListener {

    // main panel
    JPanel mainPanel = null;

    // menubar
    JMenuBar menuBar = null;

    // include mode 1 analysis
    private boolean analyzeMode1 = false;

    CalibrationData calibData;

// constructor: menubar items
    public LTCCViewer() {

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
        settingsMenu.add(createMenuItem("Switch Mode 1 Analysis", "Activate/Disactive Mode 1 histos", KeyEvent.VK_M));
        menuBar.add(settingsMenu);

        // Save Data Group
        JMenu dataGroupIO = new JMenu("Histos IO");
        dataGroupIO.add(createMenuItem("Save Histos", "Save all Histos for the current run", KeyEvent.VK_D));
        dataGroupIO.add(createMenuItem("Load Histos", "Load all Histos from a run file", KeyEvent.VK_R));
        menuBar.add(dataGroupIO);

        calibData = new CalibrationData();

    }

    private JMenuItem createMenuItem(String title, String description, int ke) {

        JMenuItem menuItem = new JMenuItem(title, ke);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(ke, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(description);
        menuItem.addActionListener(this);

        return menuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Switch Mode 1 Analysis")) {
            analyzeMode1 = !analyzeMode1;
            System.out.println("Analyze Mode 1: " + analyzeMode1);
        }
    }

    // application main
    // netbeans runs a file if it has a main
    public static void main(String[] args) {
        JFrame frame = new JFrame("LTCC Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LTCCViewer ltccViewer = new LTCCViewer();

        //frame.add(viewer.getPanel());
        frame.add(ltccViewer.mainPanel);

        frame.setJMenuBar(ltccViewer.menuBar);
        frame.setSize(1400, 800);
        frame.setVisible(true);

    }
}
