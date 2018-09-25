package it.polito.dp2.WF.sol4.server;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.WF.sol4.server.DataManager;
import it.polito.dp2.WF.sol4.server.WFControlImpl;
import it.polito.dp2.WF.sol4.server.jaxws.Action;
import it.polito.dp2.WF.sol4.server.jaxws.ActionAlreadyCreated;
import it.polito.dp2.WF.sol4.server.jaxws.ActionAlreadyCreated_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.Actortype;
import it.polito.dp2.WF.sol4.server.jaxws.CompleteAnAction;
import it.polito.dp2.WF.sol4.server.jaxws.CompleteAnActionResponse;
import it.polito.dp2.WF.sol4.server.jaxws.CreateNewAction;
import it.polito.dp2.WF.sol4.server.jaxws.CreateNewActionResponse;
import it.polito.dp2.WF.sol4.server.jaxws.CreateNewProcess;
import it.polito.dp2.WF.sol4.server.jaxws.CreateNewProcessResponse;
import it.polito.dp2.WF.sol4.server.jaxws.Monitor;
import it.polito.dp2.WF.sol4.server.jaxws.Monitor_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.Process;
import it.polito.dp2.WF.sol4.server.jaxws.ProcessAlreadyCreated_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.Status;
import it.polito.dp2.WF.sol4.server.jaxws.TakeOverAnAction;
import it.polito.dp2.WF.sol4.server.jaxws.TakeOverAnActionResponse;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownActionType_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownActor_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownWorkflowName;
import it.polito.dp2.WF.sol4.server.jaxws.UnknownWorkflowName_Exception;
import it.polito.dp2.WF.sol4.server.jaxws.WFControl;
import it.polito.dp2.WF.sol4.server.jaxws.Workflow;

/**
 * @author Besufekad Assefa Biru ID-s202500
 * 
 */
/*
 * It's the webservice that allows control over the Workflow System. List of
 * Operations: - createNewProcess(workflow Name) = search for the correct
 * workflow with given workflowName, if found create New process with processkey
 * if processkey exists notify there is already exist searched. -
 * takeOverAnAction() = - completeAnAction() (both implemented but commented)= -
 * createNewAction() =
 */
@WebService(serviceName = "WFControlService", portName = "WFControlPort", targetNamespace = "http://pad.polito.it/Workflow", endpointInterface = "it.polito.dp2.WF.sol4.server.jaxws.WFControl")
@HandlerChain(file = "META-INF/server/custom/handler-chain.xml")
public class WFControlImpl implements WFControl {
	private static DataManager manager;
	private static Logger logger = Logger.getLogger(WFControlImpl.class
			.getName());

	public WFControlImpl() {
		logger.info("WFControlImpl starting...");
		WFControlImpl.manager = DataManager.getInstance();
	}

	@Override
	public CreateNewProcessResponse createNewProcess(CreateNewProcess parameters)
			throws Monitor_Exception, ProcessAlreadyCreated_Exception,
			UnknownWorkflowName_Exception {
		logger.entering(logger.getName(), "createNewProcess");
		CreateNewProcessResponse res = new CreateNewProcessResponse();
		res.setSucess(false);
		if (parameters.getWorkflowName() == null) {
			return res;
		} else {
			try {
				GregorianCalendar gcal = new GregorianCalendar();
				Date currentTime = Calendar.getInstance().getTime();
				Timestamp currentTimestamp = new Timestamp(
						currentTime.getTime());
				gcal.setTimeInMillis(currentTimestamp.getTime());
				XMLGregorianCalendar starttime = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(gcal);

				ProcessKey key = new ProcessKey(parameters.getWorkflowName(),
						starttime);
				if (manager.containsKeyWorkflowsMap(parameters
						.getWorkflowName())) {
					Workflow w = manager.getWorkflow(parameters
							.getWorkflowName());

					for (Action ar : w.getAction()) {
						if (ar.isIsautoinst()) {
							Process newProcess = new Process();
							newProcess.setWfnameRef(parameters
									.getWorkflowName());
							newProcess.setStartTime(starttime);
							Status s = new Status();
							s.setActionName(ar.getActionName());
							s.setActortype(null);
							s.setTerminationTime(null);
							newProcess.getStatus().add(s);
							manager.putProcessesMap(key, newProcess);
							// SUCCESS!
							res.setSucess(true);
							return res;
						}
					}
				} else {

					UnknownWorkflowName unkpro = new UnknownWorkflowName();
					unkpro.setUnknownWorkflowName("The requested workflow name is not in our system");
					throw new UnknownWorkflowName_Exception(
							"The requested workflow name is not in our system",
							unkpro);

				}

			} catch (DataManagerException | DatatypeConfigurationException e) {
				e.printStackTrace();
				Monitor mon = new Monitor();
				mon.setMonitor("Error during lazy initialization");
				throw new Monitor_Exception("Error during lazy initialization",
						mon);
			}
		}
		return res;
	}

