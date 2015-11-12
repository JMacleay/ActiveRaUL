package ie.deri.raul.resources;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import ie.deri.raul.resources.RDFMediaType;
import ie.deri.raul.resources.RaULHeader;
import ie.deri.raul.resources.RaULResource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.test.framework.JerseyTest;

public class RaULResourceTest1 extends JerseyTest {

	private static Log _logger = LogFactory.getLog(RaULResourceTest1.class);
	
	public RaULResourceTest1() throws Exception {		
		super("ie.deri.raul.resources");
	}

	
	
	private String readFile(String filename) throws IOException, URISyntaxException {
		URL requestURL = Thread.currentThread().getContextClassLoader().getResource(filename);		
		String request = FileUtils.readFileToString(new File(requestURL.toURI()));
		return request;
	}
	
		
	@Test
	public void testPOSTRaulFormRDFXMLWithoutHeader() throws Exception {		
		String request = readFile("foafedit_empty_form.rdf");
		WebResource r = resource();		
		ClientResponse response1 =  r.path("public/forms/").type(RDFMediaType.APPLICATION_RDFXML).post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response1.getClientResponseStatus());		
	}
	
	/*
	 * This service test the POST & GET service of form definition.
	 * 1- It calls testPOSTRaulFormRDFXMLWithoutHeader() function that post the form definition to server
	 * 2- Then it calls endpoint http://localhost:8080/services/public/forms/foafedit to get that form definition.
	 * becasue media type is XHTML_XML therefore it returns HTML for automatic form generation 
	 * */
	 
	@Test
	public void testPUTRaulFormXHTML() throws Exception {
		WebResource r = resource();
		
		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
		
		//get the form
		ClientResponse getResponse = r.path("public/forms/foafedit").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);
		System.out.println(getResponse.getEntity(String.class));
		assertEquals(Status.OK, getResponse.getClientResponseStatus()); 				
	}
	
/*	@Test
	public void testPUTRaulDataXHTML() throws Exception {

		WebResource r = resource();

		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition		
		testPOSTRaulDataRDFXML();	//post a data instance
		
		//get the data instance
		ClientResponse getResponse = r.path("public/forms/foafedit/DrSheldon").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);		
		System.out.println(getResponse.getEntity(String.class));
		assertEquals(Status.OK, getResponse.getClientResponseStatus());
		
		//update the data instance
		String request = readFile("foafedit_example_modified.rdf");
		r = resource();		
		ClientResponse response =  r.path("public/forms/foafedit/DrSheldon").
			type(RDFMediaType.APPLICATION_RDFXML).			
			put(ClientResponse.class, request);
		assertEquals(Status.OK, response.getClientResponseStatus());
		
		//get the update data instance
		ClientResponse getResponse1 = r.path("public/forms/foafedit/DrSheldon").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);		
		System.out.println(getResponse1.getEntity(String.class));
		assertEquals(Status.OK, getResponse1.getClientResponseStatus());		
	}

	public void testPOSTRaulDataRDFXML() throws Exception {		
		String request = readFile("foafedit_example.rdf");
		WebResource r = resource();		
		ClientResponse response1 =  r.path("public/forms/foafedit").
			type(RDFMediaType.APPLICATION_RDFXML).			
			post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response1.getClientResponseStatus());		
	}*/
}


