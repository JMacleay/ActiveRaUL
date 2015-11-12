package ie.deri.raul.processor;

import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.resources.RaULResource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
//import org.openrdf.model.ValueFactory;
//import org.openrdf.query.GraphQuery;
//import org.openrdf.query.GraphQueryResult;
//import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
//import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
//import org.openrdf.repository.object.ObjectQuery;
//import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfa.RDFaMetaWriter;
//import org.openrdf.rio.turtle.TurtleWriter;

//added by pcc 2, Dec. 11
import org.openrdf.rio.rdfxml.RDFXMLWriter;	
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
//added by pcc 2, Dec. 11

import raul.Button;
import raul.CREATEOperation;
import raul.CRUDOperation;
import raul.Checkbox;
import raul.DELETEOperation;
import raul.DynamicGroup;
import raul.Group;
import raul.Listbox;
import raul.Listitem;
import raul.Page;
import raul.READOperation;
import raul.Radiobutton;
import raul.Textbox;
import raul.UPDATEOperation;
import raul.WidgetContainer;
import rdf.Statement;
import rdfs.Class;
import rdfs.Resource;

/**
 * Implements the RaUL view generation. It generates, based on a URI input of a
 * Page, and the expected return format the RDF Graph of a RaUL page from the
 * supplied source.
 * 
 * @author Armin Haller
 */

public class ActiveRaULProcessor implements IRaULProcessor {
	
	//Set<Object> GroupSet = new HashSet<Object>();
	org.openrdf.model.URI raulPage;
	org.openrdf.model.URI raulWidgetContainer;
	org.openrdf.model.URI raulCRUDOperation;
	org.openrdf.model.URI raulCREATEOperation;
	org.openrdf.model.URI raulREADOperation;	
	org.openrdf.model.URI raulUPDATEOperation;
	org.openrdf.model.URI raulDELETEOperation;
	org.openrdf.model.URI raulTextbox;
	org.openrdf.model.URI raulListbox;
	org.openrdf.model.URI raulListitem;
	org.openrdf.model.URI raulButton;
	org.openrdf.model.URI raulGroup;
	org.openrdf.model.URI raulDynamicGroup;	//added by pcc
	org.openrdf.model.URI raulselected;
	org.openrdf.model.URI raulgroup;
	org.openrdf.model.URI raulmethod; //org.openrdf.model.URI raulmethods;	
	org.openrdf.model.URI booleandatatype;
	org.openrdf.model.URI stringdatatype;
	org.openrdf.model.URI integerdatatype; //added by pcc
	org.openrdf.model.URI raullist;
	org.openrdf.model.URI rdfSubject;
	org.openrdf.model.URI rdfPredicate;
	org.openrdf.model.URI rdfObject;
	org.openrdf.model.URI raulRadiobutton;
	org.openrdf.model.URI raulCheckbox;
	org.openrdf.model.URI raulcommand;
	org.openrdf.model.URI raulaction;
	org.openrdf.model.URI raulvalue;
	org.openrdf.model.URI raullabel;
	org.openrdf.model.URI raulname;
	org.openrdf.model.URI raultitle;
	org.openrdf.model.URI raulclass;
	org.openrdf.model.URI raulid;
	org.openrdf.model.URI raulhidden;
	org.openrdf.model.URI raulisIdentifier;	//added by pcc 27Jan12
	org.openrdf.model.URI raulchecked;
	org.openrdf.model.URI rauldisabled;
	org.openrdf.model.URI raulrow;
	org.openrdf.model.URI raulsize;
	org.openrdf.model.URI raulwidgets;
	Integer opendiv;
	Integer openspan;

