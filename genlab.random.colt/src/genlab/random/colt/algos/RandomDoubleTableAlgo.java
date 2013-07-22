package genlab.random.colt.algos;

import genlab.core.exec.IExecution;
import genlab.core.model.exec.IAlgoExecution;
import genlab.core.model.instance.AlgoInstance;
import genlab.core.model.meta.basics.algos.AbstractTableGenerator;
import genlab.core.parameters.DoubleParameter;

public class RandomDoubleTableAlgo extends AbstractTableGenerator {

	public static final DoubleParameter PARAMETER_MIN = new DoubleParameter("min", "min", "minimum value", new Double(0.0));
	
	public static final DoubleParameter PARAMETER_MAX = new DoubleParameter("max", "max", "maximum value", new Double(1.0));
	
	public RandomDoubleTableAlgo() {
		super(
				"random float table (colt)", 
				"generates a table filled with random float generated by the CERN/Colt library"
				);
		
		registerParameter(PARAMETER_MIN);
		registerParameter(PARAMETER_MAX);
	}

	@Override
	public IAlgoExecution createExec(IExecution execution,
			AlgoInstance algoInstance) {
		return new RandomDoubleTableAlgoExec(execution, algoInstance);
	}

}