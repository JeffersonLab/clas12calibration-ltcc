/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import view.DetectorShape2D;
import viewer.CalibrationModule;
import viewer.CCDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.base.DetectorType;

// for mode help on groot:
// https://github.com/gavalian/groot
/**
 * @author burcu
 */
public class LTCCPulses extends CalibrationModule {

    public LTCCPulses(CCDetector d, String name) {
        super(d, name, "mean:mean_e:sigma:sigma_e");
    }

    // event processed
    public int nEventsProcessed;
    CodaEventDecoder decoder = new CodaEventDecoder();

    // define histos
    @Override
    public void resetEventListener() {

        nEventsProcessed = 0;

        for (int iSect : this.getDetector().getSectors()) {
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    // initialize data group
                    H1F ltccPulse = new H1F("speADC_" + iSect + "_" + iSide + "_" + iComp, 100, 0.0, 100.0);
                    ltccPulse.setTitleX("time bunch");
                    ltccPulse.setTitleY("ADC");

                    DataGroup dg = new DataGroup(6, 3);
                    dg.addDataSet(ltccPulse, 0);

                    this.getDataGroup().add(dg, iSect, iSide, iComp);
                }
            }
        }

        getCalibrationTable().fireTableDataChanged();
    }

    // what does this do?
    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    @Override
    public void processEvent(DataEvent event) {

        nEventsProcessed++;

                        System.out.println(" inside ltcc pulses"  );

        // getting pulses
        if (event instanceof EvioDataEvent) {
            List<DetectorDataDgtz> dataList = decoder.getDataEntries((EvioDataEvent) event);
            List<DetectorDataDgtz> counters = new ArrayList<>();

            // filling counters with LTCC only data
            for (DetectorDataDgtz entry : dataList) {
                if (entry.getDescriptor().getType() == DetectorType.LTCC) {
                    if (entry.getADCSize() > 0) {
                        counters.add(entry);
                    }
                }
            }

            // looping over LTCC counters
            for (DetectorDataDgtz counter : counters) {
                int sector = counter.getDescriptor().getSector();
                int layer = counter.getDescriptor().getSector();
                int component = counter.getDescriptor().getSector();

                System.out.println(" sector " + sector + "   layer " + layer + " component " + component );

            }
        }

    }

    // decide what's get plotted in the canvas
    @Override
    public void processShape(DetectorShape2D dsd) {

        // plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        // layer is 1, 2 in the detector shape. It is 0, 1, in the histo (order
        int side = dsd.getDescriptor().getLayer() - 1;

    }

}
