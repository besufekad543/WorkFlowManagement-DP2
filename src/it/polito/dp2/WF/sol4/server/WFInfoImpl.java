package it.polito.dp2.WF.sol4.server;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import it.polito.dp2.WF.sol4.server.DataManager;
import it.polito.dp2.WF.sol4.server.WFInfoImpl;
import it.polito.dp2.WF.sol4.server.jaxws.Action;
import it.polito.dp2.WF.sol4.server.jaxws.GetAction;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionStatus;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionStatusList;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionStatusListResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionStatusResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetActions;
import it.polito.dp2.WF.sol4.server.jaxws.GetActionsResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetActor;
import it.polito.dp2.WF.sol4.server.jaxws.GetActorResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetProcess;
import it.polito.dp2.WF.sol4.server.jaxws.GetProcessResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetProcesses;
import it.polito.dp2.WF.sol4.server.jaxws.GetProcessesResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetWorkflow;
import it.polito.dp2.WF.sol4.server.jaxws.GetWorkflowResponse;
import it.polito.dp2.WF.sol4.server.jaxws.GetWorkflows;
import it.polito.dp2.WF.sol4.server.jaxws.GetWorkflowsResponse;
import it.polito.dp2.WF.sol4.server.jaxws.InvalidArgument_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.Monitor;
import it.polito.dp2.WF.sol4.server.jaxws.Monitor_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.Process;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownAction;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownAction_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownActor_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownProcess;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownProcess_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownStatus_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownWorkflow;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownWorkflow_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.WFInfo;
import it.polito.dp2.WF.sol4.server.jaxws.Workflow;

import javax.jws.HandlerChain;
import javax.jws.WebService;

/**
 * @author Besufekad Assefa Biru ID-s202500
 */
/*
 * This Service allows Retrieving Workflow Management System Information
 * 
 * Listed Operations: - getWorkflow(workflowName) = retrieve Workflow with given
 * name - getWorkflows() = retrieve set of Workflows with given selected page
 * number of the set - getAction() = get single action of a workflow given its
 * name - getActions() = returns set of actions with given selected page number
 * of the set and workflowname
 * 
 * - getProcess() = retrieve a single process instance with key. -
 * getProcesses() = retrieve a set of processes with given selected page number
 * of the set - getActionStatus() = retrieve single action status. -
 * getActionStatusList() = retrieve list of action status. - getActor() -
 * returns single Actor information about an actor who can take over workflow
 * action executions. It includes the name and the role of the actor
 * 
 * - getAll() = retrieve all the informations
 */
@WebService(serviceName = "WFInfoService", portName = "WFInfoPort", targetNamespace = "http://pad.polito.it/Workflow", endpointInterface = "it.polito.dp2.WF.sol4.server.jaxws.WFInfo")
@HandlerChain(file = "META-INF/server/custom/handler-chain.xml")
public class WFInfoImpl implements WFInfo {
	private static DataManager manager;
	private static final Integer MAX_ENTRIES_PER_PAGE = 10;
	private static Logger logger = Logger.getLogger(WFControlImpl.class
			.getName());


	public WFInfoImpl() {
		logger.info("WFInfoImpl starting...");
		logger.entering(logger.getName(), "WFInfoImpl()");
		WFInfoImpl.manager = DataManager.getInstance();
	}

