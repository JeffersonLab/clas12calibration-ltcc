package modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

public class CalibrationRun {

    // geometry, available sectors, naming conventions for histos
    private static final int NSECTORS = 6;
    private static final int NSIDES = 2;
    private static final int NSEGMENTS = 18;
    private static final int[] ACTIVESECTORS = {0, 1, 1, 0, 1, 1};

    // calibration constants from DB
    private CalibrationConstants speCalib = null;
    private CalibrationConstants timingCalib = null;
    private int runNumber;

    // histograms are saved in datagroups
    private final IndexedList<DataGroup> dataGroups = new IndexedList<>(3);

    // histos naming conventions
    private static final String FADC_ALLNAME = "allFadc";
    private static final String FADC_RNDNAME = "rndFadc";
    private static final String FADC_ELENAME = "eleFadc";
    private static final String FADC_PIONAME = "pioFadc";
    private static final String[] speHistoNames = {FADC_ALLNAME, FADC_RNDNAME, FADC_ELENAME, FADC_PIONAME};
    private static final int[] speHistoXMax = {1000, 1000, 4000, 4000};

    // histo vectors
    private IndexedList<H1F> speHistos = new IndexedList<>(4);

//    // histo storing the fit parameters
//    private static final String FITPARS_HNAME = "fitpars";
//
//    // fit functions
//    private static final String GAUSF_HNAME = "gaussFitFunction";
    public CalibrationRun(int runNo) {

        runNumber = runNo;

        // initializing databases
        System.out.println("Initializing CCDB Database: SPE");

        speCalib = new CalibrationConstants(3, "mean:mean_e:sigma:sigma_e:ped:tet");
        speCalib.setName("LTCC SPE Calibration");
        speCalib.setPrecision(2);

        System.out.println("Initializing CCDB Database: Timing");
        timingCalib = new CalibrationConstants(3, "mean:mean_e:sigma:sigma_e:ped:tet");
        timingCalib.setName("LTCC Timing Calibration");
        timingCalib.setPrecision(2);

        // histos
        for (int s = 0; s < NSECTORS; s++) {
            for (int d = 0; d < NSIDES; d++) {
                for (int p = 0; p < NSEGMENTS; p++) {

                    // initialize constants 
                    speCalib.addEntry(s, d, p);
                    speCalib.setDoubleValue(200.0, "mean", s, d, p);
                    speCalib.setDoubleValue(10.0, "mean_e", s, d, p);
                    speCalib.setDoubleValue(20.0, "sigma", s, d, p);
                    speCalib.setDoubleValue(2.0, "sigma_e", s, d, p);

                    for (int h = 0; h < speHistoNames.length; h++) {
                        speHistos.add(new H1F(speHistoNames[h], 200, 0.0, 1000.0), s, d, p );
                    }
                    
                    // initialize histos
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

                    // one datagroup / pmt
                    // why the dimensions?
                    DataGroup thisDataGroup = new DataGroup(6, 3);
                    thisDataGroup.addDataSet(allFadc, 0);
                    thisDataGroup.addDataSet(eleFadc, 0);
                    thisDataGroup.addDataSet(pioFadc, 0);
                    thisDataGroup.addDataSet(rndFadc, 0);

                    dataGroups.add(thisDataGroup, s, d, p);

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
