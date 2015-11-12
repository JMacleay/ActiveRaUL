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

public class RaULResourceTest extends JerseyTest {

	private static Log _logger = LogFactory.getLog(RaULResourceTest.class);
	
	public RaULResourceTest() throws Exception {		
		super("ie.deri.raul.resources");
	}

	@Test
	public void testPOSTRaulFormWrongMediaType() throws Exception {		
		String request = readFile("addProduct.rdf");
		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").
			type("application/turtle").
	    header(RaULHeader.RAUL_URI, "http://raul.deri.ie/forms/addproduct#addProduct").
			post(ClientResponse.class, request);
		
		assertEquals(Status.UNSUPPORTED_MEDIA_TYPE, response.getClientResponseStatus());
	}
	

	@Test
	public void testPOSTRaulFormMalformedN3() throws Exception {		
		String request = readFile("n3-malformed-sample.rdf");
		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").		
			type(RDFMediaType.TEXT_N3).
	    header(RaULHeader.RAUL_URI, "http://raul.deri.ie/forms/addproduct#addProduct").
			post(ClientResponse.class, request);
		
		assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
	}
	
	// POSTs a RaUL form 100times to check whether the URLs are generated correctly.
	@Test
	public void testPOSTRaulFormRDFXML100() throws Exception {		
		String request = readFile("addProduct.rdf");		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").
			type(RDFMediaType.APPLICATION_RDFXML).	    
			post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response.getClientResponseStatus());
		assertEquals("http://localhost:9998/public/forms/addProduct" , response.getHeaders().get(HttpHeaders.LOCATION).get(0));		

