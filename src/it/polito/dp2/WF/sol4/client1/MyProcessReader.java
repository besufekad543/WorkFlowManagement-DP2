package it.polito.dp2.WF.sol4.client1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.polito.dp2.WF.ActionStatusReader;
import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowReader;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MyProcessReader implements ProcessReader {
	private Calendar starttime;
	private WorkflowReader Workflow;
	private List<ActionStatusReader> ListStatus = new ArrayList<ActionStatusReader>();

	public MyProcessReader(Calendar starttime,
			List<ActionStatusReader> listStatus,
			MyWorkflowReader myWorkflowReader) {
		this.starttime = starttime;
		this.ListStatus = listStatus;
		this.Workflow = myWorkflowReader;
	}

	@Override
	public Calendar getStartTime() {
		return this.starttime;
	}

	@Override
	public List<ActionStatusReader> getStatus() {
		return this.ListStatus;
	}

	@Override
	public WorkflowReader getWorkflow() {
		return this.Workflow;
	}
}
