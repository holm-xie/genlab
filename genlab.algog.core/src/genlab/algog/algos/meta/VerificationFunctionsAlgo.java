package genlab.algog.algos.meta;

import genlab.core.commons.ProgramException;
import genlab.core.exec.IExecution;
import genlab.core.model.exec.AbstractAlgoExecutionOneshot;
import genlab.core.model.exec.ComputationProgressWithSteps;
import genlab.core.model.exec.ComputationResult;
import genlab.core.model.exec.ComputationState;
import genlab.core.model.exec.IAlgoExecution;
import genlab.core.model.instance.AlgoInstance;
import genlab.core.model.meta.basics.flowtypes.DoubleInOut;
import genlab.core.parameters.ListParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VerificationFunctionsAlgo extends AbstractGeneticAlgo {
	
	// inputs: x,y
	public static final DoubleInOut INPUT_X = new DoubleInOut(
			"in_x", 
			"x",
			"parameter x",
			1.0
			);
	
	public static final DoubleInOut INPUT_Y = new DoubleInOut(
			"in_y", 
			"y",
			"parameter y",
			2.0
			);
	
	// outputs: f1, f2
	public static final DoubleInOut OUTPUT_F1 = new DoubleInOut(
			"out_f1", 
			"f1",
			"result of function F1"
			);
	
	public static final DoubleInOut OUTPUT_F2 = new DoubleInOut(
			"out_f2", 
			"f2",
			"result of function F2"
			);
	
	public static enum EAvailableFunctions {
		
		BINH_KORN ("Binh and Korn"),
		CHAKONG_HAIMES ("Chakong and Haimes"),
		CTP1 ("CTP1"),
		POLONI ("Poloni's two objective function")
		
		;
		
		public final String label;
		
		private static Map<String, EAvailableFunctions> label2value = new HashMap<String, VerificationFunctionsAlgo.EAvailableFunctions>();

		private EAvailableFunctions (String label) {
			this.label = label;
		}
	
		public static EAvailableFunctions parseFromLabel(String label) {
			return label2value.get(label);
		}
		
		public static List<String> getLabelsAsList() {
			return new LinkedList<String>(label2value.keySet());
		}
		
		static {{
			
			// cache the map from label to enum value
			for (EAvailableFunctions value : EAvailableFunctions.values()) {
				
				// refuse double values
				if (label2value.containsKey(value.label)) {
					throw new ProgramException("label "+value.label+" was defined several times in "+EAvailableFunctions.class.getCanonicalName());
				}
			
				label2value.put(value.label, value);
			}
			
		}}
		
	}
	
	// parameter: enum
	public static final ListParameter PARAM_FUNCTION = new ListParameter(
			"param_function", 
			"function", 
			"reference function to compute",
			0,
			EAvailableFunctions.getLabelsAsList()
			);
	
	
	public VerificationFunctionsAlgo() {
		super(
				"multi-objective test functions", 
				"functions for validating multi-objective functions"
				);

		inputs.add(INPUT_X);
		inputs.add(INPUT_Y);
		
		outputs.add(OUTPUT_F1);
		outputs.add(OUTPUT_F2);
		
		registerParameter(PARAM_FUNCTION);
	}


	@Override
	public IAlgoExecution createExec(IExecution execution,
			AlgoInstance algoInstance) {
		return new AbstractAlgoExecutionOneshot(execution, algoInstance, new ComputationProgressWithSteps()) {
			
			@Override
			public void kill() {
				progress.setComputationState(ComputationState.FINISHED_CANCEL);
			}
			
			@Override
			public void cancel() {
				progress.setComputationState(ComputationState.FINISHED_CANCEL);
			}
			
			@Override
			public void run() {

				ComputationResult res = new ComputationResult(algoInst, progress, messages);
				setResult(res);

				// retrieve parameters and inputs
				final Integer idxParam = (Integer)algoInst.getValueForParameter(PARAM_FUNCTION);
				final EAvailableFunctions testedFunction = EAvailableFunctions.values()[idxParam];
				
				final Double x = (Double) getInputValueForInput(INPUT_X);
				final Double y = (Double) getInputValueForInput(INPUT_Y);
				
				try {					
					// detect constraint violation 
					boolean violatesConstraint = false;
					boolean violatesSearchDomain = false;
					switch (testedFunction) {
						case BINH_KORN:
							violatesConstraint = 
								(
									(Math.pow(x - 5, 2) + Math.pow(y, 2)) > 25
								) || (
									(Math.pow(x-8, 2) + Math.pow(y+3, 2)) < 7.7		
								);
							violatesSearchDomain =
									(x < 0) || (x > 5) ||
									(y < 0) || (y > 3);
							break;
						case CHAKONG_HAIMES:
							violatesConstraint =
								(
									(Math.pow(x, 2)+Math.pow(y, 2)) > 225
								) || (
									(x - 3*y + 10) > 0
								);
							violatesSearchDomain =
									(x < -20) || (x > 20) ||
									(y < -20) || (y > 20);
							break;
						case CTP1:
							violatesConstraint =
								(
									(((1+y) * Math.exp(-x/(1+y))) / ( 0.858*Math.exp(-0.541*x) )) < 1
								) || (
									(((1+y) * Math.exp(-x/(1+y))) / ( 0.728*Math.exp(-0.295*x) )) < 1
								);
							violatesSearchDomain =
									(x < 0) || (x > 1) ||
									(y < 0) || (y > 1);
							break;
						case POLONI:
							violatesConstraint = false; // no constraint
							violatesSearchDomain =
									(x < -Math.PI) || (x > Math.PI) ||
									(y < -Math.PI) || (y > Math.PI);
							break;
						default:
							throw new ProgramException("unknown test function "+testedFunction);
					}
					
					// if constraint is violated, then fail
					if (violatesConstraint) {
						messages.debugUser("constraint violated.", getClass());
						progress.setComputationState(ComputationState.FINISHED_FAILURE);
						return;
					}
					if (violatesSearchDomain) {
						messages.errorUser("out of search domain x="+x+", y="+y+"; please correct the search domain", getClass());
						progress.setComputationState(ComputationState.FINISHED_FAILURE);
						return;
					}

					// we compute the goals
					Double f1 = null;
					Double f2 = null;
					switch (testedFunction) {
						case BINH_KORN:
							f1 = 4*Math.pow(x, 2)+4*Math.pow(y, 2);
							f2 = Math.pow(x - 5,  2) + Math.pow(y - 5, 2);
							break;
						case CHAKONG_HAIMES:
							f1 = 2 + Math.pow(x-2, 2) + Math.pow(y-1, 2);
							f2 = 9*x + Math.pow(y-1, 2);
							break;
						case CTP1:
							f1 = x;
							f2 = (1+y) * Math.exp(-x/(1+y));
							break;
						case POLONI:
						    double a1, a2, b1, b2;
						    a1 = 0.5*Math.sin(1) - 2.0*Math.cos(1) + Math.sin(2) - 1.5*Math.cos(2);
						    a2 = 1.5*Math.sin(1) - Math.cos(1) + 2*Math.sin(2) - 0.5*Math.cos(2);
						    b1 = 0.5*Math.sin(x) - 2*Math.cos(x) + Math.sin(y) - 1.5*Math.cos(y);
						    b2 = 1.5*Math.sin(x) - Math.cos(x) + 2*Math.sin(y) - 0.5*Math.cos(y);
						    
							f1 = 1 + Math.pow(a1-b1, 2) + Math.pow(a2-b2, 2);
							f2 = Math.pow(x+3, 2) + Math.pow(y+1, 2);
							break;
						default:
							throw new ProgramException("unknown test function "+testedFunction);
					}
					
					res.setResult(OUTPUT_F1, f1);
					res.setResult(OUTPUT_F2, f2);
					
					progress.setComputationState(ComputationState.FINISHED_OK);
					
				} catch (RuntimeException e) {
					messages.errorUser("unexpected error while computing "+testedFunction.label+": "+e.getMessage(), getClass());
					progress.setComputationState(ComputationState.FINISHED_FAILURE);
				}
			}
			
			@Override
			public long getTimeout() {
				return 200;
			}
		};
	}

}