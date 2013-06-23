package genlab.core.model.meta;

import genlab.core.commons.WrongParametersException;
import genlab.core.parameters.IParameterConstraint;

public class InputOutput<JavaType> implements IInputOutput<JavaType> {

	private final IFlowType<JavaType> type;
	private final String id;
	private final String name;
	private final String desc;
	
	public InputOutput(IFlowType<JavaType> type, String id, String name, String desc) {
		super();
		this.type = type;
		this.id = id;
		this.name = name;
		this.desc = desc;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public IFlowType<JavaType> getType() {
		return type;
	}

	@Override
	public JavaType decodeFromParameters(Object value) throws WrongParametersException {
		
		return type.decodeFrom(value);
		
	}

	@Override
	public String toString() {
		return id;
	}


}
