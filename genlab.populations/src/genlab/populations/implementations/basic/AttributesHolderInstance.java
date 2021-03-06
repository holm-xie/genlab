package genlab.populations.implementations.basic;

import genlab.core.commons.ProgramException;
import genlab.populations.bo.Attribute;
import genlab.populations.bo.IAttributesHolder;
import genlab.populations.bo.IAttributesHolderInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributesHolderInstance implements IAttributesHolderInstance {

	protected final Object[] attributeValues;
	
	protected final IAttributesHolder<?> attributesHolder;
	
	public AttributesHolderInstance(IAttributesHolder<?> attributesHolder, Object[] attributeValues) {

		this.attributesHolder = attributesHolder;
		this.attributeValues = attributeValues;
		
		if (attributeValues.length != attributesHolder.getAllAttributesCount())
			throw new ProgramException("the count of attributes values is not the same as defined in the agent type");
		
	}
	
	public AttributesHolderInstance(IAttributesHolder<?> attributesHolder) {

		this.attributesHolder = attributesHolder;
		this.attributeValues = new Object[attributesHolder.getAllAttributesCount()];
	}


	@Override
	public Object getValueForAttribute(Attribute attribute) {
	
		return attributeValues[attributesHolder.getAllAttributes().indexOf(attribute)];
	}

	@Override
	public Object getValueForAttribute(int idx) {
		return attributeValues[idx];
	}

	@Override
	public void setValueForAttribute(Attribute attribute, Object value) {
		attributeValues[attributesHolder.getAllAttributes().indexOf(attribute)] = value;
	}

	@Override
	public void setValueForAttribute(int idx, Object value) {
		attributeValues[idx] = value;
	}

	@Override
	public Object getValueForAttribute(String attributeName) {
		Attribute attribute = attributesHolder.getAttributeForId(attributeName);
		if (attribute == null)
			new ProgramException("no attribute named "+attributeName);
		return attributeValues[attributesHolder.getAllAttributes().indexOf(attribute)];
	}

	@Override
	public void setValueForAttribute(String attributeName, Object value) {
		Attribute attribute = attributesHolder.getAttributeForId(attributeName);
		if (attribute == null)
			new ProgramException("no attribute named "+attributeName);
		attributeValues[attributesHolder.getAllAttributes().indexOf(attribute)] = value;

	}

	@Override
	public Object[] getValuesOfAttributesAsArray() {
		return attributeValues;
	}

	@Override
	public Map<String, Object> getValuesOfAttributesAsMap() {
		Map<String,Object> res = new HashMap<String, Object>(attributeValues.length);
		
		List<Attribute> attributes = attributesHolder.getAllAttributes(); 
		for (int i=0; i<attributeValues.length; i++) {

			res.put(
					attributes.get(i).getID(), 
					attributeValues[i]
					);
		}
		return res;
	}

}
