package modules;


// jlab canvas and constants view
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import view.DetectorShape2D;

// Class dedicate to display information 
// and color the detector accordingly
public class ModuleViewController {

    private CalibrationConstantsView speCalibrationView = null;
    private CalibrationConstantsView timingCalibrationView = null;

    public String moduleName;

    public ModuleViewController(String mn) {
        moduleName = mn;
    }

    public void processShape(DetectorShape2D dsd) {

        // plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int pmt = dsd.getDescriptor().getComponent();

        // System.out.println("Selected pmt sector: " + sector + ", layer: " + layer + " pmt: " + pmt);
    }
}
