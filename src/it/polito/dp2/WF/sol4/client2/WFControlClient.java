package it.polito.dp2.WF.sol4.client2;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import it.polito.dp2.WF.sol4.client2.jaxws.CreateNewProcess;
import it.polito.dp2.WF.sol4.client2.jaxws.CreateNewProcessResponse;
import it.polito.dp2.WF.sol4.client2.jaxws.WFControl;
import it.polito.dp2.WF.sol4.client2.jaxws.WFControlService;
/**
 * @author Besufekad Assefa Biru s202500
 * 
 */
public class WFControlClient {
	private static WFControl WFControlProxy;
	private static final String regex = "([A-Za-z]{1}[a-zA-Z0-9]+)";

	// default constructor
	public WFControlClient(String url, String workflowName) {
		try {
			if (url == null)
				throw new MalformedURLException("The endpoint url is null");
			if (workflowName == null || workflowName.isEmpty()
					|| !workflowName.matches(regex)) {
				System.out.println("ERROR: workflowName(" + workflowName
						+ ") is null or Invalid workflowName format.");
				System.exit(1);
			}

			URL endpointUrl = new URL(url + "?wsdl");
			QName qname = new QName("http://pad.polito.it/Workflow",
					"WFControlService");

			// Create service and get portType reference.
			WFControlService service = new WFControlService(endpointUrl, qname);
			WFControlProxy = service.getWFControlPort();
			// Create request.
			CreateNewProcess req = new CreateNewProcess();
			// Set details to request.
			req.setWorkflowName(workflowName);

			// Invoke service operation get response.
			CreateNewProcessResponse res = WFControlProxy.createNewProcess(req);
			res.isSucess();

			System.out.println("****Success*** " + res.isSucess());
			System.out.println("****Operation Success*** ");
			System.out
					.println("*****System will exit now with exit code=0 *****");
			System.exit(0);

			/*
			 * In this case, just sleep to give the get more process time. In a
			 * production application, other useful tasks could be performed and
			 * the application could run indefinitely.
			 */
			Thread.sleep(400);

		} catch (MalformedURLException e) {
			System.out.println("ERROR: wrong url.");
			System.out
					.println("*****System will exit now with exit code=1 *****");
			System.exit(1);

		} catch (WebServiceException e) {
			System.out.println("ERROR: WebService error, " + e.getMessage());
			System.out
					.println("*****System will exit now with exit code=2 *****");
			System.exit(2);
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("*****System will exit now with exit code=2 *****");
			System.exit(2);

		}
	}

	public static void main(String[] args) {
		if ((args.length != 2) || (args[0] == null) || (args[1] == null)) {
			System.out.println("usage: URL Of service and Workflow Name");
		}
		String url = args[0];
		String workflowName = args[1];
		new WFControlClient(url, workflowName);

	}

}
