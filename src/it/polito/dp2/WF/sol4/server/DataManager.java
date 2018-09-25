package it.polito.dp2.WF.sol4.server;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.WF.sol4.server.DataManagerException;
import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.ActionStatusReader;
import it.polito.dp2.WF.ProcessActionReader;
import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.SimpleActionReader;
import it.polito.dp2.WF.WorkflowMonitor;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowMonitorFactory;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.sol4.server.jaxws.Action;
import it.polito.dp2.WF.sol4.server.jaxws.Actiontype;
import it.polito.dp2.WF.sol4.server.jaxws.Actortype;
import it.polito.dp2.WF.sol4.server.jaxws.Status;
import it.polito.dp2.WF.sol4.server.jaxws.Workflow;
import it.polito.dp2.WF.sol4.server.jaxws.Process;

/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
/*
 * Uses Lazy Initialization and Singlton technique to implement Lazy
 * Initialization-initialize the data as the request arrive not immediately
 * which inturn minimzes memory resources consumption. Where as Singlton method
 * - which initializes single instance class so we handle Concurrent problems by
 * synchornize and and the implementation is thread safe
 */
public class DataManager {
	// Lazy initialization and Singleton pattern
	private static WorkflowMonitor monitor;
	private static ConcurrentHashMap<String, Workflow> workflowsMap = null;
	private static ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> workflowActionmap = null;
	private static ConcurrentHashMap<ProcessKey, Process> processesMap = null;

	private static Logger logger = Logger
			.getLogger(DataManager.class.getName());

	private static class DataManagerHolder {
		private final static DataManager INSTANCE = new DataManager();
	}

	public static DataManager getInstance() {
		return DataManagerHolder.INSTANCE;
	}

	// Protected constructor is sufficient to suppress unauthorized calls to the
	// constructor
	private DataManager() {
		WorkflowMonitorFactory factory = WorkflowMonitorFactory.newInstance();
		try {
			monitor = factory.newWorkflowMonitor();
			if (monitor == null) {
				return;
			}
		} catch (WorkflowMonitorException e) {
			e.printStackTrace();
		}
		logger.fine("DataManager created.");
	}

	public boolean containsKeyWorkflowsMap(String key)
			throws DataManagerException {
		if (key == null) {
			logger.info("Invalid key");
			throw new DataManagerException("Invalid key");
		}

		ConcurrentHashMap<String, Workflow> res = DataManager.workflowsMap;
		if (res == null) {
			synchronized (this) {
				res = DataManager.workflowsMap;
				if (res == null) {
					try {
						res = workflowsMap = createWorkflowsMap();
					} catch (DatatypeConfigurationException e) {

						e.printStackTrace();
					}
				}
			}
		}
		return res.containsKey(key);
	}

	private ConcurrentHashMap<String, Workflow> createWorkflowsMap()
			throws DatatypeConfigurationException {
		// Lazy initialization method for the Workflows set
		ConcurrentHashMap<String, Workflow> res = new ConcurrentHashMap<String, Workflow>();
		Set<WorkflowReader> Workflows_Set = monitor.getWorkflows();
		if (Workflows_Set != null) {
			for (WorkflowReader w : Workflows_Set) {
				
				Workflow workflow = new Workflow();
				workflow.setWfName(w.getName());
				for (ActionReader a : w.getActions()) {
					StringBuffer naction;
					Action action = new Action();
					if (a instanceof SimpleActionReader) {

						action.setActionName(a.getName());
						action.setRole(a.getRole());
						action.setIsautoinst(a.isAutomaticallyInstantiated());
						action.setEnclosedWorkflow(a.getEnclosingWorkflow()
								.getName());

						Actiontype acType = Actiontype.SIMPLE;
						action.setType(acType);
						Set<ActionReader> setNxt = ((SimpleActionReader) a)
								.getPossibleNextActions();
						if (setNxt != null) {
							naction = new StringBuffer();
							for (ActionReader nAct : setNxt) {
								naction.append(nAct.getName() + " ");
							}
							action.setNextAction(naction.toString());
						}
					} else if (a instanceof ProcessActionReader) {
						// Set the Attributes of action
						action.setActionName(a.getName());
						action.setRole(a.getRole());
						action.setIsautoinst(a.isAutomaticallyInstantiated());
						Actiontype acType = Actiontype.PROCESS;
						action.setType(acType);
						action.setEnclosedWorkflow(a.getEnclosingWorkflow()
								.getName());
						action.setActionWorkflow(((ProcessActionReader) a)
								.getActionWorkflow().getName());

					}
					workflow.getAction().add(action);
				}
				
				// Add this workflow element to WorkflowsMap
				res.put(workflow.getWfName(), workflow);
			}
			return res;
		} else {
			return null;
		}
	}