	@Override
	public GetWorkflowsResponse getWorkflows(GetWorkflows parameters)
			throws InvalidArgument_Exception, Monitor_Exception {
		logger.info("Entering getWorkflows");
		GetWorkflowsResponse res = new GetWorkflowsResponse();
		try {
			int total = 0;
			int pageNumber = parameters.getPageNumber();
			int entriesPerPage = 0;
			int pageBeginning = pageNumber * WFInfoImpl.MAX_ENTRIES_PER_PAGE;
			for (Map.Entry<String, Workflow> entry : manager.getWorkflowsMap()
					.entrySet()) {
				total++;
				if (total > pageBeginning) {
					entriesPerPage++;
					res.getReturn().add(entry.getValue());
					if (entriesPerPage == WFInfoImpl.MAX_ENTRIES_PER_PAGE) {
						res.setPageNumber(pageNumber);
						if (total == manager.getWorkflowsMap().entrySet()
								.size())
							res.setLastPage(true);
						else
							res.setLastPage(false);
						return res;
					}
				}
			}
			res.setPageNumber(pageNumber);
			res.setLastPage(true);
			return res;

		} catch (DataManagerException e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
	}

	@Override
	public GetWorkflowResponse getWorkflow(GetWorkflow parameters)
			throws InvalidArgument_Exception, Monitor_Exception,
			UnknownWorkflow_Exception {
		logger.info("Entering getWorkflow");
		try {
			if (manager.containsKeyWorkflowsMap(parameters.getWorkflowName())) {
				Workflow workflow;
				synchronized (workflow = manager.getWorkflow(parameters
						.getWorkflowName())) {
					GetWorkflowResponse res = new GetWorkflowResponse();
					res.setReturn(workflow);
					return res;
				}
			} else {
				logger.warning("The requested Workflow Name is not present in our System");
				UnknownWorkflow unkwf = new UnknownWorkflow();
				unkwf.setUnknownWorkflow("The requested Workflow Name is not present in our System");
				throw new UnknownWorkflow_Exception(
						"The requested Workflow Name is not present in our System",
						unkwf);
			}
		} catch (DataManagerException e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
	}

	@Override
	public GetActionResponse getAction(GetAction parameters)
			throws InvalidArgument_Exception, Monitor_Exception,
			UnknownAction_Exception {
		logger.info("Entering getAction");
		GetActionResponse res = new GetActionResponse();
		try {

			for (Map.Entry<String, CopyOnWriteArraySet<Action>> entry : manager
					.getWorkflowAction_map().entrySet()) {
				if (entry.getKey().equals(parameters.getWorkflowName())) {
					for (Action a : entry.getValue()) {
						if (a.getActionName()
								.equals(parameters.getActionName())) {
							res.setReturn(a);
							res.getReturn();
							return res;
						} else {
							UnknownAction unkAc = new UnknownAction();
							unkAc.setUnknownAction("The specified Action not valid");
							throw new UnknownAction_Exception(
									"The specified Action is not valid", unkAc);
						}
					}
					return res;
				}
			}
		} catch (DataManagerException e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
		return res;
	}

	@Override
	public GetActionsResponse getActions(GetActions parameters)
			throws InvalidArgument_Exception, Monitor_Exception {
		logger.info("Entering getActions");
		GetActionsResponse res = new GetActionsResponse();
		try {
			int total = 0;
			int pageNumber = parameters.getPageNumber();
			int entriesPerPage = 0;
			int pageBeginning = pageNumber * WFInfoImpl.MAX_ENTRIES_PER_PAGE;
			for (Map.Entry<String, CopyOnWriteArraySet<Action>> entry : manager
					.getWorkflowAction_map().entrySet()) {
				if (entry.getValue().equals(parameters.getWorkflowName())) {
					for (Action a : entry.getValue()) {
						total++;
						if (total > pageBeginning) {
							entriesPerPage++;
							res.getReturn().add(a);
							if (entriesPerPage == WFInfoImpl.MAX_ENTRIES_PER_PAGE) {
								res.setPageNumber(pageNumber);
								if (total == entry.getValue().size())
									res.setLastPage(true);
								else
									res.setLastPage(false);
								return res;
							}
						}
					}
					res.setLastPage(true);
					res.setPageNumber(pageNumber);
					return res;
				}
			}
		} catch (DataManagerException e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
		return res;
	}

	@Override
	public GetProcessesResponse getProcesses(GetProcesses parameters)
			throws InvalidArgument_Exception, Monitor_Exception {
		logger.info("Entering getProcesses");
		GetProcessesResponse res = new GetProcessesResponse();
		res.getReturn().clear();

		try {
			int total = 0;
			int pageNumber = parameters.getPageNumber();
			int entriesPerPage = 0;
			int pageBeginning = pageNumber * WFInfoImpl.MAX_ENTRIES_PER_PAGE;
			if (manager.getProcessesMap().entrySet() != null) {
				for (Map.Entry<ProcessKey, Process> entry : manager
						.getProcessesMap().entrySet()) {
					total++;
					if (total > pageBeginning) {
						entriesPerPage++;
						res.getReturn().add(entry.getValue());

						if (entriesPerPage == WFInfoImpl.MAX_ENTRIES_PER_PAGE) {
							res.setPageNumber(pageNumber);
							if (total == manager.getProcessesMap().entrySet()
									.size())
								res.setLastPage(true);
							else
								res.setLastPage(false);
							return res;
						}

					}
				}
			} 
			
			res.setLastPage(true);
			res.setPageNumber(pageNumber);
			return res;

		} catch (DataManagerException e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
	}

	

	@Override
	public GetProcessResponse getProcess(GetProcess parameters)
			throws InvalidArgument_Exception, Monitor_Exception,
			UnknownProcess_Exception {
		logger.info("Entering getProcess");
		GetProcessResponse res = new GetProcessResponse();
		ProcessKey key = new ProcessKey(parameters.getWorkflowName(),
				parameters.getStartTime());
		try {

			if (manager.containsKeyProcessesMap(key)) {
				Process pr = manager.getProcess(key);
				logger.info("Processes returned!");
				res.setReturn_0020(pr);

			} else {
				UnknownProcess unkPro = new UnknownProcess();
				unkPro.setUnknownProcess("The specified process is not in our system");
				throw new UnknownProcess_Exception(
						"The specified process is not in our system", unkPro);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Monitor mon = new Monitor();
			mon.setMonitor("Error during lazy initialization");
			throw new Monitor_Exception("Error during lazy initialization", mon);
		}
		return res;
	}

	@Override
	public GetActionStatusResponse getActionStatus(GetActionStatus parameters)
			throws InvalidArgument_Exception, Monitor_Exception,
			UnknownStatus_Exception {
		logger.info("Entering getActionStatus");
		
		return null;
	}

	@Override
	public GetActionStatusListResponse getActionStatusList(
			GetActionStatusList parameters) throws InvalidArgument_Exception,
			Monitor_Exception {
		logger.info("Entering getActionStatusList");
		
		return null;
	}

	@Override
	public GetActorResponse getActor(GetActor parameters)
			throws InvalidArgument_Exception, Monitor_Exception,
			UnknownActor_Exception {
		logger.info("Entering getActor");
		
		return null;
	}

}
