package modules;

import java.util.HashMap;
import java.util.Map;

public class CalibrationData {
    
    Map<Integer, CalibrationRun> calibrationRuns = new HashMap<>();

    public void addRun(int runNo) {
        calibrationRuns.put(runNo, new CalibrationRun(runNo));
    }

    
    
}