/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import view.DetectorShape2D;
import viewer.CalibrationModule;
import viewer.CCDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.utils.groups.IndexedList;


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
            spePositions.setTitleX("Crystal ID"); // X axis title
            spePositions.setTitleY("Timing (ns)");   // Y axis title
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
        this.getCanvas().divide(6, 3);

        // System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        for (int paddle = 1; paddle < 19; paddle++) {
            if (this.getDataGroup().hasItem(sector, side, paddle) == true) {
                this.getCanvas().cd(paddle - 1);
                H1F speADC = this.getHistogramFromDataDataGroup("speADC_", sector, side, paddle);
                this.getCanvas().draw(speADC);

                // not working yet
//                H1F fitParHisto = this.getHistogramFromDataDataGroup("fitpars_", sector, side, paddle);
//                System.out.println(" Fit Parameter 0: " + fitParHisto.getBinContent(0));
//                System.out.println(" Fit Parameter 1: " + fitParHisto.getBinContent(1));
//                System.out.println(" Fit Parameter 2: " + fitParHisto.getBinContent(2));
//
//                poissonf poissonfFitF = new poissonf("poissonfFitF_" + sector + "_" + side + "_" + paddle, 10, 600);
//                poissonfFitF.setParameter(0, fitParHisto.getBinContent(0));
//                poissonfFitF.setParameter(1, fitParHisto.getBinContent(1));
//                poissonfFitF.setParameter(2, fitParHisto.getBinContent(2));
//                this.getCanvas().draw(poissonfFitF, "same");
            } else {
                System.out.println(" ERROR: can not find the data group for sector " + sector);
            }

        }

    }



    @Override
    public void timerUpdate() {
        analyze();
    }


    private void updateCalibration(int sector, int side, int paddle) {

        F1D gaussianFit = this.getDataGroup().getItem(sector, side, paddle).getF1D("gaussianFit" + sector + "_" + side + "_" + paddle);

        double mean = gaussianFit.parameter(1).value();
        double mean_e = gaussianFit.parameter(1).error();
        double sigma = gaussianFit.parameter(2).value();
        double sigma_e = gaussianFit.parameter(2).error();

        getCalibrationTable().setDoubleValue(mean, "mean", sector, side, paddle);
        getCalibrationTable().setDoubleValue(mean_e, "mean_e", sector, side, paddle);
        getCalibrationTable().setDoubleValue(sigma, "sigma", sector, side, paddle);
        getCalibrationTable().setDoubleValue(sigma_e, "sigma_e", sector, side, paddle);

    }
}