		for (int i = 1; i <= 100; i++) {		
			response =  r.path("public/forms").
				type(RDFMediaType.APPLICATION_RDFXML).	    
				post(ClientResponse.class, request);
			assertEquals(Status.CREATED, response.getClientResponseStatus());			
			assertEquals("http://localhost:9998/public/forms/addProduct" +i , response.getHeaders().get(HttpHeaders.LOCATION).get(0));					
		}
	}
	
	@Test
	public void testPOSTRaulFormRDFN3() throws Exception {		
		String request = readFile("addProduct-N3.rdf");
		
		_logger.debug("Request: " + request);
		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").
			type(RDFMediaType.TEXT_N3).
	    header(RaULHeader.RAUL_URI, "http://raul.deri.ie/forms/addproduct#addProduct").
			post(ClientResponse.class, request);
		
		assertEquals(Status.CREATED, response.getClientResponseStatus());
		assertEquals("http://localhost:9998/public/forms/addProduct", response.getHeaders().get(HttpHeaders.LOCATION).get(0));		
	}
	
	
	private String postAddProductFormRDFXML() throws IOException, URISyntaxException {
		String request = readFile("addProduct.rdf");		
		_logger.debug("Request: " + request);		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").
			type(RDFMediaType.APPLICATION_RDFXML).	    
			post(ClientResponse.class, request);			
		//System.out.println("Location header: " + response.getHeaders().get(HttpHeaders.LOCATION).get(0));	
		// hack it to get relative URL back
		String urlString = response.getHeaders().get(HttpHeaders.LOCATION).get(0);		
		return urlString.substring(urlString.indexOf("9998/") + 4);
	}
	
	private String postFOAFEDITFormRDFXML() throws IOException, URISyntaxException {
		String request = readFile("foafedit_example.rdf");		
		_logger.debug("Request: " + request);		
		WebResource r = resource();		
		ClientResponse response =  r.path("public/forms").		
			type(RDFMediaType.APPLICATION_RDFXML).	    
			post(ClientResponse.class, request);			
		//System.out.println("Location header: " + response.getHeaders().get(HttpHeaders.LOCATION).get(0));	
		// hack it to get relative URL back
		String urlString = response.getHeaders().get(HttpHeaders.LOCATION).get(0);		
		return urlString.substring(urlString.indexOf("9998/") + 4);
	}
	
	@Test
	public void testGETRaulFormRDFXML() throws Exception {
		// POST the addProducts form first
		//String getUrl = postAddProductFormRDFXML();
		String getUrl = postFOAFEDITFormRDFXML();
		
		// GET the POSTed form 
		WebResource r = resource();
		ClientResponse response = r.path(getUrl)
			.accept(MediaType.APPLICATION_XML)
			.get(ClientResponse.class);		
		assertEquals(Status.OK, response.getClientResponseStatus());
		
		String content = response.getEntity(String.class);
		System.out.println("Content:\n" + content);
		
		// check the identifier to see if we get the right form defintion.
		String identifier = RaULResource.parsePageIdentifier(RDFFormat.RDFXML, content);
		//assertEquals("http://raul.deri.ie/forms/addproduct#addProduct", identifier);
		//assertEquals("http://raul.deri.ie/forms/productAdd", identifier);
		assertEquals("http://w3c.org.au/raul/service/public/forms/foafedit", identifier);
	}
	
	@Test
	public void testGETRaulFormN3() throws Exception {
		// POST the addProducts form first
		String getUrl = postAddProductFormRDFXML();
		
		// GET the POSTed form 
		WebResource r = resource();
		ClientResponse response = r.path(getUrl)
			.accept(RDFMediaType.TEXT_N3_TYPE, MediaType.TEXT_PLAIN_TYPE)
			.get(ClientResponse.class);		
		assertEquals(Status.OK, response.getClientResponseStatus());
		
		String responseText = response.getEntity(String.class);
		System.out.println("Response: " + responseText);
		
		// check the identifier to see if we get the right form definition.
		String identifier = RaULResource.parsePageIdentifier(RDFFormat.N3, responseText);
		assertEquals("http://raul.deri.ie/forms/addproduct#addProduct", identifier);		
	}
	
	@Test
	public void testGETRaulFormRDFJSON() throws Exception {
		// POST the addProducts form first
		String getUrl = postAddProductFormRDFXML();
		
		// GET the POSTed form 
		WebResource r = resource();
		ClientResponse response = r.path(getUrl)
			.accept(RDFMediaType.APPLICATION_RDFJSON_TYPE, MediaType.APPLICATION_JSON_TYPE)
			.get(ClientResponse.class);		
		assertEquals(Status.OK, response.getClientResponseStatus());
		
		String responseText = response.getEntity(String.class);
		System.out.println("Response: " + responseText);
		
		// check the identifier to see if we get the right form definition.
		// String identifier = RaULResource.parsePageIdentifier(RDFFormat.RDFJSON, responseText);
		// assertEquals("http://raul.deri.ie/forms/addproduct#addProduct", identifier);		
	}
	
	private String getRDFXML(String url) {
		WebResource r = resource();
		ClientResponse response = r.path(url)
			.accept(RDFMediaType.APPLICATION_RDFXML_TYPE)
			.get(ClientResponse.class);		
		
		if (Status.OK == response.getClientResponseStatus()) {
			return response.getEntity(String.class);
		}
		return null;
	}
	
	@Test
	public void testDELETERaulForm() throws Exception {
		// POST the addProducts form first
		String url = postAddProductFormRDFXML();
		
		// GET the POSTed form 
		assertNotNull(getRDFXML(url));
				
		// DELETE the POSTed form
		ClientResponse response = 
			resource().path(url).
				delete(ClientResponse.class);
		assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());

		// check if it was really deleted
		assertNull(getRDFXML(url));
	}
	
	
	private String readFile(String filename) throws IOException, URISyntaxException {
		URL requestURL = Thread.currentThread().getContextClassLoader().getResource(filename);		
		String request = FileUtils.readFileToString(new File(requestURL.toURI()));
		return request;
	}
	
	@Test
	public void testPOSTRaulFormRDFXMLWithHeader() throws Exception {
		String request = readFile("foafedit_empty_form.rdf");
		WebResource r = resource();		
		ClientResponse response1 =  r.path("public/forms/").
			type(RDFMediaType.APPLICATION_RDFXML).
			header(RaULHeader.RAUL_URI, "http://w3c.org.au/raul/service/public/forms/foafedit").
			post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response1.getClientResponseStatus());		
	}
	
	@Test
	public void testPOSTRaulFormRDFXMLWithoutHeader() throws Exception {		
		String request = readFile("foafedit_empty_form.rdf");
		WebResource r = resource();		
		ClientResponse response1 =  r.path("public/forms/").
		//ClientResponse response1 =  r.path("testuser/forms/").
			type(RDFMediaType.APPLICATION_RDFXML).			
			post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response1.getClientResponseStatus());		
	}
	
	@Test
	public void testGETRaulFormXHTML() throws Exception {
		// post something to repo		
//		String request = readFile("raul.rdf");
//		WebResource r = resource();		
//		ClientResponse response =  r.path("public/forms").
//			type(RDFMediaType.APPLICATION_RDFXML).
//			header(RaULHeader.RAUL_URI, "http://vocab.deri.ie/raul#").
//			post(ClientResponse.class, request);
//		assertEquals(Status.CREATED, response.getClientResponseStatus());
				
		WebResource r = resource();
		
		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
		
		ClientResponse getResponse = r.path("public/forms/foafedit").
		//ClientResponse getResponse = r.path("testuser/forms/foafedit").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);
		System.out.println(getResponse.getEntity(String.class));
		assertEquals(Status.OK, getResponse.getClientResponseStatus());
		
		testPOSTRaulFormRDFXMLWithoutHeader();	//post another form definition
		
		ClientResponse getResponse1 = r.path("public/forms/foafedit1").
		//ClientResponse getResponse1 = r.path("testuser/forms/foafedit1").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);
		System.out.println(getResponse1.getEntity(String.class));
		assertEquals(Status.OK, getResponse1.getClientResponseStatus());
				
		ClientResponse getResponse2 = r.path("public/forms/wrongid").
		//ClientResponse getResponse2 = r.path("testuser/forms/foafeditxxx").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);			
		System.out.println(getResponse2.getEntity(String.class));
		assertEquals(Status.BAD_REQUEST, getResponse2.getClientResponseStatus());
					
	}

	@Test
	public void testPOSTRaulDataRDFXML() throws Exception {		
		String request = readFile("foafedit_example.rdf");
		WebResource r = resource();		
		ClientResponse response1 =  r.path("public/forms/foafedit").
			type(RDFMediaType.APPLICATION_RDFXML).			
			post(ClientResponse.class, request);
		assertEquals(Status.CREATED, response1.getClientResponseStatus());		
	}
	
	@Test
	public void testGETRaulDataXHTML() throws Exception {
		WebResource r = resource();

		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition		
		testPOSTRaulDataRDFXML();	//post a data instance
		
		//get the data instance
		ClientResponse getResponse = r.path("public/forms/foafedit/DrSheldon").		
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);		
		System.out.println(getResponse.getEntity(String.class));
		assertEquals(Status.OK, getResponse.getClientResponseStatus());
		
		ClientResponse getResponse1 = r.path("public/forms/foafedit/wrongid").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);			
		System.out.println(getResponse1.getEntity(String.class));
		assertEquals(Status.BAD_REQUEST, getResponse1.getClientResponseStatus());
	}
	