	ObjectConnection c;
	ObjectConnection c1;
	//private static Log _logger = LogFactory.getLog(RaULResource.class);
	
	
	public String serializeXHTML(String uri, RDFRepository _repository) throws IOException, RepositoryConfigException, RepositoryException, QueryEvaluationException {
		/*//added by pcc 21,Jun. 11 for profiling
		int numTextbox=0, numButton=0, numRButton=0,
		numCheckbox=0, numListbox=0, numListItem=0;
		long startTime=0, timePage=0, TimeWContainer=0, accTimeTextbox=0, accTimeButton=0,
		accTimeRButton=0, accTimeCheckbox=0, accTimeListbox=0, accTimeListItem=0;
		long totalTime_start=0, totalTime=0;
		totalTime_start = System.currentTimeMillis();
		//added by pcc 21,Jun. 11 for profiling*/
		
		// Define output writer
		StringWriter out = new StringWriter();
		RDFaMetaWriter writer = new RDFaMetaWriter(out);
		
		// Create the connection to the repository		
		c = _repository.createObjectConnection();		
		
		org.openrdf.model.URI u = c.getValueFactory().createURI(uri);
		
		// try {
		// TurtleWriter turtleWriter = new TurtleWriter(System.out);
		// c
		// .prepareGraphQuery(QueryLanguage.SERQL,
		// // "CONSTRUCT * FROM {<"+ uri
		// //
		// //
		// +">} p {y}  USING NAMESPACE raul = <http://purl.org/NET/raul#>").evaluate(turtleWriter);
		// "CONSTRUCT * FROM {x} p {y}  USING NAMESPACE raul = <http://purl.org/NET/raul#>")
		// .evaluate(turtleWriter);
		//		
		// // TurtleWriter turtleWriter1 = new TurtleWriter(System.out);
		// // c.prepareGraphQuery(QueryLanguage.SERQL,
		// // "CONSTRUCT * FROM {x} p {y}").evaluate(turtleWriter1);
		//		
		// } catch (Exception e) {
		// } finally {
		// }

		// Define output writer
		//StringWriter out = new StringWriter();
		//RDFaMetaWriter writer = new RDFaMetaWriter(out);
		

		uriInit(_repository);

		// ObjectQuery query = null;
		// try {
		// query = c.prepareObjectQuery(
		// "PREFIX raul:<http://purl.org/NET/raul#>\n PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		// "SELECT ?title WHERE {<http://raul.deri.ie/forms/addproduct#addProduct> a raul:Page. <http://raul.deri.ie/forms/addproduct#addProduct> raul:title ?title}");
		// } catch (MalformedQueryException e1) {
		// e1.printStackTrace();
		// }
		// Object page1 = query.evaluate().singleResult();
		// System.out.println(page1);
		
		// Get the RaUL page object		
		Page page = c.getObject(Page.class, u);		
		
		Map<String, String> namespaceTable = new HashMap<String, String>();
		namespaceTable.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
		namespaceTable.put("http://www.w3.org/2002/07/owl#", "owl");
		namespaceTable.put("http://www.w3.org/2001/XMLSchema#", "xsd");
		namespaceTable.put("http://purl.org/NET/raul#", "raul");
		namespaceTable.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
		//writer.startRDF(uri, namespaceTable);
		
		
		if (!page.getRaulTitles().isEmpty()) {
			/*//added by pcc 21,Jun. 11 for profiling
			startTime = System.currentTimeMillis();
			//added by pcc 21,Jun. 11 for profiling*/

			/*//modified by pcc 27, Jun. 11
			Map<String, String> namespaceTable = new HashMap<String, String>();
			namespaceTable.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
			namespaceTable.put("http://www.w3.org/2002/07/owl#", "owl");
			namespaceTable.put("http://www.w3.org/2001/XMLSchema#", "xsd");
			namespaceTable.put("http://purl.org/NET/raul#", "raul");
			namespaceTable.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
			*/
			writer.startRDF(uri, namespaceTable);			
			
			// Write the properties of the page object			
			writer.startMeta();			

			// Write the title property
			Iterator<String> iteratorTitles = page.getRaulTitles().iterator();
			Set<String> set = new LinkedHashSet<String>();
			while (iteratorTitles.hasNext()) {
				String var = iteratorTitles.next();
				if (!set.contains(var)) {
					Literal title = _repository.Literal(var, null);
					writer.handleMetaAttribute(raultitle, title);
					out.append("	<title>" + var + "</title>\n");
					set.add(var);
				} else {
					set.add(var);
				}
			}
			set.clear();

			writer.endMeta();

			// Start the Page properties in the Body
			Set<URI> raulPageProperties = new HashSet<URI>();
			raulPageProperties.add(raulPage);
			writer.startNode(uri, raulPageProperties);
			out.append("	<span style=\"display:none;\">\n");

			// Write the class property
			Set<String> clazzes = page.getRaulClasses();
			writeStringProperty(raulclass, clazzes, 2, writer, _repository);

			// Write the id property
			Set<String> ids = page.getRaulIds();
			writeStringProperty(raulid, ids, 2, writer, _repository);

			// Write the list property
			Set<Class> widgetcontainers = page.getRaulLists();
			writeObjectProperty(raullist, widgetcontainers, 2, writer, _repository);

			out.append("	</span>\n");
			writer.endNode(uri, raulPageProperties);
			
			/*//added by pcc 21,Jun. 11 for profiling
			timePage = System.currentTimeMillis() - startTime;
			//added by pcc 21,Jun. 11 for profiling*/
		}else{ //added by pcc 27, Jun. 11
			out.append("<div ");
			writer.setNamespace(uri, namespaceTable);			
			out.append(">\n");
			// Start the Page properties in the Body
			Set<URI> raulPageProperties = new HashSet<URI>();
			raulPageProperties.add(raulPage);
			writer.startNode(uri, raulPageProperties);
			out.append("	<span style=\"display:none;\">\n");

			// Write the class property
			Set<String> clazzes = page.getRaulClasses();
			writeStringProperty(raulclass, clazzes, 2, writer, _repository);

			// Write the id property
			Set<String> ids = page.getRaulIds();
			writeStringProperty(raulid, ids, 2, writer, _repository);
			
			// Write the list property
			Set<Class> widgetcontainers = page.getRaulLists();
			writeObjectProperty(raullist, widgetcontainers, 2, writer, _repository);

			out.append("	</span>\n");
			writer.endNode(uri, raulPageProperties);
		}
		
		/*//added by pcc 21,Jun. 11 for profiling
			startTime = System.currentTimeMillis();
		//added by pcc 21,Jun. 11 for profiling*/			
		
		// Get all widgetContainerLists in the Page
		Set<Class> widgetContainerLists = page.getRaulLists();
		Set<Object> setWidgetContainerLists = new HashSet<Object>();
		
		for (Iterator<Class> iWidgetContainerLists = widgetContainerLists.iterator(); iWidgetContainerLists.hasNext();) {
			
			Object var = iWidgetContainerLists.next();
			// Get the next WidgetContainerList
			
			if (!setWidgetContainerLists.contains(var)) {				
				org.openrdf.model.URI uriWidgetContainerListObject = c.getValueFactory().createURI(var.toString());
				
				Resource widgetContainerSequence = c.getObject(Resource.class, uriWidgetContainerListObject);				
				// Get all WidgetContainers in the RDF Seq				
				String widgetContainerList = widgetContainerSequence.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				
				String[] WidgetContainerString = widgetContainerList.split(", ");
				
				// Write the WidgetContainer Sequence
				writeSequenceList(uriWidgetContainerListObject, WidgetContainerString, 0, out, _repository);

				Set<Object> setWidgetContainer = new HashSet<Object>();

				// Iterate through the WidgetContainers
				for (int iWidgetContainer = 0; iWidgetContainer < WidgetContainerString.length; iWidgetContainer++) {
					
					Object WidgetContainerList = WidgetContainerString[iWidgetContainer];
					Object varWidgetContainer = WidgetContainerString[iWidgetContainer];
					if (!setWidgetContainer.contains(varWidgetContainer)) {
						
						org.openrdf.model.URI uriWidgetContainer = c.getValueFactory().createURI(WidgetContainerList.toString());
						
						// Get the WidgetContainer object
						WidgetContainer widgetContainer = c.getObject(WidgetContainer.class, uriWidgetContainer);						
						Set<CRUDOperation> methods = widgetContainer.getRaulMethods();
						Set<String> actions = widgetContainer.getRaulActions();
						Set<String> WidgetContainerClasses = widgetContainer.getRaulClasses();
						Set<String> WidgetContainerIds = widgetContainer.getRaulIds();
						Set<String> WidgetContainerNames = widgetContainer.getRaulNames();
						Set<String> WidgetContainerTitles = widgetContainer.getRaulTitles();						
						Set<Class> widgetList = widgetContainer.getRaulLists();
						
						Set<URI> raulWidgetContainerProperties = new HashSet<URI>();
						raulWidgetContainerProperties.add(raulWidgetContainer);						
						writer.startNode(uriWidgetContainer.toString(), raulWidgetContainerProperties);
						out.append("	<span style=\"display:none;\">\n");
						
						// Write the methods property
						writeMethodsProperty(methods, 2, writer, _repository);

						// Write the actions property
						writeStringProperty(raulaction, actions, 2, writer, _repository);

						// Write the class property
						writeStringProperty(raulclass, WidgetContainerClasses, 2, writer, _repository);

						// Write the id property
						writeStringProperty(raulid, WidgetContainerIds, 2, writer, _repository);

						// Write the name property
						writeStringProperty(raulname, WidgetContainerNames, 2, writer, _repository);

						// Write the titles property
						writeStringProperty(raultitle, WidgetContainerTitles, 2, writer, _repository);

						// Write the list property
						writeListsProperty(widgetList, 2, writer, _repository);

						// End the WidgetContainer properties
						out.append("	</span>\n");
						writer.endNode(uri, raulWidgetContainerProperties);

						Set<URI> raulCRUDOperationProperties = new HashSet<URI>();
						Set<Object> setCRUDOperations = new HashSet<Object>();
						Set<String> operationClass = new LinkedHashSet<String>();						
						
						for (Iterator<CRUDOperation> methodsiter = methods.iterator(); methodsiter.hasNext();) {
							Object varMethods = methodsiter.next();
							if (!setCRUDOperations.contains(varMethods)) {								
								org.openrdf.model.URI unknownCRUDOperation = c.getValueFactory().createURI(varMethods.toString());								
								// try CREATE Operation
								try {
									CREATEOperation operation = c.getObject(CREATEOperation.class, unknownCRUDOperation);
									operationClass.add("CREATEOperation");
									raulCRUDOperationProperties.add(raulCREATEOperation);									
								} catch (ClassCastException e) {
								}

								// try READ Operation
								try {
									READOperation operation = c.getObject(READOperation.class, unknownCRUDOperation);
									operationClass.add("READOperation");
									raulCRUDOperationProperties.add(raulREADOperation);									
								} catch (ClassCastException e) {
								}

								// try UPDATE Operation
								try {
									UPDATEOperation operation = c.getObject(UPDATEOperation.class, unknownCRUDOperation);
									operationClass.add("UPDATEOperation");
									raulCRUDOperationProperties.add(raulUPDATEOperation);									
								} catch (ClassCastException e) {
								}

								// try DELETE Operation
								try {
									DELETEOperation operation = c.getObject(DELETEOperation.class, unknownCRUDOperation);
									operationClass.add("DELETEOperation");
									raulCRUDOperationProperties.add(raulDELETEOperation);									
								} catch (ClassCastException e) {
								}
								
								writer.startNode(varMethods.toString(), raulCRUDOperationProperties);
								writer.endNode(varMethods.toString(), raulCRUDOperationProperties);								

								setCRUDOperations.add(varMethods);
							} else {								
								setCRUDOperations.add(varMethods);
							}
						}
						
						writeTagForm(operationClass, actions, WidgetContainerClasses, WidgetContainerIds, out);
						
						widgetsHandler(widgetList, uri, out, writer, _repository);
						
						out.append("</form>\n");
						
						setWidgetContainer.add(varWidgetContainer);
					} else {
						setWidgetContainer.add(varWidgetContainer);
					}
				}

				setWidgetContainerLists.add(var);

			} else {
				setWidgetContainerLists.add(var);
			}

		}
		
		// rdfWriter.close();
		if (!page.getRaulTitles().isEmpty())
			writer.endRDF();
		else
			out.append("</div>\n"); //added by pcc 27, Jun. 11
		
		/*//added by pcc 21,Jun. 11 for profiling
		totalTime = System.currentTimeMillis() - totalTime_start;		
		out.append("________________________________________________________________________________\n");				
		out.append("timePage:" + timePage + "\n");
		out.append("TimeWContainer:" + TimeWContainer + "\n");
		out.append("numTextbox:" + numTextbox + "\taccTimeTextbox:" + accTimeTextbox + "\n");
		out.append("numButton:" + numButton + "\taccTimeButton:" + accTimeButton + "\n");
		out.append("numRButton:" + numRButton + "\taccTimeRButton:" + accTimeRButton + "\n");
		out.append("numCheckbox:" + numCheckbox + "\taccTimeCheckbox:" + accTimeCheckbox + "\n");
		out.append("numListbox:" + numListbox + "\taccTimeListbox:" + accTimeListbox + "\n");
		out.append("numListItem:" + numListItem + "\taccTimeListItem:" + accTimeListItem + "\n");
		out.append("totalTime:" + totalTime + "\n");		
		out.append("________________________________________________________________________________\n");		
		//added by pcc 21,Jun. 11 for profiling*/
		
		//writer.endRDF();
		writer.close();
		return out.toString();
	}


	private void writeMethodsProperty(Set<CRUDOperation> methods, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<Object> setMethods = new HashSet<Object>();
		for (Iterator<CRUDOperation> iterator = methods.iterator(); iterator.hasNext();) {
			Object varMethods = iterator.next();
			if (!setMethods.contains(varMethods)) {
				Literal id = _repository.Literal(varMethods.toString(), null);
				writer.handleLiteral(indent, raulmethod, id); //writer.handleLiteral(indent, raulmethods, id);
				setMethods.add(varMethods);
			} else {
				setMethods.add(varMethods);
			}
		}
	}
	

