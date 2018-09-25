package it.polito.dp2.WF.sol4.client1;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.SimpleActionReader;
import it.polito.dp2.WF.sol4.client1.jaxws.Action;

/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MySimpleActionReader extends MyActionReader implements
		SimpleActionReader {
	private Set<ActionReader> setNxtActions;
	private StringBuffer nextactionList;

	public MySimpleActionReader(Action actiontype,
			MyWorkflowReader myWorkflowReader) {
		super(actiontype, myWorkflowReader);
		if(actiontype==null)
		{
			return;
		}
		setNxtActions = new HashSet<ActionReader>();
		nextactionList = new StringBuffer();
		nextactionList.append(actiontype.getNextAction());
	}

	@Override
	public Set<ActionReader> getPossibleNextActions() {
		return this.setNxtActions;
	}

	public StringBuffer getNextAction() {
		return this.nextactionList;
	}

	public void addNextAction(ActionReader action) {
		this.setNxtActions.add(action);
	}

}