	@Override
	public CreateNewActionResponse createNewAction(CreateNewAction parameters)
			throws ActionAlreadyCreated_Exception, Monitor_Exception {
		logger.entering(logger.getName(), "createNewAction");
		CreateNewActionResponse res = new CreateNewActionResponse();
		res.setSucess(false);
		if (parameters.getWorkflowName() == null) {
			return res;
		} else {
			try {

				if (parameters.getWorkflowName() != null
						|| parameters.getActionName() != null) {

					CopyOnWriteArraySet<Action> actionSet;
					synchronized (actionSet = manager.getActions(parameters
							.getWorkflowName())) {
						for (Action action : actionSet) {
							if (action.getActionName().equals(
									parameters.getActionName())) {
								logger.warning("This Action has already been Created on this workflow");
								ActionAlreadyCreated acCr = new ActionAlreadyCreated();
								acCr.setActionAlreadyCreated("This Action has already been Created on this workflow");
								throw new ActionAlreadyCreated_Exception(
										"This Action has already been Created on this workflow",
										acCr);
							} else {

								action.setActionName(parameters.getActionName());
								action.setActionWorkflow(parameters
										.getWorkflowName());
								// action.setIsautoinst(false);
								// action.setNextAction(value);
								// action.setRole(value);
								// action.setType(value);
								// actionSet.add(action);
								manager.putActionsMap(
										parameters.getActionName(), actionSet);
								res.setSucess(true);
								logger.info("This Action Created Sucessfully on this workflow");

								return res;

							}
						}
					}

				}

			} catch (DataManagerException e) {
				e.printStackTrace();
				Monitor mon = new Monitor();
				mon.setMonitor("Error during lazy initialization");
				throw new Monitor_Exception("Error during lazy initialization",
						mon);
			}
		}
		return res;
	}

	@Override
	public TakeOverAnActionResponse takeOverAnAction(TakeOverAnAction parameters)
			throws Monitor_Exception, UnknownActor_Exception {
		logger.entering(logger.getName(), "takeOverAnAction");
		TakeOverAnActionResponse res = new TakeOverAnActionResponse();
		res.setSucess(false);
		if (parameters.getWorkflowName() == null
				|| parameters.getActionName() == null
				|| parameters.getActorName() == null) {
			logger.warning("Can't TakeOver An Action");
			return res;
		} else {

			for (Process p : manager.getProcesses()) {
				if (p.getWfnameRef().equals(parameters.getWorkflowName())) {
					logger.info("Worklfow is found:-" + p.getWfnameRef());
					Process pro = new Process();
					pro.setWfnameRef(p.getWfnameRef());
					pro.setStartTime(p.getStartTime());

					for (Status s : p.getStatus()) {
						logger.info("ActionName:-" + s.getActionName());
						logger.info("ActionName:-" + parameters.getActionName());
						Status stat = new Status();
						if (s != null) {
							if (s.getActionName().equals(
									parameters.getActionName())
									&& s.getActortype() == null) {
								stat.setActionName(s.getActionName());
								Actortype actor = new Actortype();
								actor.setActorName(parameters.getActorName());
								actor.setRole(parameters.getActorRole());
								stat.setActortype(actor);
								stat.setTerminationTime(null);
							} else {
								stat.setActionName(s.getActionName());
								stat.setActortype(s.getActortype());
								stat.setTerminationTime(s.getTerminationTime());
							}
						}
						pro.getStatus().add(stat);

					}
					ProcessKey key = new ProcessKey(pro.getWfnameRef(),
							pro.getStartTime());
					manager.updateProcessesMap(key, pro);
					res.setSucess(true);
					res.isSucess();
				}

				else {
					logger.info("Worklfow is not found");
					res.setSucess(false);
					res.isSucess();

				}
			}
		}
		return res;
	}

	@Override
	public CompleteAnActionResponse completeAnAction(CompleteAnAction parameters)
			throws Monitor_Exception, UnknownActionType_Exception {
		logger.entering(logger.getName(), "takeOverAnAction");
		CompleteAnActionResponse res = new CompleteAnActionResponse();
		res.setSucess(false);
		if (parameters.getWorkflowName() == null
				|| parameters.getActionName() == null
				|| parameters.getActorName() == null) {
			logger.warning("Can't TakeOver An Action");
			return res;
		} else {

			try {
				for (Process p : manager.getProcesses()) {
					if (p.getWfnameRef().equals(parameters.getWorkflowName())) {
						logger.info("Worklfow is found:-" + p.getWfnameRef());
						Process pro = new Process();
						pro.setWfnameRef(p.getWfnameRef());
						pro.setStartTime(p.getStartTime());

						for (Status s : p.getStatus()) {
							logger.info("ActionName:-" + s.getActionName());
							logger.info("ActionName:-"
									+ parameters.getActionName());
							Status stat = new Status();
							if (s != null) {
								if (s.getActionName().equals(
										parameters.getActionName())
										&& s.getActortype() == null) {
									stat.setActionName(s.getActionName());
									Actortype actor = new Actortype();
									actor.setActorName(parameters
											.getActorName());
									// actor.setRole(parameters.getActorRole());
									stat.setActortype(actor);
									GregorianCalendar gcal2 = new GregorianCalendar();
									Date currentTime = Calendar.getInstance()
											.getTime();
									Timestamp currentTimestamp = new Timestamp(
											currentTime.getTime());
									gcal2.setTimeInMillis(currentTimestamp
											.getTime());
									XMLGregorianCalendar endetime;

									endetime = DatatypeFactory.newInstance()
											.newXMLGregorianCalendar(gcal2);

									stat.setTerminationTime(endetime);
								} else {
									stat.setActionName(s.getActionName());
									stat.setActortype(s.getActortype());
									stat.setTerminationTime(s
											.getTerminationTime());
								}
							}
							pro.getStatus().add(stat);

						}
						ProcessKey key = new ProcessKey(pro.getWfnameRef(),
								pro.getStartTime());
						manager.updateProcessesMap(key, pro);
						res.setSucess(true);
						res.isSucess();
					}

					else {
						logger.info("Worklfow is not found");
						res.setSucess(false);
						res.isSucess();

					}
				}
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
				Monitor mon = new Monitor();
				mon.setMonitor("Error during lazy initialization");
				throw new Monitor_Exception("Error during lazy initialization",
						mon);
			}
		}
		return res;
	}

}
