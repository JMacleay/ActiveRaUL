package ie.deri.raul.resources;

import ie.deri.raul.GenerateRaULRDFTest;
import ie.deri.raul.UserManager;
import ie.deri.raul.guigeneration.MetaDataExtraction;
import ie.deri.raul.guigeneration.RDFTripleExtractor;
import ie.deri.raul.guigeneration.RaULSearch;
import ie.deri.raul.persistence.PersistenceException;
import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.persistence.RDFRepositoryFactory;
import ie.deri.raul.processor.ActiveRaULProcessor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Statement;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.HTMLInterceptor;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RaulRDFParser;
import org.openrdf.rio.XMLBaseParser;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

import com.sun.jersey.api.container.MappableContainerException;
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
	private static final String CONTEXT_FOR_TEMPORARY_STORING = "-temporary";
	private static Log _logger = LogFactory.getLog(RaULResource.class);
	private static final String URL_FORMS="forms";

	// TODO this needs to be persisted somehow -> use a key value store
	private Map<String, InstanceIds> _urls2ontologyMap; // maps REST URL part to
														// ontology namespace
														// and context
	// private Map<String, Integer> _user2IdMap; // maps user names to available
	// ids

	private RDFRepository _repository;
	private UserManager _userManager;

	private RDFRepository _dataGraph; // added by pcc 5,Dec.11
	private RDFRepository _tmpGraph; // added by pcc 16,Jan.12
	
	private Map<String,String> dynamicGroupTemplages=new HashMap<String,String>();
	
	private Map<String,Integer> formIndexGenerator=new Hashtable<String,Integer>();

	/**
	 * Constructor create 3 reporsitories and Initialize _urls2ontologyMap on
	 * call
	 */

	public RaULResource() {
		_logger.info("Initializing RaUL Resource..."); // added by pcc 19,Sep.11

		_logger.debug("Initializing RDF repository...");
		try {
			_repository = RDFRepositoryFactory.createRepository();
			_dataGraph = RDFRepositoryFactory.createInMemoryRepository();
			_tmpGraph = RDFRepositoryFactory.createInMemoryRepository();
			// _repository.clearRepository();
		} catch (RepositoryException e) {
			final String msg = "Cannot initiate connection to RDF repository!";
			_logger.fatal(msg, e);
			throw new RuntimeException(msg, e);
		}
		// init user manager
		_userManager = new UserManager();

		// initialize storage for mapping form URL ids to ontology namespaces
		// and context names
		_urls2ontologyMap = Collections
				.synchronizedMap(new HashMap<String, InstanceIds>());

		// initialize storage for mapping a user name to highest available id
		// (for identifying a form data instance)
		// _user2IdMap = Collections.synchronizedMap(new HashMap<String,
		// Integer>());

		try {
			rebuild_urls2ontologyMap();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_logger.info("Initializing RaUL Resource...(done)"); // added by pcc
																// 19,Sep.11
	}

	/**
	 * Determine the RDF format for the posted contents from its media type
	 * 
	 * @param mediaType
	 * @return
	 */

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

	/**
	 * Convert Input contents format from JSON to XML
	 * 
	 * @param content
	 * @return
	 */
	private static String convertJSON2XML(String content) {
		JSONObject json = JSONObject.fromObject(content);
		XMLSerializer xml = new XMLSerializer();
		xml.setTypeHintsEnabled(true);
		xml.setTypeHintsCompatibility(true);
		return xml.write(json);
	}

	/**
	 * Create a form POST service to create Raul Form Definition, It takes the
	 * RDF generated from domain ontology and add it to the store
	 * 
	 * @serviceURL http://localhost:8080/raul/service/public/forms
	 * @param headers
	 * @param userId
	 * @param content
	 * @return
	 */

	@POST
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	
	public Response createRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId, String content) {

		_logger.info("Porcessing From Post..."); // added by pcc 19,Sep.11

		_logger.info("userid: " + userId);

		// check for a valid user
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		// find the media type (RDF format) of input contents
		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);

		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to
											// RDFXML
			content = convertJSON2XML(content);
		}

		if (format == null) { // no acceptable media type
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
					.build();
		}
		try {
			RaulRDFParser parser = parsePageIdentifier(format, content);
			if (!parser.isRaulRDF()) {
				_logger.info("File is not based on RAUL.Begin to convert it into RUAL-based rdf");

				_logger.info("Conversion from abitrary rdf to RAUL-based rdf finished");
				// to get page id after conversion
				// parser = parsePageIdentifier(format, content);
			}
			String formIri = extractPageIdentifier(headers, format, content,
					parser.getRaulPageID());
			// example formIri:
			// http://w3c.org.au/raul/service/public/forms/foafedit

			int index = formIri.lastIndexOf("/"); // modified by pcc
													// 13,Jul.11

			String context = "";
			String tempFormId = formIri.substring(index + 1);
			// example tempFormId: foafedit
			String formId = storeFormDefinitionUrl(userId, tempFormId, formIri);
			_logger.info("tempFormId: " + tempFormId);
			_logger.info("formId: " + formId);
			// storeFormDefinitionURL("public" , "foafedit",
			// "http://w3c.org.au/raul/service/public/forms/foafedit")
			// formId = foafedit1;

			if (!(tempFormId.equals(formId))) {
				content = content.replaceAll(
						"(/{1})(" + tempFormId + ")(#{1})", "/" + formId + "#");
				content = content.replaceAll(
						"(/{1})(" + tempFormId + ")(/{1})", "/" + formId + "/"); // added
																					// by
																					// pcc
																					// 11,Jan.12
				content = content.replaceAll("(/{1})(" + tempFormId
						+ ")(\"{1})", "/" + formId + "\"");
				formIri = formIri.substring(0, index + 1) + formId;
				// formIri:
				// http://w3c.org.au/raul/service/public/forms/foafedit1
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
				// context =
				// "http://w3c.org.au/raul/service/public/forms/foafedit1-formDefinition";
			} else {
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
				// context =
				// "http://w3c.org.au/raul/service/public/forms/foafedit-formDefinition";
			}

			// content = content.replaceAll("CREATEOperation",
			// "UPDATEOperation");

			_logger.info("formIri: " + formIri);
			_logger.info("formId: " + formId);
			_logger.info("context: " + context);

			_repository.addString(content, formIri, format, context);
			// String testString =
			// _repository.runSPARQL("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> DESCRIBE ?subjectURI WHERE{ ?subject rdf:subject ?subjectURI. }",
			// format);

			// String defaultDataURI, defaultDataContext;
			// defaultDataURI = formIri + "/" + "defaultInstanceGraph";
			// defaultDataContext = defaultDataURI +
			// CONTEXT_FORM_DATA_SUFFIX;
			// _urls2ontologyMap.put(userId + '/' + formId + '/' +
			// "defaultInstanceGraph", new InstanceIds(defaultDataURI,
			// defaultDataContext));
			// _repository.addString(content, defaultDataURI, format,
			// defaultDataContext);

			_logger.info("Porcessing From Post...(done)"); // added by pcc
															// 19,Sep.11
			return Response.created(URI.create('/' + formId)).build();
			// response =
			// http://localhost:8080/raul/service/public/forms/formedit1
		} catch (RDFParseException e) {
			_logger.error(this, e);
			return Response
					.status(Status.BAD_REQUEST)
					.entity("Error while parsing the request message: "
							+ e.getMessage()).build();
		} catch (Exception e) {
			_logger.error(this, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}

	}

	/**
	 * Tries to extract a custom RaUL Page identifier from the HTTP headers
	 * otherwise it parses the payload to extract the identifier from the RaUL
	 * page triple.
	 */
	private static String extractPageIdentifier(HttpHeaders headers,
			RDFFormat format, String content, String pageId)
			throws RDFParseException, RDFHandlerException, IOException {
		// check if URI is in the header otherwise parse content
		List<String> raulHeaders = headers
				.getRequestHeader(RaULHeader.RAUL_URI);
		String formIri = null;
		if (raulHeaders != null && raulHeaders.size() > 0) {
			formIri = raulHeaders.get(0);
			// _logger.info("headerDef " + formIri);
		} else {
			// formIri = parsePageIdentifier(format, content);
			formIri = pageId;
			// _logger.info("without headerDef " + formIri);
		}
		return formIri;
	}

	protected static RaulRDFParser parsePageIdentifier(RDFFormat format,
			String content) throws RDFParseException, RDFHandlerException,
			IOException {
		// parse out the subject URI of value triples

		// handle json as special case because no json parser is available.
		if (RDFFormat.RDFJSON.equals(format)) {
			content = convertJSON2XML(content);
			format = RDFFormat.RDFXML;
		}
		/*
		 * PageIdRDFHandler handler = new PageIdRDFHandler(); RDFParserFactory
		 * factory = RDFParserRegistry.getInstance().get(format); RDFParser
		 * parser = factory.getParser(); parser.setStopAtFirstError(true);
		 * parser.setRDFHandler(handler);
		 */
		// parser.parse(new StringReader(content),
		// "http://raul.deri.ie/forms/");
		RaulRDFParser parser = new RaulRDFParser();
		parser.parse(content);
		// _logger.info("handler.getPageId(): " + handler.getPageId());
		return parser;
	}

	private void rebuild_urls2ontologyMap() throws RepositoryException {
		String contextType, tmpContext, iri, formId, userId, dataId, var;
		int indexOf_forms, indexOf_service;
		Set<String> set = _repository.listContexts();
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			tmpContext = iterator.next();
			contextType = tmpContext.substring(tmpContext.lastIndexOf("-"));
			if (contextType.equals("-formDefinition")) {
				indexOf_forms = tmpContext.lastIndexOf("/forms/");
				indexOf_service = tmpContext.lastIndexOf("/service/");

				iri = tmpContext.substring(0, tmpContext.lastIndexOf("-"));
				userId = tmpContext.substring(
						indexOf_service + "/service/".length(), indexOf_forms);
				formId = iri.substring(iri.lastIndexOf("/") + 1);
				var = userId + '/' + formId;
				_urls2ontologyMap.put(var, new InstanceIds(iri, tmpContext));

			} else if (contextType.equals("-formData")) {

				indexOf_forms = tmpContext.lastIndexOf("/forms/");
				indexOf_service = tmpContext.lastIndexOf("/service/");

				iri = tmpContext.substring(0, tmpContext.lastIndexOf("-"));
				userId = tmpContext.substring(
						indexOf_service + "/service/".length(), indexOf_forms);
				formId = tmpContext.substring(
						indexOf_forms + "/forms/".length(),
						tmpContext.lastIndexOf("/"));
				dataId = tmpContext.substring(tmpContext.lastIndexOf("/") + 1,
						tmpContext.lastIndexOf("-"));

				var = userId + '/' + formId + '/' + dataId;
				_urls2ontologyMap.put(var, new InstanceIds(iri, tmpContext));

			}
		}
	}

	private String storeFormDefinitionUrl(String userId, String formId,
			String iri) throws PersistenceException {

		// Here userId = "public" , formId = "foafedit", iri =
		// "http://w3c.org.au/raul/service/public/forms/foafedit"
		String context = "";
		int i = 1;
		String var = userId + '/' + formId;
		// var = "public/foafedit";
		String tmpId = formId;
		// tempId = "foafedit" ;

		while ((_urls2ontologyMap.containsKey(var)) && (i < Integer.MAX_VALUE)) {
			tmpId = formId + i;
			// temId = "foafedit1";
			var = userId + '/' + tmpId;
			// var = "public/foafedit1";
			i++;
		}
		formId = tmpId;

		// formId = "foafedit1";
		iri = iri.substring(0, (iri.lastIndexOf("/")) + 1) + formId;
		// iri = "http://w3c.org.au/raul/service/public/forms/foafedit1";

		context = iri + CONTEXT_FORM_DEFINITION_SUFFIX;
		// context =
		// "http://w3c.org.au/raul/service/public/forms/foafedit1-formDefinition";
		_urls2ontologyMap.put(var, new InstanceIds(iri, context));
		// _urls2ontologyMap.put("public/foafedit1", new
		// InstanceIds("http://w3c.org.au/raul/service/public/forms/foafedit1",
		// "http://w3c.org.au/raul/service/public/forms/foafedit1-formDefinition"));

		return formId;
	}

	// added by pcc 13,Jul.12 and modified 11,Jan.12
	private String storeFormDataUrl(String userId, String formId,
			String subjectKey, String iri) {
		// storeFormDataURL(public, foafedit116, nnnnnn,
		// http://w3c.org.au/raul/service/public/forms/foafedit116 )

		String context = "";

		int i = 1;
		String var = userId + '/' + formId + '/' + subjectKey;
		String tmpKey = subjectKey;
		while ((_urls2ontologyMap.containsKey(var)) && (i < Integer.MAX_VALUE)) {
			tmpKey = subjectKey + i;
			var = userId + '/' + formId + '/' + tmpKey;
			i++;
		}
		subjectKey = tmpKey;

		iri = iri + "/" + subjectKey;
		context = iri + CONTEXT_FORM_DATA_SUFFIX;
		InstanceIds formData=_urls2ontologyMap.get(userId + '/' + formId);
		InstanceIds newIds=new InstanceIds(iri, context);
		if(formData.getBaseUri()!=null)
			newIds.setBaseUri(formData.getBaseUri());
		if(formData.getOwlClass()!=null)
			newIds.setOwlClass(formData.getOwlClass());
		_urls2ontologyMap.put(var,newIds);

		return subjectKey;
	}

	// private String storeFormDataUrl(String userId, String formId,
	// String iri, String context) {
	//
	// // generate a new form data instance id that is used as part of the URL
	// Integer id = 0;
	// if (_user2IdMap.containsKey(userId)) {
	// id = _user2IdMap.get(userId);
	// id += 1;
	// }
	// _user2IdMap.put(userId, id); // put back next used id
	//
	// String key = userId + '/' + formId + '/' + id;
	// context = context + "-" + key;
	// //_logger.info("context: " + context);
	// iri = iri + "/" + id;
	// //_logger.info("iri: " + iri);
	// _urls2ontologyMap.put(key, new InstanceIds(iri, context));
	// return key;
	// }

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

		_logger.info("Porcessing From Update..."); // added by pcc 18,Jan.12

		_logger.info("userid: " + userId);
		_logger.info("formid: " + formInstanceId);
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to
											// RDFXML
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
				_repository.addString(content, fData.getIri(), format,
						fData.getContext());

			} else {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formInstanceId)).build();
				// String formIri = extractPageIdentifier(headers, format,
				// content);
				// String context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
				// _urls2ontologyMap.put(userId + '/' + formInstanceId, new
				// InstanceIds(formIri, context));
				//
				// _repository.addString(content, formIri, format, context);
				//
				// String defaultDataURI, defaultDataContext;
				// defaultDataURI = formIri + "/" + "defaultInstanceGraph";
				// defaultDataContext = defaultDataURI +
				// CONTEXT_FORM_DATA_SUFFIX;
				// _urls2ontologyMap.put(userId + '/' + formInstanceId + '/' +
				// "defaultInstanceGraph", new InstanceIds(defaultDataURI,
				// defaultDataContext));
				// _repository.addString(content, defaultDataURI, format,
				// defaultDataContext);
			}

			_logger.info("Porcessing From Update...(done)"); // added by pcc
																// 18,Jan.12
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
			return Response.noContent().build(); // ok but not content sent
													// back.
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
	}

	/*
	 * This service returns the form definition, initially post and also return
	 * an HTML form for processing
	 */
	@Path("{formid}")
	@GET
	@Produces({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML, // handles
																			// xml
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON, // handles
																			// json
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN, // handles N3
			MediaType.APPLICATION_XHTML_XML })
	// handles XHTML+RDFa
	public Response getRaulFormDefinition(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formId,
                        @QueryParam("pathType")   String pathType,
                        @QueryParam("classURI") String classURI) {

		_logger.info("Porcessing From Get..."); // added by pcc 19,Sep.11

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
			if (fData == null&&!"program".equals(pathType)) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}
			org.openrdf.model.URI uri = null;
			if(fData!=null){
				uri=_repository.URIref(fData.getIri());
				_logger.info("uri: " + uri.toString());
			}

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
				result = queryAndSerializeAsRDF(uri, new NTriplesWriter(out),
						out);
				resultType = MediaType.TEXT_PLAIN_TYPE;
			} else if (acceptTypes
					.contains(MediaType.APPLICATION_XHTML_XML_TYPE)&&"program".equals(pathType)) {
				// TODO serialize as XHTML -- probably best solution is to
				// implement a customer RDFWriter.
				// probably use:
				// http://repo.aduna-software.org/websvn/listing.php?repname=aduna&path=/org.openrdf/sesame/trunk/core/rio/rdfa/src/main/java/org/openrdf/rio/rdfa/&rev=8587&sc=1
				// Algo: (1) retrieve the top level RaUL element (e.g., page)
				// (2) iterate through the model and create XTHML code and
				// (3) annotate XML code with RDFa using RIO RDFAWriter
				_logger.info("ActiveRaULProcessor..."); // added by pcc
														// 19,Sep.11
				Integer currentClassIndex=formIndexGenerator.get(classURI.substring(classURI.indexOf("#")+1));
				if(currentClassIndex==null){
					formIndexGenerator.put(classURI.substring(classURI.indexOf("#")+1),1);
					currentClassIndex=0;
				}
				formIndexGenerator.put(classURI.substring(classURI.indexOf("#")+1), currentClassIndex+1);
				String formIdForClass = "http://www.activeraul.org/service/"+userId+"/"+URL_FORMS+"/"
						+classURI.substring(classURI.indexOf("#")+1)+currentClassIndex;
				if(_urls2ontologyMap.get(userId+formIdForClass.substring(formIdForClass.lastIndexOf("/")))!=null){
					String index=formIdForClass.charAt(formIdForClass.length()-1)+"";
					Integer i=Integer.parseInt(index);
					formIdForClass=formIdForClass.substring(0,formIdForClass.length()-1)+(i+1);
				}
				GenerateRaULRDFTest raulRdfGenerator = new GenerateRaULRDFTest(
						_repository, formIdForClass, classURI);
				String raulBasedGraph=raulRdfGenerator.generateRaULBasedRDFGraph();
				_repository.addString(raulBasedGraph, formIdForClass, RDFFormat.RDFXML, formIdForClass
						+ CONTEXT_FORM_DEFINITION_SUFFIX);
				InstanceIds ids=new InstanceIds(formIdForClass,formIdForClass
						+ CONTEXT_FORM_DEFINITION_SUFFIX);
				ids.setBaseUri(formIdForClass);
				ids.setOwlClass(classURI.substring(classURI.indexOf("#")+1));
				//_logger.info("***************"+ids.getBaseUri()+"****************");
				_urls2ontologyMap.put(userId+formIdForClass.substring(formIdForClass.lastIndexOf("/")),ids);
				

				//_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
                                
                //String rdfResult=out.toString();
				String rdfResult=raulBasedGraph;
				_tmpGraph.addString(rdfResult, ids.getIri(),
						RDFFormat.RDFXML, ids.getContext());
                                
				ActiveRaULProcessor processor = new ActiveRaULProcessor();
				uri=_repository.URIref(ids.getIri());
				result = processor.serializeXHTML(uri.toString(), _tmpGraph);
				resultType = MediaType.APPLICATION_XHTML_XML_TYPE;

				// ActiveRaULProcessor processor = new ActiveRaULProcessor();
				// result = processor.serializeXHTML(uri.toString(),
				// _repository);
				// resultType = MediaType.APPLICATION_XHTML_XML_TYPE;
				HTMLInterceptor interceptor=new HTMLInterceptor(result);
				String fieldsets=interceptor.getFieldSetContent();
				result+=fieldsets;
				if(ids.getBaseUri()!=null)
					result=addBaseUrlHiddenInput(ids.getBaseUri(),result);
				_logger.info("template key: "+(userId+'/'+classURI.substring(classURI.indexOf("#")+1)));
				dynamicGroupTemplages.put(userId+'/'+classURI.substring(classURI.indexOf("#")+1), fieldsets);
                                //_logger.info(result);
				_logger.info("ActiveRaULProcessor...(done)"); // added by pcc
																// 19,Sep.11
			} else { // this is default (irrespective what accept header was
						// sent by the client)
				// modified by pcc 1, Dec. 11
				_repository.dumpRDF(rdfWriter, fData.getContext());
				result = out.toString();
				resultType = MediaType.APPLICATION_XML_TYPE;
				// _logger.info("***********");
				// _repository.clearRepository();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		_logger.info("Porcessing From Get...(done)"); // added by pcc 19,Sep.11
		return Response.ok(result).type(resultType).build();
	}

	private String addBaseUrlHiddenInput(String baseUri,String html){
		//_logger.info("***************************************");
		return html+="<input type=\"hidden\" value=\""+baseUri+"\" id=\"baseUri\" name=\"baseUri\"></input>";
	}
	
	private String queryAndSerializeAsRDF(org.openrdf.model.URI uri,
			RDFWriter rdfWriter, StringWriter out) throws RepositoryException,
			RDFHandlerException {
		RepositoryResult<Statement> result = _repository.getStatements(uri,
				false);
		rdfWriter.startRDF();
		while (result.hasNext()) {
			rdfWriter.handleStatement(result.next());
		}
		rdfWriter.endRDF();
		return out.toString();
	}

	/**
	 * Create a data instance
	 * 
	 * @param headers
	 * @param userId
	 * @param formId
	 * @param content
	 * @return
	 */

	@POST
	@Path("{formid}")
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	public Response createRaulFormData(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formId, String content) {


		//_logger.info("Processing POST Data..."); // added by pcc 19,Sep.11
		//_logger.info("content: " + content);

		//_logger.info("userid: " + userId);
		//_logger.info("formid: " + formId);

		
		
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to
											// RDFXML
			content = convertJSON2XML(content);
		}

		if (format == null) { // no acceptable media type
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
					.build();
		}

		try {
			//for testing
		//	_logger.info(content);
			//for testing
			RaulRDFParser parser = parsePageIdentifier(format, content);
			/*if (!parser.isRaulRDF()) {
				_logger.info("File is not based on RAUL.Begin to convert it into RUAL-based rdf");

				_logger.info("Conversion from abitrary rdf to RAUL-based rdf finished");
				// to get page id after conversion
				// parser = parsePageIdentifier(format, content);
			}*/
			String formIri = extractPageIdentifier(headers, format, content,
					parser.getRaulPageID());
			//_logger.info("formIri: " + formIri);
			// http://w3c.org.au/raul/service/public/forms/foafedit116

			int index = formIri.lastIndexOf("/"); // modified by pcc
													// 13,Jul.11
			String context = "";
		//	_logger.info("index: " + index);

			//String tmpString = content.substring(
			//		content.indexOf("<rdf:subject>"),
			//		content.indexOf("</rdf:subject>"));
                        String tmpString=System.currentTimeMillis()+"";
			//_logger.info("tmpString: " + tmpString);
			// http://w3c.org.au/raul/service/public/forms/foafedit116/nnnnnn

			String tmpSubjectKey = tmpString.substring(tmpString
					.lastIndexOf("/") + 1);
		//	_logger.info("tmpSubjectKey: " + tmpSubjectKey);
			// tmpSubjectKey = nnnnnn

			String subjectKey = storeFormDataUrl(userId,
					formIri.substring(index + 1), tmpSubjectKey, formIri);
			// storeFormDataURL(public, foafedit116, nnnnnn,
			// http://w3c.org.au/raul/service/public/forms/foafedit116 )
			
			//_logger.info("index: " + index + " tmpString: " + tmpString
			//		+ " tmpSubjectKey: " + tmpSubjectKey + " subjectKey: "
			//		+ subjectKey);

			if (!(tmpSubjectKey.equals(subjectKey))) {
				content = content.replaceAll("(/{1})(" + tmpSubjectKey
						+ ")(<{1})", "/" + subjectKey + "<");
				content = content.replaceAll("(/{1})(" + tmpSubjectKey
						+ ")(_{1})", "/" + subjectKey + "_");
				formIri = formIri + "/" + subjectKey;
				context = formIri + CONTEXT_FORM_DATA_SUFFIX;
			} else {
				formIri = formIri + "/" + subjectKey;
				context = formIri + CONTEXT_FORM_DATA_SUFFIX;
			}

			// content = content.replaceAll("CREATEOperation",
			// "UPDATEOperation");
		//	_logger.info("Processing POST Data...(done) formIri " + formIri); // added
																				// by
		//	 RDFTripleExtractor _rdfTriples = new RDFTripleExtractor(content, formIri, format, context);																		// pcc
				
			//_logger.info(" " + content);// 19,Sep.11
			_repository.addString(content, formIri, format, context);
			//_logger.info("Processing POST Data...(done)"); // added by pcc
			
			 RDFTripleExtractor rdf = new RDFTripleExtractor(_repository);
            // RDFRepository tmpGraph = RDFRepositoryFactory.createInMemoryRepository();
			 RDFRepository tmpGraph = rdf.addTriples();
			//tmpGraph = _rdfTriples.addTriples();
			
			//RepositoryConnection conn = tmpGraph.createConnection();
			_repository.addGraph(tmpGraph);// 19,Sep.11

			// return Response.created(URI.create('/' +
			// subjectKey)).build();
			return Response.created(
					URI.create('/' + URLEncoder.encode(subjectKey, "UTF-8")))
					.build();
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

	@PUT
	@Path("{formid}/{dataid}")
	public Response updateRaulFormData(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formInstanceId,
			@PathParam("dataid") String dataInstanceId, String content) {

		_logger.info("Porcessing Data Instance Update..."); // added by pcc
															// 18,Jan.12

		_logger.info("userid: " + userId);
		_logger.info("formid: " + formInstanceId);
		_logger.info("dataid: " + dataInstanceId);

		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);
		if (format == RDFFormat.RDFJSON) { // handle JSON by converting to
											// RDFXML
			content = convertJSON2XML(content);
		}

		if (format == null) { // no acceptable media type
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
					.build();
		}

		try {
			InstanceIds fData = getFormData(userId, formInstanceId,
					dataInstanceId);
			if (fData == null) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formInstanceId)).build();
			} else {
				_repository.deleteContextAndTriples(fData.getContext());
				_repository.addString(content, fData.getIri(), format,
						fData.getContext());
				_logger.info("Porcessing Data Instance Update...(done)"); // added
																			// by
																			// pcc
																			// 18,Jan.12
				return Response.ok().build();
			}
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

	@DELETE
	@Path("{formid}/{dataid}")
	public Response deleteRaulFormData(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
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

	@POST
	@Path("/class")
	@Consumes({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML,
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON,
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN })
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Response extractClassAndComments(@Context HttpHeaders headers,
			@PathParam("userid") String userId, String content) {
		_logger.info("Extracting class..."); // added by pcc 19,Sep.11

		// check for a valid user
		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		// find the media type (RDF format) of input contents
		MediaType mediaType = headers.getMediaType();
		_logger.debug("Request media type: " + mediaType);

		RDFFormat format = determineRDFContent(mediaType);

		RaulRDFParser parser = null;
		String formIri = null;
		String pageId = null;
		MetaDataExtraction extractor = null;
		String temRdfContext = null;
		try {
			parser = parsePageIdentifier(format, content);
			if (!parser.isRaulRDF()) {
				pageId = getBaseUri(content);
			} else
				pageId = parser.getRaulPageID();

			formIri = extractPageIdentifier(headers, format, content, pageId);

			// example formIri: //
			// http://w3c.org.au/raul/service/public/forms/foafedit

			int index = formIri.lastIndexOf("/");
			String context = "";
			String tempFormId = formIri.substring(index + 1);
			// example tempFormId: foafedit String formId =
			String formId = storeFormDefinitionUrl(userId, tempFormId, formIri);
			_logger.info(formId + "==============================");
			if (!(tempFormId.equals(formId))) {
				if (parser.isRaulRDF() == true) {
					content = content.replaceAll("(/{1})(" + tempFormId
							+ ")(#{1})", "/" + formId + "#");
					content = content.replaceAll("(/{1})(" + tempFormId
							+ ")(/{1})", "/" + formId + "/"); // added
																// by
																// pcc
																// 11,Jan.12
					content = content.replaceAll("(/{1})(" + tempFormId
							+ ")(\"{1})", "/" + formId + "\"");
				}
				formIri = formIri.substring(0, index + 1) + formId;
				// formIri:
				// http://w3c.org.au/raul/service/public/forms/foafedit1
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
				// context =
				// "http://w3c.org.au/raul/service/public/forms/foafedit1-formDefinition";
			} else {
				context = formIri + CONTEXT_FORM_DEFINITION_SUFFIX;
				// context =
				// "http://w3c.org.au/raul/service/public/forms/foafedit-formDefinition";
			}
			
			if(_urls2ontologyMap.get(context+CONTEXT_FOR_TEMPORARY_STORING)==null){
				temRdfContext = storeRdf(content, context, pageId,
						parser.isRaulRDF());
				_urls2ontologyMap.put(context+CONTEXT_FOR_TEMPORARY_STORING,new InstanceIds(formIri, context+CONTEXT_FOR_TEMPORARY_STORING));
				_logger.info(temRdfContext + "storing original ontology====================================");
			}
			extractor = new MetaDataExtraction(_repository.createConnection());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject result = new JSONObject();
		try {
			List<String> classes = new ArrayList<String>();
			List<String> extractedClasses = new ArrayList<String>();
			List<String> comments=new ArrayList<String>();
			getClassesAndComments(extractedClasses,comments,extractor,pageId);
			List<String> newClasses=new ArrayList<String>();
			if (!parser.isRaulRDF()){
				classes.addAll(extractedClasses);
				newClasses=conversionAndStoring(classes, formIri,userId);
			}
			else{
				classes.add(pageId.substring(pageId.lastIndexOf("/")+1));
				//_logger.info(pageId+"###########################");
				newClasses.add(formIri.substring(formIri.lastIndexOf("/")+1));
			}
			StringBuffer buffer = new StringBuffer();
			result.put("buttons", createClassSelector(newClasses,classes));
			buffer = new StringBuffer();
			for (int i = 0; i < comments.size(); i++){
				if(i==0)
					buffer.append(createCommentText(true, comments.get(i),classes.get(i)) + "\r\n");
				else
					buffer.append(createCommentText(false, comments.get(i),classes.get(i)) + "\r\n");
			}
			result.put("comments", buffer.toString());
			result.put("currentTextarea", classes.get(0).substring(classes.get(0).indexOf("#") + 1));
			//if (temRdfContext != null)
				//_repository.deleteContextAndTriples(temRdfContext);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// store the rdf into rdf store after conversion from other ontology to
		// raul-based rdf
		return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON)
				.build();
	}

	// need to add parameters later
	/*
	 * private String createClassButton(String clazz) { // just for testing
	 * return "<input type='button' id='class_" + clazz + "' value='" +
	 * clazz.substring(clazz.indexOf("#")+1) +
	 * "' style='margin:15px 15px;' onclick='showCorrespondingForm(this.id);' target='"
	 * + clazz + "'/>"; }
	 */

	private String createClassSelector(List<String> classes,List<String> originalClasses) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<label property='raul:label' style='float: left;width: 60px;margin:6px 15px;'>Classes:</label>");
		buffer.append("<select id='classSelector' name='classSelector' onchange='changeComment();'>");
		for (int i = 0; i < classes.size(); i++) {
			String clazz = classes.get(i);
			String originalClazz=originalClasses.get(i);
			buffer.append("	<option value='" + clazz + "'>"
					+ originalClazz.substring(originalClazz.indexOf("#") + 1) + "</option>");
		}
		buffer.append("</select>");
		// lack of onclick method
		buffer.append("<a id='generateRelevantRdf' class='button' style='clear:none;margin:8px 12px;' onclick='createFormBySelectedClass();' href='javascript:void(0)'><span>Create Form</span></a>");
		buffer.append("<input id='serviceURLforClass' type='hidden' name='serviceURLforClass' value=''/>");
		return buffer.toString();
	}

	private String createCommentText(boolean show, String comment,String clazz) {
		// just for testing
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div id='"+clazz.substring(clazz.indexOf("#") + 1)+"'");
		if (show == true)
			buffer.append(" style='display:block;'>");
		else
			buffer.append(" style='display:none;'>");
		buffer.append("<textarea readOnly style='resize:none;' cols='80' rows='3'>");
		buffer.append(comment);
		buffer.append("</textarea>");
		buffer.append("</div>");
		return buffer.toString();
	}

	private String getBaseUri(String content) {
		XMLBaseParser parser = new XMLBaseParser(new StringReader(content));
		return parser.getXMLBase();
	}

	private String storeRdf(String content, String context, String baseUri,
			boolean isRaulBased) throws RepositoryException, RDFParseException,
			IOException, MappableContainerException, RepositoryConfigException {
		if (isRaulBased == true) {
			_repository.addString(content, baseUri, RDFFormat.RDFXML, context);
			return null;
		} else {
			_repository.addString(content, baseUri, RDFFormat.RDFXML, context
					+ CONTEXT_FOR_TEMPORARY_STORING);
			return context + CONTEXT_FOR_TEMPORARY_STORING;
		}
	}

	//older version of conversionAndStoring
	/*private List<String> conversionAndStoring(List<String> classes, String baseUri,String userId) {
		List<String[]> raulRdfs = new ArrayList<String[]>();
		List<String> finalClassesPageId=new ArrayList<String>();
		try {
			for (int i = 0; i < classes.size(); i++) {
				String[] info = new String[2];
				String classUri = classes.get(i);
				String partUri=baseUri.substring(baseUri.lastIndexOf("/")+1);
				String formIdForClass = "http://www.activeraul.org/service/"+userId+"/"+URL_FORMS+"/"
						+ partUri+classUri.substring(classUri.indexOf("#")+1);
				//_logger.info("#######################"+formIdForClass);
				GenerateRaULRDF raulRdfGenerator = new GenerateRaULRDF(
						_repository, formIdForClass, classUri);
				GenerateRaULRDFTest raulRdfGenerator = new GenerateRaULRDFTest(
						_repository, formIdForClass, classUri);
				info[0] = formIdForClass;
				info[1] = raulRdfGenerator.generateRaULBasedRDFGraph();
				raulRdfs.add(info);
				finalClassesPageId.add(formIdForClass.substring(formIdForClass.lastIndexOf("/")+1));
                                //_logger.info(info[1]);
			}
			int index=0;
			for (String[] s : raulRdfs) {
				if(index==0){
					index++;
					_logger.info(s[1]);
				}
				_repository.addString(s[1], s[0], RDFFormat.RDFXML, s[0]
						+ CONTEXT_FORM_DEFINITION_SUFFIX);
				InstanceIds ids=new InstanceIds(s[0],s[0]
						+ CONTEXT_FORM_DEFINITION_SUFFIX);
				ids.setBaseUri(s[0]);
				//_logger.info("***************"+ids.getBaseUri()+"****************");
				_urls2ontologyMap.put(userId+s[0].substring(s[0].lastIndexOf("/")),ids);
				//_logger.info(userId+s[0].substring(s[0].lastIndexOf("/")));
			}
		} catch (Exception e) {
			_logger.error(e);
			e.printStackTrace();
		}
		return finalClassesPageId;
	}*/
	
	private List<String> conversionAndStoring(List<String> classes, String baseUri,String userId) {
		List<String> finalClassesPageId=new ArrayList<String>();
		try {
			for (int i = 0; i < classes.size(); i++) {
				//String[] info = new String[2];
				String classUri = classes.get(i);
				finalClassesPageId.add(classUri);
			}
		} catch (Exception e) {
			_logger.error(e);
			e.printStackTrace();
		}
		return finalClassesPageId;
	}
	
	@GET
	@Path("{formid}/{dataid}")
	@Produces({ RDFMediaType.APPLICATION_RDFXML, MediaType.APPLICATION_XML, // handles
																			// xml
			RDFMediaType.APPLICATION_RDFJSON, MediaType.APPLICATION_JSON, // handles
																			// json
			RDFMediaType.TEXT_N3, MediaType.TEXT_PLAIN, // handles N3
			MediaType.APPLICATION_XHTML_XML })
	// handles XHTML+RDFa
	public Response getRaulFormData(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@PathParam("formid") String formId,
			@PathParam("dataid") String dataId,
            @QueryParam("pathType")   String pathType,
            @QueryParam("classURI") String classURI) {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		_logger.info("userid: " + userId);
		_logger.info("formid: " + formId);
		_logger.info("dataid: " + dataId);
                _logger.info("pathType: "+pathType);

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
			if (fData == null&&!"program".equals(pathType)) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity(String.format("Parameter '%s' is not valid.",
								formId)).build();
			}
			org.openrdf.model.URI uri = null;
			if(fData!=null)
				uri=_repository.URIref(fData.getIri());

			//_logger.info("fData.getIri() " + fData.getIri());
			//_logger.info("fData.getContext() " + fData.getContext());

			// instantiate the corresponding writer element
			if (acceptTypes.contains(RDFMediaType.APPLICATION_RDFJSON_TYPE)
					|| acceptTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
				String xml = queryAndSerializeAsRDF(uri, rdfWriter, out);
				JSONObject json = (JSONObject) new XMLSerializer().read(xml);
				result = json.toString(2);
				resultType = MediaType.APPLICATION_JSON_TYPE;
			} else if (acceptTypes.contains(RDFMediaType.TEXT_N3_TYPE)
					|| acceptTypes.contains(MediaType.TEXT_PLAIN)) {
				result = queryAndSerializeAsRDF(uri, new NTriplesWriter(out),
						out);
				resultType = MediaType.TEXT_PLAIN_TYPE;
			} else if (acceptTypes
					.contains(MediaType.APPLICATION_XHTML_XML_TYPE)&&"program".equals(pathType)) {
				// added by pcc
				_logger.info("Processing RDFXML data to create XHTML form"); // 19,Sep.11
				
				_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
				// added by shepherd Liu on 30 Nov 12 to accommodate the
				// requirements of rendering different rdf.
				String rdfxmlContent = out.toString();
				_tmpGraph.addString(rdfxmlContent, fData.getIri(),
						RDFFormat.RDFXML, fData.getContext());

				// added by shepherd Liu on 30 Nov 12
				// to judge whether the input file is a raulRdf or another type
				// of rdf
				//RaulRDFParser parser = new RaulRDFParser();
				/*
				 * if (!parser.isRaulRDF(rdfxmlContent)) {
				 * _logger.info("space for new generator"); } else {
				 */
				_logger.info("ActiveRaULProcessor Get Data...");
				// added by pcc 16, Jan. 12
				ActiveRaULProcessor processor = new ActiveRaULProcessor();
				String formUri = uri.toString().substring(0,
						uri.toString().lastIndexOf("/"));
				result = processor.serializeXHTML(formUri, _tmpGraph); // modified
				// result = processor.serializeXHTML(uri.toString(),
				// _repository);
				_logger.info("template key: "+(userId+'/'+fData.getOwlClass()));
				String template=dynamicGroupTemplages.get(userId+"/"+fData.getOwlClass());
				result+=template;
				if(fData.getBaseUri()!=null)
					result=addBaseUrlHiddenInput(fData.getBaseUri(),result);
				resultType = MediaType.APPLICATION_XHTML_XML_TYPE;
				_logger.info("ActiveRaULProcessor Get Data...(done)");
				// }
			} else { // this is default (irrespective what accept header was
						// sent by the client)
						// result = queryAndSerializeAsRDF(uri, rdfWriter, out);
				// resultType = MediaType.APPLICATION_XML_TYPE;

				// added by pcc 16, Jan. 12
				_repository.dumpRDF(rdfWriter, fData.getContext());
				_tmpGraph.clearRepository();
                                String str=out.toString();
                               // _logger.info(str);
				_tmpGraph.addString(str, fData.getIri(),
						RDFFormat.RDFXML, fData.getContext());

				StringBuffer buf = out.getBuffer();
				buf.setLength(0);

				// added by pcc 7, Dec. 11
				ActiveRaULProcessor processor = new ActiveRaULProcessor();
				_dataGraph.clearRepository();
				String formUri = uri.toString().substring(0,
						uri.toString().lastIndexOf("/"));
				processor.dataGraphGen_RDF(formUri, _tmpGraph, _dataGraph); // modified
																			// by
																			// pcc
																			// 16,
																			// Jan.
																			// 12
				// processor.dataGraphGen_RDF(uri.toString(), _repository,
				// _dataGraph);
				_dataGraph.dumpRDF(rdfWriter, "");
				result = out.toString();
				resultType = MediaType.APPLICATION_XML_TYPE;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
		_logger.info("Processing Get Data...(done)"); // added by pcc 19,Sep.11
		return Response.ok(result).type(resultType).build();
	}
	
	@GET
	@Path("/allTriples")
	@Produces("text/plain")
	// handles XHTML+RDFa
	public Response getRaulFormData2(@Context HttpHeaders headers,
			@PathParam("userid") String userId) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		RaULSearch search = new RaULSearch(_repository);
		
	    String output = search.getCompleteGraph();

		String result =output;
		
		return Response.ok(result).type( "text/plain").build();
	}
	
	@GET
	@Path("/allTriplesOfClass")
	@Produces("text/plain")
	// handles XHTML+RDFa
	public Response getRaulFormDataForClass(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@QueryParam("range") String range) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		RaULSearch search = new RaULSearch(_repository);
		
	    String output = search.getAllTriplesOfClass(range);

		String result =output;
		
		return Response.ok(result).type( "text/plain").build();
	}
	
	@GET
	@Path("/allTriplesAsGraph")
	@Produces("text/plain")
	// handles XHTML+RDFa
	public Response getRaulFormGraph(@Context HttpHeaders headers,
			@PathParam("userid") String userId,
			@QueryParam("range") String range) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		RaULSearch search = new RaULSearch(_repository);
		
	    String output = search.getGraph(range);

		String result =output;
		
		return Response.ok(result).type( "text/plain").build();
	}
	
	@GET
	@Path("/instances")
	@Produces("text/plain")
	// handles XHTML+RDFa
	public Response getRaulFormData(@Context HttpHeaders headers,
			@PathParam("userid") String userId, 
			@QueryParam ("range") String range) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		_logger.info("userid: " + userId);
		_logger.info("range: " + range);
		

		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		RaULSearch search = new RaULSearch(_repository);
		List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
		StringWriter out = new StringWriter();
		RDFWriter rdfWriter = new RDFXMLPrettyWriter(out); // default is RDF/XML
		// search.getInstances(range);
		  HashMap<String, HashMap<String, String>> map =search.getInstances(range,"");
		  String output = "";
		  output = output + "<div id=\"result\">\n" ;
		//  output = output + "\t<div id=\"result1\" style=\"float:left;clear:both;padding:0px 0px 16px 0px;\"><a href=\"javascript:void(0)\" class=\"resultURI\" onclick=\"fillValue(this)\">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#2months</a><p class=\"resultDesc\">is property of : 2 months</p></div>\n";
		 
		  int count=1;
		  
		  System.out.println(map);
		  if (map.size()>0) {
			  		  
		  for (Entry<String,HashMap<String, String>> entry : map.entrySet()){
			  String instance = entry.getKey();
			  String[] instances = instance.split("\\|");
			  output = output + "\t<div id=\"result"+count+"\" style=\"float:left;clear:both;padding:0px 0px 16px 0px;\">\n " +
			  					"\t\t<p style=\"float:left;\"><a about=\""+instances[0]+"\" href=\"javascript:void(0)\" class=\"resultURI\" label=\""+instances[1].replaceAll("\"", "")+"\">"+instances[1].replaceAll("\"", "")+"</a>&nbsp;is a "+range.split("#")[1]+"</p>\n";
			  					//+"\t\t\t<p class=\"resultDesc\">\n";
			  HashMap<String, String> properties = entry.getValue();
			  
			//  output = output +  "\t\t\t\t<p class=\"resultDesc\" about=\""+"http://www.w3.org/2000/01/rdf-schema#label"+"\" propvalue=\""+instances[1].replaceAll("\"", "")+"\">  "+"label"+" : "+instances[1].replaceAll("\"", "")+"  </a>\t";
				
			   if(properties.size()>0) {
				  int innerCount=1;
				  
				  for (Entry<String, String> entry2 : properties.entrySet()){
				  
					  String[] property = entry2.getKey().split("\\|");
					  String[] value = entry2.getValue().split("\\|");
				  
					// _logger.info("Property : " + property[0] + "  " + property[1]);
					/// _logger.info("Value : " + value[0] + "  " + value[1]);
				  
					  if(innerCount>1)
						  output = output +  "\t\t\t\t<p class=\"resultDesc\" about=\""+property[0]+"\" propvalue=\""+value[0].replace("\"", "")+"\">  &nbsp;&nbsp;|&nbsp;&nbsp; "+property[1]+" : "+value[1].replace("\"", "")+"  </a></p>\t";
					  else
						  output = output +  "\t\t\t\t<p class=\"resultDesc\" style=\"clear:left;\" about=\""+property[0]+"\" propvalue=\""+value[0].replace("\"", "")+"\">"+property[1]+" : "+value[1].replace("\"", "")+"  </a></p>\t";
					  
					  innerCount++;
				  //output = output +  "<a href=\""+value[0]+"\" class=\"resultURI\">"+value[1]+"</a>\n";
				 }
			/*  output = output + "\t\t\t</p>\n" +
			  		"\t</div>"  + "\n";*/

			}
				  output = output + "\t</div>"  + "\n";
			   count++;
		  }
		  output = output + "</div>";
		  }
		  /* output = output + "\t\t\t</p>\n" +
			  		"\t</div>"  + "\n"; */

		  _logger.info(output);
		  String result =output;

		
		return Response.ok(result).type( "text/plain").build();
	}
	
	@GET
	@Path("/search")
	@Produces("text/plain")
	// handles XHTML+RDFa
	public Response getRaulInstances(@Context HttpHeaders headers,
			@PathParam("userid") String userId, 
			@QueryParam ("range") String range,
			@QueryParam ("q") String queryString) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		_logger.info("Processing Get Data..."); // added by pcc 19,Sep.11

		_logger.info("userid: " + userId);
		_logger.info("range: " + range);
		_logger.info("Query String: " + queryString);
		

		if (!_userManager.isValidUser(userId)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		RaULSearch search = new RaULSearch(_repository);
		List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
		StringWriter out = new StringWriter();
		RDFWriter rdfWriter = new RDFXMLPrettyWriter(out); // default is RDF/XML
		// search.getInstances(range);
		  HashMap<String, HashMap<String, String>> map =search.getInstances(range,queryString);
		  String output = "";
		  output = output + "<div id=\"result\">\n" ;
		//  output = output + "\t<div id=\"result1\" style=\"float:left;clear:both;padding:0px 0px 16px 0px;\"><a href=\"javascript:void(0)\" class=\"resultURI\" onclick=\"fillValue(this)\">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#2months</a><p class=\"resultDesc\">is property of : 2 months</p></div>\n";
		 
		  int count=1;
		  
		  System.out.println(map);
		  if (map.size()>0) {
			  		  
		  for (Entry<String,HashMap<String, String>> entry : map.entrySet()){
			  String instance = entry.getKey();
			  String[] instances = instance.split("\\|");
			  output = output + "\t<div id=\"result"+count+"\" style=\"float:left;clear:both;padding:0px 0px 16px 0px;\">\n " +
			  					"\t\t<p style=\"float:left;\"><a about=\""+instances[0]+"\" href=\"javascript:void(0)\" class=\"resultURI\" label=\""+instances[1].replaceAll("\"", "")+"\">"+instances[1]+"</a>&nbsp;is a "+range.split("#")[1]+"</p>\n";
			  					//+"\t\t\t<p class=\"resultDesc\">\n";
			  HashMap<String, String> properties = entry.getValue();
			  
			 // output = output +  "\t\t\t\t<p class=\"resultDesc\" about=\""+"http://www.w3.org/2000/01/rdf-schema#label"+"\" propvalue=\""+instances[1].replaceAll("\"", "")+"\">  "+"label"+" : "+instances[1].replaceAll("\"", "")+"  </a>\t";
				
			   if(properties.size()>0) {
				  int innerCount=1;
				  
				  for (Entry<String, String> entry2 : properties.entrySet()){
				  
					  String[] property = entry2.getKey().split("\\|");
					  String[] value = entry2.getValue().split("\\|");
				  
					// _logger.info("Property : " + property[0] + "  " + property[1]);
					/// _logger.info("Value : " + value[0] + "  " + value[1]);
				  
					  if(innerCount>1)
						  output = output +  "\t\t\t\t<p class=\"resultDesc\" about=\""+property[0]+"\" propvalue=\""+value[0]+"\">  - - -  "+property[1]+" : "+value[1]+"  </a>\t";
					  else
						  output = output +  "\t\t\t\t<p class=\"resultDesc\" style=\"clear:left;\" about=\""+property[0]+"\" propvalue=\""+value[0]+"\">"+property[1]+" : "+value[1]+"  </a>\t";
					  
					  innerCount++;
				  //output = output +  "<a href=\""+value[0]+"\" class=\"resultURI\">"+value[1]+"</a>\n";
				 }
			  output = output + "\t\t\t</p>\n" +
			  		"\t</div>"  + "\n";
			  /* output = output + "\t</div>"  + "\n"; */
			  count++;
			}
		  }

		  }
		  output = output + "</div>";
		  _logger.info(output);
		  String result =output;
		
		return Response.ok(result).type( "text/plain").build();
	}
	
	private void getClassesAndComments(List<String> classes,List<String> comments,MetaDataExtraction extractor,String pageId) throws Exception{
		Map<String,String> classAndComment=extractor.getOntologyClasses(pageId);
		if(classAndComment.size()==0)
			return;
		Iterator<String> it=classAndComment.keySet().iterator();
		for(;it.hasNext();){
			classes.add(it.next());
		}
		Collections.sort(classes);
		for(String clazz:classes){
			comments.add(classAndComment.get(clazz));
		}
	}
	
}
