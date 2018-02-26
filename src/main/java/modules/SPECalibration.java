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

// for mode help on groot:
// https://github.com/gavalian/groot
/**
 * @author ungaro
 */
public class SPECalibration extends CalibrationModule {

    public SPECalibration(CCDetector d, String name) {
        super(d, name, "mean:mean_e:sigma:sigma_e");
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
                    // if (iSect != 4) {
                    getCalibrationTable().addEntry(iSect, iSide, iComp);
                    getCalibrationTable().setDoubleValue(200.0, "mean", iSect, iSide, iComp);
                    getCalibrationTable().setDoubleValue(10.0, "mean_e", iSect, iSide, iComp);
                    getCalibrationTable().setDoubleValue(20.0, "sigma", iSect, iSide, iComp);
                    getCalibrationTable().setDoubleValue(2.0, "sigma_e", iSect, iSide, iComp);
                    // }
                    // initialize data group
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iSide + "_" + iComp, 200, 0.0, 1000.0);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("Counts");

                    String sideString = "Left";
                    if (iSide == 1) {
                        sideString = "Right";
                    }
                    speADC.setTitle("S" + iSect + " Paddle: " + iComp + " " + sideString);

                    // histos to save/retrieve the fit parameters
                    H1F fitpars = new H1F("fitpars_" + iSect + "_" + iSide + "_" + iComp, 5, 0.0, 5.0);
                    H1F fitparsDB = new H1F("fitparsDB_" + iSect + "_" + iSide + "_" + iComp, 5, 0.0, 5.0);

                    // gaussian fit function
                    F1D gaussianFit = new F1D("gaussianFit" + iSect + "_" + iSide + "_" + iComp, "[amp]*gaus(x,[mean],[sigma])", 50, 800);
//                    F1D gaussianFit = new F1D("gaussianFit" + iSect + "_" + iSide + "_" + iComp, "[amp]*gaus(x,[mean],[sigma]) + [e0]*exp(-x*[e1])", 50, 600);
                    gaussianFit.setLineColor(3);
                    gaussianFit.setLineWidth(2);

                    DataGroup dg = new DataGroup(6, 3);
                    dg.addDataSet(speADC, 0);
                    dg.addDataSet(fitpars, 0);   // added fit parameters histo to the datagroup
                    dg.addDataSet(fitparsDB, 0);   // added hist containing fit parameters from DB to the datagroup
                    dg.addDataSet(gaussianFit, 0);   // added gaussian fit function

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
        if (isCooked) {

            if (event.hasBank("LTCC::adc")) {
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
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");

        for (int iSect : this.getDetector().getSectors()) {
            for (int iSide = 0; iSide < 2; iSide++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    H1F speADC = this.getHistogramFromDataDataGroup("speADC_", iSect, iSide, iComp);

                    F1D gaussianFit = this.getDataGroup().getItem(iSect, iSide, iComp).getF1D("gaussianFit" + iSect + "_" + iSide + "_" + iComp);
                    this.initTimeGaussFitPar(gaussianFit, speADC);

                    // only fit if there are 100 events or more
                    int minEntries = 0;
                    for (int b = 0; b < 50; b++) {
                        minEntries += speADC.getBinContent(b);
                    }

                    System.out.println(minEntries);

                    if (minEntries > 100) {
                        DataFitter.fit(gaussianFit, speADC, "LQ");
                        updateCalibration(iSect, iSide, iComp);
                    }

//                    poissonExpo poissonExpoFitF = new poissonExpo("fADC_" + iSect + "_" + iSide + "_" + iComp, 10, 500);
//                    initPoissonExpoPars(poissonExpoFitF, speADC);
// 
//                    poissonf poissonfFitF = new poissonf("poissonfFitF_" + iSect + "_" + iSide + "_" + iComp, 10, 600);
//                    initPoissonfFitPars(poissonfFitF, speADC);
//                    DataFitter.fit(poissonfFitF, speADC, "");
                    // saving pars to histo
//                    H1F fitParHisto = this.getHistogramFromDataDataGroup("fitpars_", iSect, iSide, iComp);
//                    fitParHisto.setBinContent(1, poissonfFitF.getParameter(0));
//                    fitParHisto.setBinContent(2, poissonfFitF.getParameter(1));
//                    fitParHisto.setBinContent(3, poissonfFitF.getParameter(2));
//
//                    System.out.println("poissonExpo Fit Parameter 0: " + poissonfFitF.getParameter(0));
//                    System.out.println("poissonExpo Fit Parameter 1: " + poissonfFitF.getParameter(1));
//                    System.out.println("poissonExpo Fit Parameter 2: " + poissonfFitF.getParameter(2));
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }

    // decide what's get plotted in the canvas at the button click
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
        analyze();
    }

    @Override
    public Color getColor(DetectorShape2D dsd) {

        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int order = layer - 1;

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

        double hAmp = histo.getBinContent(histo.getMaximumBin());
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
        // function.setRange(rangeMin, rangeMax);

        double par0Min = hAmp * 0.5;
        double par0Max = hAmp * 2.0;

        function.setParameter(0, hAmp);
        function.setParLimits(0, par0Min, par0Max);

        double pm = (hMean * 0.5);
        double par1Min = hMean - pm;
        double par1Max = hMean + pm;
        function.setParameter(1, hMean);
        function.setParLimits(1, par1Min, par1Max);

        double par2Min = 0.01 * hRMS;
        double par2Max = 2 * hRMS;
        function.setParameter(2, 20);
        function.setParLimits(2, par2Min, par2Max);

        System.out.println("poissonf parameter 0 " + function.getParameter(0) + " min: " + par0Min + " max: " + par0Max);
        System.out.println("poissonf parameter 1 " + function.getParameter(1) + " min: " + par1Min + " max: " + par1Max);
        System.out.println("poissonf parameter 2 " + function.getParameter(2) + " min: " + par2Min + " max: " + par2Max);

    }

    private void initTimeGaussFitPar(F1D function, H1F histo) {

        double hAmp = histo.getBinContent(histo.getMaximumBin());
        double hMean = histo.getAxis().getBinCenter(histo.getMaximumBin());

        double hRMS = histo.getRMS();

        double rangeMin = (hMean - (0.8 * hRMS));
        double rangeMax = (hMean + (2.0 * hRMS));
        //  function.setRange(rangeMin, rangeMax);

        double par0Min = hAmp * 0.5;
        double par0Max = hAmp * 2.0;
        function.setParameter(0, hAmp);
        function.setParLimits(0, par0Min, par0Max);

        double pm = (hMean * 0.8);
        double par1Min = hMean - pm;
        double par1Max = hMean + pm;
        function.setParameter(1, hMean);
        function.setParLimits(1, par1Min, par1Max);

        double par2Min = 0.1 * hRMS;
        double par2Max = 4 * hRMS;
        function.setParameter(2, 20);
        function.setParLimits(2, par2Min, par2Max);

//        System.out.println("gauss parameter 0 " + function.getParameter(0) + " min: " + par0Min + " max: " + par0Max);
//        System.out.println("gauss parameter 1 " + function.getParameter(1) + " min: " + par1Min + " max: " + par1Max);
//        System.out.println("gauss parameter 2 " + function.getParameter(2) + " min: " + par2Min + " max: " + par2Max);
    }

    private void updateCalibration(int sector, int side, int paddle) {

        if (sector != 1 && sector != 4) {
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

//       @Override
//    public void  saveConstants(String filename) {
//        
//    }
}
