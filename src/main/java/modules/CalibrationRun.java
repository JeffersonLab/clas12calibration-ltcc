package modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.utils.groups.IndexedList;

public class CalibrationRun {

    // geometry, available sectors, naming conventions for histos
    private static final int NSECTORS = 6;
    private static final int NSIDES = 2;
    private static final int NSEGMENTS = 18;
    //   private static final int[] ACTIVESECTORS = {0, 1, 1, 0, 1, 1};

    // calibration constants from DB
    private CalibrationConstants speCalib = null;
    private CalibrationConstants timingCalib = null;
    private int runNumber;

    // histos naming conventions
    private static final String FADC_ALLNAME = "allFadc";
    private static final String FADC_RNDNAME = "rndFadc";
    private static final String FADC_ELENAME = "eleFadc";
    private static final String FADC_PIONAME = "pioFadc";
    private static final String[] SPE_HISTONAMES = {FADC_ALLNAME, FADC_RNDNAME, FADC_ELENAME, FADC_PIONAME};
    private static final int[] SPE_HISTOSMAXS = {1000, 1000, 4000, 4000};

    // histo vectors. First index is histo name, indexes 2,3,4 are sector, side, pmt 
    private IndexedList<H1F> speHistos = new IndexedList<>(SPE_HISTONAMES.length);

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

                    for (int h = 0; h < SPE_HISTONAMES.length; h++) {

                        H1F thisHisto = new H1F(SPE_HISTONAMES[h], 200, 0.0, SPE_HISTOSMAXS[h]);
                        thisHisto.setTitleX("ADC");
                        thisHisto.setTitleY("Counts");
                        thisHisto.setTitle(titleForObject("all ADC", s, d, p));

                        speHistos.add(thisHisto, h, s, d, p);

                    }
                }
            }
        }
    }

    // get wanted histogram from sector, side, pmt number 
    public H1F getHisto(String name, int s, int d, int p) {
        return speHistos.getItem(histoIndex(name), s, d, p);

    }

    private int histoIndex(String name) {
        for (int h = 0; h < SPE_HISTONAMES.length; h++) {
            if (name.equals(SPE_HISTONAMES[h])) {
                return h;
            }
        }
        return 0;
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
