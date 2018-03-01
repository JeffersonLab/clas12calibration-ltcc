/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import view.DetectorShape2D;
import viewer.CalibrationModule;

// for mode help on groot:
// https://github.com/gavalian/groot
/**
 * @author ungaro
 */
public class SPEModule extends CalibrationModule {

    public SPEModule(CCDetector d, String name) {
        super(d, name, "mean:mean_e:sigma:sigma_e");
    }

    CalibrationData calibData;

    // define histos
    @Override
    public void resetEventListener() {
        calibData = new CalibrationData();
        calibData.addRun(10);
    }

    @Override
    public void analyze() {

    }

    // decide what's get plotted in the canvas at the button click
    @Override
    public void processShape(DetectorShape2D dsd) {

    }

    @Override
    public void timerUpdate() {
        analyze();
    }

}
