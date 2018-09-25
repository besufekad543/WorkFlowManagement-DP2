package it.polito.dp2.WF.sol4.server;

import it.polito.dp2.WF.FactoryConfigurationError;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;

import java.util.concurrent.Executors;

/**
 * @author Besufekad Assefa Biru ID-s202500
 *
 */

/* This is the server. 
 * It publishes the wsdl file and inluded xsd files from wsdl .
 * Lastly, it publishes the two services WFControlService and WFInfoService. */
public class WorkflowServer {
	/*
	 * I can set all as per the Spec whenever.
	 */
	private static final Integer NUMBER_OF_THREADS = 10;
	private static final String WF_CONTROL_URL = "http://localhost:7070/wfcontrol?wsdl";
	private static final String WF_INFO_URL = "http://localhost:7071/wfinfo?wsdl";
	private static final String XSD_FILE = "build/META-INF/server/wsdl/WFInfo.xsd";
	private static final String XSD_URL = "http://localhost:7070/wfinfo.xsd";
	private static final String XSD_URL1 = "http://localhost:7071/wfinfo.xsd";

	public static void main(String[] args) {
		try {		
			/* Publish XSD file separately */
			/* Endpoint Creation and publishing wsdl and xsd files */
			System.out.println("Publishing http://localhost:7070/wfinfo.xsd");
			Endpoint endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING,
					new XMLFileProvider(XSD_FILE));
			endpoint.publish(XSD_URL);
			
			endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING,
					new XMLFileProvider(XSD_FILE));
			endpoint.publish(XSD_URL1);

			/* 1-Publishing WFControlService */
			System.out.println("Publishing http://localhost:7070/wfcontrol");
			Endpoint wfcontrolep = Endpoint.create(new WFControlImpl());
			/* Making the Endpoint Multi-threaded */
			wfcontrolep.setExecutor(Executors
					.newFixedThreadPool(NUMBER_OF_THREADS));
			wfcontrolep.publish(WF_CONTROL_URL);

			/* 2-WFInfoService */
			System.out.println("Publishing http://localhost:7071/wfinfo");
			Endpoint wfinfoep = Endpoint.create(new WFInfoImpl());
			wfinfoep.setExecutor(Executors
					.newFixedThreadPool(NUMBER_OF_THREADS));
			wfinfoep.publish(WF_INFO_URL);
			
			System.out.println("Now the Server is Up and Running");

		} catch (WebServiceException e) {
			System.out.println("WebserviceException: " + e.getMessage());
			System.exit(2);
		} catch (FactoryConfigurationError fce) {
			System.out.println("Factory error: " + fce.getMessage());
			System.exit(2);
		} catch (Exception e) {
			System.out.println("General error: " + e.getMessage());
			System.exit(2);
		}

	}
}
