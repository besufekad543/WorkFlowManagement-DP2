package it.polito.dp2.WF.sol4.client1;

import it.polito.dp2.WF.Actor;

import java.util.Calendar;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class MyActionStatusReader implements
		it.polito.dp2.WF.ActionStatusReader {
	private Calendar terminationtime;
	private String actionname;
	private Boolean takeIncharge, terminate;
	private Actor Actor;

	public MyActionStatusReader(String actionname,
			it.polito.dp2.WF.Actor actor, Calendar terminationtime,
			Boolean takeIncharge, Boolean terminate) {
		this.actionname = actionname;
		this.Actor = actor;
		this.terminationtime = terminationtime;
		this.takeIncharge = takeIncharge;
		this.terminate = terminate;

	}

	@Override
	public String getActionName() {

		return this.actionname;
	}

	@Override
	public Actor getActor() {
		return this.Actor;
	}

	@Override
	public Calendar getTerminationTime() {
		return terminationtime;
	}

	@Override
	public boolean isTakenInCharge() {
		return this.takeIncharge;
	}

	@Override
	public boolean isTerminated() {
		return this.terminate;
	}

}