//	@Test
//	public void specialTestGETRaulDataXHTML() throws Exception {
//		WebResource r = resource();
//
//		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
//		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
//		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
//		testPOSTRaulFormRDFXMLWithoutHeader();	//post a form definition
//		
//		String request = readFile("test.rdf");				
//		ClientResponse response1 =  r.path("public/forms/foafedit3").
//			type(RDFMediaType.APPLICATION_RDFXML).			
//			post(ClientResponse.class, request);
//		assertEquals(Status.CREATED, response1.getClientResponseStatus());
//		
//		//get the data instance
//		ClientResponse getResponse = r.path("public/forms/foafedit3/Dr.Chen").		
//			accept(MediaType.APPLICATION_XHTML_XML).
//			get(ClientResponse.class);		
//		System.out.println(getResponse.getEntity(String.class));
//		assertEquals(Status.OK, getResponse.getClientResponseStatus());		
//	}

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
				
		//update the form definition
		String request = readFile("foafedit_empty_form_modified.rdf");		
		ClientResponse response1 =  r.path("public/forms/foafedit").
			type(RDFMediaType.APPLICATION_RDFXML).			
			put(ClientResponse.class, request);
		assertEquals(Status.OK, response1.getClientResponseStatus());	
		
		//get the update form
		ClientResponse getResponse2 = r.path("public/forms/foafedit").
			accept(MediaType.APPLICATION_XHTML_XML).
			get(ClientResponse.class);		
		System.out.println(getResponse2.getEntity(String.class));
		assertEquals(Status.OK, getResponse2.getClientResponseStatus());					
	}
	
	@Test
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


}


