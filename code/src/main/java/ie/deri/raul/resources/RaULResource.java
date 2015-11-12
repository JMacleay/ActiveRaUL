package ie.deri.raul.resources;

import ie.deri.raul.UserManager;
import ie.deri.raul.persistence.PersistenceException;
import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.persistence.RDFRepositoryFactory;
import ie.deri.raul.processor.ActiveRaULProcessor;

import java.net.URLEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfa.RDFaMetaWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

import raul.Button;
import raul.Listbox;
import raul.Page;
import raul.Textbox;
import raul.Widget;
import raul.WidgetContainer;

import com.sun.jersey.spi.resource.Singleton;

/**
 * Implements the RaUL REST service. Basically it manages RaUL forms and their
 * associated data. A "form" is defined as the instance of a RaUL page
 * (according to the RaUL ontology) which can actually contain multiple (HTML)
 * forms. The data that belongs to the form if an instance of the RaUL data
 * model.
 * 
 * @author Florian Rosenberg
 */
@Singleton
@Path("/{userid: [a-zA-Z][a-zA-Z_0-9]*}/forms")
public class RaULResource {

	private static final String CONTEXT_FORM_DEFINITION_SUFFIX = "-formDefinition";
	private static final String CONTEXT_FORM_DATA_SUFFIX = "-formData";
	private static Log _logger = LogFactory.getLog(RaULResource.class);
		
	// TODO this needs to be persisted somehow -> use a key value store
	private Map<String, InstanceIds> _urls2ontologyMap; // maps REST URL part to ontology namespace and context
	//private Map<String, Integer> _user2IdMap; // maps user names to available ids	
	
	private RDFRepository _repository;
	private UserManager _userManager;
	
	private RDFRepository _dataGraph;	//added by pcc 5,Dec.11
	private RDFRepository _tmpGraph;	//added by pcc 16,Jan.12
	
