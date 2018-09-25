package it.polito.dp2.WF.sol4.client1;

import java.util.List;
import java.util.Set;

import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.ActionStatusReader;
import it.polito.dp2.WF.Actor;
import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowMonitor;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class WorkflowMonitorFactory extends
		it.polito.dp2.WF.WorkflowMonitorFactory {
	private static MyWorkflowMonitor wfmonitor;

	@Override
	public WorkflowMonitor newWorkflowMonitor() throws WorkflowMonitorException {
		wfmonitor = new MyWorkflowMonitor();
		//testPrint();
		return wfmonitor;
	}

	/* This Method is for Testing my solution it is called before monitor */
	public static void testPrint() {
		System.out.println("   ");
		System.out.println("-Workflows -");
		Set<WorkflowReader> workflows = wfmonitor.getWorkflows();
		System.out.println("#Number of Workflows: " + workflows.size());

		/* Print the header of the table */
		for (WorkflowReader wf : workflows) {
			System.out.println("   ");
			System.out.println("Workflow Name=" + wf.getName());
			Set<ActionReader> actions = wf.getActions();
			for (ActionReader ar : actions) {
				System.out.println("   ");
				System.out.println("-Action -");
				System.out.println("Action Name:" + ar.getName());
				System.out.println("Actor Role:" + ar.getRole());
				System.out.println("Automatically Instanciated:"
						+ ar.isAutomaticallyInstantiated());
				System.out.println("Enclosed Workflow:"
						+ ar.getEnclosingWorkflow().getName());
				if (ar instanceof MySimpleActionReader) {
					System.out.print("\tSimple\t\t" + "-\t\t");
					Set<ActionReader> setNxt = ((MySimpleActionReader) ar)
							.getPossibleNextActions();

					for (ActionReader nAct : setNxt) {
						System.out.print("Next Action:" + nAct.getName() + " ");
					}
				} else if (ar instanceof MyProcessActionReader) {
					System.out.print("\tProcess\t\t");
					System.out.println("Current Workflow:"
							+ ((MyProcessActionReader) ar)
									.getEnclosingWorkflow().getName());
					System.out.println("Action Workflow:"
							+ ((MyProcessActionReader) ar).getWorkflowName());
				}
			}
		}

		System.out.println("   ");
		Set<ProcessReader> processes = wfmonitor.getProcesses();
		System.out.println("#Number of Processes: " + processes.size());

		for (ProcessReader p : processes) {
			System.out.println("   ");
			System.out.println("-Processed at- ");
			System.out.println("Start Time:" + p.getStartTime().getTime());
			System.out.println("Workflow:" + p.getWorkflow().getName());
			List<ActionStatusReader> status = p.getStatus();
			for (ActionStatusReader as : status) {
				System.out.println("-Status -");
				System.out.println("Actionname:" + as.getActionName());
				if (as.getTerminationTime() == null) {
					System.out.println("Termination Time:"
							+ as.getTerminationTime());
				} else {
					System.out.println("Termination Time:"
							+ as.getTerminationTime().getTime());
				}
				System.out.println("-");
				Actor actor = as.getActor();
				if (actor != null) {
					System.out.println("Actor name:" + actor.getName());
					System.out.println("Actor Role:" + actor.getRole());
				}
			}
		}
	}
}
