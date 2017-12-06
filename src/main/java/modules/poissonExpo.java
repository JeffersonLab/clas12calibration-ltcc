package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;

public class poissonExpo extends Func1D{

	public poissonExpo(String name, double min, double max) {
		super(name, min, max);
	}
	
	
	public double poissonff(double x, double par[]){
	double arg = 0.0;
        double arg2 = 0.0;
            if (par[2] != 0) arg = x/par[2];
            if (gamma(arg+1) != 0) arg2 = Math.pow(par[1], arg) /gamma(arg+1);
       return par[0]*(arg2)*Math.exp(-par[1]) + Math.exp(x*par[3]+par[4]);
	}
}
