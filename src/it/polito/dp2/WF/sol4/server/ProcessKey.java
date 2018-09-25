package it.polito.dp2.WF.sol4.server;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ProcessKey {
	private final String WorkflowNameID;
	private final XMLGregorianCalendar starttime;
		
	public ProcessKey(String workflownameref, XMLGregorianCalendar st) {
		this.WorkflowNameID = workflownameref;
		this.starttime = st;
	}
	
	@Override
	public int hashCode() {
		int hashcode=0;
        int MOD=10007;
        int shift=29;
        for(int i=0;i<WorkflowNameID.length();i++){
            hashcode=((shift*hashcode)%MOD+WorkflowNameID.charAt(i))%MOD + starttime.getSecond()+starttime.getMinute()+starttime.getHour()+starttime.getDay()+starttime.getMonth()+starttime.getYear();
        }
        return hashcode; 
	}
	
	public static void main(final String[] args) throws Exception {
		String workflowName = "ArticleProduction";
		GregorianCalendar gcal = new GregorianCalendar();
	    XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
	            .newXMLGregorianCalendar(gcal);
	    ProcessKey ID = new ProcessKey(workflowName, xgcal); 
	    System.out.println("Date:-" + xgcal);
	    System.out.println(ID.hashCode());
	}
}
