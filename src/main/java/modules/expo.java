package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;
import org.jlab.groot.math.UserParameter;

public class expo extends Func1D {

    public expo(String name, double min, double max) {
        
        super(name, min, max);

        addParameter(new UserParameter("amp", 0, 0, 300000));
        addParameter(new UserParameter("mean", 0.1, 0.1, 0.3));

    }

    public double evaluate(double x, double par[]) {

        return par[0]*Math.exp( -par[1] * ( x - 50));

    }
}
