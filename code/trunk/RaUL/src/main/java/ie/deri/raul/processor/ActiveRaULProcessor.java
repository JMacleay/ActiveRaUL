package ie.deri.raul.processor;

//import ie.deri.raul.model.raul.Page;
import ie.deri.raul.model.raul.Button;
import ie.deri.raul.model.raul.CRUDOperation;
import ie.deri.raul.model.raul.DynamicGroup;
import ie.deri.raul.model.raul.Group;
import ie.deri.raul.model.raul.Listbox;
import ie.deri.raul.model.raul.Listitem;
import ie.deri.raul.model.raul.Page;
import ie.deri.raul.model.raul.Textbox;
import ie.deri.raul.model.raul.Widget;
import ie.deri.raul.model.raul.WidgetContainer;
import ie.deri.raul.model.rdf.Statement;
import ie.deri.raul.model.rdfs.Resource;
import ie.deri.raul.persistence.RDFRepository;

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
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfa.RDFaMetaWriter;

//import org.openrdf.rio.turtle.TurtleWriter;

/**
 * Implements the RaUL view generation. It generates, based on a URI input of a
 * Page, and the expected return format the RDF Graph of a RaUL page from the
 * supplied source.
 * 
 * @author Armin Haller
 */

public class ActiveRaULProcessor implements IRaULProcessor {

	// Set<Object> GroupSet = new HashSet<Object>();
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
	org.openrdf.model.URI raulDynamicGroup; // added by pcc
	org.openrdf.model.URI raulselected;
	org.openrdf.model.URI raulgroup;
	org.openrdf.model.URI raulmethod; // org.openrdf.model.URI raulmethods;
	org.openrdf.model.URI booleandatatype;
	org.openrdf.model.URI stringdatatype;
	org.openrdf.model.URI integerdatatype; // added by pcc
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
	org.openrdf.model.URI raulisIdentifier; // added by pcc 27Jan12
	org.openrdf.model.URI raulchecked;
	org.openrdf.model.URI rauldisabled;
	org.openrdf.model.URI raulrow;
	org.openrdf.model.URI raulsize;
	org.openrdf.model.URI raulwidgets;
	org.openrdf.model.URI raulMultipleWidgets;
	org.openrdf.model.URI rdfsComment;
	org.openrdf.model.URI rdfsRange;
	org.openrdf.model.URI raulLevel;
	Integer opendiv;
	Integer openspan;

	// ObjectConnection c;
	// ObjectConnection c1;
	RepositoryConnection con;
	RepositoryConnection con1;
	private static Log _logger = LogFactory.getLog(ActiveRaULProcessor.class);

	private final static String OBJECT_PROP = "ObjectProperty";

	private final static String RAUL_RDF_PREFIX = "http://purl.org/NET/raul";

	private final static String FIELD_SEARCHED = "searched";

	// private Map<String,String> textboxRegister=new HashMap<String,String>();

	// added by Shepherd to see if we need to append a lookup and an edit button
	// at the end of a object property
	private Textbox objectPropertyTextbox = null;

	private Textbox textboxMultiple = null;

	private boolean groupSearched = false;

