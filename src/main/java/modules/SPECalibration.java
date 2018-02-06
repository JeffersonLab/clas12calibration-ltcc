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
            for (int iOrde = 0; iOrde < 2; iOrde++) { // order indicate left (0) / right (1)
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {
                    
                    // initialize calibration table
                    this.getCalibrationTable().addEntry(iSect, iOrde, iComp);

                    // initialize data group
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iOrde + "_" + iComp, 200, 0.0, 1000.0);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("Counts");
                    speADC.setTitle("Spe ADC Channel (" + iSect + "," + iOrde + "," + iComp + ")");
                    speADC.setFillColor(3);

                    // histos to save/retrieve the fit parameters
                    H1F fitpar   = new H1F("fitpar_"   + iSect + "_" + iOrde + "_" + iComp, 5, 0.0, 5.0);
                    H1F fitparDB = new H1F("fitparDB_" + iSect + "_" + iOrde + "_" + iComp, 5, 0.0, 5.0);

                    DataGroup dg = new DataGroup(3, 2);
                    dg.addDataSet(speADC,   (iComp - 1) % 6);
                    dg.addDataSet(fitpar,   (iComp - 1) % 6);   // added fit parameters histo to the datagroup
                    dg.addDataSet(fitparDB, (iComp - 1) % 6);   // added hist containing fit parameters from DB to the datagroup
                    
                    this.getDataGroup().add(dg, iSect, iOrde, iComp);
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
    private H1F getHistogramFromDataDataGroup(String histoName, int... index){
        // for(int i = 0; i < index.length; i++) System.out.println( index[i]);
        return this.getDataGroup().getItem(index[0], index[1], index[2]).getH1F(histoName + index[0] + "_" + index[1] + "_" + index[2]);   
    }
    
   
    
    @Override
    public void processEvent(DataEvent event) {
        nEventsProcessed++;

        // selecting random trigger events
        DataBank triggerBank = event.getBank("RUN::config");
        long triggerWord = triggerBank.getLong("trigger", 0);
        // System.out.println("trigger word: " + triggerWord);

        // System.out.println(nEventsProcessed);
        if (event.hasBank("LTCC::adc") == true) {
            DataBank bank = event.getBank("LTCC::adc");
            
            // number of hits in the event
            int nHitsInEvent = bank.rows();
            int[] index = new int[3];
            for (int hitIndex = 0; hitIndex < nHitsInEvent; hitIndex++) {

                int sector    = bank.getByte("sector", hitIndex);
                int order     = bank.getByte("order", hitIndex);
                int component = bank.getShort("component", hitIndex);
                              
                int adc = bank.getInt("ADC", hitIndex);

                if (sector > 0 && (order == 0 || order == 1) && component > 0 && adc > 0) {
                    this.getHistogramFromDataDataGroup("speADC_", sector, order, component).fill(adc);
                }
            }
        }
    }

    public void analyze() {
        System.out.println("Analyzing");

        for (int iSect : this.getDetector().getSectors()) {
            // order indicate left (0) / right (1)
            for (int iOrde = 0; iOrde < 2; iOrde++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {

                    H1F speADC = this.getDataGroup().getItem(iSect, iOrde, iComp).getH1F("speADC_" + iSect + "_" + iOrde + "_" + iComp);

                    // poissonExpo fADC = new poissonExpo("fADC_" + iSect + "_" + iOrde + "_" + iComp, 10, 500);
                    // poissonf fADC = new poissonf("fADC_" + iSect + "_" + iOrde + "_" + iComp, 10, 500);
                    expo fADC = new expo("expo", 10, 500);

//                    fADC.setParameter(0, 100.0);
//                    fADC.setParameter(1, 4.0);
//                    fADC.setParameter(2, 0.2);
////                    fADC.setParameter(3, 10.0);
//                    fADC.setParameter(4, 100);
                  //  DataFitter.fit(fADC, speADC, "");

                    if (iSect == 3 && iOrde == 0 && iComp == 6) {
                        System.out.println("poissonExpoFit Parameter 0: " + fADC.getParameter(0));
                        System.out.println("poissonExpoFit Parameter 1: " + fADC.getParameter(1));
//                        System.out.println("poissonExpoFit Parameter 2: " + fADC.getParameter(2));
//                        System.out.println("poissonExpoFit Parameter 3: " + fADC.getParameter(3));
//                        System.out.println("poissonExpoFit Parameter 4: " + fADC.getParameter(4));
                    }

                    H1F fitpar = this.getDataGroup().getItem(iSect, iOrde, iComp).getH1F("fitpar_" + iSect + "_" + iOrde + "_" + iComp);

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

    @Override
    public void processShape(DetectorShape2D dsd) {

        //plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        // layer is 1, 2 in the detector shape. It is 0, 1, in the histo (order
        int layer = dsd.getDescriptor().getLayer();
        int order = layer - 1;
        int paddle = dsd.getDescriptor().getComponent();
        
        // System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        
        if (this.getDataGroup().hasItem(sector, order, paddle) == true) {
            
    
            // do not redraw if one selected padde 13 and aboce
            int segN = paddle;
            if (segN > 13) {
                segN = 13;
            }

            for (paddle = segN; paddle < segN + 6; paddle++) {
                this.getCanvas().divide(3, 2);
                this.getCanvas().cd((paddle - segN) % 6);
                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getH1F("speADC_" + sector + "_" + order + "_" + paddle));
                H1F fitpar = this.getDataGroup().getItem(sector, order, paddle).getH1F("fitpar_" + sector + "_" + order + "_" + paddle);

//                System.out.println("poissonExpo Fit Parameter 0: " + fitpar.getBinContent(0));
//                System.out.println("poissonExpo Fit Parameter 1: " + fitpar.getBinContent(1));
//                System.out.println("poissonExpo Fit Parameter 2: " + fitpar.getBinContent(2));
//                System.out.println("poissonExpo Fit Parameter 3: " + fitpar.getBinContent(3));
//                System.out.println("poissonExpo Fit Parameter 4: " + fitpar.getBinContent(4));
                poissonf fADCp = new poissonf("fADCp_" + sector + "_" + order + "_" + paddle, 100, 2000);
                //               fADCp.setParameter(0, fitpar.getBinContent(0));
//                fADCp.setParameter(1, fitpar.getBinContent(1));
//                fADCp.setParameter(2, fitpar.getBinContent(2));
//                fADCp.setLineColor(1);
                fADCp.setLineWidth(2);
//                expo fADCe = new expo("fADCe_" + sector + "_" + order + "_" + paddle, 100, 2000);
//                fADCe.setParameter(1, fitpar.getBinContent(1));
//                fADCe.setParameter(2, fitpar.getBinContent(2));
//                fADCe.setLineColor(2);
//                fADCe.setLineWidth(2);
//                poissonExpo fADC = new poissonExpo("fADC_" + sector + "_" + order + "_" + paddle, 100, 2000);
//                fADC.setParameter(1, fitpar.getBinContent(1));
//                fADC.setParameter(2, fitpar.getBinContent(2));
//                fADC.setParameter(3, fitpar.getBinContent(3));
//                fADC.setParameter(4, fitpar.getBinContent(4));
//                fADC.setParameter(5, fitpar.getBinContent(5));
//                fADC.setLineColor(3);
//                fADC.setLineWidfADCpth(2);
                /*this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADC_" + sector + "_" + order + "_" + paddle));
                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADCe_" + sector + "_" + order + "_" + paddle), "same");
                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getF1D("fADCp_" + sector + "_" + order + "_" + paddle), "same");
                 */

                //               this.getCanvas().draw(fADCp);
            }

        } else {
            System.out.println(" ERROR: can not find the data group");
        }

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
}
