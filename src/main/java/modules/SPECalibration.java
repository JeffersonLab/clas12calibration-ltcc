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
//                    System.out.println(iSect + "_" + iLay + "_" + iComp);
                    // initialize calibration table
                    this.getCalibrationTable().addEntry(iSect, iLay, iComp);
                    //getCalibrationTable().setDoubleValue(0., "offset", iSect, iLay, iComp);
                    //getCalibrationTable().setDoubleValue(0., "offset_error", iSect, iLay, iComp);
                    //getCalibrationTable().setDoubleValue(1., "resolution", iSect, iLay, iComp);
     
                    // initialize data group
                    
                    H1F speADC = new H1F("speADC_" + iSect + "_" + iLay + "_" + iComp, 100, 0.0, 5000);
                    speADC.setTitleX("ADC");
                    speADC.setTitleY("counts");
                    speADC.setTitle("spe ADC Channel (" + iSect + "," + iLay + "," + iComp +")");
                    speADC.setFillColor(3);
                    
             
                    
         
                    DataGroup dg = new DataGroup(2, 2);
                    dg.addDataSet(speADC, 0);
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
   

    public int nevents;
    
    @Override
    public void processEvent(DataEvent event) {
        nevents++;  
        
        if(event.hasBank("LTCC::adc")==true){
	    DataBank bank = event.getBank("LTCC::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer",loop);
                int component = bank.getShort("component", loop);
                int adc = bank.getInt("ADC", loop);
                float time = bank.getFloat("time", loop); //ns
                if(sector>0 && layer>0 && component>0) {
                this.getDataGroup().getItem(sector, layer, component).getH1F("speADC_" + sector + "_" + layer + "_" + component).fill(adc);
                }
            }
        }
    }
    

    public void analyze() {
//        System.out.println("Analyzing");
        
        for (int iSect : this.getDetector().getSectors()) {
            for(int iLay=1; iLay<=this.getLayers(); iLay++) {
                for(int iComp=1; iComp<=this.getSegments(); iComp++) { 
                    H1F speADC = this.getDataGroup().getItem(iSect, iLay, iComp).getH1F("speADC_" + iSect + "_" + iLay + "_" + iComp);
                    H1F speADC1 = this.getDataGroup().getItem(iSect, iLay, iComp).getH1F("speADC_" + iSect + "_" + iLay + "_" + iComp);
                    H1F speADC2 = this.getDataGroup().getItem(iSect, iLay, iComp).getH1F("speADC_" + iSect + "_" + iLay + "_" + iComp);          
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }
}

