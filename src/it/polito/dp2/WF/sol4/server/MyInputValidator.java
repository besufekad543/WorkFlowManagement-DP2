package it.polito.dp2.WF.sol4.server;

public class MyInputValidator {
	/*
	 * the first character is alphabet and others alphanumeric characters only
	 */
	private static final String WorkflowNameID_PATTERN = "^[a-zA-Z][a-zA-Z0-9]*$";
	private static final String ActionNameID_PATTERN = "^[a-zA-Z][a-zA-Z0-9]*$";
	private static final String Role_Pattern = "^[a-zA-Z ]*$";

	public boolean validateWorkflowName(String wfnameID) {
		if ((wfnameID == null) || (wfnameID.isEmpty()))
			return false;
		return wfnameID.matches(WorkflowNameID_PATTERN);
	}

	public boolean validateActionName(String actionname) {
		if ((actionname == null) || (actionname.isEmpty()))
			return false;
		return actionname.matches(ActionNameID_PATTERN);
	}

	public boolean validateRole(String role) {
		if ((role == null) || (role.isEmpty()))
			return false;
		return role.matches(Role_Pattern);
	}
}
