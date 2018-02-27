package modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
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

                    H1F allFadc = new H1F(nameForObject(FADC_ALLNAME, s, d, p), 200, 0.0, 1000.0);
                    H1F eleFadc = new H1F(nameForObject(FADC_ELENAME, s, d, p), 200, 0.0, 4000.0);
                    H1F pioFadc = new H1F(nameForObject(FADC_PIONAME, s, d, p), 200, 0.0, 4000.0);
                    H1F rndFadc = new H1F(nameForObject(FADC_RNDNAME, s, d, p), 200, 0.0, 1000.0);

                    allFadc.setTitleX("ADC");
                    allFadc.setTitleY("Counts");
                    allFadc.setTitle(titleForObject("all ADC", s, d, p));
                    eleFadc.setTitleX("ADC");
                    eleFadc.setTitleY("Counts");
                    eleFadc.setTitle(titleForObject("electrons ADC", s, d, p));
                    pioFadc.setTitleX("ADC");
                    pioFadc.setTitleY("Counts");
                    pioFadc.setTitle(titleForObject("pions ADC", s, d, p));
                    rndFadc.setTitleX("ADC");
                    rndFadc.setTitleY("Counts");
                    rndFadc.setTitle(titleForObject("rnd trigger ADC", s, d, p));

                  
//                    DataGroup dg = new DataGroup(6, 3);
//                    dg.addDataSet(speADC, 0);
//                    dg.addDataSet(fitpars, 0);   // added fit parameters histo to the datagroup
//                    dg.addDataSet(fitparsDB, 0);   // added hist containing fit parameters from DB to the datagroup
//                    dg.addDataSet(gaussianFit, 0);   // added gaussian fit function
//
//                    this.getDataGroup().add(dg, iSect, iSide, iComp);

                }
            }
        }

    }

    // naming convention
    private String nameForObject(String baseName, int sector, int side, int segment) {
        return baseName + "_S" + (sector + 1) + "_L" + (side + 1) + "_P" + (segment + 1);
    }

    private String titleForObject(String baseName, int sector, int side, int segment) {
        String sideString = "Left";
        if (side == 1) {
            sideString = "Right";
        }
        return baseName + "S:" + (sector + 1) + sideString + " P: " + (segment + 1);
    }

}

