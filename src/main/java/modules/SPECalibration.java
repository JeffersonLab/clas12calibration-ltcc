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
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.utils.groups.IndexedList;

/**
 * @author burcu
 */
public class SPECalibration extends CalibrationModule {

    public SPECalibration(CCDetector d, String name) {
        super(d, name, "offset:offset_error:resolution");
    }

    // event processed
    public int nEventsProcessed;

    // define histos
    @Override
    public void resetEventListener() {

        nEventsProcessed = 0;

        for (int iSect : this.getDetector().getSectors()) {
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    // initialize calibration table
                    this.getCalibrationTable().addEntry(iSect, iSide, iComp);

                    // initialize data group
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iSide + "_" + iComp, 200, 0.0, 1000.0);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("Counts");
                    String sideString = "Left";
                    if(iSide == 1) sideString = "Right";
                    speADC.setTitle("S" + iSect + " Paddle: " + iComp + " " + sideString);
                    speADC.setFillColor(3);

                    // histos to save/retrieve the fit parameters
                    H1F fitpar = new H1F("fitpar_" + iSect + "_" + iSide + "_" + iComp, 5, 0.0, 5.0);
                    H1F fitparDB = new H1F("fitparDB_" + iSect + "_" + iSide + "_" + iComp, 5, 0.0, 5.0);

                    DataGroup dg = new DataGroup(6, 3);
                    dg.addDataSet(speADC, 0);
                    dg.addDataSet(fitpar, 0);   // added fit parameters histo to the datagroup
                    dg.addDataSet(fitparDB, 0);   // added hist containing fit parameters from DB to the datagroup

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

    // get number of entries for a paddle.
    // why is there a limit of 13?
    private int getNumberOfEntries(int isec, int order, int icomp) {
        return this.getDataGroup().getItem(isec, order, icomp).getH1F("speADC_" + isec + "_" + order + "_" + icomp).getEntries();
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

        // selecting random trigger events
        //       DataBank triggerBank = event.getBank("RUN::config");
        //       long triggerWord = triggerBank.getLong("trigger", 11);
        // System.out.println("trigger word: " + triggerWord);
        // System.out.println(nEventsProcessed);
        if (event.hasBank("LTCC::adc") == true) {
            DataBank bank = event.getBank("LTCC::adc");

            // number of hits in the event
            int nHitsInEvent = bank.rows();
            for (int hitIndex = 0; hitIndex < nHitsInEvent; hitIndex++) {

                int sector = bank.getByte("sector", hitIndex);
                int order = bank.getByte("order", hitIndex);
                int component = bank.getShort("component", hitIndex);

                int adc = bank.getInt("ADC", hitIndex);

                if (sector > 0 && (order == 0 || order == 1) && component > 0 && adc > 0) {
                    this.getHistogramFromDataDataGroup("speADC_", sector, order, component).fill(adc);
                }
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");

        for (int iSect : this.getDetector().getSectors()) {
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    H1F speADC = this.getHistogramFromDataDataGroup("speADC_", iSect, iSide, iComp);

//                    poissonExpo poissonExpoFitF = new poissonExpo("fADC_" + iSect + "_" + iSide + "_" + iComp, 10, 500);
//                    initPoissonExpoPars(poissonExpoFitF, speADC);
// 
                    poissonf poissonfFitF = new poissonf("fADC_" + iSect + "_" + iSide + "_" + iComp, 10, 600);
                    initPoissonfFitPars(poissonfFitF, speADC);
                    DataFitter.fit(poissonfFitF, speADC, "");




                    if (iSect == 3 && iSide == 0 && iComp == 6) {
                        //                      System.out.println("poissonExpoFit Parameter 0: " + fADC.getParameter(0));
                        //                      System.out.println("poissonExpoFit Parameter 1: " + fADC.getParameter(1));
//                        System.out.println("poissonExpoFit Parameter 2: " + fADC.getParameter(2));
//                        System.out.println("poissonExpoFit Parameter 3: " + fADC.getParameter(3));
//                        System.out.println("poissonExpoFit Parameter 4: " + fADC.getParameter(4));
                    }

                    //                  H1F fitpar = this.getDataGroup().getItem(iSect, iSide, iComp).getH1F("fitpar_" + iSect + "_" + iSide + "_" + iComp);
//                    fitpar.setBinContent(1, fADC.getParameter(0));
//                    fitpar.setBinContent(2, fADC.getParameter(1));
//                    fitpar.setBinContent(3, fADC.getParameter(2));
//                    fitpar.setBinContent(4, fADC.getParameter(3));
//                    fitpar.setBinContent(5, fADC.getParameter(4));
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
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
                this.getCanvas().cd(paddle-1);
                H1F speADC = this.getHistogramFromDataDataGroup("speADC_", sector, side, paddle);
                this.getCanvas().draw(speADC);
            } else {
                System.out.println(" ERROR: can not find the data group for sector " + sector);
            }

        }

//            for (paddle = segN; paddle < segN + 6; paddle++) {
//                this.getCanvas().divide(3, 2);
//                this.getCanvas().cd((paddle - segN) % 6);
//                this.getCanvas().draw(this.getDataGroup().getItem(sector, side, paddle).getH1F("speADC_" + sector + "_" + side + "_" + paddle));
//                H1F fitpar = this.getDataGroup().getItem(sector, side, paddle).getH1F("fitpar_" + sector + "_" + side + "_" + paddle);
//
////                System.out.println("poissonExpo Fit Parameter 0: " + fitpar.getBinContent(0));
////                System.out.println("poissonExpo Fit Parameter 1: " + fitpar.getBinContent(1));
////                System.out.println("poissonExpo Fit Parameter 2: " + fitpar.getBinContent(2));
////                System.out.println("poissonExpo Fit Parameter 3: " + fitpar.getBinContent(3));
////                System.out.println("poissonExpo Fit Parameter 4: " + fitpar.getBinContent(4));
//                poissonf fADCp = new poissonf("fADCp_" + sector + "_" + side + "_" + paddle, 100, 2000);
//                //               fADCp.setParameter(0, fitpar.getBinContent(0));
////                fADCp.setParameter(1, fitpar.getBinContent(1));
////                fADCp.setParameter(2, fitpar.getBinContent(2));
////                fADCp.setLineColor(1);
//                fADCp.setLineWidth(2);
////                expo fADCe = new expo("fADCe_" + sector + "_" + order + "_" + paddle, 100, 2000);
////                fADCe.setParameter(1, fitpar.getBinContent(1));
////                fADCe.setParameter(2, fitpar.getBinContent(2));
////                fADCe.setLineColor(2);
////                fADCe.setLineWidth(2);
////                poissonExpo fADC = new poissonExpo("fADC_" + sector + "_" + order + "_" + paddle, 100, 2000);
////                fADC.setParameter(1, fitpar.getBinContent(1));
////                fADC.setParameter(2, fitpar.getBinContent(2));
////                fADC.setParameter(3, fitpar.getBinContent(3));
////                fADC.setParameter(4, fitpar.getBinContent(4));
////                fADC.setParameter(5, fitpar.getBinContent(5));
////                fADC.setLineColor(3);
////                fADC.setLineWidfADCpth(2);
//                /*this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADC_" + sector + "_" + order + "_" + paddle));
//                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADCe_" + sector + "_" + order + "_" + paddle), "same");
//                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADCp_" + sector + "_" + order + "_" + paddle), "same");
//                 */
//
//                //               this.getCanvas().draw(fADCp);
//            }
    

}

@Override
        public Color getColor(DetectorShape2D dsd) {
        
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int order  = layer - 1;
        
        
        int key = dsd.getDescriptor().getComponent();
        
        ColorPalette palette = new ColorPalette();
        
        Color col = new Color(100, 100, 100);
        
        // retrieve number of entries from speADC
        int nent = this.getNumberOfEntries(sector, order, key);
        
        if (nent > 0) {
            col = palette.getColor3D(nent, this.getnProcessed(), true);
        }

        return col;
    }

    @Override
        public void timerUpdate() {
        analyze();
    }
        
    private void initPoissonExpoPars(poissonExpo function, H1F histo) {
        
        double hAmp  = histo.getBinContent(histo.getMaximumBin());
        double hMean = histo.getAxis().getBinCenter(histo.getMaximumBin());
        
        double hRMS = histo.getRMS();
        
        double rangeMin = (hMean - (0.8 * hRMS));
        double rangeMax = (hMean + (0.2 * hRMS));
        function.setRange(rangeMin, rangeMax);
        
        double pm = (hMean * 3.) / 100.0;
        
        function.setParameter(0, hAmp);
        function.setParLimits(0, hAmp * 0.8, hAmp * 1.2);
        
        function.setParameter(1, hMean);
        function.setParLimits(1, hMean - pm, hMean + (pm));
        
        function.setParameter(2, 0.05);
        function.setParLimits(2, 0.001 * hRMS, 0.8 * hRMS);
    }
       
    private void initPoissonfFitPars(poissonf function, H1F histo) {

        double hAmp = histo.getBinContent(histo.getMaximumBin());
        double hMean = histo.getAxis().getBinCenter(histo.getMaximumBin());

        double hRMS = histo.getRMS();

        double rangeMin = (hMean - (0.8 * hRMS));
        double rangeMax = (hMean + (0.2 * hRMS));
        function.setRange(rangeMin, rangeMax);

        double par0Min = hAmp * 0.8;
        double par0Max = hAmp * 1.2;

        function.setParameter(0, hAmp);
        function.setParLimits(0, par0Min, par0Max);

        double pm = (hMean * 3.) / 100.0;
        double par1Min = hMean - pm;
        double par1Max = hMean + pm;
        function.setParameter(1, hMean);
        function.setParLimits(1, par1Min, par1Max);

        double par2Min = 0.01 * hRMS;
        double par2Max = 0.5  * hRMS;
        function.setParameter(2, 20);
        function.setParLimits(2, par2Min, par2Max);

        System.out.println("poissonf parameter 0 " + function.getParameter(0) + " min: " + par0Min + " max: " + par0Max);
        System.out.println("poissonf parameter 1 " + function.getParameter(1) + " min: " + par1Min + " max: " + par1Max);
        System.out.println("poissonf parameter 2 " + function.getParameter(2) + " min: " + par0Min + " max: " + par0Max);

    }
    
}
