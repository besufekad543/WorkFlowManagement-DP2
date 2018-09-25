package it.polito.dp2.WF.sol4.client1;

import it.polito.dp2.WF.ProcessActionReader;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.sol4.client1.jaxws.Action;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MyProcessActionReader extends MyActionReader implements
		ProcessActionReader {
	private String currentworkflow;
	private WorkflowReader enclosed_workflow, actionworkflow;

	public MyProcessActionReader(Action actiontype,
			MyWorkflowReader myWorkflowReader) {
		super(actiontype, myWorkflowReader);
		if(actiontype==null)
		{
			return;
		}
		this.currentworkflow = actiontype.getActionWorkflow();
		this.enclosed_workflow = myWorkflowReader;
	}

	@Override
	public WorkflowReader getActionWorkflow() {
		return actionworkflow;
	}

	public String getWorkflowName() {
		return this.currentworkflow;
	}

	public void setNxtWorkflow(WorkflowReader workflow) {
		this.actionworkflow = workflow;
	}

	public WorkflowReader enclosedWorkflowReader() {
		return enclosed_workflow;
	}
}
