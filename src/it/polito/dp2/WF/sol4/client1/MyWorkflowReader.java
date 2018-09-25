package it.polito.dp2.WF.sol4.client1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.ActionStatusReader;
import it.polito.dp2.WF.Actor;
import it.polito.dp2.WF.ProcessActionReader;
import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.SimpleActionReader;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.sol4.client1.jaxws.Action;
import it.polito.dp2.WF.sol4.client1.jaxws.Actortype;
import it.polito.dp2.WF.sol4.client1.jaxws.GetProcesses;
import it.polito.dp2.WF.sol4.client1.jaxws.GetProcessesResponse;
import it.polito.dp2.WF.sol4.client1.jaxws.InvalidArgumentException;
import it.polito.dp2.WF.sol4.client1.jaxws.MonitorException;
import it.polito.dp2.WF.sol4.client1.jaxws.Process;
import it.polito.dp2.WF.sol4.client1.jaxws.Status;
import it.polito.dp2.WF.sol4.client1.jaxws.WFInfo;
import it.polito.dp2.WF.sol4.client1.jaxws.Workflow;
import it.polito.dp2.WF.sol4.client1.jaxws.WorkflowMonitor;

public class MyWorkflowReader implements WorkflowReader {

	private Set<ActionReader> actions;
	private Set<ProcessReader> processes;
	private String workflowname;
	private StringBuffer nextaction;
	private MyInputValidator validator = new MyInputValidator();

	private Boolean takeIncharge, terminate;
	private Calendar terminationtime, starttime;
	private String actionname, actorname, actorrole;
	private XMLGregorianCalendar ttime;

	private Actor Actor;
	private Actortype actortype;
	private List<ActionStatusReader> ListStatus;

	public MyWorkflowReader(
			it.polito.dp2.WF.sol4.client1.jaxws.Workflow workflowtype,
			WorkflowMonitor root, MyWorkflowMonitor monitor, WFInfo wFproxy) {
		try {
			if (workflowtype == null) {
				return;
			}
			workflowname = workflowtype.getWfName();
			if (validator.validateWorkflowName(workflowname) == false)
				throw new WorkflowMonitorException("Invalid workflow name");
			actions = new HashSet<ActionReader>();
			List<Action> actionList = workflowtype.getAction();
			for (Action actiontype : actionList) {

				String type = actiontype.getType().toString();
				if (type.equals("SIMPLE")) {
					SimpleActionReader simpleaction = new MySimpleActionReader(
							actiontype, this);
					actions.add(simpleaction);
				} else if (type.equals("PROCESS")) {
					ProcessActionReader processaction = new MyProcessActionReader(
							actiontype, this);
					actions.add(processaction);
				}
			}

			for (ActionReader insaction : actions) {

				if (insaction instanceof MySimpleActionReader) {
					nextaction = ((MySimpleActionReader) insaction)
							.getNextAction();
					String nextAction = nextaction.toString();
					if (nextAction != null) {
						String[] nAcarry = nextAction.split(" ");
						for (String naction : nAcarry) {
							ActionReader nAction = this.getAction(naction);
							if (nAction != null) {
								((MySimpleActionReader) insaction)
										.addNextAction(nAction);
							}
						}
					}
				} else if (insaction instanceof MyProcessActionReader) {
					String workflowName = ((MyProcessActionReader) insaction)
							.getWorkflowName();
					((MyProcessActionReader) insaction).setNxtWorkflow(monitor
							.getWorkflow(workflowName));
					((MyProcessActionReader) insaction).getEnclosingWorkflow();
				}
			}
			createProcesses(workflowtype, actions, workflowname, root, wFproxy);
		} catch (WorkflowMonitorException e) {
			e.printStackTrace();
		}
	}

	private void createProcesses(Workflow workflowtype,
			Set<ActionReader> actions, String workflowName,
			WorkflowMonitor monitor, WFInfo wFproxy) {
		try {
			// Processes
			// Process and its nested elements

			this.processes = new HashSet<ProcessReader>();
			List<Process> processList = new ArrayList<Process>();
			Boolean lastPage = false;
			int pageNumber = 0;

			while (!lastPage) {
				GetProcesses req = new GetProcesses();
				req.setPageNumber(pageNumber);

				try {

					GetProcessesResponse res = wFproxy.getProcesses(req);
					pageNumber++;
					processList.addAll(res.getReturn());

					if (res.isLastPage())
						lastPage = true;
				} catch (InvalidArgumentException e) {
					e.printStackTrace();
				} catch (MonitorException e) {
					e.printStackTrace();
				}
			}
			for (Process processtype : processList) {

				System.out.println(processtype.getStartTime());

				if (workflowName.equals(processtype.getWfnameRef())) {
					starttime = processtype.getStartTime()
							.toGregorianCalendar();
					workflowname = processtype.getWfnameRef();
					if (validator.validateWorkflowName(workflowname) == false)
						try {
							throw new WorkflowMonitorException(
									"Invalid workflow name");
						} catch (WorkflowMonitorException e) {
							e.printStackTrace();
						}

					ListStatus = new ArrayList<ActionStatusReader>();
					List<Status> statusList = processtype.getStatus();
					for (Status statustype : statusList) {
						if (statustype == null) {
							return;
						}
						actionname = statustype.getActionName();
						ttime = statustype.getTerminationTime();
						if (ttime != null) {
							takeIncharge = true;
							terminate = true;

							if (!starttime.after(ttime)) {
								terminationtime = statustype
										.getTerminationTime()
										.toGregorianCalendar();
							}
							actortype = statustype.getActortype();
							actorname = actortype.getActorName();
							if (validator.validateActorName(actorname) == false)
								throw new WorkflowMonitorException(
										"Invalid actor name");
							actorrole = actortype.getRole();
							if (validator.validateRole(actorrole) == false)
								throw new WorkflowMonitorException(
										"Invalid actor role");
							Actor = new Actor(actorname, actorrole);

						} else {
							this.terminate = false;
							actortype = statustype.getActortype();
							
							if (actortype != null) {
								actorname = actortype.getActorName();
								if (validator.validateActorName(actorname) == false)
									throw new WorkflowMonitorException(
											"Invalid actor name");
								actorrole = actortype.getRole();
								if (validator.validateRole(actorrole) == false)

									throw new WorkflowMonitorException(
											"Invalid actor role");

								Actor = new Actor(actorname, actorrole);

								this.takeIncharge = true;
							} else {
								this.terminate = false;
								this.takeIncharge = false;
							}
						}

						ActionStatusReader status = new MyActionStatusReader(
								actionname, Actor, terminationtime,
								takeIncharge, terminate);
						ListStatus.add(status);
					}

					ProcessReader process = new MyProcessReader(starttime,
							ListStatus, this);
					processes.add(process);
				}
			}
		} catch (WorkflowMonitorException e) {

			e.printStackTrace();
		}
	}

	@Override
	public ActionReader getAction(String arg0) {
		for (ActionReader Action : actions) {
			if (Action.getName().equals(arg0)) {
				return Action;
			}
		}
		return null;
	}

	@Override
	public Set<ActionReader> getActions() {
		if (actions == null) {
			actions = Collections.emptySet();
		}
		return actions;
	}

	@Override
	public String getName() {
		return workflowname;
	}

	@Override
	public Set<ProcessReader> getProcesses() {
		if (processes == null) {
			processes = Collections.emptySet();
		}
		return processes;

	}
}
