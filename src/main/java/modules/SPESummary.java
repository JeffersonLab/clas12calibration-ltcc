/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import java.util.Arrays;
import java.util.List;
import view.DetectorShape2D;
import viewer.CalibrationModule;
import viewer.CCDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.group.DataGroup;

// for mode help on groot:
// https://github.com/gavalian/groot
/**
 * @author burcu
 */
public class SPESummary extends CalibrationModule {

    public SPESummary(CCDetector d, String name) {
        super(d, name, "mean:mean_e:sigma:sigma_e");
    }

    // event processed
    public int nEventsProcessed;

    // define histos
    @Override
    public void resetEventListener() {

        nEventsProcessed = 0;

        for (int iSect : this.getDetector().getSectors()) {

            GraphErrors spePositions = new GraphErrors("spePositions" + iSect);
            spePositions.setTitle("SPE Positions Sector " + iSect); //  title
            spePositions.setTitleX("PMT"); // X axis title
            spePositions.setTitleY("SPE Position");   // Y axis title
            spePositions.setMarkerColor(5); // color from 0-9 for given palette
            spePositions.setMarkerSize(5);  // size in points on the screen

            DataGroup dg = new DataGroup(2, 2);
            dg.addDataSet(spePositions, 0);
            this.getDataGroup().add(dg, iSect, 0, 0);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    // what does this do?
    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    private GraphErrors getGraphFromDataDataGroup(String histoName, int sector) {
        // int[] index = new int[3]; << this is how a new array is defined in java
        // for(int i = 0; i < index.length; i++) System.out.println( index[i]);
        return this.getDataGroup().getItem(sector, 0, 0).getGraph(histoName + sector);
    }

    // decide what's get plotted in the canvas
    @Override
    public void processShape(DetectorShape2D dsd) {

        // plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        // layer is 1, 2 in the detector shape. It is 0, 1, in the histo (order
        int side = dsd.getDescriptor().getLayer() - 1;

    }

    @Override
    public void timerUpdate() {
        
        // make this in LTCC Module?
        int[] availableSectors = new int[4];
        availableSectors[0] = 2;
        availableSectors[1] = 3;
        availableSectors[2] = 5;
        availableSectors[3] = 6;

        this.getCanvas().divide(2, 2);

        for (int iSect = 0; iSect < 4; iSect++) {
            GraphErrors spePos = getGraphFromDataDataGroup("spePositions", availableSectors[iSect]);
            spePos.reset();
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int pmt = 1; pmt < 19; pmt++) {
                    int iPmt = pmt + iSide * 18;
                    spePos.addPoint(iPmt, getCalibrationTable().getDoubleValue("mean", availableSectors[iSect], iSide, pmt),
                            0, getCalibrationTable().getDoubleValue("mean_e", availableSectors[iSect], iSide, pmt));
                }
            }
            this.getCanvas().cd(iSect);
            this.getCanvas().draw(spePos);

        }

    }

}