	public RaULResource() {
		_logger.info("Initializing RaUL Resource...");	//added by pcc 19,Sep.11
		
		_logger.debug("Initializing RDF repository...");		
		try {
			_repository = RDFRepositoryFactory.createRepository();
			_dataGraph =  RDFRepositoryFactory.createInMemoryRepository();
			_tmpGraph =  RDFRepositoryFactory.createInMemoryRepository();
			
			//_repository.clearRepository();
		} catch (RepositoryException e) {
			final String msg = "Cannot initiate connection to RDF repository!";
			_logger.fatal(msg, e);
			throw new RuntimeException(msg, e);
		}
		
		// init user manager
		_userManager = new UserManager();
		
		// initialize storage for mapping form URL ids to ontology namespaces and context names
		_urls2ontologyMap = Collections.synchronizedMap(new HashMap<String, InstanceIds>());
		
		// initialize storage for mapping a user name to highest available id (for identifying a form data instance)
		//_user2IdMap = Collections.synchronizedMap(new HashMap<String, Integer>());
		
		try {
			rebuild_urls2ontologyMap();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		_logger.info("Initializing RaUL Resource...(done)");	//added by pcc 19,Sep.11
	}
	
	private static RDFFormat determineRDFContent(MediaType mediaType) {
		RDFFormat format = null;
		if (RDFMediaType.APPLICATION_RDFJSON_TYPE.equals(mediaType)
				|| MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {		
			format = RDFFormat.RDFJSON;
		} else if (RDFMediaType.APPLICATION_RDFXML_TYPE.equals(mediaType)
				|| MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
			format = RDFFormat.RDFXML;
		} else if (RDFMediaType.TEXT_N3_TYPE.equals(mediaType)
				|| MediaType.TEXT_PLAIN_TYPE.equals(mediaType)) {
			format = RDFFormat.N3;
		}  
		return format;
	}
	
	private static String convertJSON2XML(String content) {
		JSONObject json = JSONObject.fromObject(content);
		XMLSerializer xml = new XMLSerializer();
		xml.setTypeHintsEnabled(true);
		xml.setTypeHintsCompatibility(true);
		return xml.write(json);
	}
	
	@POST
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
	RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
	RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	public Response createRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId, String content) {
		
		_logger.info("Porcessing From Post...");	//added by pcc 19,Sep.11
		
		_logger.info("userid: " + userId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to RDFXML
			content = convertJSON2XML(content);
		}
		
		if (format == null) { // no acceptable media type 
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
			.build();
		}

		try {
			String formIri = extractPageIdentifier(headers, format, content);
			
			int index = formIri.lastIndexOf("/");	//modified by pcc 13,Jul.11
			
			String context = "";
			String tempFormId = formIri.substring(index + 1);
			String formId = storeFormDefinitionUrl(userId, tempFormId, formIri);
			
			if(!(tempFormId.equals(formId))){
				content = content.replaceAll("(/{1})(" + tempFormId + ")(#{1})", "/" + formId + "#");
				content = content.replaceAll("(/{1})(" + tempFormId + ")(/{1})", "/" + formId + "/");	//added by pcc 11,Jan.12
				content = content.replaceAll("(/{1})(" + tempFormId + ")(\"{1})", "/" + formId + "\"");
				formIri = formIri.substring(0, index + 1) + formId;
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
			}
			else{
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
			}
			
//			content = content.replaceAll("CREATEOperation", "UPDATEOperation");
						
			_logger.info("formIri: " + formIri);
			_logger.info("formId: " + formId);
			_logger.info("context: " + context);
			
			
			_repository.addString(content, formIri, format, context);
			//String testString = _repository.runSPARQL("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> DESCRIBE ?subjectURI WHERE{ ?subject rdf:subject ?subjectURI. }", format);
			
//			String defaultDataURI, defaultDataContext;
//			defaultDataURI = formIri + "/" + "defaultInstanceGraph";
//			defaultDataContext = defaultDataURI + CONTEXT_FORM_DATA_SUFFIX;
//			_urls2ontologyMap.put(userId + '/' + formId + '/' + "defaultInstanceGraph", new InstanceIds(defaultDataURI, defaultDataContext));
//			_repository.addString(content, defaultDataURI, format,	defaultDataContext);
						
			_logger.info("Porcessing From Post...(done)");	//added by pcc 19,Sep.11
			return Response.created(URI.create('/' + formId)).build();
		} catch (RDFParseException e) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity("Error while parsing the request message: "
							+ e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		
		
	}


	/**
	 * Tries to extract a custom RaUL Page identifier from the HTTP headers
	 * otherwise it parses the payload to extract the identifier from the
	 * RaUL page triple.  
	 */
	private static String extractPageIdentifier(HttpHeaders headers, RDFFormat format,
			String content) throws RDFParseException, RDFHandlerException, IOException {
		// check if URI is in the header otherwise parse content
		List<String> raulHeaders = headers.getRequestHeader(RaULHeader.RAUL_URI);		
		String formIri = null;
		if (raulHeaders != null && raulHeaders.size() > 0) {			
			formIri = raulHeaders.get(0);
			//_logger.info("headerDef " + formIri);
		} else {			
			formIri = parsePageIdentifier(format, content);
			//_logger.info("without headerDef " + formIri);
		}
		return formIri;
	}

	
	protected static String parsePageIdentifier(RDFFormat format, String content) throws RDFParseException, RDFHandlerException, IOException {
		// parse out the subject URI of value triples
		
		// handle json as special case because no json parser is available.
		if (RDFFormat.RDFJSON.equals(format)) {
			content = convertJSON2XML(content);
			format = RDFFormat.RDFXML;
		}
		PageIdRDFHandler handler = new PageIdRDFHandler();		
		RDFParserFactory factory = RDFParserRegistry.getInstance().get(format);
		RDFParser parser = factory.getParser();		
		parser.setStopAtFirstError(true);
		parser.setRDFHandler(handler);	
		//parser.parse(new StringReader(content), "http://raul.deri.ie/forms/");
		parser.parse(new StringReader(content), "");
		//_logger.info("handler.getPageId(): " + handler.getPageId());
		return handler.getPageId();	
	}

	private void rebuild_urls2ontologyMap() throws RepositoryException{
		String contextType, tmpContext, iri, formId, userId, dataId, var;
		int indexOf_forms, indexOf_service;

		Set<String> set = _repository.listContexts(); 
		
		Iterator<String> iterator = set.iterator();
        while(iterator.hasNext()) {
        	tmpContext = iterator.next();
        	contextType = tmpContext.substring(tmpContext.lastIndexOf("-"));
            if(contextType.equals("-formDefinition")){	
            	indexOf_forms = tmpContext.lastIndexOf("/forms/");
            	indexOf_service = tmpContext.lastIndexOf("/service/");
            	            	
            	iri = tmpContext.substring(0, tmpContext.lastIndexOf("-"));
            	userId = tmpContext.substring(indexOf_service + "/service/".length() , indexOf_forms);
            	formId = iri.substring(iri.lastIndexOf("/") + 1);
            	var = userId + '/' + formId;
            	_urls2ontologyMap.put(var, new InstanceIds(iri, tmpContext));
            	
            }
            else if(contextType.equals("-formData")){

            	indexOf_forms = tmpContext.lastIndexOf("/forms/");
            	indexOf_service = tmpContext.lastIndexOf("/service/");
            	            	
            	iri = tmpContext.substring(0, tmpContext.lastIndexOf("-"));
            	userId = tmpContext.substring(indexOf_service + "/service/".length() , indexOf_forms);
            	formId = tmpContext.substring(indexOf_forms + "/forms/".length() , tmpContext.lastIndexOf("/"));
            	dataId = tmpContext.substring(tmpContext.lastIndexOf("/") + 1, tmpContext.lastIndexOf("-"));
            	
            	var = userId + '/' + formId + '/' + dataId;            	
        		_urls2ontologyMap.put(var, new InstanceIds(iri, tmpContext));
            	
            }            
        }
	}
	
	private String storeFormDefinitionUrl(String userId, String formId, String iri) throws PersistenceException {
		String context = "";
		
		int i = 1;
		String var = userId + '/' + formId;
		String tmpId = formId;
		while((_urls2ontologyMap.containsKey(var)) && (i < Integer.MAX_VALUE)) {			
			tmpId = formId + i;
			var = userId + '/' + tmpId;
			i++;
		}
		formId = tmpId;
		
		iri = iri.substring(0, (iri.lastIndexOf("/")) + 1) + formId;
		context = iri + CONTEXT_FORM_DEFINITION_SUFFIX;		
		_urls2ontologyMap.put(var, new InstanceIds(iri, context));
		
		return formId;
	}
	
	//added by pcc 13,Jul.12 and modified 11,Jan.12
	private String storeFormDataUrl(String userId, String formId, String subjectKey, String iri) {
		String context = "";
		
		int i = 1;
		String var = userId + '/' + formId + '/' + subjectKey;
		String tmpKey = subjectKey;
		while((_urls2ontologyMap.containsKey(var)) && (i < Integer.MAX_VALUE)) {			
			tmpKey = subjectKey + i;
			var = userId + '/' + formId + '/' + tmpKey;
			i++;
		}
		subjectKey = tmpKey;
		
		iri = iri + "/" + subjectKey;
		context = iri + CONTEXT_FORM_DATA_SUFFIX;
		_urls2ontologyMap.put(var, new InstanceIds(iri, context));
		
		return subjectKey;
	}
	
	
//	private String storeFormDataUrl(String userId, String formId, 
//			String iri, String context) {
//		
//		// generate a new form data instance id that is used as part of the URL
//		Integer id = 0;
//		if (_user2IdMap.containsKey(userId)) {
//			id = _user2IdMap.get(userId);
//			id += 1; 
//		} 
//		_user2IdMap.put(userId, id); // put back next used id		
//		
//		String key = userId + '/' + formId + '/' + id;
//		context = context + "-" + key;
//		//_logger.info("context: " + context);
//		iri = iri + "/" + id;
//		//_logger.info("iri: " + iri);
//		_urls2ontologyMap.put(key, new InstanceIds(iri, context));				
//		return key;
//	}
	
	private InstanceIds getFormDefinition(String userId, String formId) {		
		return _urls2ontologyMap.get(userId + '/' + formId);
	}
	
	private InstanceIds getFormData(String userId, String formId, String dataId) {
		return _urls2ontologyMap.get(userId + '/' + formId + '/' + dataId);
	}
	
	@Path("{formid}")
	@PUT
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
		RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
		RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	public Response updateRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formInstanceId, String content) {

		_logger.info("Porcessing From Update...");	//added by pcc 18,Jan.12
		
		_logger.info("userid: " + userId);
		_logger.info("formid: " + formInstanceId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to RDFXML
			content = convertJSON2XML(content);
		}
		
		if (format == null) { // no acceptable media type 
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
			.build();
		}
		
		try {
			
			InstanceIds fData = getFormDefinition(userId, formInstanceId);
			if (fData != null) {				
				_repository.deleteContextAndTriples(fData.getContext());					
				_repository.addString(content, fData.getIri(), format, fData.getContext());
				
			}		
			else{
				return Response.status(Status.BAD_REQUEST).entity(String.format("Parameter '%s' is not valid.", formInstanceId)).build();
//				String formIri = extractPageIdentifier(headers, format, content);
//				String context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;				
//				_urls2ontologyMap.put(userId + '/' + formInstanceId, new InstanceIds(formIri, context));
//				
//				_repository.addString(content, formIri, format, context);
//				
//				String defaultDataURI, defaultDataContext;
//				defaultDataURI = formIri + "/" + "defaultInstanceGraph";
//				defaultDataContext = defaultDataURI + CONTEXT_FORM_DATA_SUFFIX;
//				_urls2ontologyMap.put(userId + '/' + formInstanceId + '/' + "defaultInstanceGraph", new InstanceIds(defaultDataURI, defaultDataContext));
//				_repository.addString(content, defaultDataURI, format,	defaultDataContext);			
			}
			
			_logger.info("Porcessing From Update...(done)");	//added by pcc 18,Jan.12
			return Response.ok().build();
						
		} catch (RDFParseException e) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity("Error while parsing the request message: "
							+ e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
	}

	@Path("{formid}")
	@DELETE
	public Response deleteRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formId) {
		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		try {
			InstanceIds fData = getFormDefinition(userId, formId);
			if (fData == null) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}		
			_repository.deleteContextAndTriples(fData.getContext());
			return Response.noContent().build(); // ok but not content sent back.
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}		
	}

