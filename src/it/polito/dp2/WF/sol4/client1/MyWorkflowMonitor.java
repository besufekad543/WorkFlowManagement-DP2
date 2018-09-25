package it.polito.dp2.WF.sol4.client1;

import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.sol4.client1.jaxws.GetWorkflows;
import it.polito.dp2.WF.sol4.client1.jaxws.GetWorkflowsResponse;
import it.polito.dp2.WF.sol4.client1.jaxws.InvalidArgumentException;
import it.polito.dp2.WF.sol4.client1.jaxws.MonitorException;
import it.polito.dp2.WF.sol4.client1.jaxws.WFInfo;
import it.polito.dp2.WF.sol4.client1.jaxws.WFInfoService;
import it.polito.dp2.WF.sol4.client1.jaxws.Workflow;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MyWorkflowMonitor implements it.polito.dp2.WF.WorkflowMonitor {
	private WorkflowReader workflow;
	private Set<WorkflowReader> workflows = new HashSet<WorkflowReader>();
	private Set<ProcessReader> processes = new HashSet<ProcessReader>();
	private MyInputValidator validator = new MyInputValidator();
	private it.polito.dp2.WF.sol4.client1.jaxws.WorkflowMonitor WorkflowMonitor;

	// Default constructor
	public MyWorkflowMonitor() {
		WFInfoService service;
		WFInfo WFproxy;
		try {
			String infourl = System.getProperty("it.polito.dp2.WF.lab4.URL");
			if (infourl != null) {
				URL url = new URL(infourl + "?wsdl");
				QName qname = new QName("http://pad.polito.it/Workflow",
						"WFInfoService");
				service = new WFInfoService(url, qname);
				WFproxy = service.getWFInfoPort();
			} else {
				service = new WFInfoService();
				WFproxy = service.getWFInfoPort();
			}

			List<Workflow> workflowList = new ArrayList<Workflow>();
			Boolean lastPage = false;
			int pageNumber = 0;
			while (!lastPage) {
				GetWorkflows req = new GetWorkflows();
				req.setPageNumber(pageNumber);

				try {
					GetWorkflowsResponse res = WFproxy.getWorkflows(req);
					pageNumber++;
					workflowList.addAll(res.getReturn());

					if (res.isLastPage())
						lastPage = true;
				} catch (InvalidArgumentException e1) {

					e1.printStackTrace();
				} catch (MonitorException e1) {

					e1.printStackTrace();
				}

				for (Workflow workflowtype : workflowList) {
					workflows.add(new MyWorkflowReader(workflowtype,
							WorkflowMonitor, this, WFproxy));
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println("Wrong url!");
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
	}

	@Override
	public Set<ProcessReader> getProcesses() {
		for (WorkflowReader workflow : workflows) {
			this.processes.addAll(workflow.getProcesses());
		}

		if (processes == null) {
			processes = Collections.emptySet();
		}
		return processes;
	}

	@Override
	public WorkflowReader getWorkflow(String arg0) {
		// Check if the argument is valid
		workflow=null;
		// Check if the argument is valid
		if (validator.validateWorkflowName(arg0) == false)
			try {
				throw new WorkflowMonitorException(
						"Invalid workflow name format");
			} catch (WorkflowMonitorException e) {
				e.printStackTrace();
			}
		for (WorkflowReader j : workflows) {
			if (j.getName().equals(arg0)) {
				workflow = j;

			}
		}
		return workflow;
	}

	@Override
	public Set<WorkflowReader> getWorkflows() {
		if (workflows.isEmpty()) {
			workflows = Collections.emptySet();
		}
		return workflows;
	}

}
