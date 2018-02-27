package modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;
import viewer.CCDetector;

public class CalibrationRun {

    // calibration constants from DB
    private CalibrationConstants speCalib = null;
    private CalibrationConstants timingCalib = null;

    // histograms are saved in datagroups
    private final IndexedList<DataGroup> dataGroups = new IndexedList<>(3);

    // geometry, available sectors, naming conventions for histos
    private static final int NSECTORS = 6;
    private static final int NSIDES = 2;
    private static final int NSEGMENTS = 18;
    private static final int[] ACTIVESECTORS = {0, 1, 1, 0, 1, 1};

    // histos
    private static final String FADC_HNAME = "fadc";

    // histo storing the fit parameters
    private static final String FITPARS_HNAME = "fitpars";

    // fit functions
    private static final String GAUSF_HNAME = "gaussFitFunction";

    public CalibrationRun() {

    }

}