	public Workflow getWorkflow(String workflowName) {

		if (workflowName == null) {
			logger.info("Invalid key");
			try {
				throw new DataManagerException("Invalid key");
			} catch (DataManagerException e) {
				e.printStackTrace();
			}
		}

		ConcurrentHashMap<String, Workflow> res = workflowsMap;

		if (res == null)
			synchronized (this) {
				res = workflowsMap;
				if (res == null) {

					try {
						res = workflowsMap = createWorkflowsMap();
					} catch (DatatypeConfigurationException e) {
						logger.warning("Unable to search for for "
								+ workflowName + " in Workflows Map");
						e.printStackTrace();
					}
				}
			}
		return res.get(workflowName);
	}

	public ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> getWorkflowAction_map()
			throws DataManagerException {
		ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> res = workflowActionmap;

		if (res == null) {
			synchronized (this) {
				res = workflowActionmap;
				if (res == null) {
					try {
						res = workflowActionmap = createworkflowActionMap();
					} catch (DatatypeConfigurationException e) {

						e.printStackTrace();
					}
				}
			}
		}
		return res;
	}

	public CopyOnWriteArraySet<Action> getActions(String workflowName) {
		if (workflowName == null) {
			logger.info("Invalid key");
			try {
				throw new DataManagerException("Invalid key");
			} catch (DataManagerException e) {
				e.printStackTrace();
			}
		}

		ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> res = workflowActionmap;
		if (res == null) {
			synchronized (this) {
				res = workflowActionmap;
				if (res == null) {
					try {
						res = workflowActionmap = createworkflowActionMap();
					} catch (DatatypeConfigurationException e) {
						logger.warning("unable to searche for " + workflowName
								+ " in Workflows Map");
						e.printStackTrace();
					}

				}
			}
		}
		return res.get(workflowName);

	}

	private ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> createworkflowActionMap()
			throws DatatypeConfigurationException {

		Set<WorkflowReader> workflowASet = monitor.getWorkflows();
		ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> res = new ConcurrentHashMap<String, CopyOnWriteArraySet<Action>>();

		for (WorkflowReader wf : workflowASet) {

			Workflow workflow = new Workflow();
			workflow.setWfName(wf.getName());
			CopyOnWriteArraySet<Action> actionSet = new CopyOnWriteArraySet<Action>();

			// Convert ActionReader into Web Service's Action type
			for (ActionReader ar : wf.getActions()) {
				StringBuffer naction;
				Action acs = new Action();
				if (ar instanceof SimpleActionReader) {

					acs.setActionName(ar.getName());
					acs.setRole(ar.getRole());
					acs.setIsautoinst(ar.isAutomaticallyInstantiated());
					acs.setEnclosedWorkflow(ar.getEnclosingWorkflow().getName());

					Actiontype acType = Actiontype.SIMPLE;
					acs.setType(acType);
					Set<ActionReader> setNxt = ((SimpleActionReader) ar)
							.getPossibleNextActions();
					if (setNxt != null) {
						naction = new StringBuffer();
						for (ActionReader nAct : setNxt) {
							naction.append(nAct.getName() + " ");
						}
						acs.setNextAction(naction.toString());
					}
				} else if (ar instanceof ProcessActionReader) {
					// Set the Attributes of action
					acs.setActionName(ar.getName());
					acs.setRole(ar.getRole());
					acs.setIsautoinst(ar.isAutomaticallyInstantiated());
					Actiontype acType = Actiontype.PROCESS;
					acs.setType(acType);
					acs.setEnclosedWorkflow(ar.getEnclosingWorkflow().getName());
					acs.setActionWorkflow(((ProcessActionReader) ar)
							.getActionWorkflow().getName());

				}

				actionSet.add(acs);
			}
			
			res.put(wf.getName(), actionSet);
		}
		return res;
	}

	public ConcurrentHashMap<String, Workflow> getWorkflowsMap()
			throws DataManagerException {
		ConcurrentHashMap<String, Workflow> res = DataManager.workflowsMap;

		if (res == null) {
			synchronized (this) {
				res = DataManager.workflowsMap;
				if (res == null) {
					try {
						res = workflowsMap = createWorkflowsMap();
					} catch (DatatypeConfigurationException e) {

						e.printStackTrace();
					}
				}
			}
		}
		return res;
	}

