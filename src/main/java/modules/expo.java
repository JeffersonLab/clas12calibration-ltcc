package modules;

import org.jlab.groot.math.Func1D;
import static org.apache.commons.math3.special.Gamma.gamma;

public class expo extends Func1D{

	public expo(String name, double min, double max) {
		super(name, min, max);
	}
	
	
	public double expof(double x, double par[]){
	
       return Math.exp(x*par[3]+par[4]);
	}
}
