package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;
import org.jlab.groot.math.UserParameter;

public class expo extends Func1D {

    public expo(String name, double min, double max) {
        
        super(name, min, max);

        addParameter(new UserParameter("amp",     0,   0, 300000 ));
        addParameter(new UserParameter("mean",  0.1, 0.1,    0.3 ));

    }

    public double evaluate(double x) {

        double xshift = 50;

        double par0 = this.getParameter(0);
        double par1 = this.getParameter(1);
        return par0*Math.exp( -par1 * ( x - xshift));

    }
}
