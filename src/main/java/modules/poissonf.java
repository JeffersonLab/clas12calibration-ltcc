package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;
import org.jlab.groot.math.UserParameter;

public class poissonf extends Func1D {

    public poissonf(String name, double min, double max) {

        super(name, min, max);

        addParameter(new UserParameter("amp",     0, 100, 300000 ));
        addParameter(new UserParameter("mean",  200,  50,    600 ));
        addParameter(new UserParameter("sigma",  20,  10,    400 ));
    }

    public double evaluate(double x) {

        double c1 = 0.0;
        double c2 = 0.0;

        double par0 = this.getParameter(0);
        double par1 = this.getParameter(1);
        double par2 = this.getParameter(2);

        if (par2 != 0) {
            c1 = x / par2;
        }

        double denom = gamma(c1 + 1);

        if (denom != 0) {
            c2 = Math.pow(par1, c1) / denom;
        }

        double poissonValue = par0 * c2 * Math.exp(-par1);

        return poissonValue;
    }
}
