package modules;

// java graphics
import javax.swing.JSplitPane;

// jlab canvas and constants view
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.groot.graphics.EmbeddedCanvas;


// Class dedicate to display information 
// and color the detector accordingly
public class ModuleViewController {
    
    private CalibrationConstantsView speCalibrationView = null;
    private CalibrationConstantsView timingCalibrationView = null;
 
    public String moduleName;
    
    
    public ModuleViewController(String mn) {
        moduleName = mn;
    }
    
}