	private void writeIntegerProperty(URI raulProperty, Set<Integer> literal, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {
		Set<Number> setProperties = new HashSet<Number>();
		Iterator<Integer> iterator = literal.iterator();
		
		while (iterator.hasNext()) {	
			Number varProperties = iterator.next();
			
			if (!setProperties.contains(varProperties)) {
				Literal id = _repository.Literal(varProperties.toString(), integerdatatype);
				writer.handleLiteral(indent, raulProperty, id);
				setProperties.add(varProperties);
			} else {
				setProperties.add(varProperties);
			}
		}
	}


	private void writeStringProperty(URI raulProperty, Set<String> literal, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<String> setProperties = new HashSet<String>();
		Iterator<String> iterator = literal.iterator();
		while (iterator.hasNext()) {
			String varProperties = iterator.next();
			if (!setProperties.contains(varProperties)) {
				Literal id = _repository.Literal(varProperties.toString(), null);
				writer.handleLiteral(indent, raulProperty, id);
				setProperties.add(varProperties);
			} else {
				setProperties.add(varProperties);
			}
		}
	}

	private void writeListsProperty(Set<Class> lists, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<Object> setLists = new HashSet<Object>();

		for (Iterator<Class> iterator = lists.iterator(); iterator.hasNext();) {
			Object var = iterator.next();
			if (!setLists.contains(var)) {
				Literal id = _repository.Literal(var.toString(), null);
				writer.handleLiteral(indent, raullist, id);
				setLists.add(var);
			} else {
				setLists.add(var);
			}
		}
	}

	private void writeSequenceList(URI clazz, String[] lists, Integer indent, StringWriter out, RDFRepository _repository) throws IOException {

		Set<Object> setWidget = new HashSet<Object>();

		for (int i = 0; i < indent; i++) {
			out.append("	");
		};
		out.append("<ol style=\"display:none;\" about=\"" + clazz + "\">\n");
		for (int j = 0; j < lists.length; j++) {
			Object varLists = lists[j];
			if (!setWidget.contains(varLists)) {
				for (int k = 0; k < indent; k++) {
					out.append("	");
				};
				out.append("	<li rel=\"rdf:_" + (j + 1) + "\" resource=\"" + varLists + "\"></li>\n");
				setWidget.add(varLists);
			} else {
				setWidget.add(varLists);
			}
		}
		for (int l = 0; l < indent; l++) {
			out.append("	");
		};
		out.append("</ol>\n");
	}

	private void writeBooleanProperty(URI raulProperty, Set<Boolean> selected, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<Boolean> setBoolean = new HashSet<Boolean>();
		Iterator<Boolean> iterator = selected.iterator();
		while (iterator.hasNext()) {
			Boolean var = iterator.next();
			if (!setBoolean.contains(var)) {
				Literal id = _repository.Literal(var.toString(), booleandatatype);
				writer.handleLiteral(indent, raulProperty, id);
				setBoolean.add(var);
			} else {
				setBoolean.add(var);
			}
		}
	}

	private void writeObjectProperty(URI raulProperty, Set<Class> object, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<Object> setProperties = new HashSet<Object>();

		for (Iterator<Class> iterator = object.iterator(); iterator.hasNext();) {
			Object var = iterator.next();
			if (!setProperties.contains(var)) {
				Literal id = _repository.Literal(var.toString(), null);
				writer.handleLiteral(indent, raulProperty, id);
				setProperties.add(var);
			} else {
				setProperties.add(var);
			}
		}
	}


	private void writeValueStatement(Set<String> textboxValues, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws RepositoryException, QueryEvaluationException, IOException {
		Set<Object> setTextboxValues = new HashSet<Object>();		
		for (Iterator<String> iTextboxValues = textboxValues.iterator(); iTextboxValues.hasNext();) {			
			Object varValues = iTextboxValues.next();

			if (!setTextboxValues.contains(varValues)) {

				org.openrdf.model.URI uriValueStatement = c.getValueFactory().createURI(varValues.toString());
				
				try {					
					Statement valueStatement = c.getObject(Statement.class, uriValueStatement);
					Set<Object> valueSubjects = valueStatement.getRdfSubjects();
					Set<Object> valuePredicates = valueStatement.getRdfPredicates();
					Set<Object> valueObjects = valueStatement.getRdfObjects();
					//out.append("<div style=\"display:none;\">\n");	//modified by pcc
					out.append("<div style=\"display:none;\" about=\"" +  uriValueStatement.toString() + "\">\n");
										
					Set<Object> setSubjects = new HashSet<Object>();

					for (Iterator<Object> iSubjects = valueSubjects.iterator(); iSubjects.hasNext();) {

						Object varSubjects = iSubjects.next();

						if (!setSubjects.contains(varSubjects)) {							
							Literal value = _repository.Literal(varSubjects.toString(), null);							
							writer.handleLiteral(1, rdfSubject, value);
							setSubjects.add(varSubjects);							
						} else {
							setSubjects.add(varSubjects);
						}
					}

					Set<Object> setPredicates = new HashSet<Object>();

					for (Iterator<Object> iPredicates = valuePredicates.iterator(); iPredicates.hasNext();) {

						Object varPredicates = iPredicates.next();

						if (!setPredicates.contains(varPredicates)) {

							Literal value = _repository.Literal(varPredicates.toString(), null);
							writer.handleLiteral(1, rdfPredicate, value);
							setPredicates.add(varPredicates);
						} else {
							setPredicates.add(varPredicates);
						}
					}

					Set<Object> setObjects = new HashSet<Object>();

					for (Iterator<Object> iObjects = valueObjects.iterator(); iObjects.hasNext();) {

						Object varObjects = iObjects.next();

						if (!setObjects.contains(varObjects)) {

							Literal value = _repository.Literal(varObjects.toString(), null);
							writer.handleLiteral(1, rdfObject, value);							
							setObjects.add(varObjects);
						} else {
							setObjects.add(varObjects);
						}
					}					
					out.append("</div>\n");					
					setTextboxValues.add(varValues);
				} catch (ClassCastException e) {					
				}
			} else {
				setTextboxValues.add(varValues);
			}
		}
	}

	private void writeTagForm(Set<String> operation, Set<String> actions, Set<String> widgetContainerClasses, Set<String> widgetContainerIds, StringWriter out) throws IOException {
		
		out.append("<form");		

		Set<String> set = new LinkedHashSet<String>();
		Iterator<String> iteratorOperation = operation.iterator();

		while (iteratorOperation.hasNext()) {
			String var = iteratorOperation.next();
			if (!set.contains(var)) {
				if (var == "CREATEOperation") {
					out.append(" method=\"post\" ");
				} else if (var == "READOperation") {
					out.append(" method=\"get\" ");
				} else if (var == "UPDATEOperation") {
					out.append(" method=\"post\" ");					
				} else if (var == "DELETEOperation") {
					out.append(" method=\"delete\" ");
				}
				set.add(var);
			} else {
				set.add(var);
			}
		}
		set.clear();
		Iterator<String> iteratorAction = actions.iterator();
		while (iteratorAction.hasNext()) {
			String var = iteratorAction.next();
			if (!set.contains(var)) {
				out.append("action=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
		set.clear();
		Iterator<String> iteratorIds = widgetContainerIds.iterator();
		while (iteratorIds.hasNext()) {
			String var = iteratorIds.next();
			if (!set.contains(var)) {
				out.append(" id=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
		set.clear();
		String varClass = new String();
		if (!widgetContainerClasses.isEmpty()) {
			out.append(" class=\"");
			Iterator<String> iteratorClasses = widgetContainerClasses.iterator();
			while (iteratorClasses.hasNext()) {
				String var = iteratorClasses.next();
				if (!set.contains(var)) {
					varClass += var + " ";
					set.add(var);
				} else {
					set.add(var);
				}
			}
			out.append(varClass.trim() + "\"");
		}
		out.append(">\n");
	}

	
	private void writeTagButton(Button button, URI buttontype, StringWriter out) throws IOException {

		out.append("<div>\n");
		if (buttontype == raulRadiobutton) {
			out.append("	<input type=\"radio\"");
			
		} else if (buttontype == raulCheckbox){
			out.append("	<input type=\"checkbox\"");
		}
		else {
			//out.append("<input type=\"submit\"");
			out.append("	<input type=\"button\"");
			//<raul:command>
			Iterator<String> iteratorCommands = button.getRaulCommands().iterator();
			writeTagCommands(iteratorCommands, out);
		}

		if (!button.getRaulClasses().isEmpty()) {
			Iterator<String> iteratorClasses = button.getRaulClasses().iterator();
			writeTagClass(iteratorClasses, out);
		}

		Iterator<String> iteratorNames = button.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);
		
		Iterator<String> iteratorIds = button.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);

		Iterator<String> iteratorTitles = button.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);
		
		Iterator<String> iteratorValues = button.getRaulValues().iterator();
		writeTagValues(iteratorValues, out);
		
		//the following two lines added by pcc
		Iterator<Boolean> iteratorChecked = button.getRaulChecked().iterator();
		writeTagChecked(iteratorChecked, out);
		
		// out.append("value=\"" + Objectvalue + "\"");
		out.append("/>\n");
		out.append("</div>\n");
	}
	
	/*//original writeTagInputTextarea()
	private void writeTagInputTextarea(Textbox textbox, StringWriter out) throws IOException {
		out.append("<div>\n");
		if (textbox.getRaulRows().isEmpty()) {
			Set<Boolean> setBoolean = new HashSet<Boolean>();
			Iterator<Boolean> iteratorInputHidden = textbox.getRaulHiddens().iterator();
			if (!textbox.getRaulHiddens().isEmpty()) {
				while (iteratorInputHidden.hasNext()) {
					Boolean var = iteratorInputHidden.next();
					if (!setBoolean.contains(var)) {
						if (var) {
							out.append("<input type=\"hidden\"");
						} else {
							out.append("<input type=\"text\"");
						}
						setBoolean.add(var);
					} else {
						setBoolean.add(var);
					}
				}

			} else {
				out.append("<input type=\"text\"");
			}
		} else {
			out.append("<textarea");

		}
		if (!textbox.getRaulClasses().isEmpty()) {
			Iterator<String> iteratorClasses = textbox.getRaulClasses().iterator();
			writeTagClass(iteratorClasses, out);
		}
		Iterator<String> iteratorIds = textbox.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);
		Iterator<String> iteratorNames = textbox.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);
		Iterator<String> iteratorTitles = textbox.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);		
		if (!textbox.getRaulRows().isEmpty()) {
			Iterator<Integer> iteratorRows = textbox.getRaulRows().iterator();
			writeTagRows(iteratorRows, out);
			Iterator<String> iteratorSizes = textbox.getRaulSizes().iterator();
			writeTagCols(iteratorSizes, out);
			out.append("></textarea>\n");
		} else
			out.append("/>\n");
		out.append("</div>\n");
	}
	*/
	
	private void ButtonHandler(Button button, String uri, URI ButtonType, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		Set<URI> raulButtonProperties = new HashSet<URI>();
		raulButtonProperties.add(ButtonType);
		writer.startNode(button.toString(), raulButtonProperties);

		// Write the label property
		Set<String> ButtonLabels = button.getRaulLabels();
		writeStringProperty(raullabel, ButtonLabels, 1, writer, _repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> ButtonClasses = button.getRaulClasses();
		writeStringProperty(raulclass, ButtonClasses, 2, writer, _repository);

		// Write the id property
		Set<String> ButtonIds = button.getRaulIds();
		writeStringProperty(raulid, ButtonIds, 2, writer, _repository);

		// Write the name property
		Set<String> ButtonNames = button.getRaulNames();
		writeStringProperty(raulname, ButtonNames, 2, writer, _repository);
		
		// Write the titles property
		Set<String> ButtonTitles = button.getRaulTitles();
		writeStringProperty(raultitle, ButtonTitles, 2, writer, _repository);
		
		//Write the isIdentifier property //added by pcc 27Jan12
		Set<Boolean> ButtonIsIdentifiers = button.getRaulIsIdentifier();
		writeBooleanProperty(raulisIdentifier, ButtonIsIdentifiers, 2, writer, _repository);
		
		//Write the group property //added by pcc 22Nov11
		Set<Group> groups = button.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);

		// Write the value property											
		Set<String> ButtonValues = button.getRaulValues();
		writeStringProperty(raulvalue, ButtonValues, 2, writer, _repository);											

		// Write the command property											
		Set<String> ButtonCommands = button.getRaulCommands();
		writeStringProperty(raulcommand, ButtonCommands, 2, writer, _repository);

		// Write the checked property
		Set<Boolean> ButtonChecked = button.getRaulChecked();
		writeBooleanProperty(raulchecked, ButtonChecked, 2, writer, _repository);											

		// Write the disabled property
		Set<Boolean> ButtonDisabled = button.getRaulDisabled();
		writeBooleanProperty(rauldisabled, ButtonDisabled, 2, writer, _repository);
		
		// End the Button properties
		out.append("	</span>\n");
		writer.endNode(uri, raulButtonProperties);

		// Write the value triple here
		if(ButtonType != raulButton) writeValueStatement(ButtonValues, out, writer, _repository);
		
		// Write the HTML button element
		writeTagButton(button, ButtonType, out);
		
	}
	
	private void ListboxHandler(Listbox listbox, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		/*//added by pcc 21,Jun. 11 for profiling											
		numListbox++;
		//added by pcc 21,Jun. 11 for profiling*/
		
		Set<URI> raulListboxProperties = new HashSet<URI>();
		raulListboxProperties.add(raulListbox);
		writer.startNode(listbox.toString(), raulListboxProperties);

		// Write the label property
		Set<String> ListboxLabels = listbox.getRaulLabels();
		writeStringProperty(raullabel, ListboxLabels, 1, writer, _repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> ListboxClasses = listbox.getRaulClasses();
		writeStringProperty(raulclass, ListboxClasses, 2, writer, _repository);

		// Write the id property
		Set<String> ListboxIds = listbox.getRaulIds();
		writeStringProperty(raulid, ListboxIds, 2, writer, _repository);

		// Write the name property
		Set<String> ListboxNames = listbox.getRaulNames();
		writeStringProperty(raulname, ListboxNames, 2, writer, _repository);

		// Write the titles property
		Set<String> ListboxTitles = listbox.getRaulTitles();
		writeStringProperty(raultitle, ListboxTitles, 2, writer, _repository);

		//Write the group property //added by pcc 22Nov11
		Set<Group> groups = listbox.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);		

		// Write the value property
		Set<String> ListboxValues = listbox.getRaulValues();
		writeStringProperty(raulvalue, ListboxValues, 2, writer, _repository);											

		// Write the options property
		Set<Class> ListboxOptions = listbox.getRaulLists();
		//writeObjectProperty(raullist, ListboxOptions, 2, writer, _repository);
		writeObjectProperty(raullist, ListboxOptions, 2, writer, _repository);
											
		// End the Listbox properties
		out.append("	</span>\n");
		
		writer.endNode(uri, raulListboxProperties);
		
		// Write the value triple here
		writeValueStatement(ListboxValues, out, writer, _repository);
	}
	
	private void ListBoxOptionsHandler(Listbox listbox, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		/*//added by pcc 21,Jun. 11 for profiling											
		startTime = System.currentTimeMillis();
		//added by pcc 21,Jun. 11 for profiling*/
		
		Set<Class> ListboxOptions = listbox.getRaulLists();
		//writeObjectProperty(raullist, ListboxOptions, 2, writer, _repository);
		
		// Helper StringWriter for the HTML
		// select/option elements
		StringWriter outListitem = new StringWriter();

		Set<Object> setListboxOptions = new HashSet<Object>();

		for (Iterator<Class> iListBoxOptions = ListboxOptions.iterator(); iListBoxOptions.hasNext();) {											
			String[] ListItemString = null;
			Object varListBoxOptionsList = iListBoxOptions.next();
			// Get the next
			// ListBoxOptionsList
			Object ListBoxOptionsObject = varListBoxOptionsList;
			if (!setListboxOptions.contains(varListBoxOptionsList)) {
				org.openrdf.model.URI uriListBoxOptionsObject = c.getValueFactory().createURI(ListBoxOptionsObject.toString());
				Resource optionsList = c.getObject(Resource.class, uriListBoxOptionsObject);

				// Get all Listitems in the
				// RDF Seq
				String listItemList = optionsList.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				ListItemString = listItemList.split(", ");

				// Write the Listitem
				// Sequence
				writeSequenceList(uriListBoxOptionsObject, ListItemString, 0, out, _repository);

				for (int iListitem = 0; iListitem < ListItemString.length; iListitem++) {
					/*//added by pcc 21,Jun. 11 for profiling
					numListItem++;
					//added by pcc 21,Jun. 11 for profiling*/

					org.openrdf.model.URI uriListitem = c.getValueFactory().createURI(ListItemString[iListitem]);
					Listitem listitem = c.getObject(Listitem.class, uriListitem);
					
					Set<URI> raulListItemProperties = new HashSet<URI>();
					raulListItemProperties.add(raulListitem);
					writer.startNode(ListItemString[iListitem], raulListItemProperties);

					out.append("	<span style=\"display:none;\">\n");

					// Write the label
					// property
					Set<String> ListitemLabels = listitem.getRaulLabels();
					writeStringProperty(raullabel, ListitemLabels, 1, writer, _repository);

					// Write the class
					// property
					Set<String> ListitemClasses = listitem.getRaulClasses();
					writeStringProperty(raulclass, ListitemClasses, 2, writer, _repository);

					// Write the id property
					Set<String> ListitemIds = listitem.getRaulIds();
					writeStringProperty(raulid, ListitemIds, 2, writer, _repository);

					// Write the name
					// property
					Set<String> ListitemNames = listitem.getRaulNames();
					writeStringProperty(raulname, ListitemNames, 2, writer, _repository);

					// Write the titles
					// property
					Set<String> ListitemTitles = listitem.getRaulTitles();
					writeStringProperty(raultitle, ListitemTitles, 2, writer, _repository);
					
					//Write the isIdentifier property //added by pcc 27Jan12
					Set<Boolean> ListitemIsIdentifiers = listitem.getRaulIsIdentifier();
					writeBooleanProperty(raulisIdentifier, ListitemIsIdentifiers, 2, writer, _repository);

					// Write the value
					// property
					Set<String> ListitemValues = listitem.getRaulValues();
					writeStringProperty(raulvalue, ListitemValues, 2, writer, _repository);
					
					
					// Write the selected property //added by pcc
					Set<Boolean> ListitemSelected = listitem.getRaulSelected();														
					writeBooleanProperty(raulselected, ListitemSelected, 2, writer, _repository);

					// End the Listitem
					// properties
					out.append("	</span>\n");
					writer.endNode(uri, raulListItemProperties);
					
					writeTagListitem(listitem, listbox, outListitem); //added by pcc
				}
				setListboxOptions.add(varListBoxOptionsList);

				writeTagListbox(listbox, out);
				out.append(outListitem.toString());
				out.append("	</select>\n</div>\n");
				
				/*//added by pcc 21,Jun. 11 for profiling											
				accTimeListItem += System.currentTimeMillis() - startTime;
				//added by pcc 21,Jun. 11 for profiling*/

			} else {
				setListboxOptions.add(varListBoxOptionsList);
			}
		}
	}
	
	
	private void DynamicGroupHandler(DynamicGroup dynamicgroup, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		
		Set<URI> raulDynamicGroupProperties = new HashSet<URI>();
		raulDynamicGroupProperties.add(raulDynamicGroup);
		
		writer.startNode(dynamicgroup.toString(), raulDynamicGroupProperties);
		
		// Write the label property
		Set<String> dynamicgroupLabels = dynamicgroup.getRaulLabels();
		writeStringProperty(raullabel, dynamicgroupLabels, 1, writer, _repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> dynamicgroupClasses = dynamicgroup.getRaulClasses();
		writeStringProperty(raulclass, dynamicgroupClasses, 2, writer, _repository);

		// Write the id property
		Set<String> dynamicgroupIds = dynamicgroup.getRaulIds();
		writeStringProperty(raulid, dynamicgroupIds, 2, writer, _repository);

		// Write the name property
		Set<String> dynamicgroupNames = dynamicgroup.getRaulNames();
		writeStringProperty(raulname, dynamicgroupNames, 2, writer, _repository);
		
		//Write the group property //added by pcc 22Nov11
		Set<Group> groups = dynamicgroup.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);
		
		Set<Class> dynamicWidgetList = dynamicgroup.getRaulLists();
		writeListsProperty(dynamicWidgetList, 2, writer, _repository);
		
		// End the dynamicgroup properties
		out.append("	</span>\n");
		writer.endNode(uri, raulDynamicGroupProperties);
		
		widgetsHandler(dynamicWidgetList, uri, out, writer, _repository);
		
		//out.append("<div>\n\t<input type=\"button\" onclick=\"javascript:dynamicWidgetsAdd(\"" + dynamicgroup.toString() + "\");\" name=\"addDynamicWidgets\" id=\"addDynamicWidgets\" value=\"+\"/>\n</div>\n");
		//out.append("<div>\n\t<input type=\"button\" onclick=\"javascript:dynamicWidgetsAdd('" + dynamicgroup.toString() + "');\" name=\"addDynamicWidgets\" id=\"addDynamicWidgets\" value=\"+\"/>\n</div>\n");
		
		
		Iterator<String> iteratorIds = dynamicgroup.getRaulIds().iterator();		
		if (iteratorIds.hasNext()) {
			String var = iteratorIds.next();
			out.append("<div>\n\t<input type=\"button\" onclick=\"dynamicWidgetsAdd('" + dynamicgroup.toString()  + "', 'addDynamicWidgets_" + var + "')\"" + " name=\"addDynamicWidgets_" + var + "\" id=\"addDynamicWidgets_" + var + "\" value=\"+\"/>\n</div>\n");
		}
		
	}
	
	
	private void GroupHandler(Group group, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		Set<URI> raulGroupProperties = new HashSet<URI>();
		raulGroupProperties.add(raulGroup);
		writer.startNode(group.toString(), raulGroupProperties);

		// Write the label property
		Set<String> GroupLabels = group.getRaulLabels();		
		writeStringProperty(raullabel, GroupLabels, 1, writer, _repository);
		
		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> GroupClasses = group.getRaulClasses();
		writeStringProperty(raulclass, GroupClasses, 2, writer, _repository);

		// Write the id property
		Set<String> GroupIds = group.getRaulIds();
		writeStringProperty(raulid, GroupIds, 2, writer, _repository);

		// Write the name property
		Set<String> GroupNames = group.getRaulNames();
		writeStringProperty(raulname, GroupNames, 2, writer, _repository);
		
		//Write the group property //added by pcc 22Nov11
		Set<Group> groups = group.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);		
		
		Set<Class> GroupWidgetList = group.getRaulLists();
		writeListsProperty(GroupWidgetList, 2, writer, _repository);
		
		// Write the value property
		Set<String> GroupValues = group.getRaulValues();
		writeStringProperty(raulvalue, GroupValues, 2, writer, _repository);
		
		// End the group properties
		out.append("	</span>\n");
		writer.endNode(uri, raulGroupProperties);
		
		//Write the value triple here
		writeValueStatement(GroupValues, out, writer, _repository);
		widgetsHandler(GroupWidgetList, uri, out, writer, _repository);
	}
	
	
	private void TextboxHandler(Textbox textbox, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException, RepositoryException, QueryEvaluationException {
		/*//added by pcc 21,Jun. 11 for profiling
		numTextbox++;
		//added by pcc 21,Jun. 11 for profiling*/											

		Set<URI> raulTextboxProperties = new HashSet<URI>();
		raulTextboxProperties.add(raulTextbox);
		writer.startNode(textbox.toString(), raulTextboxProperties);

		// Write the label property
		Set<String> textboxLabels = textbox.getRaulLabels();
		writeStringProperty(raullabel, textboxLabels, 1, writer, _repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> textboxClasses = textbox.getRaulClasses();
		writeStringProperty(raulclass, textboxClasses, 2, writer, _repository);

		// Write the id property
		Set<String> textboxIds = textbox.getRaulIds();
		writeStringProperty(raulid, textboxIds, 2, writer, _repository);

		// Write the name property
		Set<String> textboxNames = textbox.getRaulNames();
		writeStringProperty(raulname, textboxNames, 2, writer, _repository);

		// Write the titles property
		Set<String> textboxTitles = textbox.getRaulTitles();
		writeStringProperty(raultitle, textboxTitles, 2, writer, _repository);
		
		//Write the group property //added by pcc 22Nov11
		Set<Group> groups = textbox.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);
		
		//Write the isIdentifier property //added by pcc 27Jan12
		Set<Boolean> textboxIsIdentifiers = textbox.getRaulIsIdentifier();
		writeBooleanProperty(raulisIdentifier, textboxIsIdentifiers, 2, writer, _repository);

		// Write the value property
		Set<String> textboxValues = textbox.getRaulValues();
		writeStringProperty(raulvalue, textboxValues, 2, writer, _repository);

		Set<Integer> textboxRows = textbox.getRaulRows();
		writeIntegerProperty(raulrow, textboxRows, 2, writer, _repository);
		
		Set<String> textboxSizes = textbox.getRaulSizes();
		writeStringProperty(raulsize, textboxSizes, 2, writer, _repository);
		
		// Write the hidden property
		Set<Boolean> textboxHiddens = textbox.getRaulHiddens();
		writeBooleanProperty(raulhidden, textboxHiddens, 2, writer, _repository);
		
		// End the Textbox properties
		out.append("	</span>\n");
		//writer.endNode(uri, raulWidgetContainerProperties);
		writer.endNode(uri, raulTextboxProperties);

		// Write the value triple here
		writeValueStatement(textboxValues, out, writer, _repository);
		
		// Write the HTML input/textarea
		// element
		//writeTagInputTextarea(textbox, out); //modified by pcc
		writeTagInputTextarea(textbox, textboxValues, out, writer, _repository); //added by pcc
		
	}

	private void widgetsHandler(Set<Class> widgetList, String uri, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws RepositoryException, QueryEvaluationException, IOException{
		
		// Get all widget lists in the WidgetContainer
		Set<Object> setWidgetList = new HashSet<Object>();
		for (Iterator<Class> iWidgetList = widgetList.iterator(); iWidgetList.hasNext();) {

			Object varList = iWidgetList.next();
			// Get the next WidgetList
			Object WidgetListObject = varList;
			if (!setWidgetList.contains(varList)) {
				org.openrdf.model.URI uriWidgetListObject = c.getValueFactory().createURI(WidgetListObject.toString());								
				Resource widgetsTypeList = c.getObject(Resource.class, uriWidgetListObject);

				// Get all widgets in the RDF Seq
				String widgetTypeList = widgetsTypeList.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				String[] unknownType = widgetTypeList.split(", ");
				Set<Object> setUnknownType = new HashSet<Object>();

				// Write the WidgetList
				writeSequenceList(uriWidgetListObject, unknownType, 0, out, _repository);
				
				/*//added by pcc 21,Jun. 11 for profiling
				TimeWContainer = System.currentTimeMillis() - startTime;								
				//added by pcc 21,Jun. 11 for profiling*/
				
				// Iterate through the Widgets and determine
				// their type
				for (int iUnknownType = 0; iUnknownType < unknownType.length; iUnknownType++) {
					Object unknownTypeList = unknownType[iUnknownType];
					Object varUnknownType = unknownType[iUnknownType];									
					if (!setUnknownType.contains(varUnknownType)) {
						org.openrdf.model.URI unknownTypeBox = c.getValueFactory().createURI(unknownTypeList.toString());
																
						// get Textbox
						try {
							/*//added by pcc 21,Jun. 11 for profiling											
							startTime = System.currentTimeMillis();
							//added by pcc 21,Jun. 11 for profiling*/
							
							Textbox textbox = c.getObject(Textbox.class, unknownTypeBox);											
							TextboxHandler(textbox, uri, out, writer, _repository);
							
							/*//added by pcc 21,Jun. 11 for profiling
							accTimeTextbox += System.currentTimeMillis() - startTime;
							//added by pcc 21,Jun. 11 for profiling*/
							
							continue; //added by pcc
						} catch (ClassCastException e) {							
						}

						// get Listbox
						try {
						
							/*//added by pcc 21,Jun. 11 for profiling											
							startTime = System.currentTimeMillis();
							//added by pcc 21,Jun. 11 for profiling*/

							Listbox listbox = c.getObject(Listbox.class, unknownTypeBox);											
							ListboxHandler(listbox, uri, out, writer, _repository);
							ListBoxOptionsHandler(listbox, uri, out, writer, _repository);
							
							/*//added by pcc 21,Jun. 11 for profiling											
							accTimeListbox += System.currentTimeMillis() - startTime;											
							//added by pcc 21,Jun. 11 for profiling*/

							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						try {	//added by pcc											
							Group group = c.getObject(Group.class, unknownTypeBox);
							GroupHandler(group, uri, out, writer, _repository);
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}

						// get Button
						try {
						
							/*//added by pcc 21,Jun. 11 for profiling											
							startTime = System.currentTimeMillis();
							//added by pcc 21,Jun. 11 for profiling*/
							
							Button button = c.getObject(Button.class, unknownTypeBox);
							
							/*//added by pcc 21,Jun. 11 for profiling
							numButton++;
							//added by pcc 21,Jun. 11 for profiling*/
							
							URI ButtonType = raulButton;

							// Determine if the Button type is
							// Checkbox or Radiobutton
							try {
								Radiobutton radiobutton = c.getObject(Radiobutton.class, unknownTypeBox);
								/*//added by pcc 21,Jun. 11 for profiling
								numButton--;
								numRButton++;
								//added by pcc 21,Jun. 11 for profiling*/
								ButtonType = raulRadiobutton;												
							} catch (ClassCastException e) {
							}
							
							try {
								Checkbox checkbox = c.getObject(Checkbox.class, unknownTypeBox);
								/*//added by pcc 21,Jun. 11 for profiling
								numButton--;
								numCheckbox++;
								//added by pcc 21,Jun. 11 for profiling*/												
								ButtonType = raulCheckbox;
							} catch (ClassCastException e) {
							}
							
							ButtonHandler(button, uri, ButtonType, out, writer, _repository);
							
							/*//added by pcc 21,Jun. 11 for profiling
							if(ButtonType == raulRadiobutton){											
								accTimeRButton += System.currentTimeMillis() - startTime;
							}else if(ButtonType == raulCheckbox){
								accTimeCheckbox += System.currentTimeMillis() - startTime;
							}else if(ButtonType == raulButton){
								accTimeButton += System.currentTimeMillis() - startTime;												
							}
							//added by pcc 21,Jun. 11 for profiling*/
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						// get DynamicGroup added by pcc
						try {											
							DynamicGroup dynamicgroup = c.getObject(DynamicGroup.class, unknownTypeBox);
							DynamicGroupHandler(dynamicgroup, uri, out, writer, _repository);
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						setUnknownType.add(varUnknownType);
					} else {
						setUnknownType.add(varUnknownType);
					}

				}
				setWidgetList.add(varList);
			} else {
				setWidgetList.add(varList);
			}

		}
	}
	
	//this method is modified by pcc
	private void writeTagInputTextarea(Textbox textbox, Set<String> textboxValues, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {
		out.append("<div>\n");
		if (textbox.getRaulRows().isEmpty()) {
			Set<Boolean> setBoolean = new HashSet<Boolean>();
			Iterator<Boolean> iteratorInputHidden = textbox.getRaulHiddens().iterator();
			if (!textbox.getRaulHiddens().isEmpty()) {
				while (iteratorInputHidden.hasNext()) {
					Boolean var = iteratorInputHidden.next();
					if (!setBoolean.contains(var)) {
						if (var) {
							out.append("	<input type=\"hidden\"");
						} else {
							out.append("	<input type=\"text\"");
						}
						setBoolean.add(var);
					} else {
						setBoolean.add(var);
					}
				}

			} else {
				out.append("	<input type=\"text\"");				
			}
		} else {
			out.append("<textarea");

		}
		if (!textbox.getRaulClasses().isEmpty()) {
			Iterator<String> iteratorClasses = textbox.getRaulClasses().iterator();
			writeTagClass(iteratorClasses, out);
		}
		Iterator<String> iteratorIds = textbox.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);
		Iterator<String> iteratorNames = textbox.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);
		Iterator<String> iteratorTitles = textbox.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);		
		if (!textbox.getRaulRows().isEmpty()) {
			Iterator<Integer> iteratorRows = textbox.getRaulRows().iterator();
			writeTagRows(iteratorRows, out);
			Iterator<String> iteratorSizes = textbox.getRaulSizes().iterator();
			writeTagCols(iteratorSizes, out);
			//out.append("></textarea>\n");
			out.append(">");
			try {
				writeTagTagInputTextareaValue(textboxValues, out, writer, _repository);
			} catch (RepositoryException e) {				
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}
			out.append("</textarea>\n");
			
		} else {
			out.append(" value=\"");
			try {
				writeTagTagInputTextareaValue(textboxValues, out, writer, _repository);
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}
			out.append("\"");
			out.append("/>\n");
		}
		out.append("</div>\n");
	}
	
	private void writeTagListbox(Listbox listbox, StringWriter out) throws IOException {
		out.append("<div>\n");
		out.append("	<select");

		Iterator<String> iteratorIds = listbox.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);

		Iterator<String> iteratorNames = listbox.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);

		Iterator<String> iteratorTitles = listbox.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);

		out.append(">\n");

	}

	private void writeTagListitem(Listitem listitem, Listbox listbox, StringWriter out) throws IOException {
		out.append("		<option");

		Iterator<String> iteratorTitles = listitem.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);

		Iterator<String> iteratorIds = listitem.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);

		Iterator<String> iteratorNames = listitem.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);

		Iterator<String> iteratorValues = listitem.getRaulValues().iterator();
		writeTagValues(iteratorValues, out);

		//added by pcc
		Iterator<Boolean> iteratorSelected = listitem.getRaulSelected().iterator();
		writeTagSelected(iteratorSelected, out);

		out.append(">");
		Iterator<String> iteratorLabels = listitem.getRaulLabels().iterator();
		writeTagLabels(iteratorLabels, out);
		out.append("</option>\n");
	}
	

	
	private void writeTagClass(Iterator<String> iteratorClasses, StringWriter out) {
		out.append(" class=\"");

		Set<String> set = new LinkedHashSet<String>();
		String varClass = new String();

		while (iteratorClasses.hasNext()) {
			String var = iteratorClasses.next();
			if (!set.contains(var)) {
				varClass += var + " ";
				set.add(var);
			} else {
				set.add(var);
			}
		}
		out.append(varClass.trim() + "\"");
	}
	private void writeTagIds(Iterator<String> iteratorIds, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorIds.hasNext()) {
			String var = iteratorIds.next();
			if (!set.contains(var)) {
				out.append(" id=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}
	private void writeTagNames(Iterator<String> iteratorNames, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorNames.hasNext()) {
			String var = iteratorNames.next();
			if (!set.contains(var)) {
				out.append(" name=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}
	private void writeTagTitles(Iterator<String> iteratorTitles, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorTitles.hasNext()) {
			String var = iteratorTitles.next();
			if (!set.contains(var)) {
				out.append(" title=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}
	private void writeTagValues(Iterator<String> iteratorValues, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorValues.hasNext()) {
			String var = iteratorValues.next();
			if (!set.contains(var)) {
				out.append(" value=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
		
	}

	//added by pcc
	private void writeTagCommands(Iterator<String> iteratorCommands, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorCommands.hasNext()) {
			String var = iteratorCommands.next();
			if (!set.contains(var)) {
				out.append(" onclick=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}
	
	//added by pcc
	private void writeTagSelected(Iterator<Boolean> iteratorSelected, StringWriter out) {		
		if(iteratorSelected.hasNext()) {
			if(iteratorSelected.next())
				out.append(" selected=\"selected\" ");			
		}
	}
	
	private void writeTagLabels(Iterator<String> iteratorLabels, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorLabels.hasNext()) {
			String var = iteratorLabels.next();
			if (!set.contains(var)) {
				out.append(var);
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}

	private void writeTagRows(Iterator<Integer> iteratorRows, StringWriter out) {

		Set<Object> set = new LinkedHashSet<Object>();
		while (iteratorRows.hasNext()) {
			Object var = iteratorRows.next();
			if (!set.contains(var)) {
				out.append(" rows=\"" + var.toString() + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}

	private void writeTagCols(Iterator<String> iteratorCols, StringWriter out) {
		Set<String> set = new LinkedHashSet<String>();
		while (iteratorCols.hasNext()) {
			String var = iteratorCols.next();
			if (!set.contains(var)) {
				out.append(" cols=\"" + var + "\"");
				set.add(var);
			} else {
				set.add(var);
			}
		}
	}
	
	
	//added by pcc
	private void writeTagChecked(Iterator<Boolean> iteratorChecked, StringWriter out) {		
		if(iteratorChecked.hasNext()) {
			if(iteratorChecked.next())
				out.append(" checked=\"checked\" ");			
		}
	}

	//this method is added by  pcc
	private void writeTagTagInputTextareaValue(Set<String> textboxValues, StringWriter out, RDFaMetaWriter writer, RDFRepository _repository) throws RepositoryException, QueryEvaluationException, IOException {
		Set<Object> setTextboxValues = new HashSet<Object>();
		Set<String> setObjects = new HashSet<String>();
		
		for (Iterator<String> iTextboxValues = textboxValues.iterator(); iTextboxValues.hasNext();) {
			Object varValues = iTextboxValues.next();
			if (!setTextboxValues.contains(varValues)) {				
				org.openrdf.model.URI uriValueStatement = c.getValueFactory().createURI(varValues.toString());
				try {
					Statement valueStatement = c.getObject(Statement.class, uriValueStatement);					
					Set<Object> valueObjects = valueStatement.getRdfObjects();
					//Set<Object> setObjects = new HashSet<Object>();					
					for (Iterator<Object> iObjects = valueObjects.iterator(); iObjects.hasNext();) {
						Object varObjects = iObjects.next();
						//if (!setObjects.contains(varObjects)) {
						if (!setObjects.contains(varObjects.toString())) {
							//out.append(" value=\"" + varObjects.toString() + "\"");							
							out.append(varObjects.toString());
							//setObjects.add(varObjects);
							setObjects.add(varObjects.toString());
						} else {
							//setObjects.add(varObjects);
							setObjects.add(varObjects.toString());
						}
					}										
					setTextboxValues.add(varValues);					
				} catch (ClassCastException e) {
				}
			} else {
				setTextboxValues.add(varValues);				
			}
		}
	}
	
	/*
	//no longer use
	private void writeGroupClass(Set<String> groups, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {
		Set<URI> raulGroupProperties = new HashSet<URI>();
		raulGroupProperties.add(raulGroup);
		GroupSet.clear();
		for (Iterator<String> iterator = groups.iterator(); iterator.hasNext();) {
			Object var = iterator.next();
			if (!GroupSet.contains(var)) {
				writer.startNode(var.toString(), raulGroupProperties);
				writer.endNode(var.toString(), raulGroupProperties);
				GroupSet.add(var);
			} else {
				GroupSet.add(var);
			}
		}
	}
	*/
	
	private void writeGroupsProperty(Set<Group> groups, Integer indent, RDFaMetaWriter writer, RDFRepository _repository) throws IOException {

		Set<Object> setGroups = new HashSet<Object>();
		for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext();) {
			Object varGroups = iterator.next();
			if (!setGroups.contains(varGroups)) {
				Literal id = _repository.Literal(varGroups.toString(), null);
				writer.handleLiteral(indent, raulgroup, id); 
				setGroups.add(varGroups);
			} else {
				setGroups.add(varGroups);
			}
		}
	}

	private void uriInit(RDFRepository _repository){
		// Define RDF predicates
		rdfSubject = _repository.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

		rdfPredicate = _repository.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

		rdfObject = _repository.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

		// Define RaUL Classes and basic datatypes
		raulPage = _repository.URIref("http://purl.org/NET/raul#Page");

		raulWidgetContainer = _repository.URIref("http://purl.org/NET/raul#WidgetContainer");

		raulCRUDOperation = _repository.URIref("http://purl.org/NET/raul#CRUDOperation");

		raulCREATEOperation = _repository.URIref("http://purl.org/NET/raul#CREATEOperation");

		raulREADOperation = _repository.URIref("http://purl.org/NET/raul#READOperation");

		raulUPDATEOperation = _repository.URIref("http://purl.org/NET/raul#UPDATEOperation");

		raulDELETEOperation = _repository.URIref("http://purl.org/NET/raul#DELETEOperation");

		raulTextbox = _repository.URIref("http://purl.org/NET/raul#Textbox");

		raulListbox = _repository.URIref("http://purl.org/NET/raul#Listbox");

		raulListitem = _repository.URIref("http://purl.org/NET/raul#Listitem");

		raulButton = _repository.URIref("http://purl.org/NET/raul#Button");

		raulCheckbox = _repository.URIref("http://purl.org/NET/raul#Checkbox");

		raulRadiobutton = _repository.URIref("http://purl.org/NET/raul#Radiobutton");

		raulGroup = _repository.URIref("http://purl.org/NET/raul#Group");
		
		raulDynamicGroup = _repository.URIref("http://purl.org/NET/raul#DynamicGroup");

		// Define datatypes

		stringdatatype = _repository.URIref("http://www.w3.org/2001/XMLSchema#string");

		booleandatatype = _repository.URIref("http://www.w3.org/2001/XMLSchema#boolean");
		
		integerdatatype = _repository.URIref("http://www.w3.org/2001/XMLSchema#integer"); //added by pcc

		raulcommand = _repository.URIref("http://purl.org/NET/raul#command");

		raulaction = _repository.URIref("http://purl.org/NET/raul#action");

		raulvalue = _repository.URIref("http://purl.org/NET/raul#value");

		raullabel = _repository.URIref("http://purl.org/NET/raul#label");

		raulname = _repository.URIref("http://purl.org/NET/raul#name");

		raultitle = _repository.URIref("http://purl.org/NET/raul#title");

		raulclass = _repository.URIref("http://purl.org/NET/raul#class");

		raulid = _repository.URIref("http://purl.org/NET/raul#id");

		raulhidden = _repository.URIref("http://purl.org/NET/raul#hidden");
		
		raulisIdentifier = _repository.URIref("http://purl.org/NET/raul#isIdentifier");

		raulselected = _repository.URIref("http://purl.org/NET/raul#selected");

		raulchecked = _repository.URIref("http://purl.org/NET/raul#checked");

		rauldisabled = _repository.URIref("http://purl.org/NET/raul#disabled");
		
		raulrow = _repository.URIref("http://purl.org/NET/raul#row");
		
		raulsize = _repository.URIref("http://purl.org/NET/raul#size");

		raullist = _repository.URIref("http://purl.org/NET/raul#list");

		raulgroup = _repository.URIref("http://purl.org/NET/raul#group");

		raulmethod = _repository.URIref("http://purl.org/NET/raul#method"); //raulmethods = _repository.URIref("http://purl.org/NET/raul#methods");

		// Define RaUL list property
		raulwidgets = _repository.URIref("http://purl.org/NET/raul#list");

	}
	
	public void dataGraphGen_RDF(String uri, RDFRepository _repository, RDFRepository _dataGraph) throws IOException, RepositoryConfigException, RepositoryException, QueryEvaluationException, RDFHandlerException {
		
		// Create the connection to the repository
		c = _repository.createObjectConnection();
		c1 = _dataGraph.createObjectConnection();
		
		Namespace tmpNamespace;
		RepositoryResult<Namespace> _repositoryNamespace = c.getNamespaces();
		while (_repositoryNamespace.hasNext()) {
			tmpNamespace = _repositoryNamespace.next();
			c1.setNamespace(tmpNamespace.getPrefix(), tmpNamespace.getName()); 
		}
		
		org.openrdf.model.URI u = c.getValueFactory().createURI(uri);
		
		uriInit(_repository);

		// Get the RaUL page object
		Page page = c.getObject(Page.class, u);
		
		// Start the Page properties in the Body
		Set<URI> raulPageProperties = new HashSet<URI>();
		raulPageProperties.add(raulPage);
		
		// Get all widgetContainerLists in the Page
		Set<Class> widgetContainerLists = page.getRaulLists();
		Set<Object> setWidgetContainerLists = new HashSet<Object>();
		
		for (Iterator<Class> iWidgetContainerLists = widgetContainerLists.iterator(); iWidgetContainerLists.hasNext();) {

			Object var = iWidgetContainerLists.next();
			// Get the next WidgetContainerList

			if (!setWidgetContainerLists.contains(var)) {

				org.openrdf.model.URI uriWidgetContainerListObject = c.getValueFactory().createURI(var.toString());
				Resource widgetContainerSequence = c.getObject(Resource.class, uriWidgetContainerListObject);

				// Get all WidgetContainers in the RDF Seq
				String widgetContainerList = widgetContainerSequence.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				String[] WidgetContainerString = widgetContainerList.split(", ");
				
				
				Set<Object> setWidgetContainer = new HashSet<Object>();

				// Iterate through the WidgetContainers
				for (int iWidgetContainer = 0; iWidgetContainer < WidgetContainerString.length; iWidgetContainer++) {

					Object WidgetContainerList = WidgetContainerString[iWidgetContainer];
					Object varWidgetContainer = WidgetContainerString[iWidgetContainer];
					if (!setWidgetContainer.contains(varWidgetContainer)) {
						org.openrdf.model.URI uriWidgetContainer = c.getValueFactory().createURI(WidgetContainerList.toString());

						// Get the WidgetContainer object
						WidgetContainer widgetContainer = c.getObject(WidgetContainer.class, uriWidgetContainer);
						
						Set<Class> widgetList = widgetContainer.getRaulLists();
						
						dataGraphGen_widgetsHandler(widgetList, uri, _repository, _dataGraph);
						
						setWidgetContainer.add(varWidgetContainer);
					} else {
						setWidgetContainer.add(varWidgetContainer);
					}
				}

				setWidgetContainerLists.add(var);

			} else {
				setWidgetContainerLists.add(var);
			}

		}
	}

	private void dataGraphGen_widgetsHandler(Set<Class> widgetList, String uri, RDFRepository _repository, RDFRepository _dataGraph) throws RepositoryException, QueryEvaluationException, IOException, RepositoryConfigException{
		
		// Get all widget lists in the WidgetContainer
		Set<Object> setWidgetList = new HashSet<Object>();
		for (Iterator<Class> iWidgetList = widgetList.iterator(); iWidgetList.hasNext();) {

			Object varList = iWidgetList.next();
			
			// Get the next WidgetList
			Object WidgetListObject = varList;
			if (!setWidgetList.contains(varList)) {
				org.openrdf.model.URI uriWidgetListObject = c.getValueFactory().createURI(WidgetListObject.toString());								
				Resource widgetsTypeList = c.getObject(Resource.class, uriWidgetListObject);

				// Get all widgets in the RDF Seq
				
				String widgetTypeList = widgetsTypeList.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				String[] unknownType = widgetTypeList.split(", ");
				Set<Object> setUnknownType = new HashSet<Object>();
				
				// Iterate through the Widgets and determine
				// their type
				for (int iUnknownType = 0; iUnknownType < unknownType.length; iUnknownType++) {
					Object unknownTypeList = unknownType[iUnknownType];
					Object varUnknownType = unknownType[iUnknownType];									
					if (!setUnknownType.contains(varUnknownType)) {						
						org.openrdf.model.URI unknownTypeBox = c.getValueFactory().createURI(unknownTypeList.toString());
						// get Textbox
						try {
							
							Textbox textbox = c.getObject(Textbox.class, unknownTypeBox);
							Set<String> textboxValues = textbox.getRaulValues();							
							dataGraphGen_Statement(textboxValues, _dataGraph);
							
							continue; //added by pcc
						} catch (ClassCastException e) {							
						}

						// get Listbox
						try {
							Listbox listbox = c.getObject(Listbox.class, unknownTypeBox);											
							Set<String> ListboxValues = listbox.getRaulValues();
							dataGraphGen_Statement(ListboxValues, _dataGraph);							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						try {	//added by pcc							
							Group group = c.getObject(Group.class, unknownTypeBox);
							
							Set<Class> GroupWidgetList = group.getRaulLists();							
							Set<String> GroupValues = group.getRaulValues();
							dataGraphGen_Statement(GroupValues, _dataGraph);							
							dataGraphGen_widgetsHandler(GroupWidgetList, uri, _repository, _dataGraph);
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}

						// get Button
						try {
							Button button = c.getObject(Button.class, unknownTypeBox);													
							URI ButtonType = raulButton;
							// Determine if the Button type is
							// Checkbox or Radiobutton
							try {
								Radiobutton radiobutton = c.getObject(Radiobutton.class, unknownTypeBox);								
								ButtonType = raulRadiobutton;												
							} catch (ClassCastException e) {
							}
							
							try {
								Checkbox checkbox = c.getObject(Checkbox.class, unknownTypeBox);												
								ButtonType = raulCheckbox;
							} catch (ClassCastException e) {
							}
							Set<String> ButtonValues = button.getRaulValues();
							if(ButtonType != raulButton) dataGraphGen_Statement(ButtonValues, _dataGraph);
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						// get DynamicGroup added by pcc
						try {							
							DynamicGroup dynamicgroup = c.getObject(DynamicGroup.class, unknownTypeBox);
							
							Set<Class> dynamicWidgetList = dynamicgroup.getRaulLists();
							dataGraphGen_widgetsHandler(dynamicWidgetList, uri, _repository, _dataGraph);
							
							continue; //added by pcc
						} catch (ClassCastException e) {
						}
						
						setUnknownType.add(varUnknownType);
					} else {
						setUnknownType.add(varUnknownType);
					}

				}
				setWidgetList.add(varList);
			} else {
				setWidgetList.add(varList);
			}

		}
	}
	
	private void dataGraphGen_Statement(Set<String> widgetValues, RDFRepository _dataGraph) throws RepositoryException, QueryEvaluationException, IOException, RepositoryConfigException {		
		org.openrdf.model.URI s = null, p = null;
		Value o = null;
		
		Set<Object> setWidgetValues = new HashSet<Object>();		
		for (Iterator<String> iWidgetValues = widgetValues.iterator(); iWidgetValues.hasNext();) {			
			Object varValues = iWidgetValues.next();

			if (!setWidgetValues.contains(varValues)) {

				org.openrdf.model.URI uriValueStatement = c.getValueFactory().createURI(varValues.toString());
				
				try {					
					Statement valueStatement = c.getObject(Statement.class, uriValueStatement);
					Set<Object> valueSubjects = valueStatement.getRdfSubjects();
					Set<Object> valuePredicates = valueStatement.getRdfPredicates();
					Set<Object> valueObjects = valueStatement.getRdfObjects();
										
					Set<Object> setSubjects = new HashSet<Object>();

					for (Iterator<Object> iSubjects = valueSubjects.iterator(); iSubjects.hasNext();) {

						Object varSubjects = iSubjects.next();

						if (!setSubjects.contains(varSubjects)) {
							s = _dataGraph.URIref(varSubjects.toString());
														
							setSubjects.add(varSubjects);							
						} else {
							setSubjects.add(varSubjects);
						}
					}

					Set<Object> setPredicates = new HashSet<Object>();

					for (Iterator<Object> iPredicates = valuePredicates.iterator(); iPredicates.hasNext();) {

						Object varPredicates = iPredicates.next();

						if (!setPredicates.contains(varPredicates)) {
							String strPredicate = varPredicates.toString();
							
							if( strPredicate.indexOf("http://") != -1){
								//an entire uri
								p = _dataGraph.URIref(strPredicate);
							}
							else{
								//prefix + local name
								
								String[] qName = strPredicate.split(":");
								String strPrefix = qName[0];
								String strLocalname = qName[1];

								String strNamespace = c1.getNamespace(strPrefix);
								if(strNamespace != null){
									strPredicate = strNamespace + strLocalname;
								}
								
								p = _dataGraph.URIref(strPredicate);
							}
																					
							setPredicates.add(varPredicates);
						} else {
							setPredicates.add(varPredicates);
						}
					}

					Set<Object> setObjects = new HashSet<Object>();

					for (Iterator<Object> iObjects = valueObjects.iterator(); iObjects.hasNext();) {

						Object varObjects = iObjects.next();

						if (!setObjects.contains(varObjects)) {

							if( varObjects.toString().indexOf("http://") != -1)
								o = _dataGraph.URIref(varObjects.toString());	//resource
							else							
								o = _dataGraph.Literal(varObjects.toString());	//literal
														
							setObjects.add(varObjects);
						} else {
							setObjects.add(varObjects);
						}
					}
					
					_dataGraph.add(s, p, o);
					setWidgetValues.add(varValues);
				} catch (ClassCastException e) {					
				}
			} else {
				setWidgetValues.add(varValues);
			}
		}
	}

	

	
}

	

