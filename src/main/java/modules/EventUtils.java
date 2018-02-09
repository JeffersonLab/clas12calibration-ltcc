package modules;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

public interface EventUtils {

    default public void printEventUtils() {
        System.out.println(" printEventUtils ");

    }

    default public int getRunNumber(DataEvent event, int defaultRun) {
        int rNum = defaultRun;
        DataBank bank = null;
        if (event.hasBank("RUN::config")) {
            bank = event.getBank("RUN::config");

            if (bank != null) {
                rNum = bank.getInt("run", 0);
            }
        }
        return rNum;
    }
}
