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
import org.jlab.detector.decode.DetectorEventDecoder;

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
    DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();

    // define histos
    @Override
    public void resetEventListener() {

        nEventsProcessed = 0;

        for (int iSect : this.getDetector().getSectors()) {
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    // initialize data group
                    H1F ltccPulse = new H1F("ltccPulse_" + iSect + "_" + iSide + "_" + iComp, 100, 0.0, 100.0);
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

    // the second argument means I can call this function with an index[3] or with an explicity list like sector,layer,component
    private H1F getHistogramFromDataDataGroup(String histoName, int... index) {
        // int[] index = new int[3]; << this is how a new array is defined in java
        // for(int i = 0; i < index.length; i++) System.out.println( index[i]);
        return this.getDataGroup().getItem(index[0], index[1], index[2]).getH1F(histoName + index[0] + "_" + index[1] + "_" + index[2]);
    }

    @Override
    public void processEvent(DataEvent event) {

        nEventsProcessed++;

        // getting pulses
        if (event instanceof EvioDataEvent) {

            List<DetectorDataDgtz> dataList = decoder.getDataEntries((EvioDataEvent) event);
            detectorDecoder.translate(dataList);  // to decode the wave
            detectorDecoder.fitPulses(dataList);  // to decode the pedestals as well

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
                int side = counter.getDescriptor().getOrder();
                int pmt = counter.getDescriptor().getComponent();

                H1F ltccPulse = this.getHistogramFromDataDataGroup("ltccPulse_", sector, side, pmt);

                short pulse[] = counter.getADCData(0).getPulseArray();
                int pedestal = counter.getADCData(0).getPedestal();

                // System.out.println(" sector " + sector + "   side " + side + " pmt " + pmt + " pedestal " + pedestal);

                for (int i = 0; i < pulse.length; i++) {
//                    ltccPulse.fill(i, pulse[i]);
                    ltccPulse.fill(i, pulse[i] - pedestal);
                }
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
        this.getCanvas().divide(6, 3);

        // System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        for (int paddle = 1; paddle < 19; paddle++) {
            if (this.getDataGroup().hasItem(sector, side, paddle) == true) {
                this.getCanvas().cd(paddle - 1);
                H1F ltccPulse = this.getHistogramFromDataDataGroup("ltccPulse_", sector, side, paddle);

                H1F normalizedPulse = ltccPulse.histClone("normalized");

                System.out.println(" sector " + sector + "   side " + side + " pmt " + paddle + " entries: " + ltccPulse.getEntries() / 100);

                //   every event gives 100 entries 
//                if (ltccPulse.getEntries() > 0) {
//                    normalizedPulse.divide(ltccPulse.getEntries() / 100);
//                }

                this.getCanvas().draw(normalizedPulse);

            } else {
                System.out.println(" ERROR: can not find the data group for sector " + sector);
            }

        }

    }

}
