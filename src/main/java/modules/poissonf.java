package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;
import org.jlab.groot.math.UserParameter;

public class poissonf extends Func1D {

    public poissonf(String name, double min, double max) {

        super(name, min, max);

        addParameter(new UserParameter("amp", 0, 100, 300000));
        addParameter(new UserParameter("mean", 200, 50, 600));
        addParameter(new UserParameter("sigma", 10, 20, 400));
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

        return poissonValue;
    }
}