	public String serializeXHTML(String uri, RDFRepository _repository)
			throws IOException, RepositoryConfigException, RepositoryException,
			QueryEvaluationException {
		/*
		 * //added by pcc 21,Jun. 11 for profiling int numTextbox=0,
		 * numButton=0, numRButton=0, numCheckbox=0, numListbox=0,
		 * numListItem=0; long startTime=0, timePage=0, TimeWContainer=0,
		 * accTimeTextbox=0, accTimeButton=0, accTimeRButton=0,
		 * accTimeCheckbox=0, accTimeListbox=0, accTimeListItem=0; long
		 * totalTime_start=0, totalTime=0; totalTime_start =
		 * System.currentTimeMillis(); //added by pcc 21,Jun. 11 for profiling
		 */
		// System.out.println("uri: " + uri);
		// Define output writer
		StringWriter out = new StringWriter();
		RDFaMetaWriter writer = new RDFaMetaWriter(out);

		// Create the connection to the repository
		con = _repository.createConnection();

		// org.openrdf.model.URI u = c.getValueFactory().createURI(uri);
		org.openrdf.model.URI u = _repository.URIref(uri);
		// System.out.println("RDF URI : " + u.toString());

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
		// StringWriter out = new StringWriter();
		// RDFaMetaWriter writer = new RDFaMetaWriter(out);

		// System.out.println("uriInit method ........");
		uriInit(_repository);
		// System.out.println("uriInit method Done ........");
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
		// Page page = c.getObject(Page.class, u);

		Page page = new Page(con, uri);
		// ie.deri.raul.model.raul.Page rpage = new
		// ie.deri.raul.model.raul.Page(c,uri);

		Map<String, String> namespaceTable = new HashMap<String, String>();
		namespaceTable
				.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
		namespaceTable.put("http://www.w3.org/2002/07/owl#", "owl");
		namespaceTable.put("http://www.w3.org/2001/XMLSchema#", "xsd");
		namespaceTable.put("http://purl.org/NET/raul#", "raul");
		namespaceTable.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
		RepositoryResult<Namespace> rs = con.getNamespaces();
		while (rs.hasNext()) {
			String namespaces = rs.next().toString();
			// System.out.println(" Namespaces : "+ namespaces);
			String namespace[] = namespaces.split("::");
			String prefix = namespace[0].replaceAll(" ", "");
			String URI = namespace[1].replaceAll(" ", "");
			if (namespaceTable.get(URI) == null)
				namespaceTable.put(URI, prefix);
		}

		// writer.startRDF(uri, namespaceTable);

		if (!page.getRaulTitles().isEmpty()) {

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
			Set<ie.deri.raul.model.rdfs.Class> widgetcontainers = page
					.getRaulLists();
			writeObjectProperty(raullist, widgetcontainers, 2, writer,
					_repository);

			out.append("	</span>\n");
			writer.endNode(uri, raulPageProperties);

			/*
			 * //added by pcc 21,Jun. 11 for profiling timePage =
			 * System.currentTimeMillis() - startTime; //added by pcc 21,Jun. 11
			 * for profiling
			 */
		} else { // added by pcc 27, Jun. 11

			// System.out.println(" VAR . :" );
			out.append("<div ");
			writer.setNamespace(uri, namespaceTable);
			out.append(">\n");

			// System.out.println("----------- After setNamespace Funtion -------------");
			// System.out.println(out.toString());

			// Start the Page properties in the Body
			Set<URI> raulPageProperties = new HashSet<URI>();
			raulPageProperties.add(raulPage);
			writer.startNode(uri, raulPageProperties);
			out.append("	<span style=\"display:none;\">\n");

			// System.out.println("----------- After setNode Funtion -------------");
			// System.out.println(out.toString());
			// Write the class property
			Set<String> clazzes = page.getRaulClasses();
			// System.out.println(clazzes);
			// Set<String> clazzes = rpage.getRaulClasses();
			writeStringProperty(raulclass, clazzes, 2, writer, _repository);
			// System.out.println("----------- After Write String Funtion -------------");
			// System.out.println(out.toString());
			// Write the id property
			// Set<String> ids = page.getRaulIds();
			Set<String> ids = page.getRaulIds();
			// System.out.println(ids);
			writeStringProperty(raulid, ids, 2, writer, _repository);
			// System.out.println("----------- After Write String Funtion -------------");
			// System.out.println(out.toString());
			// Write the list property
			Set<ie.deri.raul.model.rdfs.Class> widgetcontainers = page
					.getRaulLists();
			// System.out.println(widgetcontainers);
			// Set<Class> widgetcontainers = rPage.getRaulLists(c, uri);
			writeObjectProperty(raullist, widgetcontainers, 2, writer,
					_repository);
			// System.out.println("----------- After Write Object Funtion -------------");
			// System.out.println(out.toString());
			out.append("	</span>\n");
			writer.endNode(uri, raulPageProperties);
			// System.out.println("----------- After endNode Funtion -------------");
			// System.out.println(out.toString());
		}

		/*
		 * //added by pcc 21,Jun. 11 for profiling startTime =
		 * System.currentTimeMillis(); //added by pcc 21,Jun. 11 for profiling
		 */

		// Get all widgetContainerLists in the Page
		Set<ie.deri.raul.model.rdfs.Class> widgetContainerLists = page
				.getRaulLists();
		Set<Object> setWidgetContainerLists = new HashSet<Object>();

		for (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetContainerLists = widgetContainerLists
				.iterator(); iWidgetContainerLists.hasNext();) {

			ie.deri.raul.model.rdfs.Class widgetContainerItem = iWidgetContainerLists
					.next();
			Object var = widgetContainerItem.getResourceURI();
			// System.out.println("----------- In for loop to get widget container lists  -------------");
			// System.out.println(var.toString());
			// Get the next WidgetContainerList

			if (!setWidgetContainerLists.contains(var)) {
				org.openrdf.model.URI uriWidgetContainerListObject = con
						.getValueFactory().createURI(var.toString());
				// System.out.println("uriWidgetContainerListObject :" +
				// uriWidgetContainerListObject);

				// Resource widgetContainerSequence =
				// c.getObject(Resource.class, uriWidgetContainerListObject);
				Resource widgetContainerSequence = new Resource(con,
						uriWidgetContainerListObject.toString());
				// System.out.println("widgetContainerSequence :" +
				// widgetContainerSequence.getResourceURI().toString());
				// Get all WidgetContainers in the RDF Seq
				String widgetContainerList = widgetContainerSequence
						.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				// System.out.println("widgetContainerList :" +
				// widgetContainerList.toString());

				String[] WidgetContainerString = widgetContainerList
						.split(", ");

				// Write the WidgetContainer Sequence
				writeSequenceList(uriWidgetContainerListObject,
						WidgetContainerString, 0, out, _repository);
				// System.out.println("----------- AFter write sequence list  -------------");
				Set<String> setWidgetContainer = new HashSet<String>();

				// Iterate through the WidgetContainers
				for (int iWidgetContainer = 0; iWidgetContainer < WidgetContainerString.length; iWidgetContainer++) {

					String WidgetContainerList = WidgetContainerString[iWidgetContainer];
					// System.out.println(WidgetContainerList);
					// System.out.println("WidgetContainerList :" +
					// WidgetContainerList.toString());
					String varWidgetContainer = WidgetContainerString[iWidgetContainer];
					// System.out.println(varWidgetContainer);
					// System.out.println("varWidgetContainer :" +
					// varWidgetContainer.toString());
					if (!setWidgetContainer.contains(varWidgetContainer)) {
						// System.out.println(WidgetContainerList);

						org.openrdf.model.URI uriWidgetContainer = con
								.getValueFactory().createURI(
										WidgetContainerList.toString());

						// Get the WidgetContainer object
						// WidgetContainer widgetContainer =
						// c.getObject(WidgetContainer.class,
						// uriWidgetContainer);
						WidgetContainer widgetContainer = new WidgetContainer(
								con, uriWidgetContainer.toString());
						Set<CRUDOperation> methods = widgetContainer
								.getRaulMethods();
						Set<String> actions = widgetContainer.getRaulActions();
						Set<String> WidgetContainerClasses = widgetContainer
								.getRaulClasses();
						Set<String> WidgetContainerIds = widgetContainer
								.getRaulIds();
						Set<String> WidgetContainerNames = widgetContainer
								.getRaulNames();
						Set<String> WidgetContainerTitles = widgetContainer
								.getRaulTitles();
						Set<ie.deri.raul.model.rdfs.Class> widgetList = widgetContainer
								.getRaulLists();

						Set<URI> raulWidgetContainerProperties = new HashSet<URI>();
						raulWidgetContainerProperties.add(raulWidgetContainer);
						writer.startNode(uriWidgetContainer.toString(),
								raulWidgetContainerProperties);
						// System.out.println("----------- after statNode for raulWidgetContainerProperties  -------------");
						out.append("	<span style=\"display:none;\">\n");
						// System.out.println(out.toString());
						// Write the methods property
						writeMethodsProperty(methods, 2, writer, _repository);
						// System.out.println(out.toString());
						// Write the actions property
						writeStringProperty(raulaction, actions, 2, writer,
								_repository);
						// System.out.println(out.toString());
						// Write the class property
						writeStringProperty(raulclass, WidgetContainerClasses,
								2, writer, _repository);
						// System.out.println(out.toString());
						// Write the id property
						writeStringProperty(raulid, WidgetContainerIds, 2,
								writer, _repository);
						// System.out.println(out.toString());
						// Write the name property
						writeStringProperty(raulname, WidgetContainerNames, 2,
								writer, _repository);
						// System.out.println(out.toString());
						// Write the titles property
						writeStringProperty(raultitle, WidgetContainerTitles,
								2, writer, _repository);
						// System.out.println(out.toString());
						// Write the list property
						writeListsProperty(widgetList, 2, writer, _repository);
						// System.out.println(out.toString());
						// End the WidgetContainer properties
						out.append("	</span>\n");
						writer.endNode(uri, raulWidgetContainerProperties);
						// System.out.println("-------------- After end node function for widget cibtainer --------------------");
						// System.out.println(out.toString());

						Set<URI> raulCRUDOperationProperties = new HashSet<URI>();
						Set<Object> setCRUDOperations = new HashSet<Object>();
						Set<String> operationClass = new LinkedHashSet<String>();

						for (Iterator<CRUDOperation> methodsiter = methods
								.iterator(); methodsiter.hasNext();) {
							Object varMethods = methodsiter.next()
									.getOperationURI();
							if (!setCRUDOperations.contains(varMethods)) {
								org.openrdf.model.URI unknownCRUDOperation = con
										.getValueFactory().createURI(
												varMethods.toString());
								org.openrdf.model.URI perationURI;
								String operationClassValue;
								TupleQuery query = null;
								try {
									query = con
											.prepareTupleQuery(
													QueryLanguage.SPARQL,
													""
															+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
															+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
															+ "SELECT Distinct ?type "
															+ "WHERE {<"
															+ unknownCRUDOperation
																	.toString()
															+ "> rdf:type ?type. "
															+ "FILTER ( ?type != rdfs:Resource) } ");

								} catch (MalformedQueryException e1) {
									e1.printStackTrace();
								}
								TupleQueryResult resultSet = query.evaluate();
								while (resultSet.hasNext()) {
									BindingSet result = resultSet.next();
									String value = result.getValue("type")
											.toString();
									perationURI = con.getValueFactory()
											.createURI(value);
									operationClassValue = value.split("#")[1];
									operationClass.add(operationClassValue);
									raulCRUDOperationProperties
											.add(perationURI);
								}
								writer.startNode(varMethods.toString(),
										raulCRUDOperationProperties);
								writer.endNode(varMethods.toString(),
										raulCRUDOperationProperties);

								setCRUDOperations.add(varMethods);
							} else {
								setCRUDOperations.add(varMethods);
							}
						}
						// System.out.println("-------------- After CRUD operation  --------------------");
						// System.out.println(out.toString());
						// System.out.println("-------------- END CRUD --------------------");

						writeTagForm(operationClass, actions,
								WidgetContainerClasses, WidgetContainerIds, out);
						// System.out.println("-------------- After writeTagForm  --------------------");
						// System.out.println(out.toString());
						// System.out.println("-------------- END writeTagForm --------------------");

						widgetsHandler(widgetList, uri, out, writer,
								_repository, widgetContainer);

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
			out.append("</div>\n"); // added by pcc 27, Jun. 11

		/*
		 * //added by pcc 21,Jun. 11 for profiling totalTime =
		 * System.currentTimeMillis() - totalTime_start; out.append(
		 * "________________________________________________________________________________\n"
		 * ); out.append("timePage:" + timePage + "\n");
		 * out.append("TimeWContainer:" + TimeWContainer + "\n");
		 * out.append("numTextbox:" + numTextbox + "\taccTimeTextbox:" +
		 * accTimeTextbox + "\n"); out.append("numButton:" + numButton +
		 * "\taccTimeButton:" + accTimeButton + "\n"); out.append("numRButton:"
		 * + numRButton +method "\taccTimeRButton:" + accTimeRButton + "\n");
		 * out.append("numCheckbox:" + numCheckbox + "\taccTimeCheckbox:" +
		 * accTimeCheckbox + "\n"); out.append("numListbox:" + numListbox +
		 * "\taccTimeListbox:" + accTimeListbox + "\n");
		 * out.append("numListItem:" + numListItem + "\taccTimeListItem:" +
		 * accTimeListItem + "\n"); out.append("totalTime:" + totalTime + "\n");
		 * out.append(
		 * "________________________________________________________________________________\n"
		 * ); //added by pcc 21,Jun. 11 for profiling
		 */

		// writer.endRDF();
		writer.close();
		return out.toString();
	}

	private void writeMethodsProperty(Set<CRUDOperation> methods,
			Integer indent, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {
		// System.out.println("----------- Start Write Method Property  -------------");
		Set<Object> setMethods = new HashSet<Object>();
		for (Iterator<CRUDOperation> iterator = methods.iterator(); iterator
				.hasNext();) {
			Object varMethods = iterator.next().getOperationURI();
			// System.out.println("Method : " + varMethods);
			if (!setMethods.contains(varMethods)) {
				// System.out.print(" VARMETHOD : " + varMethods);
				Literal id = _repository.Literal(varMethods.toString(), null);

				writer.handleLiteral(indent, raulmethod, id); // writer.handleLiteral(indent,
																// raulmethods,
																// id);
				setMethods.add(varMethods);
			} else {
				setMethods.add(varMethods);
			}
		}
		// System.out.println( out.toString());
		// System.out.println("----------- End Write MEthod Property  -------------");
	}

	private void writeIntegerProperty(URI raulProperty, Set<Integer> literal,
			Integer indent, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {
		Set<Number> setProperties = new HashSet<Number>();
		Iterator<Integer> iterator = literal.iterator();

		while (iterator.hasNext()) {
			Number varProperties = iterator.next();

			if (!setProperties.contains(varProperties)) {
				Literal id = _repository.Literal(varProperties.toString(),
						integerdatatype);
				writer.handleLiteral(indent, raulProperty, id);
				setProperties.add(varProperties);
			} else {
				setProperties.add(varProperties);
			}
		}
	}

	private void writeStringProperty(URI raulProperty, Set<String> literal,
			Integer indent, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {

		// System.out.println("----------- In WriteStringProperty Function -------------");
		// System.out.println(" raulProperty: " +raulProperty );
		// System.out.println(" indent: " + indent);

		Set<String> setProperties = new HashSet<String>();
		Iterator<String> iterator = literal.iterator();
		while (iterator.hasNext()) {
			String varProperties = iterator.next();
			// System.out.println(" varProperties : " + varProperties);
			if (varProperties != null) {
				if (!setProperties.contains(varProperties)) {
					Literal id = _repository.Literal(varProperties.toString(),
							null);
					// System.out.println(" id : " + id.toString());
					writer.handleLiteral(indent, raulProperty, id);
					setProperties.add(varProperties);
				} else {
					setProperties.add(varProperties);
				}
			}
		}
	}

	private void writeListsProperty(Set<ie.deri.raul.model.rdfs.Class> lists,
			Integer indent, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {

		Set<Object> setLists = new HashSet<Object>();

		for (Iterator<ie.deri.raul.model.rdfs.Class> iterator = lists
				.iterator(); iterator.hasNext();) {
			Object var = iterator.next().getResourceURI();
			if (!setLists.contains(var)) {
				Literal id = _repository.Literal(var.toString(), null);
				writer.handleLiteral(indent, raullist, id);
				setLists.add(var);
			} else {
				setLists.add(var);
			}
		}
	}

	// take all elements of sequence and convert it to html list

	private void writeSequenceList(URI clazz, String[] lists, Integer indent,
			StringWriter out, RDFRepository _repository) throws IOException {
		// System.out.println("----------- In Write Sequence List  -------------");
		// System.out.println(clazz.toString());
		Set<Object> setWidget = new HashSet<Object>();

		for (int i = 0; i < indent; i++) {
			out.append("	");
		}
		;
		out.append("<ol style=\"display:none;\" about=\"" + clazz + "\">\n");
		for (int j = 0; j < lists.length; j++) {
			Object varLists = lists[j];
			if (!setWidget.contains(varLists) && !"".equals(varLists)) {
				for (int k = 0; k < indent; k++) {
					out.append("	");
				}
				;
				out.append("	<li rel=\"rdf:_" + (j + 1) + "\" resource=\""
						+ varLists + "\"></li>\n");
				setWidget.add(varLists);
			} else {
				setWidget.add(varLists);
			}
		}
		for (int l = 0; l < indent; l++) {
			out.append("	");
		}
		;
		out.append("</ol>\n");
		// System.out.println( out.toString());
	}

	private void writeBooleanProperty(URI raulProperty, Set<Boolean> selected,
			Integer indent, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {

		Set<Boolean> setBoolean = new HashSet<Boolean>();
		Iterator<Boolean> iterator = selected.iterator();
		while (iterator.hasNext()) {
			Boolean var = iterator.next();
			if (!setBoolean.contains(var)) {
				Literal id = _repository.Literal(var.toString(),
						booleandatatype);
				writer.handleLiteral(indent, raulProperty, id);
				setBoolean.add(var);
			} else {
				setBoolean.add(var);
			}
		}
	}

	private void writeObjectProperty(URI raulProperty,
			Set<ie.deri.raul.model.rdfs.Class> object, Integer indent,
			RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {

		// System.out.println("----------- In WriteObjectProperty Function -------------");
		// System.out.println(" raulProperty: " +raulProperty );
		// System.out.println(" indent: " + indent);

		Set<Object> setProperties = new HashSet<Object>();

		for (Iterator<ie.deri.raul.model.rdfs.Class> iterator = object
				.iterator(); iterator.hasNext();) {
			Object var = iterator.next().getResourceURI();
			if (!setProperties.contains(var)) {
				Literal id = _repository.Literal(var.toString(), null);
				// System.out.println(" Object Property id : " + id.toString());
				writer.handleLiteral(indent, raulProperty, id);
				setProperties.add(var);
			} else {
				setProperties.add(var);
			}
		}
	}

	private void writeValueStatement(Set<String> textboxValues,
			StringWriter out, RDFaMetaWriter writer, RDFRepository _repository,
			String groupId) throws RepositoryException,
			QueryEvaluationException, IOException {
		Set<Object> setTextboxValues = new HashSet<Object>();
		// System.out.println("Text Box Values : " + textboxValues );

		for (Iterator<String> iTextboxValues = textboxValues.iterator(); iTextboxValues
				.hasNext();) {
			// setTextboxValues.add("update");
			Object varValues = iTextboxValues.next();
			// System.out.println("iTextboxValues : " + varValues);
			if (!setTextboxValues.contains(varValues)) {

				org.openrdf.model.URI uriValueStatement = con.getValueFactory()
						.createURI(varValues.toString());

				try {
					Statement valueStatement = new Statement(con,
							uriValueStatement.toString());
					Set<String> valueSubjects = valueStatement.getRdfSubjects();
					Set<String> valuePredicates = valueStatement
							.getRdfPredicates();
					Set<String> valueObjects = valueStatement.getRdfObjects();
					// System.out.println(" Value Subjects : " + valueSubjects +
					// " Value Predicates : " +valuePredicates +
					// " Value Objects : "+valueObjects);
					// out.append("<div style=\"display:none;\">\n"); //modified
					// by pcc
					out.append("<div style=\"display:none;\" about=\""
							+ uriValueStatement.toString() + "\">\n");

					Set<String> setSubjects = new HashSet<String>();

					for (Iterator<String> iSubjects = valueSubjects.iterator(); iSubjects
							.hasNext();) {

						String varSubjects = iSubjects.next();

						if (!setSubjects.contains(varSubjects)) {
							String stringValue = varSubjects.toString();
							Literal value = _repository.Literal(stringValue,
									null);
							if (groupId != null) {
								_logger.info("===========================  "
										+ value.getLabel() + " : "
										+ value.stringValue());
							}
							writer.handleLiteral(1, rdfSubject, value);
							setSubjects.add(varSubjects);
						} else {
							setSubjects.add(varSubjects);
						}
					}

					Set<String> setPredicates = new HashSet<String>();

					for (Iterator<String> iPredicates = valuePredicates
							.iterator(); iPredicates.hasNext();) {

						String varPredicates = iPredicates.next();

						if (!setPredicates.contains(varPredicates)) {

							Literal value = _repository.Literal(
									varPredicates.toString(), null);
							writer.handleLiteral(1, rdfPredicate, value);
							setPredicates.add(varPredicates);
						} else {
							setPredicates.add(varPredicates);
						}
					}

					Set<String> setObjects = new HashSet<String>();

					for (Iterator<String> iObjects = valueObjects.iterator(); iObjects
							.hasNext();) {

						String varObjects = iObjects.next();

						if (!setObjects.contains(varObjects)) {

							Literal value = _repository.Literal(
									varObjects.toString(), null);
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
				// setTextboxValues.add(varValues);
			}
		}
	}

	// add a form tag and set action , method , id and class tags for form
	private void writeTagForm(Set<String> operation, Set<String> actions,
			Set<String> widgetContainerClasses, Set<String> widgetContainerIds,
			StringWriter out) throws IOException {

		out.append("<form");

		Set<String> set = new LinkedHashSet<String>();
		Iterator<String> iteratorOperation = operation.iterator();

		while (iteratorOperation.hasNext()) {
			String var = iteratorOperation.next();
			if (!set.contains(var)) {
				if (var.equalsIgnoreCase("CREATEOperation")) {
					out.append(" method=\"post\" ");
				} else if (var.equalsIgnoreCase("READOperation")) {
					out.append(" method=\"get\" ");
				} else if (var.equalsIgnoreCase("UPDATEOperation")) {
					out.append(" method=\"post\" ");
				} else if (var.equalsIgnoreCase("DELETEOperation")) {
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
			Iterator<String> iteratorClasses = widgetContainerClasses
					.iterator();
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

	private void writeTagButton(Button button, URI buttontype, StringWriter out)
			throws IOException, RepositoryException, QueryEvaluationException {

		out.append("<div>\n");
		if (buttontype == raulRadiobutton) {
			out.append("	<input type=\"radio\"");

		} else if (buttontype == raulCheckbox) {
			out.append("	<input type=\"checkbox\"");
		} else {
			// out.append("<input type=\"submit\"");
			out.append("	<a class=\"button\" href=\"javascript:void(0)\" ");
			// <raul:command>
			Iterator<String> iteratorCommands = button.getRaulCommands()
					.iterator();
			writeTagCommands(iteratorCommands, out);
		}

		if (!button.getRaulClasses().isEmpty()) {
			Iterator<String> iteratorClasses = button.getRaulClasses()
					.iterator();
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

		// the following two lines added by pcc
		Iterator<Boolean> iteratorChecked = button.getRaulChecked().iterator();
		writeTagChecked(iteratorChecked, out);

		if (buttontype == raulRadiobutton || buttontype == raulCheckbox)
			out.append("></input>\n");
		else
			out.append("><span>" + button.getRaulValues().iterator().next()
					+ "</span></a>\n");
		out.append("</div>\n");
	}

	/*
	 * //original writeTagInputTextarea() private void
	 * writeTagInputTextarea(Textbox textbox, StringWriter out) throws
	 * IOException { out.append("<div>\n"); if (textbox.getRaulRows().isEmpty())
	 * { Set<Boolean> setBoolean = new HashSet<Boolean>(); Iterator<Boolean>
	 * iteratorInputHidden = textbox.getRaulHiddens().iterator(); if
	 * (!textbox.getRaulHiddens().isEmpty()) { while
	 * (iteratorInputHidden.hasNext()) { Boolean var =
	 * iteratorInputHidden.next(); if (!setBoolean.contains(var)) { if (var) {
	 * out.append("<input type=\"hidden\""); } else {
	 * out.append("<input type=\"text\""); } setBoolean.add(var); } else {
	 * setBoolean.add(var); } }
	 * 
	 * } else { out.append("<input type=\"text\""); } } else {
	 * out.append("<textarea");
	 * 
	 * } if (!textbox.getRaulClasses().isEmpty()) { Iterator<String>
	 * iteratorClasses = textbox.getRaulClasses().iterator();
	 * writeTagClass(iteratorClasses, out); } Iterator<String> iteratorIds =
	 * textbox.getRaulIds().iterator(); writeTagIds(iteratorIds, out);
	 * Iterator<String> iteratorNames = textbox.getRaulNames().iterator();
	 * writeTagNames(iteratorNames, out); Iterator<String> iteratorTitles =
	 * textbox.getRaulTitles().iterator(); writeTagTitles(iteratorTitles, out);
	 * if (!textbox.getRaulRows().isEmpty()) { Iterator<Integer> iteratorRows =
	 * textbox.getRaulRows().iterator(); writeTagRows(iteratorRows, out);
	 * Iterator<String> iteratorSizes = textbox.getRaulSizes().iterator();
	 * writeTagCols(iteratorSizes, out); out.append("></textarea>\n"); } else
	 * out.append("/>\n"); out.append("</div>\n"); }
	 */

	private void ButtonHandler(Button button, String uri, URI ButtonType,
			StringWriter out, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException, RepositoryException, QueryEvaluationException {
		Set<URI> raulButtonProperties = new HashSet<URI>();
		raulButtonProperties.add(ButtonType);
		writer.startNode(button.getPageId(), raulButtonProperties);

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

		// Write the isIdentifier property //added by pcc 27Jan12
		Set<Boolean> ButtonIsIdentifiers = button.getRaulIsIdentifier();
		writeBooleanProperty(raulisIdentifier, ButtonIsIdentifiers, 2, writer,
				_repository);

		// Write the group property //added by pcc 22Nov11
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
		writeBooleanProperty(rauldisabled, ButtonDisabled, 2, writer,
				_repository);

		// End the Button properties
		out.append("	</span>\n");
		writer.endNode(uri, raulButtonProperties);

		// Write the value triple here
		if (ButtonType != raulButton)
			writeValueStatement(ButtonValues, out, writer, _repository, null);

		// Write the HTML button element
		writeTagButton(button, ButtonType, out);

	}

	private void ListboxHandler(Listbox listbox, String uri, StringWriter out,
			RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException, RepositoryException, QueryEvaluationException {
		/*
		 * //added by pcc 21,Jun. 11 for profiling numListbox++; //added by pcc
		 * 21,Jun. 11 for profiling
		 */

		Set<URI> raulListboxProperties = new HashSet<URI>();
		raulListboxProperties.add(raulListbox);
		writer.startNode(listbox.getPageId(), raulListboxProperties);

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

		// Write the group property //added by pcc 22Nov11
		Set<Group> groups = listbox.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);

		// Write the value property
		Set<String> ListboxValues = listbox.getRaulValues();
		writeStringProperty(raulvalue, ListboxValues, 2, writer, _repository);

		// Write the options property
		Set<ie.deri.raul.model.rdfs.Class> ListboxOptions = listbox
				.getRaulLists();
		writeObjectProperty(raullist, ListboxOptions, 2, writer, _repository);
		// writeObjectProperty(raullist, ListboxOptions, 2, writer,
		// _repository);

		Set<String> textboxTypeofProp = new HashSet<String>();
		textboxTypeofProp.add(listbox.getPropertyType());
		writeStringProperty(rdfsComment, textboxTypeofProp, 2, writer,
				_repository);

		Set<String> listboxRange = new HashSet<String>();
		listboxRange.add(listbox.getRange());
		writeStringProperty(rdfsRange, listboxRange, 2, writer, _repository);

		// End the Listbox properties
		out.append("	</span>\n");

		writer.endNode(uri, raulListboxProperties);

		// Write the value triple here
		writeValueStatement(ListboxValues, out, writer, _repository, null);
	}

	private void ListBoxOptionsHandler(Listbox listbox, String uri,
			StringWriter out, RDFaMetaWriter writer, RDFRepository _repository,
			Widget parentWidget) throws IOException, RepositoryException,
			QueryEvaluationException {
		/*
		 * //added by pcc 21,Jun. 11 for profiling startTime =
		 * System.currentTimeMillis(); //added by pcc 21,Jun. 11 for profiling
		 */

		Set<ie.deri.raul.model.rdfs.Class> ListboxOptions = listbox
				.getRaulLists();
		// writeObjectProperty(raullist, ListboxOptions, 2, writer,
		// _repository);

		// Helper StringWriter for the HTML
		// select/option elements
		StringWriter outListitem = new StringWriter();

		Set<Object> setListboxOptions = new HashSet<Object>();

		for (Iterator<ie.deri.raul.model.rdfs.Class> iListBoxOptions = ListboxOptions
				.iterator(); iListBoxOptions.hasNext();) {
			String[] ListItemString = null;
			Object varListBoxOptionsList = iListBoxOptions.next()
					.getResourceURI();
			// Get the next
			// ListBoxOptionsList
			Object ListBoxOptionsObject = varListBoxOptionsList;
			if (!setListboxOptions.contains(varListBoxOptionsList)) {
				org.openrdf.model.URI uriListBoxOptionsObject = con
						.getValueFactory().createURI(
								ListBoxOptionsObject.toString());
				Resource optionsList = new Resource(con,
						uriListBoxOptionsObject.toString());

				// Get all Listitems in the
				// RDF Seq
				String listItemList = optionsList.getRdfsMembers().toString()
						.replaceAll("\\[|\\]", "");
				ListItemString = listItemList.split(", ");

				// Write the Listitem
				// Sequence
				writeSequenceList(uriListBoxOptionsObject, ListItemString, 0,
						out, _repository);

				for (int iListitem = 0; iListitem < ListItemString.length; iListitem++) {
					/*
					 * //added by pcc 21,Jun. 11 for profiling numListItem++;
					 * //added by pcc 21,Jun. 11 for profiling
					 */

					org.openrdf.model.URI uriListitem = con.getValueFactory()
							.createURI(ListItemString[iListitem]);
					Listitem listitem = new Listitem(con,
							uriListitem.toString());

					Set<URI> raulListItemProperties = new HashSet<URI>();
					raulListItemProperties.add(raulListitem);
					writer.startNode(ListItemString[iListitem],
							raulListItemProperties);

					out.append("	<span style=\"display:none;\">\n");

					// Write the label
					// property
					Set<String> ListitemLabels = listitem.getRaulLabels();
					writeStringProperty(raullabel, ListitemLabels, 1, writer,
							_repository);

					// Write the class
					// property
					Set<String> ListitemClasses = listitem.getRaulClasses();
					writeStringProperty(raulclass, ListitemClasses, 2, writer,
							_repository);

					// Write the id property
					Set<String> ListitemIds = listitem.getRaulIds();
					writeStringProperty(raulid, ListitemIds, 2, writer,
							_repository);

					// Write the name
					// property
					Set<String> ListitemNames = listitem.getRaulNames();
					writeStringProperty(raulname, ListitemNames, 2, writer,
							_repository);

					// Write the titles
					// property
					Set<String> ListitemTitles = listitem.getRaulTitles();
					writeStringProperty(raultitle, ListitemTitles, 2, writer,
							_repository);

					// Write the isIdentifier property //added by pcc 27Jan12
					Set<Boolean> ListitemIsIdentifiers = listitem
							.getRaulIsIdentifier();
					writeBooleanProperty(raulisIdentifier,
							ListitemIsIdentifiers, 2, writer, _repository);

					// Write the value
					// property
					Set<String> ListitemValues = listitem.getRaulValues();
					writeStringProperty(raulvalue, ListitemValues, 2, writer,
							_repository);

					// Write the selected property //added by pcc
					Set<Boolean> ListitemSelected = listitem.getRaulSelected();
					writeBooleanProperty(raulselected, ListitemSelected, 2,
							writer, _repository);

					// End the Listitem
					// properties
					out.append("	</span>\n");
					writer.endNode(uri, raulListItemProperties);

					writeTagListitem(listitem, listbox, outListitem); // added
																		// by
																		// pcc
				}
				setListboxOptions.add(varListBoxOptionsList);

				writeTagListbox(listbox, out, parentWidget);
				out.append(outListitem.toString());
				out.append("	</select>\n</div>\n");

				/*
				 * //added by pcc 21,Jun. 11 for profiling accTimeListItem +=
				 * System.currentTimeMillis() - startTime; //added by pcc
				 * 21,Jun. 11 for profiling
				 */

			} else {
				setListboxOptions.add(varListBoxOptionsList);
			}
		}
	}

	private void DynamicGroupHandler(DynamicGroup dynamicgroup, String uri,
			StringWriter out, RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException, RepositoryException, QueryEvaluationException {

		Set<Integer> levels = dynamicgroup.getLevel();
		Integer level = levels.iterator().next();
		Set<String> dynamicgroupIds = dynamicgroup.getRaulIds();
		if (level > 1) {
			String id = dynamicgroupIds.iterator().next();
			out.append("<div style=\"display:none;\" level=\"" + level
					+ "\" id=\"" + id + "_temp\">");

		}
		Set<URI> raulDynamicGroupProperties = new HashSet<URI>();
		raulDynamicGroupProperties.add(raulDynamicGroup);

		writer.startNode(dynamicgroup.getPageId(), raulDynamicGroupProperties);

		// Write the label property
		Set<String> dynamicgroupLabels = dynamicgroup.getRaulLabels();
		writeStringProperty(raullabel, dynamicgroupLabels, 1, writer,
				_repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> dynamicgroupClasses = dynamicgroup.getRaulClasses();
		writeStringProperty(raulclass, dynamicgroupClasses, 2, writer,
				_repository);

		// Write the id property
		writeStringProperty(raulid, dynamicgroupIds, 2, writer, _repository);

		// Write the name property
		Set<String> dynamicgroupNames = dynamicgroup.getRaulNames();
		writeStringProperty(raulname, dynamicgroupNames, 2, writer, _repository);

		// Write the level property
		// Set<Integer> levels = dynamicgroup.get
		writeIntegerProperty(raulLevel, levels, 2, writer, _repository);
		// Write the group property //added by pcc 22Nov11
		Set<Group> groups = dynamicgroup.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);

		Set<ie.deri.raul.model.rdfs.Class> dynamicWidgetList = dynamicgroup
				.getRaulLists();
		writeListsProperty(dynamicWidgetList, 2, writer, _repository);

		// End the dynamicgroup properties
		out.append("	</span>\n");
		writer.endNode(uri, raulDynamicGroupProperties);
		if (level > 1) {
			out.append("</div>");
		}
		if (dynamicWidgetList.size() > 1) {
			Iterator<ie.deri.raul.model.rdfs.Class> it = dynamicWidgetList
					.iterator();
			for (; it.hasNext();) {
				ie.deri.raul.model.rdfs.Class raulClass = it.next();
				raulClass.setAssociatedWithDynamicClass(true);
				// raulClass.setAssoicatedClassUri(raulClass.getResourceURI());
			}
		}
		// add fieldset for the superior visibility
		Iterator<ie.deri.raul.model.rdfs.Class> it = dynamicWidgetList
				.iterator();
		for (; it.hasNext();) {
			ie.deri.raul.model.rdfs.Class clazz = it.next();
			Set<ie.deri.raul.model.rdfs.Class> clazzes = new HashSet<ie.deri.raul.model.rdfs.Class>();
			clazz.setAssociatedClassId(dynamicgroup.getRaulIds().iterator()
					.next());
			clazzes.add(clazz);
			widgetsHandler(clazzes, uri, out, writer, _repository, dynamicgroup);
		}

		Iterator<String> iteratorIds = dynamicgroup.getRaulIds().iterator();
		if (iteratorIds.hasNext()) {
			String var = iteratorIds.next();
			if (dynamicWidgetList.size() > 1 && level == 1)
				// out.append("<div>\n\t<input type=\"button\" onclick=\"dynamicWidgetsAdd('"
				// + dynamicgroup.getPageId() + "', 'addDynamicWidgets_" + var +
				// "')\"" + " name=\"addDynamicWidgets_" + var +
				// "\" id=\"addDynamicWidgets_" + var +
				// "\" value=\"+\"/ style=\"margin:14px 5px;\" group=\""+dynamicgroup.getPageId()+"\">\n</div>\n");
				out.append("<div>\n\t<a class=\"button\" onclick=\"dynamicWidgetsAdd('"
						+ dynamicgroup.getPageId()
						+ "', 'addDynamicWidgets_"
						+ var
						+ "')\""
						+ " name=\"addDynamicWidgets_"
						+ var
						+ "\" id=\"addDynamicWidgets_"
						+ var
						+ "\" href=\"javascript:void(0)\" style=\"clear:none;margin:28px 5px;\" group=\""
						+ dynamicgroup.getPageId()
						+ "\">\n<span> + </span>\n</a></div>\n");
			else if (level == 1)
				out.append("<div>\n\t<a class=\"button\" onclick=\"dynamicWidgetsAdd('"
						+ dynamicgroup.getPageId()
						+ "', 'addDynamicWidgets_"
						+ var
						+ "')\""
						+ " name=\"addDynamicWidgets_"
						+ var
						+ "\" id=\"addDynamicWidgets_"
						+ var
						+ "\" href=\"javascript:void(0)\" style=\"clear:none;margin:28px 5px;\" group=\""
						+ dynamicgroup.getPageId()
						+ "\">\n<span> + </span>\n</a></div>\n");
			// out.append("<div>\n\t<input type=\"button\" onclick=\"dynamicWidgetsAdd('"
			// + dynamicgroup.getPageId() + "', 'addDynamicWidgets_" + var +
			// "')\"" + " name=\"addDynamicWidgets_" + var +
			// "\" id=\"addDynamicWidgets_" + var +
			// "\" value=\"+\"/ group=\""+dynamicgroup.getPageId()+"\" style=\"margin:14px 5px;\">\n</div>\n");
			// out.append("<div>\n\t<input type=\"button\" onclick=\"dynamicWidgetsRemove('"
			// + dynamicgroup.getPageId() + "', 'removeDynamicWidgets_" + var +
			// "')\"" + " name=\"removeDynamicWidgets_" + var +
			// "\" id=\"removeDynamicWidgets_" + var +
			// "\" style=\"margin:10px 10px;\" value=\"-\"/>\n</div>\n");
		}

	}

	private void GroupHandler(Group group, String uri, StringWriter out,
			RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException, RepositoryException, QueryEvaluationException {
		Set<Integer> levels = group.getLevel();
		Integer level = levels.iterator().next();
		Set<String> GroupIds = group.getRaulIds();
		String hiddenDivId = "";
		String fieldSetName = preHandleDynId(
				group.getAssociatedDynamicGroupId(), group.getRaulIds()
						.iterator().next());
		if (level > 1) {
			String buttonClass="button";
			if(groupSearched==true)
				buttonClass="buttonDisabled";
			hiddenDivId = GroupIds.iterator().next() + "_temp";
			String range = group.getRange().substring(
					group.getRange().indexOf("#") + 1);
			if (level == 2) {
				out.append("<span property=\"raul:label\">" + fieldSetName + "</span>");
				if (group.getPropertyType() != null
						&& group.getPropertyType().contains("instanceEdited")) {
					out.append("<a class=\""+buttonClass+"\" onclick=\"openSubClassWindow('"
							+ hiddenDivId
							+ "',this);\" href=\"javascript:void(0)\" style=\"clear:none;margin:8px;\"><span>Update "
							+ range + "</span></a>");
					out.append("<a class=\""+buttonClass+"\" onclick=\"removeAddedWidget('"
							+ hiddenDivId
							+ "',this);\" href=\"javascript:void(0)\" style=\"clear:none;margin:8px;\"><span>Remove</span></a>");
				} else
					out.append("<a class=\""+buttonClass+"\" onclick=\"openSubClassWindow('"
							+ hiddenDivId
							+ "',this);\" href=\"javascript:void(0)\" style=\"clear:none;margin:8px;\"><span>Add "
							+ range + "</span></a>");
				out.append("<div style=\"display:none;\" level=\"" + level
						+ "\" id=\"" + hiddenDivId + "\">");
			} else
				out.append("<div style=\"display:none;\" level=\"" + level
						+ "\" id=\"" + hiddenDivId + "\">");
		}
		boolean isMultiLevel = false;
		boolean isSearched = false;
		if (group.getRange() != null) {
			String range = group.getRange().substring(
					group.getRange().indexOf("#") + 1);
			/*
			 * if (isMultiLevelGroup(group.getRaulLists(), group) ==
			 * true&&level==1) { out.append("<div><fieldset><legend>" +
			 * fieldSetName + "  " +
			 * "<a href=\"javascript:void(0)\" onclick=\"openSearchWindow('"
			 * +group.getRange()+"','"+group.getPageId()+
			 * "',this);\" class=\"editGroup\">Add Existing "+range+"</a>"
			 * //out.
			 * append("<a class=\"button\" onclick=\"openSearchWindow('"+widget
			 * .getRange()+"','"+para+
			 * "',this);\" href=\"javascript:void(0)\"><span>Add Existing</span></a>"
			 * ); //+
			 * ":<a href=\"javascript:void(0)\" class=\"addClass\" onclick=\"showSubClassForm(this)\">Add Class</a>"
			 * + "</legend>"); isMultiLevel = true; } else if
			 * (group.getPropertyType
			 * ()!=null&&group.getPropertyType().contains("MultiLevel"
			 * )&&level==1) { out.append("<div><fieldset><legend>" +
			 * fieldSetName + "  " +
			 * "<a href=\"javascript:void(0)\" onclick=\"openSearchWindow('"
			 * +group.getRange()+"','"+group.getPageId()+
			 * "',this);\" class=\"editGroup\">Add Existing "+range+"</a>" //+
			 * "<a href=\"javascript:void(0)\" onclick=\"editGroup(this)\" class=\"editGroup\">"
			 * +range+"</a>" //+
			 * ":<a href=\"javascript:void(0)\" class=\"addClass\" onclick=\"showSubClassForm(this)\">Add Class</a>"
			 * + "</legend>"); isMultiLevel = true; } else
			 * out.append("<div><fieldset><legend>" + fieldSetName + "  " +
			 * "<a href=\"javascript:void(0)\" onclick=\"openSearchWindow('"
			 * +group.getRange()+"','"+group.getPageId()+
			 * "',this);\" class=\"editGroup\">Add Existing "+range+"</a>" +
			 * "</legend>");
			 */
			if (level == 1) {
				out.append("<div><fieldset copied=\"copied\"><legend><i>"
						+ fieldSetName + "</i>"
						+ "&#160;&#160;<b>"
						+ range + "</b>"
						+ " | "
						+ "<a href=\"javascript:void(0)\" onclick=\"openSearchWindow('"
						+ group.getRange()
						+ "','"
						+ group.getPageId()
						+ "',this);\" class=\"editGroup\">Look up Existing</a>"
						+ "</legend>");
			} else {
				out.append("<div><fieldset><legend><i>"
						+ fieldSetName + "</i>"
						+ "&#160;&#160;"
						+ "  <b>"
						+ range + "</b>"
						+ " | "
						+ "<a href=\"javascript:void(0)\" onclick=\"openSearchWindow('"
						+ group.getRange()
						+ "','"
						+ group.getPageId()
						+ "',this);\" class=\"editGroup\">Look up Existing</a>"
						+ "</legend>");
			}
			isMultiLevel = true;
		} else
			out.append("<div><fieldset><legend style=\"font-style:italic;\">"
					+ fieldSetName + "</legend>");
		if (group.getPropertyType() != null
				&& group.getPropertyType().contains("searched")) {
			isSearched = true;
			//groupSearched = true;
		}
		// hasSupClassPropsInside(group.getRaulLists());
		Set<URI> raulGroupProperties = new HashSet<URI>();
		raulGroupProperties.add(raulGroup);
		writer.startNode(group.getPageId(), raulGroupProperties);

		// Write the label property
		Set<String> GroupLabels = group.getRaulLabels();
		writeStringProperty(raullabel, GroupLabels, 1, writer, _repository);

		out.append("	<span style=\"display:none;\">\n");

		// Write the class property
		Set<String> GroupClasses = group.getRaulClasses();
		writeStringProperty(raulclass, GroupClasses, 2, writer, _repository);

		// Write the id property
		writeStringProperty(raulid, GroupIds, 2, writer, _repository);

		// Write the name property
		Set<String> GroupNames = group.getRaulNames();
		writeStringProperty(raulname, GroupNames, 2, writer, _repository);

		writeIntegerProperty(raulLevel, levels, 2, writer, _repository);

		// Write the group property //added by pcc 22Nov11
		Set<Group> groups = group.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);

		Set<ie.deri.raul.model.rdfs.Class> GroupWidgetList = group
				.getRaulLists();
		writeListsProperty(GroupWidgetList, 2, writer, _repository);

		// Write the value property
		Set<String> GroupValues = group.getRaulValues();
		writeStringProperty(raulvalue, GroupValues, 2, writer, _repository);

		Set<String> comments = new HashSet<String>();
		if (isMultiLevel == true || isSearched == true) {
			String comment = group.getPropertyType() == null ? "" : group
					.getPropertyType();
			if (isMultiLevel == true) {
				if ("".equals(comment))
					comment += "MultiLevel";
				else if (!comment.contains("MultiLevel"))
					comment += ":MultiLevel";
			}
			if (isSearched == true) {
				if ("".equals(comment))
					comment += "edited";
				else if (!comment.contains("edited"))
					comment += ":edited";
			}
			comments.add(comment);
			writeStringProperty(rdfsComment, comments, 2, writer, _repository);
		}

		Set<String> groupRange = new HashSet<String>();
		groupRange.add(group.getRange());
		writeStringProperty(rdfsRange, groupRange, 2, writer, _repository);
		// End the group properties
		out.append("	</span>\n");
		writer.endNode(uri, raulGroupProperties);

		// Write the value triple here
		if (level > 1)
			writeValueStatement(GroupValues, out, writer, _repository, group
					.getRaulIds().iterator().next());
		else
			writeValueStatement(GroupValues, out, writer, _repository, null);
		// if(fieldSetName.equals("hasMeasurementCapability"))
		// System.out.println("start debugging");
		widgetsHandler(GroupWidgetList, uri, out, writer, _repository, group);
		if (this.textboxMultiple != null) {
			writeMultipleButtonForTextbox(textboxMultiple, out);
			this.textboxMultiple = null;
		}
		out.append("</fieldset></div>");
		
		// add by Shepherd Liu 11 Dec 2012
		// the "remove button" is added if the dynamic group contains more than
		// one group
		if (group.isNeedRemoveButton() == true && level == 1) {
			_logger.info("Start to generate button for removing group");
			String pageId = group.getPageId();
			String index = "0";
			if (pageId.lastIndexOf("_") > 0)
				index = pageId.substring(pageId.lastIndexOf("_") + 1);
			// out.append("<div>\n\t<input type=\"button\" onclick=\"dynamicWidgetsRemove('"
			// + group.getDynamicGroupUri() +
			// "', '"+group.getPageId()+"',this,'"+index+"')\"" +
			// " name=\"removeDynamicWidgets_" + index +
			// "\" id=\"removeDynamicWidgets_" + index +
			// "\" value=\"-\" style='margin:14px 5px;'/>\n</div>\n");
			out.append("<div>\n\t<a class=\"button\" onclick=\"dynamicWidgetsRemove('"
					+ group.getDynamicGroupUri()
					+ "', '"
					+ group.getPageId()
					+ "',this,'"
					+ index
					+ "')\""
					+ " name=\"removeDynamicWidgets_"
					+ index
					+ "\" id=\"removeDynamicWidgets_"
					+ index
					+ "\" href=\"javascript:void(0)\" style=\"clear:none;margin:28 5px;\"><span> - </span></a>\n</div>\n");
			_logger.info("End generating button for removing group");
		}
		if (level > 1)
			out.append("</div>");
		//groupSearched = false;
	}

	private String preHandleDynId(String dynamicGroupId, String groupId) {
		if (dynamicGroupId.indexOf("_") != -1)
			dynamicGroupId = dynamicGroupId.substring(dynamicGroupId
					.lastIndexOf("_") + 1);
		/*
		 * if (groupId.indexOf("_") != -1) { String index =
		 * groupId.substring(groupId.lastIndexOf("_") + 1); try {
		 * Integer.parseInt(index); dynamicGroupId += "_" + index; } catch
		 * (Exception e) { } }
		 */
		return dynamicGroupId;
	}

	private boolean isMultiLevelGroup(
			Set<ie.deri.raul.model.rdfs.Class> widgetList, Widget parentWidget)
			throws RepositoryException, QueryEvaluationException {
		boolean isMultilevel = false;
		for (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetList = widgetList
				.iterator(); iWidgetList.hasNext();) {
			ie.deri.raul.model.rdfs.Class raulClass = iWidgetList.next();
			Object varList = raulClass.getResourceURI();
			// System.out.println(" varList  " + varList );
			// Get the next WidgetList
			Object WidgetListObject = varList;
			org.openrdf.model.URI uriWidgetListObject = con.getValueFactory()
					.createURI(WidgetListObject.toString());
			Resource widgetsTypeList = new Resource(con,
					uriWidgetListObject.toString());

			// Get all widgets in the RDF Seq
			String widgetTypeList = widgetsTypeList.getRdfsMembersInSeq()
					.toString().replaceAll("\\[|\\]", "");
			String[] unknownType = widgetTypeList.split(", ");
			String[] types = new String[unknownType.length];

			// Write the WidgetList
			getAllTypes(unknownType, types, parentWidget);
			for (String type : types) {
				if ("DynamicGroup".equals(type)) {
					isMultilevel = true;
					break;
				}
			}
			if (isMultilevel == true)
				break;
		}
		return isMultilevel;
	}

	private void TextboxHandler(Textbox textbox, String uri, StringWriter out,
			RDFaMetaWriter writer, RDFRepository _repository,
			Widget parentWidget) throws IOException, RepositoryException,
			QueryEvaluationException {
		/*
		 * //added by pcc 21,Jun. 11 for profiling numTextbox++; //added by pcc
		 * 21,Jun. 11 for profiling
		 */
		Set<String> textboxValues = textbox.getRaulValues();

		Set<URI> raulTextboxProperties = new HashSet<URI>();
		// System.out.println(" Text Box : " + textbox.toString());
		// System.out.println("uri : " + uri.toString());
		raulTextboxProperties.add(raulTextbox);
		writer.startNode(textbox.getPageId(), raulTextboxProperties);

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

		// Write the group property //added by pcc 22Nov11
		Set<Group> groups = textbox.getRaulGroups();
		writeGroupsProperty(groups, 2, writer, _repository);

		// Write the isIdentifier property //added by pcc 27Jan12
		Set<Boolean> textboxIsIdentifiers = textbox.getRaulIsIdentifier();
		writeBooleanProperty(raulisIdentifier, textboxIsIdentifiers, 2, writer,
				_repository);

		Set<Boolean> isTextboxMultiple = textbox.isWidgetMultiple();
		if (isTextboxMultiple.iterator().next().booleanValue() == true) {
			this.textboxMultiple = textbox;
		}
		writeBooleanProperty(raulMultipleWidgets, isTextboxMultiple, 2, writer,
				_repository);

		// Write the value property
		// Set<String> textboxValues = textbox.getRaulValues();
		writeStringProperty(raulvalue, textboxValues, 2, writer, _repository);

		Set<Integer> textboxRows = textbox.getRaulRows();
		writeIntegerProperty(raulrow, textboxRows, 2, writer, _repository);

		Set<String> textboxSizes = textbox.getRaulSizes();
		writeStringProperty(raulsize, textboxSizes, 2, writer, _repository);

		// Write the hidden property
		Set<Boolean> textboxHiddens = textbox.getRaulHiddens();
		writeBooleanProperty(raulhidden, textboxHiddens, 2, writer, _repository);

		Set<String> textboxTypeofProp = new HashSet<String>();
		textboxTypeofProp.add(textbox.getPropertyType());
		writeStringProperty(rdfsComment, textboxTypeofProp, 2, writer,
				_repository);

		Set<String> textboxRange = new HashSet<String>();
		textboxRange.add(textbox.getRange());
		writeStringProperty(rdfsRange, textboxRange, 2, writer, _repository);

		// End the Textbox properties
		out.append("	</span>\n");
		// writer.endNode(uri, raulWidgetContainerProperties);
		writer.endNode(uri, raulTextboxProperties);

		// Write the value triple here
		writeValueStatement(textboxValues, out, writer, _repository, null);

		// Write the HTML input/textarea
		// element
		// writeTagInputTextarea(textbox, out); //modified by pcc
		boolean readonly = false;
		// for testing change the value of readonly later to true
		String propertyType = textbox.getPropertyType() == null ? "" : textbox
				.getPropertyType();
		// if
		// (propertyType.contains(OBJECT_PROP)&&!propertyType.contains(FIELD_EDITED)&&groupEdited==false)

		if (groupSearched == true || propertyType.contains(FIELD_SEARCHED)){
			readonly = true;
		}
		writeTagInputTextarea(textbox, textboxValues, out, writer, _repository,
				readonly);	
		if ((!(parentWidget instanceof DynamicGroup || parentWidget instanceof Group))
				&& propertyType.contains(OBJECT_PROP)
				&& textbox.getRaulLabels().size() > 0)
			// writeLookUplink(textbox, out, "lookupLinkSingle");
			objectPropertyTextbox = textbox;
		if ((parentWidget instanceof Group)
				&& propertyType.contains(OBJECT_PROP))
			((Group) parentWidget).setHasObjectProperty(true);

		/*
		 * if(textbox.getRaulIds().size()>0){ String
		 * id=textbox.getRaulIds().iterator().next();
		 * if(id.lastIndexOf("_")!=-1) id=id.substring(0,id.lastIndexOf("_"));
		 * if
		 * (textbox.isWidgetMultiple().size()>0&&textbox.isWidgetMultiple().iterator
		 * ().next().booleanValue()==true){ if(textboxRegister.get(id)==null){
		 * String
		 * buttonId="addDynamicProperty_"+textbox.getRaulIds().iterator().
		 * next(); out.append("<input type='button' id='"+buttonId+
		 * "' value='+' onclick='addDynamicProperty(\""
		 * +textbox.getPageId()+"\",\""+buttonId+"\");'/>");
		 * textboxRegister.put(id, id); }else{ out.append(
		 * "<input type='button' value='-' onclick='removeDynamicProperty(this,\""
		 * +textbox.getPageId()+"\")' style='margin:10px 0px;'/>"); } } }
		 */
	}

	private void widgetsHandler(Set<ie.deri.raul.model.rdfs.Class> widgetList,
			String uri, StringWriter out, RDFaMetaWriter writer,
			RDFRepository _repository, Widget parentWidget)
			throws RepositoryException, QueryEvaluationException, IOException {
		// Get all widget lists in the WidgetContainer
		Set<Object> setWidgetList = new HashSet<Object>();
		for (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetList = widgetList
				.iterator(); iWidgetList.hasNext();) {
			ie.deri.raul.model.rdfs.Class raulClass = iWidgetList.next();
			Object varList = raulClass.getResourceURI();
			// System.out.println(" varList  " + varList );
			// Get the next WidgetList
			Object WidgetListObject = varList;
			if (!setWidgetList.contains(varList)) {
				org.openrdf.model.URI uriWidgetListObject = con
						.getValueFactory().createURI(
								WidgetListObject.toString());
				Resource widgetsTypeList = new Resource(con,
						uriWidgetListObject.toString());

				// Get all widgets in the RDF Seq
				String widgetTypeList = widgetsTypeList.getRdfsMembersInSeq()
						.toString().replaceAll("\\[|\\]", "");
				String[] unknownType = widgetTypeList.split(", ");
				Set<Object> setUnknownType = new HashSet<Object>();
				String[] types = new String[unknownType.length];

				// Write the WidgetList
				String[] lists = getAllTypes(unknownType, types, parentWidget);
				// writeSequenceList(uriWidgetListObject, unknownType, 0, out,
				// _repository);
				writeSequenceList(uriWidgetListObject, lists, 0, out,
						_repository);

				/*
				 * //added by pcc 21,Jun. 11 for profiling TimeWContainer =
				 * System.currentTimeMillis() - startTime; //added by pcc
				 * 21,Jun. 11 for profiling
				 */

				// Iterate through the Widgets and determine
				// their type
				// System.out.println("------------------ after writing sequenc list of widget container ");
				// System.out.println(out.toString());
				// System.out.println("--------------------");
				for (int iUnknownType = 0; iUnknownType < unknownType.length; iUnknownType++) {
					String unknownTypeList = unknownType[iUnknownType];
					String varUnknownType = unknownType[iUnknownType];
					if ("".equals(unknownTypeList))
						continue;
					if (!setUnknownType.contains(varUnknownType)) {
						org.openrdf.model.URI unknownTypeBox = con
								.getValueFactory().createURI(
										unknownTypeList.toString());
						/*
						 * String type= ""; TupleQuery query = null; try { query
						 * = con.prepareTupleQuery(QueryLanguage.SPARQL, "" +
						 * "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						 * +
						 * "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
						 * + "SELECT Distinct ?type " +
						 * "WHERE {<"+unknownTypeBox
						 * .toString()+"> rdf:type ?type. " +
						 * "FILTER ( ?type != rdfs:Resource) } ");
						 * 
						 * } catch (MalformedQueryException e1) {
						 * e1.printStackTrace(); } TupleQueryResult resultSet =
						 * query.evaluate(); while (resultSet.hasNext()) {
						 * BindingSet result = resultSet.next();
						 * //System.out.println
						 * (result.getValue("type").toString()); type =
						 * result.getValue("type").toString().split("#")[1]; }
						 */
						String type = types[iUnknownType];
						if (("DynamicGroup".equalsIgnoreCase(type)
								|| "Group".equalsIgnoreCase(type) || "Button"
									.equalsIgnoreCase(type))) {
							if (objectPropertyTextbox != null) {
								writeLookUplink(objectPropertyTextbox, out,
										"lookupLinkSingle");
								objectPropertyTextbox = null;
							}
							if (this.textboxMultiple != null) {
								writeMultipleButtonForTextbox(textboxMultiple,
										out);
								this.textboxMultiple = null;
							}
						}
						if (type.equalsIgnoreCase("Textbox")) {
							Textbox textbox = new Textbox(con,
									unknownTypeBox.toString());
							if (textbox.getRaulLabels().size() > 0) {
								if (objectPropertyTextbox != null) {
									writeLookUplink(objectPropertyTextbox, out,
											"lookupLinkSingle");
									objectPropertyTextbox = null;
								}
								if (this.textboxMultiple != null) {
									writeMultipleButtonForTextbox(
											textboxMultiple, out);
									this.textboxMultiple = null;
								}
							}
							TextboxHandler(textbox, uri, out, writer,
									_repository, parentWidget);
						} else if (type.equalsIgnoreCase("Listbox")) {
							Listbox listbox = new Listbox(con,
									unknownTypeBox.toString());
							ListboxHandler(listbox, uri, out, writer,
									_repository);
							ListBoxOptionsHandler(listbox, uri, out, writer,
									_repository, parentWidget);
						} else if (type.equalsIgnoreCase("Group")) {
							Group group = new Group(con,
									unknownTypeBox.toString());
							// _logger.info("======================="+raulClass.isAssociatedWithDynamicClass()+" : "+raulClass.getAssoicatedClassUri());
							if (raulClass.isAssociatedWithDynamicClass() == true) {
								group.setNeedRemoveButton(true);
								group.setDynamicGroupUri(raulClass
										.getResourceURI());
							}
							group.setAssociatedDynamicGroupId(raulClass
									.getAssociatedClassId());
							GroupHandler(group, uri, out, writer, _repository);
						} /*
						 * else if (type.equalsIgnoreCase("Group")){ Group group
						 * = new Group(con, unknownTypeBox.toString());
						 * GroupHandler(group, uri, out, writer, _repository); }
						 */
						else if (type.equalsIgnoreCase("DynamicGroup")) {
							DynamicGroup dynamicgroup = new DynamicGroup(con,
									unknownTypeBox.toString());
							DynamicGroupHandler(dynamicgroup, uri, out, writer,
									_repository);
						} else if (type.equalsIgnoreCase("Button")
								|| type.equalsIgnoreCase("Radiobutton")
								|| type.equalsIgnoreCase("Checkbox")) {
							Button button = new Button(con,
									unknownTypeBox.toString());
							URI ButtonType = con.getValueFactory().createURI(
									"http://purl.org/NET/raul#" + type);
							ButtonHandler(button, uri, ButtonType, out, writer,
									_repository);
						}

						// get Listbox
						/*
						 * try {
						 * 
						 * Listbox listbox = new Listbox(c,
						 * unknownTypeBox.toString()); ListboxHandler(listbox,
						 * uri, out, writer, _repository);
						 * ListBoxOptionsHandler(listbox, uri, out, writer,
						 * _repository);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * try { //added by pcc Group group = new Group(c,
						 * unknownTypeBox.toString()); GroupHandler(group, uri,
						 * out, writer, _repository);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * // get Button try {
						 * 
						 * Button button = new Button( c,
						 * unknownTypeBox.toString());
						 * 
						 * 
						 * URI ButtonType = raulButton;
						 * 
						 * try { Radiobutton radiobutton = new Radiobutton( c,
						 * unknownTypeBox.toString()); ButtonType =
						 * raulRadiobutton; } catch (ClassCastException e) { }
						 * 
						 * try { Checkbox checkbox = new Checkbox
						 * (c,unknownTypeBox.toString()); ButtonType =
						 * raulCheckbox; } catch (ClassCastException e) { }
						 * 
						 * ButtonHandler(button, uri, ButtonType, out, writer,
						 * _repository);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * // get DynamicGroup added by pcc try { DynamicGroup
						 * dynamicgroup = new DynamicGroup(c,
						 * unknownTypeBox.toString());
						 * DynamicGroupHandler(dynamicgroup, uri, out, writer,
						 * _repository);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 */

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

	private String[] getAllTypes(String[] urls, String[] types,
			Widget parentWidget) throws RepositoryException,
			QueryEvaluationException {
		String[] list = new String[urls.length];
		Set<Object> setUnknownType = new HashSet<Object>();
		for (int iUnknownType = 0; iUnknownType < urls.length; iUnknownType++) {
			String unknownTypeList = urls[iUnknownType];
			String varUnknownType = urls[iUnknownType];
			if ("".equals(unknownTypeList)) {
				types[iUnknownType] = "";
				list[iUnknownType] = "";
				continue;
			}
			if (!setUnknownType.contains(varUnknownType)) {
				org.openrdf.model.URI unknownTypeBox = con.getValueFactory()
						.createURI(unknownTypeList.toString());
				String type = "";
				String unknownTypeBoxStr = unknownTypeBox.toString();
				TupleQuery query = null;
				try {
					query = con
							.prepareTupleQuery(
									QueryLanguage.SPARQL,
									""
											+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
											+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
											+ "SELECT Distinct ?type "
											+ "WHERE {<"
											+ unknownTypeBoxStr
											+ "> rdf:type ?type. "
											+ "FILTER ( ?type != rdfs:Resource) } ");

				} catch (MalformedQueryException e1) {
					e1.printStackTrace();
				}
				TupleQueryResult resultSet = query.evaluate();
				while (resultSet.hasNext()) {
					BindingSet result = resultSet.next();
					// System.out.println(result.getValue("type").toString());
					if (result.getValue("type").toString()
							.contains(RAUL_RDF_PREFIX))
						type = result.getValue("type").toString().split("#")[1];
				}
				types[iUnknownType] = type;
				list[iUnknownType] = urls[iUnknownType];
			}
		}
		return list;
	}

	/*
	 * private Boolean hasSupClassPropsInside(Set<ie.deri.raul.model.rdfs.Class>
	 * widgetList) throws RepositoryException, QueryEvaluationException,
	 * IOException{ boolean isObjectProperty=false; for
	 * (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetList =
	 * widgetList.iterator(); iWidgetList.hasNext();) {
	 * ie.deri.raul.model.rdfs.Class raulClass=iWidgetList.next(); Object
	 * classUri=raulClass.getResourceURI(); org.openrdf.model.URI
	 * uriWidgetListObject =
	 * con.getValueFactory().createURI(classUri.toString()); Resource
	 * widgetsTypeList = new Resource(con,uriWidgetListObject.toString());
	 * 
	 * // Get all widgets in the RDF Seq String widgetTypeList =
	 * widgetsTypeList.getRdfsMembersInSeq().toString().replaceAll("\\[|\\]",
	 * ""); String[] unknownType = widgetTypeList.split(", "); for(String
	 * s:unknownType){ TupleQuery query = null; try { query =
	 * con.prepareTupleQuery(QueryLanguage.SPARQL, "" +
	 * "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	 * "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
	 * "PREFIX raul:<http://purl.org/NET/raul#> "+ "SELECT Distinct ?value " +
	 * "WHERE {<"+s+"> raul:value ?value}"); TupleQueryResult resultSet =
	 * query.evaluate(); String valueTriple =
	 * resultSet.next().getValue("value").stringValue(); resultSet.close();
	 * query = con.prepareTupleQuery(QueryLanguage.SPARQL, "" +
	 * "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	 * "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
	 * "PREFIX raul:<http://purl.org/NET/raul#> "+ "SELECT Distinct ?value " +
	 * "WHERE {<"+valueTriple+"> rdfs:comment ?value}"); resultSet =
	 * query.evaluate(); String dataType =
	 * resultSet.next().getValue("value").stringValue(); resultSet.close(); }
	 * catch (MalformedQueryException e1) { e1.printStackTrace(); }
	 * 
	 * //System.out.println(valueTriple); } } return new
	 * Boolean(isObjectProperty); }
	 */

	// this method is modified by pcc
	private void writeTagInputTextarea(Textbox textbox,
			Set<String> textboxValues, StringWriter out, RDFaMetaWriter writer,
			RDFRepository _repository, boolean readonly) throws IOException,
			RepositoryException, QueryEvaluationException {
		out.append("<div>\n");
		if (textbox.getRaulRows().isEmpty()) {
			Set<Boolean> setBoolean = new HashSet<Boolean>();
			Iterator<Boolean> iteratorInputHidden = textbox.getRaulHiddens()
					.iterator();
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
			Iterator<String> iteratorClasses = textbox.getRaulClasses()
					.iterator();
			writeTagClass(iteratorClasses, out);
		}
		Iterator<String> iteratorIds = textbox.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);
		Iterator<String> iteratorNames = textbox.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);
		Iterator<String> iteratorTitles = textbox.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);
		out.append(" aboutValue=\"" + textbox.getPageId() + "\" ");
		if (readonly == true)
			out.append(" disabled=\"disabled\" ");
		if (textbox.getPropertyType() != null
				&& textbox.getPropertyType().contains(OBJECT_PROP))
			out.append(" onblur=\"autoAddBaseUrl(this);\" ");
		if (!textbox.getRaulRows().isEmpty()) {
			Iterator<Integer> iteratorRows = textbox.getRaulRows().iterator();
			writeTagRows(iteratorRows, out);
			Iterator<String> iteratorSizes = textbox.getRaulSizes().iterator();
			writeTagCols(iteratorSizes, out);
			// out.append("></textarea>\n");
			out.append(">");
			try {
				writeTagTagInputTextareaValue(textboxValues, out, writer,
						_repository,textbox.getPageId());
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}
			out.append("</textarea>\n");

		} else {
			out.append(" value=\"");
			try {
				writeTagTagInputTextareaValue(textboxValues, out, writer,
						_repository,textbox.getPageId());
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}
			out.append("\"");
			out.append("></input>\n");
		}
		out.append("</div>\n");
	}

	private void writeLookUplink(Widget widget, StringWriter out, String style)
			throws RepositoryException, QueryEvaluationException {
		// need to add js function for opening a popup window
		// String para=null;
		// if(widget.getRaulNames().size()==0)
		String para = widget.getPageId();
		// else
		// para=widget.getRaulNames().iterator().next();
		if (widget instanceof Textbox) {
			out.append("<a class=\"button\" style=\"clear:none;margin:8px;\" onclick=\"openSearchWindow('"
					+ widget.getRange()
					+ "','"
					+ para
					+ "',this);\" href=\"javascript:void(0)\" field=\""
					+ para
					+ "\"><span> Look up Existing "
					+ widget.getRange().split("#")[1]
					+ "</span></a>\n");
			// out.append("<a class=\"button\" style=\"clear:none;margin:8px;\" onclick=\"editSingleField('"+para+"',this);\" href=\"javascript:void(0)\" field=\""+para+"\"><span>New</span></a>");
		}
		// else
		// out.append("<a class=\"button\" onclick=\"openSearchWindow('"+widget.getRange()+"','"+para+"',this);\" href=\"javascript:void(0)\"><span>Add Existing</span></a>");
	}

	private void writeMultipleButtonForTextbox(Textbox box, StringWriter out) {
		try {
			String pageId = box.getPageId();
			String property = box.getPropertyType();
			String index = "0";
			if (pageId.lastIndexOf("_") > 0) {
				try {
					Integer.parseInt(pageId.substring(pageId.lastIndexOf("_") + 1));
					index = pageId.substring(pageId.lastIndexOf("_") + 1);
				} catch (Exception e) {

				}
				index = pageId.substring(pageId.lastIndexOf("_") + 1);
			}
			if (property.contains("singleTextAdded")) {
				out.append("<div>\n\t<a class=\"button\" onclick=\"dynamicComponentRemove('"
						+ box.getPageId()
						+ "',this)\""
						+ " pageId=\""
						+ box.getPageId()
						+ "\""
						+ " name=\"removeDynamicComponent_"
						+ index
						+ "\" id=\"removeDynamicComponent_"
						+ index
						+ "\" href=\"javascript:void(0)\" style=\"clear:none;margin:8px;\"><span> - </span></a>\n</div>\n");
			} else {
				out.append("<div>\n\t<a class=\"button\" onclick=\"dynamicComponentAdd('"
						+ box.getPageId()
						+ "',this)\""
						+ " pageId=\""
						+ box.getPageId()
						+ "\""
						+ " name=\"addDynamicComponent_"
						+ index
						+ "\" id=\"addDynamicComponent_"
						+ index
						+ "\" href=\"javascript:void(0)\" style=\"clear:none;margin:8px;\"><span> + </span></a>\n</div>\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeTagListbox(Listbox listbox, StringWriter out,
			Widget parentWidget) throws IOException, RepositoryException,
			QueryEvaluationException {
		String propertyType = listbox.getPropertyType() == null ? "" : listbox
				.getPropertyType();
		// if
		// (propertyType.contains(OBJECT_PROP)&&!propertyType.contains(FIELD_EDITED)&&groupEdited==false)
		if (groupSearched == true || propertyType.contains(FIELD_SEARCHED))
			out.append("<div style=\"display:none;\">\n");
		else
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

	private void writeTagListitem(Listitem listitem, Listbox listbox,
			StringWriter out) throws IOException, RepositoryException,
			QueryEvaluationException {
		out.append("		<option");

		Iterator<String> iteratorTitles = listitem.getRaulTitles().iterator();
		writeTagTitles(iteratorTitles, out);

		Iterator<String> iteratorIds = listitem.getRaulIds().iterator();
		writeTagIds(iteratorIds, out);

		Iterator<String> iteratorNames = listitem.getRaulNames().iterator();
		writeTagNames(iteratorNames, out);

		Iterator<String> iteratorValues = listitem.getRaulValues().iterator();
		writeTagValues(iteratorValues, out);

		// added by pcc
		Iterator<Boolean> iteratorSelected = listitem.getRaulSelected()
				.iterator();
		writeTagSelected(iteratorSelected, out);

		out.append(">");
		Iterator<String> iteratorLabels = listitem.getRaulLabels().iterator();
		writeTagLabels(iteratorLabels, out);
		out.append("</option>\n");
	}

	private void writeTagClass(Iterator<String> iteratorClasses,
			StringWriter out) {
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

	private void writeTagTitles(Iterator<String> iteratorTitles,
			StringWriter out) {
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

	private void writeTagValues(Iterator<String> iteratorValues,
			StringWriter out) {
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

	// added by pcc
	private void writeTagCommands(Iterator<String> iteratorCommands,
			StringWriter out) {
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

	// added by pcc
	private void writeTagSelected(Iterator<Boolean> iteratorSelected,
			StringWriter out) {
		if (iteratorSelected.hasNext()) {
			if (iteratorSelected.next())
				out.append(" selected=\"selected\" ");
		}
	}

	private void writeTagLabels(Iterator<String> iteratorLabels,
			StringWriter out) {
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

	// added by pcc
	private void writeTagChecked(Iterator<Boolean> iteratorChecked,
			StringWriter out) {
		if (iteratorChecked.hasNext()) {
			if (iteratorChecked.next())
				out.append(" checked=\"checked\" ");
		}
	}

	// this method is added by pcc
	private void writeTagTagInputTextareaValue(Set<String> textboxValues,
			StringWriter out, RDFaMetaWriter writer, RDFRepository _repository,String textBoxAbout)
			throws RepositoryException, QueryEvaluationException, IOException {
		Set<Object> setTextboxValues = new HashSet<Object>();
		Set<String> setObjects = new HashSet<String>();

		for (Iterator<String> iTextboxValues = textboxValues.iterator(); iTextboxValues
				.hasNext();) {
			Object varValues = iTextboxValues.next();
			if (!setTextboxValues.contains(varValues)) {
				org.openrdf.model.URI uriValueStatement = con.getValueFactory()
						.createURI(varValues.toString());
				try {
					Statement valueStatement = new Statement(con,
							uriValueStatement.toString());
					Set<String> valueObjects = valueStatement.getRdfObjects();
					// Set<Object> setObjects = new HashSet<Object>();
					for (Iterator<String> iObjects = valueObjects.iterator(); iObjects
							.hasNext();) {
						String varObjects = iObjects.next();
						// if (!setObjects.contains(varObjects)) {
						if (!setObjects.contains(varObjects.toString())) {
							// out.append(" value=\"" + varObjects.toString() +
							// "\"")
							String value=varObjects.toString();
							if(value.contains(textBoxAbout)==true){
								value=value.substring(textBoxAbout.length()+1);
							}
							out.append(value);
							// setObjects.add(varObjects);
							setObjects.add(varObjects.toString());
						} else {
							// setObjects.add(varObjects);
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
	 * //no longer use private void writeGroupClass(Set<String> groups, Integer
	 * indent, RDFaMetaWriter writer, RDFRepository _repository) throws
	 * IOException { Set<URI> raulGroupProperties = new HashSet<URI>();
	 * raulGroupProperties.add(raulGroup); GroupSet.clear(); for
	 * (Iterator<String> iterator = groups.iterator(); iterator.hasNext();) {
	 * Object var = iterator.next(); if (!GroupSet.contains(var)) {
	 * writer.startNode(var.toString(), raulGroupProperties);
	 * writer.endNode(var.toString(), raulGroupProperties); GroupSet.add(var); }
	 * else { GroupSet.add(var); } } }
	 */

	private void writeGroupsProperty(Set<Group> groups, Integer indent,
			RDFaMetaWriter writer, RDFRepository _repository)
			throws IOException {

		Set<Object> setGroups = new HashSet<Object>();
		for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext();) {
			Object varGroups = iterator.next().getPageId();
			if (!setGroups.contains(varGroups)) {
				Literal id = _repository.Literal(varGroups.toString(), null);
				writer.handleLiteral(indent, raulgroup, id);
				setGroups.add(varGroups);
			} else {
				setGroups.add(varGroups);
			}
		}
	}

	private void uriInit(RDFRepository _repository) {
		// Define RDF predicates

		rdfSubject = _repository
				.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

		rdfPredicate = _repository
				.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

		rdfObject = _repository
				.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

		// Define RaUL Classes and basic datatypes
		raulPage = _repository.URIref("http://purl.org/NET/raul#Page");

		raulWidgetContainer = _repository
				.URIref("http://purl.org/NET/raul#WidgetContainer");

		raulCRUDOperation = _repository
				.URIref("http://purl.org/NET/raul#CRUDOperation");

		raulCREATEOperation = _repository
				.URIref("http://purl.org/NET/raul#CREATEOperation");

		raulREADOperation = _repository
				.URIref("http://purl.org/NET/raul#READOperation");

		raulUPDATEOperation = _repository
				.URIref("http://purl.org/NET/raul#UPDATEOperation");

		raulDELETEOperation = _repository
				.URIref("http://purl.org/NET/raul#DELETEOperation");

		raulTextbox = _repository.URIref("http://purl.org/NET/raul#Textbox");

		raulListbox = _repository.URIref("http://purl.org/NET/raul#Listbox");

		raulListitem = _repository.URIref("http://purl.org/NET/raul#Listitem");

		raulButton = _repository.URIref("http://purl.org/NET/raul#Button");

		raulCheckbox = _repository.URIref("http://purl.org/NET/raul#Checkbox");

		raulRadiobutton = _repository
				.URIref("http://purl.org/NET/raul#Radiobutton");

		raulGroup = _repository.URIref("http://purl.org/NET/raul#Group");

		raulDynamicGroup = _repository
				.URIref("http://purl.org/NET/raul#DynamicGroup");

		// Define datatypes

		stringdatatype = _repository
				.URIref("http://www.w3.org/2001/XMLSchema#string");

		booleandatatype = _repository
				.URIref("http://www.w3.org/2001/XMLSchema#boolean");

		integerdatatype = _repository
				.URIref("http://www.w3.org/2001/XMLSchema#integer"); // added by
																		// pcc

		raulcommand = _repository.URIref("http://purl.org/NET/raul#command");

		raulaction = _repository.URIref("http://purl.org/NET/raul#action");

		raulvalue = _repository.URIref("http://purl.org/NET/raul#value");

		raullabel = _repository.URIref("http://purl.org/NET/raul#label");

		raulname = _repository.URIref("http://purl.org/NET/raul#name");

		raultitle = _repository.URIref("http://purl.org/NET/raul#title");

		raulclass = _repository.URIref("http://purl.org/NET/raul#class");

		raulid = _repository.URIref("http://purl.org/NET/raul#id");

		raulhidden = _repository.URIref("http://purl.org/NET/raul#hidden");

		raulisIdentifier = _repository
				.URIref("http://purl.org/NET/raul#isIdentifier");

		raulselected = _repository.URIref("http://purl.org/NET/raul#selected");

		raulchecked = _repository.URIref("http://purl.org/NET/raul#checked");

		rauldisabled = _repository.URIref("http://purl.org/NET/raul#disabled");

		raulrow = _repository.URIref("http://purl.org/NET/raul#row");

		raulsize = _repository.URIref("http://purl.org/NET/raul#size");

		raullist = _repository.URIref("http://purl.org/NET/raul#list");

		raulgroup = _repository.URIref("http://purl.org/NET/raul#group");

		raulmethod = _repository.URIref("http://purl.org/NET/raul#method"); // raulmethods
																			// =
																			// _repository.URIref("http://purl.org/NET/raul#methods");

		// Define RaUL list property
		raulwidgets = _repository.URIref("http://purl.org/NET/raul#list");

		raulMultipleWidgets = _repository
				.URIref("http://purl.org/NET/raul#multiple");

		rdfsComment = _repository
				.URIref("http://www.w3.org/2000/01/rdf-schema#comment");

		rdfsRange = _repository
				.URIref("http://www.w3.org/2000/01/rdf-schema#range");

		raulLevel = _repository.URIref("http://purl.org/NET/raul#level");

	}

	public void dataGraphGen_RDF(String uri, RDFRepository _repository,
			RDFRepository _dataGraph) throws IOException,
			RepositoryConfigException, RepositoryException,
			QueryEvaluationException, RDFHandlerException {

		// Create the connection to the repository
		con = _repository.createConnection();
		con1 = _dataGraph.createConnection();

		Namespace tmpNamespace;
		RepositoryResult<Namespace> _repositoryNamespace = con.getNamespaces();
		while (_repositoryNamespace.hasNext()) {
			tmpNamespace = _repositoryNamespace.next();
			con1.setNamespace(tmpNamespace.getPrefix(), tmpNamespace.getName());
		}

		org.openrdf.model.URI u = con.getValueFactory().createURI(uri);

		uriInit(_repository);

		// Get the RaUL page object
		// Page page = c.getObject(Page.class, u);
		Page page = new Page(con, uri);
		// Start the Page properties in the Body
		Set<URI> raulPageProperties = new HashSet<URI>();
		raulPageProperties.add(raulPage);

		// Get all widgetContainerLists in the Page
		Set<ie.deri.raul.model.rdfs.Class> widgetContainerLists = page
				.getRaulLists();
		Set<Object> setWidgetContainerLists = new HashSet<Object>();
		for (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetContainerLists = widgetContainerLists
				.iterator(); iWidgetContainerLists.hasNext();) {

			Object var = iWidgetContainerLists.next().getResourceURI();
			// Get the next WidgetContainerList

			if (!setWidgetContainerLists.contains(var)) {

				org.openrdf.model.URI uriWidgetContainerListObject = con
						.getValueFactory().createURI(var.toString());
				Resource widgetContainerSequence = new Resource(con,
						uriWidgetContainerListObject.toString());

				// Get all WidgetContainers in the RDF Seq
				String widgetContainerList = widgetContainerSequence
						.getRdfsMembers().toString().replaceAll("\\[|\\]", "");
				String[] WidgetContainerString = widgetContainerList
						.split(", ");

				Set<Object> setWidgetContainer = new HashSet<Object>();
				// Iterate through the WidgetContainers
				for (int iWidgetContainer = 0; iWidgetContainer < WidgetContainerString.length; iWidgetContainer++) {

					Object WidgetContainerList = WidgetContainerString[iWidgetContainer];
					Object varWidgetContainer = WidgetContainerString[iWidgetContainer];
					if (!setWidgetContainer.contains(varWidgetContainer)) {
						org.openrdf.model.URI uriWidgetContainer = con
								.getValueFactory().createURI(
										WidgetContainerList.toString());

						// Get the WidgetContainer object
						WidgetContainer widgetContainer = new WidgetContainer(
								con, uriWidgetContainer.toString());

						Set<ie.deri.raul.model.rdfs.Class> widgetList = widgetContainer
								.getRaulLists();
						dataGraphGen_widgetsHandler(widgetList, uri,
								_repository, _dataGraph);

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

	private void dataGraphGen_widgetsHandler(
			Set<ie.deri.raul.model.rdfs.Class> widgetList, String uri,
			RDFRepository _repository, RDFRepository _dataGraph)
			throws RepositoryException, QueryEvaluationException, IOException,
			RepositoryConfigException {

		// Get all widget lists in the WidgetContainer
		Set<Object> setWidgetList = new HashSet<Object>();
		for (Iterator<ie.deri.raul.model.rdfs.Class> iWidgetList = widgetList
				.iterator(); iWidgetList.hasNext();) {

			Object varList = iWidgetList.next().getResourceURI();

			// Get the next WidgetList
			Object WidgetListObject = varList;
			if (!setWidgetList.contains(varList)) {
				org.openrdf.model.URI uriWidgetListObject = con
						.getValueFactory().createURI(
								WidgetListObject.toString());
				Resource widgetsTypeList = new Resource(con,
						uriWidgetListObject.toString());

				// Get all widgets in the RDF Seq

				String widgetTypeList = widgetsTypeList.getRdfsMembers()
						.toString().replaceAll("\\[|\\]", "");
				String[] unknownType = widgetTypeList.split(", ");
				Set<Object> setUnknownType = new HashSet<Object>();

				// Iterate through the Widgets and determine
				// their type
				for (int iUnknownType = 0; iUnknownType < unknownType.length; iUnknownType++) {
					Object unknownTypeList = unknownType[iUnknownType];
					Object varUnknownType = unknownType[iUnknownType];
					if (!setUnknownType.contains(varUnknownType)) {
						if (unknownTypeList.toString().equals(""))
							continue;
						org.openrdf.model.URI unknownTypeBox = con
								.getValueFactory().createURI(
										unknownTypeList.toString());
						String type = null;
						String unknownTypeBoxStr = unknownTypeBox.toString();
						TupleQuery query = null;
						try {
							query = con
									.prepareTupleQuery(
											QueryLanguage.SPARQL,
											""
													+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
													+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
													+ "SELECT Distinct ?type "
													+ "WHERE {<"
													+ unknownTypeBoxStr
													+ "> rdf:type ?type. "
													+ "FILTER ( ?type != rdfs:Resource) } ");

						} catch (MalformedQueryException e1) {
							e1.printStackTrace();
						}
						TupleQueryResult resultSet = query.evaluate();
						while (resultSet.hasNext()) {
							BindingSet result = resultSet.next();
							if (result.getValue("type").toString()
									.contains(RAUL_RDF_PREFIX))
								type = result.getValue("type").toString()
										.split("#")[1];
						}
						if (type.equalsIgnoreCase("Textbox")) {
							Textbox textbox = new Textbox(con,
									unknownTypeBox.toString());

							Set<String> textboxValues = textbox.getRaulValues();
							dataGraphGen_Statement(textboxValues, _dataGraph);

						} else if (type.equalsIgnoreCase("Listbox")) {
							Listbox listbox = new Listbox(con,
									unknownTypeBox.toString());
							Set<String> ListboxValues = listbox.getRaulValues();
							dataGraphGen_Statement(ListboxValues, _dataGraph);

						} else if (type.equalsIgnoreCase("Group")) {
							Group group = new Group(con,
									unknownTypeBox.toString());
							Set<ie.deri.raul.model.rdfs.Class> GroupWidgetList = group
									.getRaulLists();

							Set<String> GroupValues = group.getRaulValues();
							dataGraphGen_Statement(GroupValues, _dataGraph);
							dataGraphGen_widgetsHandler(GroupWidgetList, uri,
									_repository, _dataGraph);
						} else if (type.equalsIgnoreCase("DynamicGroup")) {
							DynamicGroup dynamicgroup = new DynamicGroup(con,
									unknownTypeBox.toString());
							Set<ie.deri.raul.model.rdfs.Class> dynamicWidgetList = dynamicgroup
									.getRaulLists();
							dataGraphGen_widgetsHandler(dynamicWidgetList, uri,
									_repository, _dataGraph);
						} else if (type.equalsIgnoreCase("Button")
								|| type.equalsIgnoreCase("Radiobutton")
								|| type.equalsIgnoreCase("Checkbox")) {
							Button button = new Button(con,
									unknownTypeBox.toString());
							URI ButtonType = con.getValueFactory().createURI(
									"http://purl.org/NET/raul#" + type);
							Set<String> ButtonValues = button.getRaulValues();
							if (ButtonType != raulButton)
								dataGraphGen_Statement(ButtonValues, _dataGraph);
						}
						/*
						 * org.openrdf.model.URI unknownTypeBox =
						 * con.getValueFactory
						 * ().createURI(unknownTypeList.toString()); // get
						 * Textbox try {
						 * 
						 * Textbox textbox = new Textbox(con,
						 * unknownTypeBox.toString()); Set<String> textboxValues
						 * = textbox.getRaulValues();
						 * dataGraphGen_Statement(textboxValues, _dataGraph);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * // get Listbox try { Listbox listbox = new
						 * Listbox(con, unknownTypeBox.toString()); Set<String>
						 * ListboxValues = listbox.getRaulValues();
						 * dataGraphGen_Statement(ListboxValues, _dataGraph);
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * try { //added by pcc Group group = new Group(con,
						 * unknownTypeBox.toString());
						 * 
						 * Set<ie.deri.raul.model.rdfs.Class> GroupWidgetList =
						 * group.getRaulLists(); Set<String> GroupValues =
						 * group.getRaulValues();
						 * dataGraphGen_Statement(GroupValues, _dataGraph);
						 * dataGraphGen_widgetsHandler(GroupWidgetList, uri,
						 * _repository, _dataGraph);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * // get Button try { Button button = new Button(con,
						 * unknownTypeBox.toString()); URI ButtonType =
						 * raulButton; // Determine if the Button type is //
						 * Checkbox or Radiobutton try { Radiobutton radiobutton
						 * = new Radiobutton (con, unknownTypeBox.toString());
						 * ButtonType = raulRadiobutton; } catch
						 * (ClassCastException e) { }
						 * 
						 * try { Checkbox checkbox = new Checkbox(con,
						 * unknownTypeBox.toString()); ButtonType =
						 * raulCheckbox; } catch (ClassCastException e) { }
						 * Set<String> ButtonValues = button.getRaulValues();
						 * if(ButtonType != raulButton)
						 * dataGraphGen_Statement(ButtonValues, _dataGraph);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 * 
						 * // get DynamicGroup added by pcc try { DynamicGroup
						 * dynamicgroup = new DynamicGroup(con,
						 * unknownTypeBox.toString());
						 * 
						 * Set<ie.deri.raul.model.rdfs.Class> dynamicWidgetList
						 * = dynamicgroup.getRaulLists();
						 * dataGraphGen_widgetsHandler(dynamicWidgetList, uri,
						 * _repository, _dataGraph);
						 * 
						 * continue; //added by pcc } catch (ClassCastException
						 * e) { }
						 */

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

	private void dataGraphGen_Statement(Set<String> widgetValues,
			RDFRepository _dataGraph) throws RepositoryException,
			QueryEvaluationException, IOException, RepositoryConfigException {
		org.openrdf.model.URI s = null, p = null;
		Value o = null;

		Set<Object> setWidgetValues = new HashSet<Object>();
		for (Iterator<String> iWidgetValues = widgetValues.iterator(); iWidgetValues
				.hasNext();) {
			Object varValues = iWidgetValues.next();

			if (!setWidgetValues.contains(varValues)) {

				org.openrdf.model.URI uriValueStatement = con.getValueFactory()
						.createURI(varValues.toString());

				try {
					Statement valueStatement = new Statement(con,
							uriValueStatement.toString());
					Set<String> valueSubjects = valueStatement.getRdfSubjects();
					Set<String> valuePredicates = valueStatement
							.getRdfPredicates();
					Set<String> valueObjects = valueStatement.getRdfObjects();

					Set<String> setSubjects = new HashSet<String>();

					for (Iterator<String> iSubjects = valueSubjects.iterator(); iSubjects
							.hasNext();) {

						String varSubjects = iSubjects.next();

						if (!setSubjects.contains(varSubjects)) {
							s = _dataGraph.URIref(varSubjects.toString());

							setSubjects.add(varSubjects);
						} else {
							setSubjects.add(varSubjects);
						}
					}

					Set<String> setPredicates = new HashSet<String>();

					for (Iterator<String> iPredicates = valuePredicates
							.iterator(); iPredicates.hasNext();) {

						String varPredicates = iPredicates.next();

						if (!setPredicates.contains(varPredicates)) {
							String strPredicate = varPredicates.toString();

							if (strPredicate.indexOf("http://") != -1) {
								// an entire uri
								p = _dataGraph.URIref(strPredicate);
							} else {
								// prefix + local name

								String[] qName = strPredicate.split(":");
								String strPrefix = qName[0];
								String strLocalname = qName[1];

								String strNamespace = con1
										.getNamespace(strPrefix);
								if (strNamespace != null) {
									strPredicate = strNamespace + strLocalname;
								}

								p = _dataGraph.URIref(strPredicate);
							}

							setPredicates.add(varPredicates);
						} else {
							setPredicates.add(varPredicates);
						}
					}

					Set<String> setObjects = new HashSet<String>();

					for (Iterator<String> iObjects = valueObjects.iterator(); iObjects
							.hasNext();) {

						String varObjects = iObjects.next();

						if (!setObjects.contains(varObjects)) {

							if (varObjects.toString().indexOf("http://") != -1)
								o = _dataGraph.URIref(varObjects.toString()); // resource
							else
								o = _dataGraph.Literal(varObjects.toString()); // literal

							setObjects.add(varObjects);
						} else {
							setObjects.add(varObjects);
						}
					}

					_dataGraph.add(s, p, o);
					setWidgetValues.add(varValues);
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			} else {
				setWidgetValues.add(varValues);
			}
		}
	}

}
