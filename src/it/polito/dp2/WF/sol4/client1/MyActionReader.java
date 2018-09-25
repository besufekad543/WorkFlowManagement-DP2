package it.polito.dp2.WF.sol4.client1;

import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.sol4.client1.jaxws.Action;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MyActionReader implements ActionReader {
	private String actionname;
	private String role;
	private Boolean automaticallyInstantiated;
	private WorkflowReader encworkflow;
	private MyInputValidator validator = new MyInputValidator();

	public MyActionReader(Action actiontype, MyWorkflowReader myWorkflowReader) {
		if(actiontype==null)
		{
			return;
		}
		this.actionname = actiontype.getActionName();
		if (validator.validateActionName(actionname) == false)
			try {
				throw new WorkflowMonitorException("Invalid Action Name");
			} catch (WorkflowMonitorException e) {
				e.printStackTrace();
			}
		this.role = actiontype.getRole();
		if (validator.validateRole(role) == false)
			try {
				throw new WorkflowMonitorException("Invalid Role");
			} catch (WorkflowMonitorException e) {
				e.printStackTrace();
			}
		this.automaticallyInstantiated = actiontype.isIsautoinst();
		this.encworkflow = myWorkflowReader;
	
	}
	

	@Override
	public WorkflowReader getEnclosingWorkflow() {
		return this.encworkflow;
	}

	@Override
	public String getName() {
		return this.actionname;

	}

	@Override
	public String getRole() {
		return this.role;
	}

	@Override
	public boolean isAutomaticallyInstantiated() {
		return this.automaticallyInstantiated;
	}
	
	public boolean setIsAutomaticallyInstantiated(boolean a) {
		return this.automaticallyInstantiated = a;
	}
}
