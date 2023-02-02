package com.looksee.visualDesignAudit.models.journeys;


import com.looksee.visualDesignAudit.models.enums.Action;
import com.looksee.visualDesignAudit.models.enums.StepType;
import com.looksee.visualDesignAudit.models.ElementState;
import com.looksee.visualDesignAudit.models.PageState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A Step is the increment of work that start with a {@link PageState} contians an {@link ElementState} 
 * 	 that has an {@link Action} performed on it and results in an end {@link PageState}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SIMPLE")
@Node
public class SimpleStep extends Step  {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SimpleStep.class);
	
	
	@Relationship(type = "HAS", direction = Direction.OUTGOING)
	private ElementState element;
	
	private String action;
	private String actionInput;
	
	public SimpleStep() {
		super();
		setActionInput("");
		setAction(Action.UNKNOWN);
	}
	
    @JsonCreator
	public SimpleStep(@JsonProperty("startPage") PageState start_page,
						@JsonProperty("elementState") ElementState element,
						@JsonProperty("action") Action action,
						@JsonProperty("actionInput") String action_input, 
						@JsonProperty("endPage") PageState end_page) 
	{
		setStartPage(start_page);
		setElementState(element);
		setAction(action);
		setActionInput(action_input);
		setEndPage(end_page);
		setKey(generateKey());
	}
	
	public Step clone() {
		return new SimpleStep(getStartPage(), 
							  getElementState(), 
							  getAction(), 
							  getActionInput(), 
							  getEndPage());
	}
	
	public ElementState getElementState() {
		return this.element;
	}
	
	public void setElementState(ElementState element) {
		this.element = element;
	}
	
	public Action getAction() {
		return Action.create(action);
	}
	
	public void setAction(Action action) {
		this.action = action.getShortName();
	}
	
	@Override
	public String generateKey() {
		String key = "";
		if(getStartPage() != null) {
			key += getStartPage().getId();
		}
		if(element != null) {
			key += element.getId();
		}
		if(getEndPage() != null) {
			key += getEndPage().getId();
		}

		return "simplestep"+key+action+actionInput;
	}

	
	@Override
	public String toString() {
		return "key = "+getKey()+",\n start_page = "+getStartPage()+"\n element ="+getElementState()+"\n end page = "+getEndPage();
	}
	
	public String getActionInput() {
		return actionInput;
	}

	public void setActionInput(String action_input) {
		this.actionInput = action_input;
	}

	@Override
	public StepType getStepType() {
		return StepType.SIMPLE;
	}
}
