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
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.utils.groups.IndexedList;
import java.util.Scanner;

/**
 *
 * @author burcu
 */
public class SPECalibration extends CalibrationModule {

    public SPECalibration(CCDetector d, String name) {
        super(d, name, "offset:offset_error:resolution");

    }

    @Override
    public void resetEventListener() {

        nevents = 0;

        for (int iSect : this.getDetector().getSectors()) {
            // order indicate left (0) / right (1)
            for (int iOrde = 0; iOrde < 2; iOrde++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {
                    // initialize calibration table
                    this.getCalibrationTable().addEntry(iSect, iOrde, iComp);
                    
                    // initialize data group
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iOrde + "_" + iComp, 100, 100.0, 3000.0);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("counts");
                    speADC.setTitle("spe ADC Channel (" + iSect + "," + iOrde + "," + iComp + ")");
                    speADC.setFillColor(3);
		    // Exponential fit function with 2 parameters
                    F1D fADCe = new F1D("fADCe_" + iSect + "_" + iOrde + "_" + iComp, "exp(x,par[0],par[1])", 100, 3000);;
                    fADCe.setParameter(1, 100.0);
                    fADCe.setParameter(2, 0.1);
                    fADCe.setLineColor(2);
                    fADCe.setLineWidth(2);
		    
                    
                    //saving the fit parameter in histogram bins
                    H1F fitpar = new H1F("fitpar_" + iSect + "_" + iOrde + "_" + iComp, 5, 0.0, 5.0);
                    //fit parameters from DB
                    H1F fitparDB = new H1F("fitparDB_" + iSect + "_" + iOrde + "_" + iComp, 5, 0.0, 5.0);
                   
                    DataGroup dg = new DataGroup(3, 2);
                    dg.addDataSet(speADC, (iComp - 1) % 6);
                    dg.addDataSet(fADCe, (iComp - 1) % 6); //added expo func to do
                    dg.addDataSet(fitpar, (iComp - 1) % 6);//added fit parameters histo to the datagroup
                    dg.addDataSet(fitparDB, (iComp - 1) % 6);//added histo. of the fit parameters from DB to the datagroup
                    this.getDataGroup().add(dg, iSect, iOrde, iComp);
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int order, int icomp) {
        if (icomp < 14) {
            return this.getDataGroup().getItem(isec, order, icomp).getH1F("speADC_" + isec + "_" + order + "_" + icomp).getEntries();
        } else {
            int maxComponent = 13;
            return this.getDataGroup().getItem(isec, order, maxComponent).getH1F("speADC_" + isec + "_" + order + "_" + maxComponent).getEntries();
        }

    }
    public int nevents;

    @Override
    public void processEvent(DataEvent event) {
        nevents++;
        //System.out.println(nevents);
        if (event.hasBank("LTCC::adc") == true) {
            DataBank bank = event.getBank("LTCC::adc");
            int rows = bank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int sector = bank.getByte("sector", loop);
                int order = bank.getByte("order", loop);
                int component = bank.getShort("component", loop);
                int adc = bank.getInt("ADC", loop);

                if (sector > 0 && (order == 0 || order == 1) && component > 0 && adc > 0) {
                    this.getDataGroup().getItem(sector, order, component).getH1F("speADC_" + sector + "_" + order + "_" + component).fill(adc);
                }
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");

        for (int iSect : this.getDetector().getSectors()) {
            for (int iOrde = 0; iOrde < 1; iOrde++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {
                    H1F speADC = this.getDataGroup().getItem(iSect, iOrde, iComp).getH1F("speADC_" + iSect + "_" + iOrde + "_" + iComp);
                    // Poissonian fit function defined in poissonf.java with 3 parameters
	   	    poissonf fADCp = new poissonf("fADCp_" + iSect + "_" + iOrde + "_" + iComp, 100, 2000);
		    fADCp.setParameter(1, 1000.0);
		    fADCp.setParameter(2, 4.0);
		    fADCp.setParameter(3, 40.0);
                    fADCp.setLineColor(1);
                    fADCp.setLineWidth(2);
                    poissonExpo fADC = new poissonExpo("fADC_" + iSect + "_" + iOrde + "_" + iComp, 100, 3000);
                    fADC.setParameter(1, 1000.0);
		    fADC.setParameter(2, 4.0);
		    fADC.setParameter(3, 40.0);
		    fADC.setParameter(4, 100.0);
		    fADC.setParameter(5, 0.1);
                    fADC.setLineColor(1);
                    fADC.setLineWidth(2);
                    DataFitter.fit(fADC, speADC, "LQ");
                    
                    fitpar.setBinContent(1, fADC.getParameter(1));
                    fitpar.setBinContent(2, fADC.getParameter(2));
                    fitpar.setBinContent(3, fADC.getParameter(3));
                    fitpar.setBinContent(4, fADC.getParameter(4));
                    fitpar.setBinContent(5, fADC.getParameter(5));
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
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = this.getDataGroup();

        if (group.hasItem(sector, order, paddle) == true) {
            if (paddle % 6 == 0) {
                this.getCanvas().clear();
            }

            int segN = paddle;
            if(segN > 13) segN = 13;
            for (paddle = segN; paddle < segN + 6; paddle++) {
                this.getCanvas().divide(3, 2);
                this.getCanvas().cd((paddle - segN) % 6);
                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getH1F("speADC_" + sector + "_" + order + "_" + paddle));
                this.getCanvas().draw(this.getDataGroup().getItem(sector,order,paddle).getF1D("fADCe_" + sector + "_" + order + "_" + paddle),"same");
		this.getCanvas().draw(this.getDataGroup().getItem(sector,order,paddle).getF1D("fADCp_" + sector + "_" + order + "_" + paddle),"same");
		this.getCanvas().draw(this.getDataGroup().getItem(sector,order,paddle).getF1D("fADC_" + sector + "_" + order + "_" + paddle),"same");
            }

        } else {
            System.out.println(" ERROR: can not find the data group");
        }

    }
    
    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int order = layer - 1;
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        int nent = this.getNEvents(sector, order, key);
        if (nent > 0) {
            col = palette.getColor3D(nent, this.getnProcessed(), true);
        }

//        col = new Color(100, 0, 0);
        return col;
    }

    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