	@Path("{formid}")
	@GET
	@Produces({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML, // handles  xml
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON, // handles json
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN, // handles N3
			MediaType.APPLICATION_XHTML_XML })	// handles XHTML+RDFa
	public Response getRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formId) {
			
		_logger.info("Porcessing From Get...");	//added by pcc 19,Sep.11
		
		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
		StringWriter out = new StringWriter();
		RDFWriter rdfWriter = new RDFXMLPrettyWriter(out); // default is RDF/XML
		String result = null;
		MediaType resultType = null; // resulting mime-type
		try {
			// build URI
			InstanceIds fData = getFormDefinition(userId, formId);
			if (fData == null) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}
			org.openrdf.model.URI uri = _repository.URIref(fData.getIri());
			_logger.info("uri: " + uri.toString());
			
			// instantiate the corresponding writer element
			if (acceptTypes.contains(RDFMediaType.APPLICATION_RDFJSON_TYPE)
					|| acceptTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
				String xml = queryAndSerializeAsRDF(uri, rdfWriter, out);
				XMLSerializer xmlSerializer = new XMLSerializer();
				xmlSerializer.setTypeHintsEnabled(true);
				xmlSerializer.setTypeHintsCompatibility(true);
				JSON json = (JSON) new XMLSerializer().read(xml);				
				result = json.toString(2);
				resultType = MediaType.APPLICATION_JSON_TYPE;
			} else if (acceptTypes.contains(RDFMediaType.TEXT_N3_TYPE)
					|| acceptTypes.contains(MediaType.TEXT_PLAIN)) {
				result = queryAndSerializeAsRDF(uri, new NTriplesWriter(out), out);
				resultType = MediaType.TEXT_PLAIN_TYPE;
			} else if (acceptTypes.contains(MediaType.APPLICATION_XHTML_XML_TYPE)) {

				// TODO serialize as XHTML -- probably best solution is to
				// implement a customer RDFWriter.
				// probably use:
				// http://repo.aduna-software.org/websvn/listing.php?repname=aduna&path=/org.openrdf/sesame/trunk/core/rio/rdfa/src/main/java/org/openrdf/rio/rdfa/&rev=8587&sc=1
				// Algo: (1) retrieve the top level RaUL element (e.g., page)
				// (2) iterate through the model and create XTHML code and
				// (3) annotate XML code with RDFa using RIO RDFAWriter
				_logger.info("ActiveRaULProcessor...");	//added by pcc 19,Sep.11
				
				
				_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
				_tmpGraph.addString(out.toString(), fData.getIri(), RDFFormat.RDFXML, fData.getContext());
							
				ActiveRaULProcessor processor = new ActiveRaULProcessor();
				result = processor.serializeXHTML(uri.toString(), _tmpGraph);
				resultType = MediaType.APPLICATION_XHTML_XML_TYPE;
				
												
//				ActiveRaULProcessor processor = new ActiveRaULProcessor();
//				result = processor.serializeXHTML(uri.toString(), _repository);
//				resultType = MediaType.APPLICATION_XHTML_XML_TYPE;
				
				_logger.info("ActiveRaULProcessor...(done)");	//added by pcc 19,Sep.11
			} else { // this is default (irrespective what accept header was
						// sent by the client)				
				//modified by pcc 1, Dec. 11				
				_repository.dumpRDF(rdfWriter, fData.getContext());
				result = out.toString();				
				resultType = MediaType.APPLICATION_XML_TYPE;
				
//				_logger.info("***********");
//				_repository.clearRepository();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		_logger.info("Porcessing From Get...(done)");	//added by pcc 19,Sep.11		
		return Response.ok(result).type(resultType).build();
	}


	private String queryAndSerializeAsRDF(org.openrdf.model.URI uri, RDFWriter rdfWriter, StringWriter out) throws RepositoryException, RDFHandlerException {		
		RepositoryResult<Statement> result = _repository.getStatements(uri, false);
		rdfWriter.startRDF();
		while (result.hasNext()) {			
			rdfWriter.handleStatement(result.next());
		}
		rdfWriter.endRDF();
		return out.toString();
	}

	@POST
	@Path("{formid}")	
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	public Response createRaulFormData(
			@Context HttpHeaders headers, @PathParam("userid") String userId,
			@PathParam("formid") String formId, String content) {
			
		_logger.info("Processing POST Data...");	//added by pcc 19,Sep.11
		
		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to RDFXML
			content = convertJSON2XML(content);
		}
		
		if (format == null) { // no acceptable media type 
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
			.build();
		}
		
		try {
			String formIri = extractPageIdentifier(headers, format, content);
			_logger.info("formIri: " + formIri);
			
			int index = formIri.lastIndexOf("/");	//modified by pcc 13,Jul.11
			String context = "";
			_logger.info("index: " + index);		
			String tmpString = content.substring(content.indexOf("<rdf:subject>"), content.indexOf("</rdf:subject>"));
			_logger.info("tmpString: " + tmpString);
			String tmpSubjectKey = tmpString.substring(tmpString.lastIndexOf("/") + 1);
			_logger.info("tmpSubjectKey: " + tmpSubjectKey);
			String subjectKey = storeFormDataUrl(userId, formIri.substring(index + 1), tmpSubjectKey, formIri);
			
			//_logger.info("index: " + index + " tmpString: " + tmpString + " tmpSubjectKey: " + tmpSubjectKey + " subjectKey: " + subjectKey);
			
			if(!(tmpSubjectKey.equals(subjectKey))){
				content = content.replaceAll("(/{1})(" + tmpSubjectKey + ")(<{1})", "/" + subjectKey + "<");
				content = content.replaceAll("(/{1})(" + tmpSubjectKey + ")(_{1})", "/" + subjectKey + "_");
				formIri = formIri + "/" + subjectKey;
				context = formIri + CONTEXT_FORM_DATA_SUFFIX;			
			}
			else{
				formIri = formIri + "/" + subjectKey;
				context = formIri + CONTEXT_FORM_DATA_SUFFIX;
			}
			
			//content = content.replaceAll("CREATEOperation", "UPDATEOperation");
			_logger.info("Processing POST Data...(done) formIri " + formIri);	//added by pcc 19,Sep.11
			_repository.addString(content, formIri, format,	context);			
			_logger.info("Processing POST Data...(done)");	//added by pcc 19,Sep.11
			
			//return Response.created(URI.create('/' + subjectKey)).build();
			return Response.created(URI.create('/' + URLEncoder.encode(subjectKey, "UTF-8"))).build();
			
		} catch (RDFParseException e) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity("Error while parsing the request message: "
							+ e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("{formid}/{dataid}")
	@Produces({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML, // handles  xml
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON, // handles json
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN, // handles N3
			MediaType.APPLICATION_XHTML_XML })	// handles XHTML+RDFa
	public Response getRaulFormData(
			@Context HttpHeaders headers, @PathParam("userid") String userId,
			@PathParam("formid") String formId,
			@PathParam("dataid") String dataId) {
			
		_logger.info("Processing Get Data...");	//added by pcc 19,Sep.11

		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		_logger.info("dataid: " + dataId);
		
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
		StringWriter out = new StringWriter();
		RDFWriter rdfWriter = new RDFXMLPrettyWriter(out); // default is RDF/XML
		String result = null;
		MediaType resultType = null; // resulting mime-type
		try {
			// build URI
			InstanceIds fData = getFormData(userId, formId, dataId);
			if (fData == null) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}
			org.openrdf.model.URI uri = _repository.URIref(fData.getIri());
			
			_logger.info("fData.getIri() " + fData.getIri());
			_logger.info("fData.getContext() " + fData.getContext());
			
			// instantiate the corresponding writer element
			if (acceptTypes.contains(RDFMediaType.APPLICATION_RDFJSON_TYPE)
					|| acceptTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
				String xml = queryAndSerializeAsRDF(uri, rdfWriter, out);
				JSONObject json = (JSONObject) new XMLSerializer().read(xml);
				result = json.toString(2);
				resultType = MediaType.APPLICATION_JSON_TYPE;
			} else if (acceptTypes.contains(RDFMediaType.TEXT_N3_TYPE)
					|| acceptTypes.contains(MediaType.TEXT_PLAIN)) {
				result = queryAndSerializeAsRDF(uri, new NTriplesWriter(out), out);
				resultType = MediaType.TEXT_PLAIN_TYPE;
			} else if (acceptTypes.contains(MediaType.APPLICATION_XHTML_XML_TYPE)) {

				// TODO serialize as XHTML -- probably best solution is to
				// implement a customer RDFWriter.
				// probably use:
				// http://repo.aduna-software.org/websvn/listing.php?repname=aduna&path=/org.openrdf/sesame/trunk/core/rio/rdfa/src/main/java/org/openrdf/rio/rdfa/&rev=8587&sc=1
				// Algo: (1) retrieve the top level RaUL element (e.g., page)
				// (2) iterate through the model and create XTHML code and
				// (3) annotate XML code with RDFa using RIO RDFAWriter

				// String baseURI =
				// "http://www.armin-haller.com/resources/example";
				// rdfWriter = new RDFaWriterFactory().getWriter(out);
				// RDFaMetaWriter writer = new RDFaMetaWriter(out);
				// result = serializeXHTML(uri.toString(), writer, out);
				
				_logger.info("ActiveRaULProcessor Get Data...");	//added by pcc 19,Sep.11
				
				
				_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
				_tmpGraph.addString(out.toString(), fData.getIri(), RDFFormat.RDFXML, fData.getContext());
				
				
				//added by pcc 16, Jan. 12
				ActiveRaULProcessor processor = new ActiveRaULProcessor();
				String formUri = uri.toString().substring(0, uri.toString().lastIndexOf("/"));
				result = processor.serializeXHTML(formUri, _tmpGraph); //modified by pcc 16, Jan. 12				
				//result = processor.serializeXHTML(uri.toString(), _repository);
				resultType = MediaType.APPLICATION_XHTML_XML_TYPE;
				
				_logger.info("ActiveRaULProcessor Get Data...(done)");	//added by pcc 19,Sep.11
				
			} else { // this is default (irrespective what accept header was
						// sent by the client)
//				result = queryAndSerializeAsRDF(uri, rdfWriter, out);								
//				resultType = MediaType.APPLICATION_XML_TYPE;

				
				//added by pcc 16, Jan. 12
				_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
				_tmpGraph.addString(out.toString(), fData.getIri(), RDFFormat.RDFXML, fData.getContext());
				
				StringBuffer buf = out.getBuffer();
				buf.setLength(0);
				
				//added by pcc 7, Dec. 11
				ActiveRaULProcessor processor = new ActiveRaULProcessor();			
				_dataGraph.clearRepository();				
				String formUri = uri.toString().substring(0, uri.toString().lastIndexOf("/"));				
				processor.dataGraphGen_RDF(formUri, _tmpGraph, _dataGraph);	//modified by pcc 16, Jan. 12
				//processor.dataGraphGen_RDF(uri.toString(), _repository, _dataGraph);
				_dataGraph.dumpRDF(rdfWriter, "");
				result = out.toString();				
				resultType = MediaType.APPLICATION_XML_TYPE;
				
			}

		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		_logger.info("Processing Get Data...(done)");	//added by pcc 19,Sep.11
		return Response.ok(result).type(resultType).build();		
	}

	@PUT
	@Path("{formid}/{dataid}")
	public Response updateRaulFormData(
			@Context HttpHeaders headers, @PathParam("userid") String userId,
			@PathParam("formid") String formInstanceId,
			@PathParam("dataid") String dataInstanceId, String content) {

		_logger.info("Porcessing Data Instance Update...");	//added by pcc 18,Jan.12
		
		_logger.info("userid: " + userId);
		_logger.info("formid: " + formInstanceId);
		_logger.info("dataid: " + dataInstanceId);
		
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to RDFXML
			content = convertJSON2XML(content);
		}
		
		if (format == null) { // no acceptable media type 
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
		}
				
		try {
			InstanceIds fData = getFormData(userId, formInstanceId, dataInstanceId);
			if (fData == null) {
				return Response.status(Status.BAD_REQUEST).entity(String.format("Parameter '%s' is not valid.", formInstanceId)).build();
			}
			else{
				_repository.deleteContextAndTriples(fData.getContext());
				_repository.addString(content, fData.getIri(), format, fData.getContext());
				_logger.info("Porcessing Data Instance Update...(done)");	//added by pcc 18,Jan.12
				return Response.ok().build();
			}			
		} catch (RDFParseException e) {
			return Response.status(Status.BAD_REQUEST).entity("Error while parsing the request message: " + e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		
	}


	@DELETE
	@Path("{formid}/{dataid}")
	public Response deleteRaulFormData(
			@Context HttpHeaders headers, @PathParam("userid") String userId,
			@PathParam("formid") String formId,
			@PathParam("dataid") String dataId) {

		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		_logger.info("dataid: " + dataId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			InstanceIds fData = getFormData(userId, formId, dataId);
			if (fData == null) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}		
			_repository.deleteContextAndTriples(fData.getContext());
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		return Response.noContent().build(); // ok but not content sent back.
	}

}