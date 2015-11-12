package ie.deri.raul;

import ie.deri.raul.persistence.RDFRepository;

import java.util.ArrayList;
import java.util.HashMap;

import org.openrdf.model.Namespace;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class GenerateRaULRDF {

	private final static String RAUL_PREFIX = "raul";

	RepositoryConnection connection;
	TupleQuery query = null;
	String classURI = "";
	String endpoint = "";
	String output = "";
	ArrayList<String> classesVisited = new ArrayList<String>();
	HashMap<String, ArrayList<String>> traversedNodes = new HashMap<String, ArrayList<String>>();

	// ArrayList<String> visitHistory = new ArrayList<String>();

	public GenerateRaULRDF(RDFRepository repository, String endpoint,
			String classURI) {
		// TODO Auto-generated constructor stub
		try {
			this.connection = repository.createConnection();
			this.endpoint = endpoint;
			this.classURI = classURI;
			// this.classesVisited.add(this.classURI);
		} catch (Exception e) {
		}
	}

	public String generateRaULBasedRDFGraph() {

		output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<rdf:RDF\n";
		output = output + writeNameSpaces();
		output = output + writePageProperty();
		output = output + "</rdf:RDF>";
		try {
			this.connection.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return output;
	}

	/*
	 * Write All Namespaces of Ontology
	 */

	public String writeNameSpaces() {
		String _output = "";
		try {
			RepositoryResult<Namespace> rs = connection.getNamespaces();
			boolean needAddRaulNs = true;
			while (rs.hasNext()) {
				String namespace[] = rs.next().toString().split("::");
				String prefix = namespace[0].replaceAll(" ", "");
				String URI = namespace[1].replaceAll(" ", "");
				_output = _output + "	xmlns:" + prefix + "=\"" + URI + "\"\n";
				if (prefix.toLowerCase().equals(RAUL_PREFIX))
					needAddRaulNs = false;
			}
			if (needAddRaulNs == true)
				_output = _output
						+ "	xmlns:raul=\"http://purl.org/NET/raul#\"\n>\n";
			else
				_output = _output + "\n>\n";
		} catch (Exception e) {
			System.out.println("Connection Failed .......");
		}
		return _output;
	}

	public String writePageProperty() {
		String _output = "";
		ArrayList<String> listMembers = new ArrayList<String>();
		String containerURI = endpoint + "#container";
		String listURI = endpoint + "#widgetContainerList";
		listMembers.add(containerURI);

		_output = "<rdf:Description rdf:about=\""
				+ endpoint
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Page\"></rdf:type>\n"
				+ "	<raul:id>content</raul:id>\n" + "	<raul:list>" + listURI
				+ "</raul:list>\n" + "</rdf:Description>\n";
		_output = _output + writeListProperty(listURI, listMembers);
		_output = _output + writeContainerProperty(containerURI);
		return _output;
	}

	public String writeListProperty(String listSubject,
			ArrayList<String> listMembers) {
		String _output = "";
		_output = "<rdf:Description rdf:about=\""
				+ listSubject
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq\"></rdf:type>\n";
		for (int memberCount = 0; memberCount < listMembers.size(); memberCount++) {
			_output = _output + "	<rdf:_" + (memberCount + 1)
					+ " rdf:resource=\"" + listMembers.get(memberCount)
					+ "\"></rdf:_" + (memberCount + 1) + ">\n";
		}
		if (listSubject.equalsIgnoreCase(endpoint + "#containerList")) {
			_output = _output + "	<rdf:_" + (listMembers.size() + 1)
					+ " rdf:resource=\"" + endpoint + "#submit\"></rdf:_"
					+ (listMembers.size() + 1) + ">\n";
		} else {
		}

		_output = _output + "</rdf:Description>\n";

		return _output;
	}

	public String writeContainerProperty(String containerURI) {
		String _output = "";
		String id[] = endpoint.split("\\/");
		String method = endpoint + "#method";
		String listURI = containerURI + "List";
		ArrayList<PropertyModel> propertiesList = new ArrayList<PropertyModel>();
		ArrayList<String> listMembers = new ArrayList<String>();

		_output = "<rdf:Description rdf:about=\""
				+ containerURI
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#WidgetContainer\"></rdf:type>\n"
				+ "	<raul:method>" + method + "</raul:method>\n" + "	<raul:id>"
				+ id[id.length - 1] + "</raul:id>\n" + "	<raul:list>" + listURI
				+ "</raul:list>\n" + "</rdf:Description>\n";
		_output = _output + writeMethodProperty(method);

		// get All properties attached with classURI
		try {
			propertiesList = getRelevantPropertiesList(classURI);
		} catch (Exception e) {
		}

		for (int count = 0; count < propertiesList.size(); count++) {
			listMembers.add(endpoint + "#"
					+ propertiesList.get(count).getProperty().split("#")[1]);
			// classesVisited.add(e)
		}

		_output = _output + writeListProperty(listURI, listMembers);

		try {
			for (int count = 0; count < propertiesList.size(); count++) {
				_output = _output
						+ writePropertiesDescription(propertiesList.get(count),
								endpoint, classURI);
			}
		} catch (Exception e) {

		}
		_output = _output + writeCREATEOperation(endpoint + "#submit");

		return _output;
	}

	public static String writeMethodProperty(String method) {
		String _output = "";
		_output = "<rdf:Description rdf:about=\""
				+ method
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#method\"></rdf:type>\n"
				+ "</rdf:Description>\n";
		return _output;
	}

	/*
	 * provide description of all properties of List
	 * 
	 * @param map is HashMap of type <String, String>, Where key is one of the
	 * "property" of list and value string is property description i.e. property
	 * "label", "type", "range", "inverseProperty" separated by comma.
	 * 
	 * @param resource is a prefix string that we will use to create URI for
	 * each property
	 * 
	 * @param _classURI is the class whose properties this method is listing
	 */

	/*
	 * public String writeListPropertiesDescription(HashMap<String,String> map,
	 * String resource, String _classURI) throws Exception{ String _output = "";
	 * String detail[]; String property, range, label, type; String resourceURI
	 * ,valueWithoutURI;
	 * 
	 * for (Entry<String, String> entry : map.entrySet()) { property =
	 * entry.getKey();
	 * 
	 * valueWithoutURI = property.split("#")[1]; if(resource.contains("#")){
	 * resourceURI = resource + "_" + valueWithoutURI ; } else{ resourceURI =
	 * resource + "#" +valueWithoutURI;} System.out.println("Resource URI : " +
	 * resourceURI); detail = entry.getValue().split(","); label = detail[0];
	 * type = detail[1]; range = detail[2];
	 * 
	 * if (type.equalsIgnoreCase("ObjectProperty")){ // if domain and range of a
	 * property is same then add a text box for it
	 * if(range.equalsIgnoreCase(_classURI)){ _output = _output +
	 * writeTextBoxProperty(resourceURI , label , valueWithoutURI, property,
	 * ""); }else{ HashMap<String, String> _rangeMap = new HashMap<String,
	 * String>(); _rangeMap = getRelevantPropertiesList(range);
	 * System.out.println("Range Properties no : " + _rangeMap.size());
	 * System.out.println("Range Properties : " + _rangeMap);
	 * if(_rangeMap.size() < 1){ System.out.println("No Range Properties " +
	 * range); _output = _output + writeTextBoxProperty(resourceURI , label,
	 * valueWithoutURI, property, ""); } else { _output = _output +
	 * writeDynamicGroupProperty(resourceURI, property, entry); } } } else {
	 * _output = _output + writeTextBoxProperty(resourceURI , label,
	 * valueWithoutURI, property, ""); } } return _output; }
	 */

	public String writePropertiesDescription(PropertyModel model,
			String resource, String _classURI) throws Exception {

		ArrayList<String> propertiesList = new ArrayList<String>();
		/*
		 * if (classesVisited.contains(_classURI) ) { } else {
		 * classesVisited.add(_classURI); }
		 */

		if (traversedNodes.containsKey(_classURI)) {
			propertiesList = traversedNodes.get(_classURI);
		} else {
			traversedNodes.put(_classURI, propertiesList);
		}

		String _output = "";
		String detail[];
		String property, range, label, type, inverse;
		String resourceURI, valueWithoutURI;

		property = model.getProperty().toString();

		valueWithoutURI = property.split("#")[1];

		if (resource.contains("#")) {
			resourceURI = resource + "_" + valueWithoutURI;
		} else {
			resourceURI = resource + "#" + valueWithoutURI;
		}

		// System.out.println("Resource URI : " + resourceURI);

		label = model.getPropertyLabel();
		type = model.getPropertyType();
		range = model.getPropertyRange();
		inverse = model.getPropertyInverseProperty();

		if (type.equalsIgnoreCase("ObjectProperty")) {
			// System.out.println(" Property: "+ property + " Is a " + type +
			// " property & needs further processing ");
			if (_classURI.equalsIgnoreCase(range)) {
				// countOfProperties++;
				// System.out.println(" Property: "+ property +
				// " Is a transitive property & donot process It further create a text box ");
				if (_classURI.equalsIgnoreCase(classURI)) {
					_output = _output
							+ writeTextBoxProperty(resourceURI, label,
									property, "");
				} else {

				}
				propertiesList.add(resourceURI);
				traversedNodes.put(_classURI, propertiesList);
			} else {
				ArrayList<PropertyModel> childProp = getRelevantPropertiesList(range);

				if ((childProp.size() == 0)) {
					// countOfProperties++;
					// System.out.println(" Property: "+ property +
					// " range doesnot have further properties so print a text box here ");
					_output = _output
							+ writeTextBoxProperty(resourceURI, label,
									property, "");
					propertiesList.add(resourceURI);
					traversedNodes.put(_classURI, propertiesList);
				} else if ((childProp.size() == 1)
						&& (childProp.get(0).getProperty()
								.equalsIgnoreCase(inverse))) {
					// countOfProperties++;
					// System.out.println( "Child Property : " +
					// childProp.get(0).getProperty());
					// System.out.println( "Child Property Range : " +
					// childProp.get(0).getPropertyRange() );
					// System.out.println(" Property: "+ property +
					// " is an inverse property and only property of the range class, create text box ");
					_output = _output
							+ writeTextBoxProperty(resourceURI, label,
									property, "");
					propertiesList.add(resourceURI);
					traversedNodes.put(_classURI, propertiesList);
				} else {
					// countOfProperties--;
					// System.out.println(" Property: "+ property +
					// " range size " + childProp.size());
					// System.out.println("Resource URI : " + resourceURI );
					// propertiesList.add(property);
					// _output = _output +
					// writeDynamicGroupProperty(resourceURI, property, model,
					// _classURI);
					_output = _output
							+ writeTextBoxProperty(resourceURI, label,
									property, "");

				}
			}
		} else {

			// System.out.println(" Property: "+ property + " Is a " + type +
			// " property & needs a text box to print in ");
			_output = _output
					+ writeTextBoxProperty(resourceURI, label, property, "");
			propertiesList.add(resourceURI);
			traversedNodes.put(_classURI, propertiesList);
			// _output = _output + writeDynamicGroupProperty(resourceURI,
			// property, model);
		}

		/*
		 * if (type.equalsIgnoreCase("ObjectProperty")){ // if domain and range
		 * of a property is same then add a text box for it
		 * if(range.equalsIgnoreCase(_classURI)){ _output = _output +
		 * writeTextBoxProperty(resourceURI , label , valueWithoutURI, property,
		 * ""); }else{ ArrayList<PropertyModel> _rangeList = new
		 * ArrayList<PropertyModel>(); _rangeList =
		 * getRelevantPropertiesList(range);
		 * //System.out.println("Range Properties no : " + _rangeMap.size());
		 * //System.out.println("Range Properties : " + _rangeMap);
		 * if(_rangeList.size() == 0){ System.out.println("No Range Properties "
		 * + range); _output = _output + writeTextBoxProperty(resourceURI ,
		 * label, valueWithoutURI, property, ""); } else { _output = _output +
		 * writeDynamicGroupProperty(resourceURI, property, model); } } } else {
		 * _output = _output + writeTextBoxProperty(resourceURI , label,
		 * valueWithoutURI, property, ""); }
		 */

		return _output;
	}

	public String writeDynamicGroupProperty(String resourceURI,
			String property, PropertyModel model, String _classURI) {

		String _output = "";
		if (traversedNodes.containsKey(model.getPropertyRange())) {
			System.out.println("Already in Map : " + model.getPropertyRange());

			// countOfProperties++;
		} else {
			// al = getRelevantPropertiesList(className);
			ArrayList<String> propertiesList = new ArrayList<String>();
			String _range = model.getPropertyRange();
			if (traversedNodes.containsKey(_classURI)) {
				propertiesList = traversedNodes.get(_classURI);
				propertiesList.add(resourceURI);
				traversedNodes.put(_classURI, propertiesList);
			} else {
				propertiesList.add(resourceURI);
				traversedNodes.put(_classURI, propertiesList);
			}
			String value = resourceURI.split("#")[1];
			String listURI = resourceURI + "List";
			ArrayList<String> listURIFields = new ArrayList<String>();
			listURIFields.add(listURI + "Fields");

			_output = _output
					+ "<rdf:Description rdf:about=\""
					+ resourceURI
					+ "\">\n"
					+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#DynamicGroup\"></rdf:type>\n"
					+ "	<raul:id>" + value + "</raul:id>\n" + "	<raul:list>"
					+ listURI + "</raul:list>\n";
			_output = _output + "</rdf:Description>\n";
			_output = _output + writeListProperty(listURI, listURIFields);
			_output = _output
					+ writeGroupProperty(property, listURIFields, model);
		}
		return _output;
	}

	public String writeGroupProperty(String propertyURI,
			ArrayList<String> listElements, PropertyModel model) {
		String _output = "";

		for (int counter = 0; counter < listElements.size(); counter++) {
			ArrayList<PropertyModel> rangeProperties = new ArrayList<PropertyModel>();
			String property[] = listElements.get(counter).split("#");
			String id = property[1];
			String list = listElements.get(counter) + "List";
			String value = listElements.get(counter) + "Value";
			ArrayList<String> rangePropertiesList = new ArrayList<String>();
			String _propertyURI = "", _resourceURI = "";

			String range = model.getPropertyRange();
			String iProperty = model.getPropertyInverseProperty();

			try {
				String __output = "";
				rangeProperties = getRelevantPropertiesList(range);
				// System.out.println("Range Properties : " + rangeProperties);

				for (int index = 0; index < rangeProperties.size(); index++) {
					_propertyURI = rangeProperties.get(index).getProperty();

					// System.out.println(" property to Write : " +
					// _propertyURI);
					_resourceURI = list + "_" + _propertyURI.split("#")[1];
					// rangePropertiesList.add(_resourceURI);
					__output = __output
							+ writePropertiesDescription(
									rangeProperties.get(index), list, range);
				}
				// System.out.println(" ************* ***************** ********************* ************");
				// System.out.println(" Property  : : : : : " + range);
				// System.out.println(" Traversal Map : : : : : " +
				// traversedNodes.get(range));
				// System.out.println(" ************* ***************** ********************* ************");
				rangePropertiesList = traversedNodes.get(range);
				if (rangePropertiesList.size() == 0) {
					System.out.println("RANGE WITHOUT ANY PROPERTY : " + range);
					_output = _output
							+ writeTextBoxProperty(list, list,
									model.getProperty(), "");

				} else {
					_output = _output
							+ "<rdf:Description rdf:about=\""
							+ listElements.get(counter)
							+ "\">\n"
							+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Group\"></rdf:type>\n"
							+ "	<raul:id>" + id + "</raul:id>\n"
							+ "	<raul:list>" + list + "</raul:list>\n"
							+ "	<raul:value>" + value + "</raul:value>\n"
							+ "</rdf:Description>\n";

					_output = _output + writeValueTriple(value, propertyURI);
					_output = _output
							+ writeListProperty(list, rangePropertiesList)
							+ __output;
				}

			} catch (Exception e) {
			}
		}
		return _output;
	}

	public String writeTextBoxPropertyWithoutValue(String resourceURI,
			String label, String group) {
		String _output = "";
		String valueURI = resourceURI;
		_output = _output
				+ "<rdf:Description rdf:about=\""
				+ resourceURI
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Textbox\"></rdf:type>\n"
				+ "	<raul:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
				+ label + ": </raul:label>\n" + "	<raul:id>"
				+ valueURI.split("#")[1] + "</raul:id>\n" + "	<raul:name>"
				+ valueURI.split("#")[1] + "</raul:name>\n" + "	<raul:value>"
				+ valueURI + "</raul:value>\n";
		if (group != "") {
			_output = _output + "	<raul:group>" + group + "</raul:group>\n";
		} else {
		}
		_output = _output + "</rdf:Description>\n";
		return _output;
	}

	public String writeTextBoxProperty(String resourceURI, String label,
			String property, String group) {
		String _output = "";
		String valueURI = resourceURI + "_Value";
		_output = _output
				+ "<rdf:Description rdf:about=\""
				+ resourceURI
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Textbox\"></rdf:type>\n"
				+ "	<raul:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
				+ label + ": </raul:label>\n" + "	<raul:id>"
				+ valueURI.split("#")[1] + "</raul:id>\n" + "	<raul:name>"
				+ valueURI.split("#")[1] + "</raul:name>\n" + "	<raul:value>"
				+ valueURI + "</raul:value>\n";
		if (group != "") {
			_output = _output + "	<raul:group>" + group + "</raul:group>\n";
		} else {
		}
		_output = _output + "</rdf:Description>\n";
		_output = _output + writeValueTriple(valueURI, property);
		return _output;
	}

	public String writeValueTriple(String valueURI, String property) {
		String output = "";
		String prefix, predicate;
		int startOfPrefix = property.lastIndexOf("/");
		int endOfPrefix = property.lastIndexOf("#");

		prefix = property.substring(startOfPrefix + 1, endOfPrefix);
		predicate = property.substring(endOfPrefix + 1);

		output = output + "<rdf:Description rdf:about=\"" + valueURI + "\">\n"
				+ "	<rdf:subject></rdf:subject>\n" + "	<rdf:predicate>"
				+ prefix + ":" + predicate + "</rdf:predicate>\n"
				+ "	<rdf:object></rdf:object>\n";
		output = output + "</rdf:Description>\n";
		return output;
	}

	public String writeCREATEOperation(String subject) {
		String _output = "";

		_output = _output
				+ "<rdf:Description rdf:about=\""
				+ subject
				+ "\">\n"
				+ "	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Button\"></rdf:type>\n"
				+ "	<raul:command>submitDataWrap()</raul:command>\n"
				+ "	<raul:id>Submit</raul:id>\n"
				+ "	<raul:name>Submit</raul:name>\n"
				+ "	<raul:value>Submit</raul:value>\n" + "</rdf:Description>";
		return _output;
	}

	public ArrayList<PropertyModel> getRelevantPropertiesList(String URI)
			throws RepositoryException, QueryEvaluationException,
			MalformedQueryException {
		ArrayList<PropertyModel> al = new ArrayList<PropertyModel>();
		try {

			query = connection
					.prepareTupleQuery(
							QueryLanguage.SPARQL,
							"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
									+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
									+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> "
									+

									"SELECT DISTINCT ?property ?label ?range ?type ?iProperty "
									+ "WHERE {<"
									+ URI
									+ "> rdfs:subClassOf ?restriction."
									+ "?restriction owl:onProperty ?property."
									+ "OPTIONAL {?property rdfs:label ?label.}"
									+ "OPTIONAL {"
									+ "{?property rdfs:range ?range.} "
									+ "UNION {"
									+ "{?restriction owl:allValuesFrom ?range.} "
									+ "UNION {?restriction owl:someValuesFrom ?range}"
									+ "}"
									+ "}"
									+ "OPTIONAL {"
									+ "?property rdf:type ?type. "
									+ "FILTER ((?type = owl:ObjectProperty) || (?type = owl:DatatypeProperty))}"
									+ "OPTIONAL {?property owl:inverseOf ?iProperty.}"
									+ "}");
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}

		TupleQueryResult resultSet = query.evaluate();

		while (resultSet.hasNext()) {

			BindingSet results = resultSet.next();
			String property = results.getValue("property").stringValue();
			String label = results.getValue("label").stringValue();
			String range = results.getValue("range").stringValue();
			String type = results.getValue("type").stringValue().split("#")[1];
			String inverse = "";
			if (results.hasBinding("iProperty")) {
				inverse = results.getValue("iProperty").toString();
			}
			PropertyModel model = new PropertyModel(property, type, range,
					label, inverse);
			al.add(model);
		}
		return al;
	}

}
