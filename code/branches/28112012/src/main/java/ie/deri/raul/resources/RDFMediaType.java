package ie.deri.raul.resources;

import javax.ws.rs.core.MediaType;

public class RDFMediaType {
	
	public static final String APPLICATION_RDFXML = "application/rdf+xml; charset=UTF-8";
	public static final MediaType APPLICATION_RDFXML_TYPE = MediaType.valueOf(APPLICATION_RDFXML);
	
	public static final String APPLICATION_RDFJSON = "application/json+rdf";
	public static final MediaType APPLICATION_RDFJSON_TYPE = MediaType.valueOf(APPLICATION_RDFJSON);
	
	public static final String TEXT_N3 = "text/n3";
	public static final MediaType TEXT_N3_TYPE = MediaType.valueOf(TEXT_N3);
		
}
