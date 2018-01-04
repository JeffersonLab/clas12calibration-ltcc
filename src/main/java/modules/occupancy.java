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
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author burcu
 */
public class occupancy extends CalibrationModule {

    public occupancy(CCDetector d, String name) {
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
                    H2F chanADC = new H2F("chanADC_" + iSect + "_" + iOrde + "_" + iComp, 100, 100.0, 3000.0, 1, 1, 36);
                    chanADC.setTitleX("ADC" + " " + "Sector" + "_" + iSect);
                    chanADC.setTitleY("channel");
                  
                    DataGroup dg = new DataGroup(3, 2);
                    dg.addDataSet(chanADC, (iSect - 1) % 6);
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

    /*public int getNEvents(int isec, int order, int icomp) {
       
            return this.getDataGroup().getItem(isec, order, icomp).getH2F("chanADC_"+ isec + "_" + order + "_" + icomp).getEntries();    
    }*/

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
                int order  = bank.getByte("order", loop);
                int component = bank.getShort("component", loop);
                int adc = bank.getInt("ADC", loop);
                    
                    for(sector : this.getDetector().getSectors()){
                        for(order = 0; order < 2; order++){    
                            for (component = 1; component <= this.getSegments(); component++) {
                                if (sector > 0 && adc > 0) {
                                this.getDataGroup().getItem(sector, order, component).getH2F("chanADC_" + sector + "_" + order + "_" + component).fill(adc, component);
                            }     
                        }
                    }
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

                    H2F chanADC = this.getDataGroup().getItem(iSect, iOrde, iComp).getH2F("chanADC_"+ iSect + "_" + iOrde + "_" + iComp);
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
            if (sector % 6 == 0) {
                this.getCanvas().clear();
            }
            
            for (sector = 1; sector <= 6; sector++) {
                this.getCanvas().divide(3, 2);
                this.getCanvas().cd((sector-1) % 6);
                this.getCanvas().draw(this.getDataGroup().getItem(sector, order, paddle).getH2F("chanADC_" + sector + "_" + order + "_" + paddle));
            }
        } else {
            System.out.println(" ERROR: can not find the data group");
        }

    }

    /*@Override
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
    }*/

    @Override
    public void timerUpdate() {
        analyze();
    }
}
