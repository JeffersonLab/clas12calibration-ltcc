package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;

public class poissonExpo extends Func1D {

    public poissonExpo(String name, double min, double max) {
        super(name, min, max);
        addParameter("amp");
        addParameter("mean");
        addParameter("sigma");
        addParameter("f1");
        addParameter("f2");
    }

    public double poissonff(double x, double par[]) {
        
        double arg = 0.0;

        if (par[2] != 0) {
            arg = x / par[2];
        }

        double arg2 = 0.0;
        double denom = gamma(arg + 1);

        if (denom != 0) {
            arg2 = Math.pow(par[1], arg) / denom;
        }

        double poissonValue = par[0] * arg2 * Math.exp(-par[1]);
        double exponValue = par[3] * Math.exp( -par[4] * (x - 50));

        return poissonValue + exponValue;
    }
}
