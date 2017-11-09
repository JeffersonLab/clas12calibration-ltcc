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
            for(int iLay=1; iLay<=this.getLayers(); iLay++) {
                for(int iComp=1; iComp<=this.getSegments(); iComp++) { 
                    // initialize calibration table
                    this.getCalibrationTable().addEntry(iSect, iLay, iComp);
                  
     
                    // initialize data group
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iLay + "_" + iComp, 100, 100.0, 300.0);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("counts");
                    speADC.setTitle("spe ADC Channel (" + iSect + "," + iLay + "," + iComp +")");
                    speADC.setFillColor(3);
                    F1D fADC = new F1D("fADC_"+ iSect + "_" + iLay + "_" + iComp, "[amp]*gaus(x,[mean],[sigma])", 120, 270);
                    fADC.setParameter(0, 0.0);
                    fADC.setParameter(1, 0.0);
                    fADC.setParameter(2, 20.0);
                    fADC.setLineColor(2);
                    fADC.setLineWidth(2);
                    
                    DataGroup dg = new DataGroup(3,2);
                    dg.addDataSet(speADC, (iComp-1)%6);
                    dg.addDataSet(fADC, (iComp-1)%6);
                    this.getDataGroup().add(dg,iSect, iLay, iComp);
                }
            }
        }               
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }
   
    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(isec, ilay, icomp).getH1F("speADC_" + isec + "_" + ilay + "_" + icomp).getEntries();
    }
    public int nevents;
    
    @Override
    public void processEvent(DataEvent event) {
        nevents++;  
        //System.out.println(nevents);
        if(event.hasBank("LTCC::adc")==true){
	    DataBank bank = event.getBank("LTCC::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer",loop);
                int component = bank.getShort("component", loop);
                int adc = bank.getInt("ADC", loop);
               
                if(sector>0 && layer>0  && component>0 && adc>0) {
                this.getDataGroup().getItem(sector,layer,component).getH1F("speADC_" + sector + "_" + layer + "_" + component).fill(adc);
                }
            }
        }
    }
    
    public void analyze() {
//        System.out.println("Analyzing");
        
        for (int iSect : this.getDetector().getSectors()) {
            for(int iLay=1; iLay<=this.getLayers(); iLay++) {
                for(int iComp=1; iComp<=this.getSegments(); iComp++) { 
                    H1F speADC = this.getDataGroup().getItem(iSect,iLay,iComp).getH1F("speADC_" + iSect + "_" + iLay + "_" + iComp);
                    /*F1D fADC = this.getDataGroup().getItem(iSect,iLay,iComp).getF1D("fADC_" + iSect + "_" + iLay + "_" + iComp);
                    this.initADCgaussFitPar(fADC, speADC);
                    DataFitter.fit(fADC, speADC, "LQ");*/
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }
    
        @Override
        public void processShape(DetectorShape2D dsd) {
        //plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = this.getDataGroup();        
        
        if(group.hasItem(sector,layer,paddle)==true){
            if(paddle%6==0){
                this.getCanvas().clear();
            } 
           
            int segN = paddle;
                for(paddle=segN; paddle < segN + 6; paddle++) { 
                    this.getCanvas().divide(3,2);
                    this.getCanvas().cd((paddle-segN)%6);
                    this.getCanvas().draw(this.getDataGroup().getItem(sector,layer,paddle).getH1F("speADC_"+ sector + "_" + layer + "_" + paddle));
                   // this.getCanvas().draw(this.getDataGroup().getItem(sector,layer,paddle).getF1D("fADC_" + sector + "_" + layer + "_" + paddle),"same");
                }
            
            
        } else {
            System.out.println(" ERROR: can not find the data group");
       }
         
   }
        
        /*private void initADCgaussFitPar(F1D fADC, H1F speADC) {
        
        fADC.setRange(120, 270);
        fADC.setParameter(0, 15);
        //fADC.setParLimits(0, -46, 46);
        fADC.setParameter(1, 200);
        //fADC.setParLimits(1, -600, 600);
        fADC.setParameter(2, 20);
        //fADC.setParLimits(2, 0, 200);
    }*/
       
    
    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        int nent = this.getNEvents(sector, layer, key);
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