	public Process getProcess(ProcessKey key) {

		if (key == null) {
			logger.info("Invalid key");
			try {
				throw new DataManagerException("Invalid key");
			} catch (DataManagerException e) {
				e.printStackTrace();
			}
		}

		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;
				if (res == null) {
					try {
						processesMap = res = createProcessesMap();
					} catch (DatatypeConfigurationException e) {
						e.printStackTrace();
					}

				}
			}
		}
		return res.get(key);
	}

	public boolean containsKeyProcessesMap(ProcessKey key)
			throws DataManagerException {
		if (key == null) {
			logger.info("Invalid key");
			throw new DataManagerException("Invalid key");
		}

		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;

				if (res == null)
					try {
						processesMap = res = createProcessesMap();
					} catch (DatatypeConfigurationException e) {
						logger.warning("Error while looking for " + key
								+ " in processesMap");
						e.printStackTrace();
					}
			}
		}
		return res.containsKey(key);
	}

	private ConcurrentHashMap<ProcessKey, Process> createProcessesMap()
			throws DatatypeConfigurationException {

		Set<ProcessReader> Processes_set = monitor.getProcesses();
		ConcurrentHashMap<ProcessKey, Process> res = new ConcurrentHashMap<ProcessKey, Process>();
		if (Processes_set != null) {
			for (ProcessReader pr : Processes_set) {
				Process process = new Process();
				
				process.setWfnameRef(pr.getWorkflow().getName());

				if (pr.getStartTime() != null) {
					GregorianCalendar cal1 = new GregorianCalendar();
					cal1.setTime(pr.getStartTime().getTime());

					XMLGregorianCalendar startime = DatatypeFactory
							.newInstance().newXMLGregorianCalendar(cal1);
					process.setStartTime(startime);
				}

				for (ActionStatusReader as : pr.getStatus()) {
					Status actionstatus = new Status();
					actionstatus.setActionName(as.getActionName());
					if (as.getActor() != null) {
						Actortype actor = new Actortype();
						actor.setActorName(as.getActor().getName());
						actor.setRole(as.getActor().getRole());
						actionstatus.setActortype(actor);
					}
					if (as.getTerminationTime() != null) {
						GregorianCalendar cal2 = new GregorianCalendar();
						cal2.setTime(as.getTerminationTime().getTime());

						XMLGregorianCalendar terminationtime = DatatypeFactory
								.newInstance().newXMLGregorianCalendar(cal2);
						actionstatus.setTerminationTime(terminationtime);
					}
					process.getStatus().add(actionstatus);
				}
				
				ProcessKey key = new ProcessKey(process.getWfnameRef(),
						process.getStartTime());
				res.put(key, process);
			}
		}
		return res;
	}

	public List<Process> getProcesses() {
		List<Process> processList = new Vector<Process>();
		for (Map.Entry<ProcessKey, Process> entry : processesMap.entrySet()) {
			Process pp = entry.getValue();
			processList.add(pp);
		}
		logger.info("Data Sucessfully Retrieved from processesMap");
		return processList;
	}

	public List<Workflow> getWorkflows() {
		List<Workflow> workflowList = new Vector<Workflow>();
		for (Map.Entry<String, Workflow> entry : workflowsMap.entrySet()) {
			Workflow ww = entry.getValue();
			workflowList.add(ww);
		}
		logger.info("Data Sucessfully Retrieved from WorkflowsMap");
		return workflowList;
	}

	public ConcurrentHashMap<ProcessKey, Process> getProcessesMap()
			throws DataManagerException {
		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;
				if (res == null)
					try {
						processesMap = res = createProcessesMap();

					} catch (DatatypeConfigurationException e) {
						throw new DataManagerException();
					}
			}
		}
		return res;
	}

	public ConcurrentHashMap<ProcessKey, Process> getProcessesMap(ProcessKey key)
			throws DataManagerException {
		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;
				if (res == null)
					try {
						processesMap = res = createProcessesMap();
						processesMap.get(key);
						logger.info(" ProcessKey" + processesMap.get(key));
					} catch (DatatypeConfigurationException e) {
						throw new DataManagerException();
					}
			}
		}
		return res;
	}

	public void putProcessesMap(ProcessKey key, Process value)
			throws DataManagerException {
		if (key == null) {
			logger.info("Invalid key");
			throw new DataManagerException("Invalid key");
		}

		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;
				if (res == null)
					try {
						processesMap = res = createProcessesMap();
						processesMap.put(key, value);
						logger.info("KEY=" + key + " added to processesMap");

					} catch (DatatypeConfigurationException e) {
						logger.warning("Impossible adding " + key
								+ " to processesMap");
						throw new DataManagerException();
					}
			}
		}
	}

	public void updateProcessesMap(ProcessKey key, Process pro) {
		if (pro == null) {
			logger.info("Invalid key");
			
		}

		ConcurrentHashMap<ProcessKey, Process> res = processesMap;
		if (res == null) {
			synchronized (this) {
				res = processesMap;
				if (res == null)
					try {
						processesMap = res = createProcessesMap();
						
						processesMap.replace(key, pro);
						logger.info("KEY=" + key + " added to processesMap");

					} catch (DatatypeConfigurationException e) {
						logger.warning("Impossible adding " + key
								+ " to processesMap");
					}
			}
		}
		
	}
	public void putActionsMap(String key, CopyOnWriteArraySet<Action> value)
			throws DataManagerException {
		if (key == null) {
			logger.info("Invalid key");
			throw new DataManagerException("Invalid key");
		}

		ConcurrentHashMap<String, CopyOnWriteArraySet<Action>> res = workflowActionmap;
		if (res == null) {
			synchronized (this) {
				res = workflowActionmap;
				if (res == null) {
					try {
						workflowActionmap = res = createworkflowActionMap();
						workflowActionmap.put(key, value);
						logger.info("KEY=" + key + " added to workflowActionmap");
					} catch (DatatypeConfigurationException e) {
						
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
