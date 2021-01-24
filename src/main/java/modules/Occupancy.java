/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import java.awt.Color;
import view.DetectorShape2D;
import viewer.CalibrationModule;
import viewer.CCDetector;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author burcu
 */
public class Occupancy extends CalibrationModule {

    public Occupancy(CCDetector d, String name) {
        super(d, name, "offset:offset_error:resolution");

    }

    @Override
    public void resetEventListener() {

        nevents = 0;

        for (int iSect : this.getDetector().getSectors()) {
            // initialize data group
            H2F chanADC = new H2F("chanADC_", 200, 10.0, 2010.0, 36, 0.5, 36.5);
            chanADC.setTitleX("Sector" + "_" + iSect + " " + "ADC");
            chanADC.setTitleY("Sector" + "_" + iSect + " " + "channel");

            DataGroup dg = new DataGroup(3, 2);
            dg.addDataSet(chanADC, (iSect - 1));
            this.getDataGroup().add(dg, iSect, 0, 0);
        }
    }

    public int getNEvents(int isec, int order, int icomp) {
        // order indicate left (0) / right (1)
        // 1-18 left side; 19-36 right side
        // in the past it was 1 = 1 left, 2 = 1 right, etc
        int pmtIndex = (order * 18) + icomp;
        return this.getDataGroup().getItem(isec, 0, 0).getH2F("chanADC_").getEntries();
    }

    public int nevents;

    @Override
    public void processEvent(DataEvent event) {
        nevents++;

        if (isCooked) {

            if (event.hasBank("LTCC::adc")) {
                DataBank bank = event.getBank("LTCC::adc");
                int nhits = bank.rows();
                for (int hitNum = 0; hitNum < nhits; hitNum++) {
                    int sector = bank.getByte("sector", hitNum);
                    int order = bank.getByte("order", hitNum);
                    int component = bank.getShort("component", hitNum);
                    int adc = bank.getInt("ADC", hitNum);
                    int pmtIndex = (order * 18) + component;

                    if (adc > 0) {
                        this.getDataGroup().getItem(sector, 0, 0).getH2F("chanADC_").fill(adc, pmtIndex);
                        //  System.out.println("filling : " + sector + " " + order + " " + component);
                    }

                }
            }
        }
    }

    public void analyze() {
        //System.out.println("Analyzing");

        for (int iSect : this.getDetector().getSectors()) {
            // order indicate left (0) / right (1)
            // 1-18 left side; 19-36 right side
            // in the past it was 1 = 1 left, 2 = 1 right, etc
            for (int iOrde = 0; iOrde < 2; iOrde++) {
                for (int iComp = 1; iComp <= this.getSegments(); iComp++) {
                    int pmtIndex = (iOrde * 18) + iComp;
                    H2F chanADC = this.getDataGroup().getItem(iSect, 0, 0).getH2F("chanADC_");
                }
            }
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {

        int[] availableSectors = new int[4];
        availableSectors[0] = 2;
        availableSectors[1] = 3;
        availableSectors[2] = 5;
        availableSectors[3] = 6;

        //plot histos for each sector
        int sector = dsd.getDescriptor().getSector();
        // layer is 1, 2 in the detector shape. It is 0, 1, in the histo order
        int layer = dsd.getDescriptor().getLayer();
        int order = layer - 1;
        int paddle = dsd.getDescriptor().getComponent();
        int pmtIndex = (order * 18) + paddle;
        System.out.println("Selected shape " + sector + " " + pmtIndex);
        IndexedList<DataGroup> group = this.getDataGroup();

        this.getCanvas().divide(2, 2);

        if (group.hasItem(sector, 0, 0) == true) {

            for (int iSect = 0; iSect < 4; iSect++) {
                this.getCanvas().cd(iSect);
                this.getCanvas().getPad(iSect).getAxisZ().setLog(true);
                this.getCanvas().draw(this.getDataGroup().getItem(availableSectors[iSect], 0, 0).getH2F("chanADC_"));
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
        int nent = this.getNEvents(sector, 0, 0);
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
