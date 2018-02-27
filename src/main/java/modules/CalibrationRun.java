package modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

public class CalibrationRun {

    // calibration constants from DB
    private CalibrationConstants speCalib = null;
    private CalibrationConstants timingCalib = null;
    private int runNumber;

    // histograms are saved in datagroups
    private final IndexedList<DataGroup> dataGroups = new IndexedList<>(3);

    // geometry, available sectors, naming conventions for histos
    private static final int NSECTORS = 6;
    private static final int NSIDES = 2;
    private static final int NSEGMENTS = 18;
    private static final int[] ACTIVESECTORS = {0, 1, 1, 0, 1, 1};

    // histos
    private static final String FADC_ALLNAME = "allFadc";
    private static final String FADC_ELENAME = "eleFadc";
    private static final String FADC_PIONAME = "pioFadc";
    private static final String FADC_RNDNAME = "rndFadc";

    // histo storing the fit parameters
    private static final String FITPARS_HNAME = "fitpars";

    // fit functions
    private static final String GAUSF_HNAME = "gaussFitFunction";

    public CalibrationRun(int runNo) {

        runNumber = runNo;

        // initializing databases
        System.out.println("Initializing CCDB Database: SPE");

        speCalib = new CalibrationConstants(3, "mean:mean_e:sigma:sigma_e:ped:tet");
        speCalib.setName("LTCC SPE Calibration");
        speCalib.setPrecision(2);
        // initialize constants from CCDB

        System.out.println("Initializing CCDB Database: Timing");
        timingCalib = new CalibrationConstants(3, "mean:mean_e:sigma:sigma_e:ped:tet");
        timingCalib.setName("LTCC Timing Calibration");
        timingCalib.setPrecision(2);

        // histos
        for (int s = 0; s < NSECTORS; s++) {
            for (int d = 0; d < NSIDES; d++) {
                for (int p = 0; p < NSEGMENTS; p++) {

                }
            }
        }

    }

    
    // naming convention
    private String nameForObject(String baseName, int sector, int side, int segment) {
        return baseName + "_S" + (sector + 1) + "_L" + (side + 1) + "_P" + (segment + 1);
    }

    private String titleForObject(String baseName, int sector, int side, int segment) {
        return baseName + "S:" + (sector + 1) + " Side: " + (side + 1) + " P: " + (segment + 1);
    }

}
