package ie.deri.raul;



import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.resources.RaULResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Namespace;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class GenerateRaULRDF2 {

	private final static String RAUL_PREFIX = "raul";
	private static Log _logger = LogFactory.getLog(RaULResource.class);
	
	RepositoryConnection connection;
	TupleQuery query= null;
	BooleanQuery bQuery= null;
	String classURI="";
	String endpoint ="";
	String output ="" ;

	HashMap<String, HashMap<String, String>> traversedNodes = new HashMap<String, HashMap<String, String>>();

	public GenerateRaULRDF2(RDFRepository repository, String endpoint, String classURI) {
		// TODO Auto-generated constructor stub
		try{ 
			this.connection = repository.createConnection();
			this.endpoint = endpoint;
			this.classURI = classURI;
			//this.classesVisited.add(this.classURI);
			//System.out.println(generateRaULBasedRDFGraph());
			//System.out.println(getSuperClasses(this.classURI));
		//	System.out.println(getRelevantPropertiesListTest(this.classURI));
		}catch(Exception e){}
	}
	
	public GenerateRaULRDF2(Repository myRepository, String endpoint, String classURI) {
		// TODO Auto-generated constructor stub
		try{ 
			this.connection = myRepository.getConnection();
			this.endpoint = endpoint;
			this.classURI = classURI;
			//this.classesVisited.add(this.classURI);
			//generateRaULBasedRDFGraph();
			System.out.println(generateRaULBasedRDFGraph());
			//System.out.println(getSuperClasses(this.classURI));
		//	System.out.println(getRelevantPropertiesListTest(this.classURI));
		}catch(Exception e){}
	}

	public String generateRaULBasedRDFGraph(){
		
		output =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		   "<rdf:RDF\n";
		output = output + writeNameSpaces();
		output = output + writePageProperty();		
		output = output + "\n</rdf:RDF>";
		
		return output;
	}

	/*
	 * Write All Namespaces of Ontology 
	 * 
	 * */
	
	public String writeNameSpaces() {
		String _output = "";
		try {
			RepositoryResult<Namespace> rs = connection.getNamespaces();
			boolean needAddRaulNs = true;
			while (rs.hasNext()) {
				String namespaces = rs.next().toString();
				//System.out.println(" Namespaces : "+ namespaces);
				String namespace[] = namespaces.split("::");
				String prefix = namespace[0].replaceAll(" ", "");
				String URI = namespace[1].replaceAll(" ", "");
				if(prefix.equalsIgnoreCase("")){
					prefix = "core";
				}
				_output = _output + "	xmlns:" + prefix + "=\"" + URI + "\"\n";
				if (prefix.toLowerCase().equals(RAUL_PREFIX))
					needAddRaulNs = false;
			}
			if (needAddRaulNs == true){
				_output = _output + "	xmlns:raul=\"http://purl.org/NET/raul#\"\n>";
				//_output+=" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n>";
			}
			else
				_output = _output + "\n>\n";
		} catch (Exception e) {
			System.out.println("Connection Failed .......");
		}
		return _output;
	}
	
	public String writePageProperty(){
		String _output="";
		ArrayList<String> listMembers = new ArrayList<String>();
		String containerURI = endpoint +"#container" ;
		String listURI = endpoint + "#widgetContainerList" ;
		listMembers.add(containerURI);
		
		_output = "<rdf:Description rdf:about=\""+endpoint+"\">\n" +
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Page\"></rdf:type>\n"+
				"	<raul:id>content</raul:id>\n"+
				"	<raul:list>"+listURI+"</raul:list>\n"+
				"</rdf:Description>\n";
		
		_output = _output + writeListProperty(listURI, listMembers);
		_output = _output + writeContainerProperty(containerURI);
		_output = _output + writeCREATEOperation(endpoint+"#submit");
		return _output;
	}
	
	public String writeListProperty(String listSubject, ArrayList<String> listMembers){
		String _output= "";
		_output ="<rdf:Description rdf:about=\""+listSubject+"\">\n"+
				"	<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq\"></rdf:type>\n";
		for(int memberCount=0; memberCount<listMembers.size(); memberCount++){
				_output = _output + "	<rdf:_"+(memberCount+1)+" rdf:resource=\""+listMembers.get(memberCount)+"\"></rdf:_"+(memberCount+1)+">\n";
		}
		if (listSubject.equalsIgnoreCase(endpoint+"#containerList")){
			_output = _output + "	<rdf:_"+ (listMembers.size()+1) +" rdf:resource=\""+endpoint+"#submit\"></rdf:_"+ (listMembers.size()+1) +">\n";
		} else {}
		_output = _output + "</rdf:Description>\n";	
		return _output;
	}
	
	public String writeContainerProperty(String containerURI){
		String _output= "";
		String id[] = endpoint.split("\\/");
		String method = endpoint + "#method";
		String listURI = containerURI + "List";
		ArrayList<PropertyModel> propertiesList = new ArrayList<PropertyModel>();
		ArrayList<String> listMembers = new ArrayList<String>();  
		
		_output = "<rdf:Description rdf:about=\""+containerURI+"\">\n"+
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#WidgetContainer\"></rdf:type>\n"+
				"	<raul:method>"+method+"</raul:method>\n"+
				"	<raul:id>"+id[id.length-1]+"</raul:id>\n"+
				"	<raul:list>"+listURI+"</raul:list>\n"+
				"</rdf:Description>\n";
		
		//System.out.println("Inside writeContainerProperty ......." + _output);
		_output = _output + writeMethodProperty(method);
		// get All properties attached with classURI
		try{
			propertiesList = getRelevantPropertiesList(classURI);
		} catch (Exception e){	
		}
		
		for(int count=0; count< propertiesList.size() ; count++){
			String property=propertiesList.get(count).getProperty();
			String propertyWithoutURI = "";
			if (property.contains("#")){
				propertyWithoutURI = property.split("#")[1];
			} else {
				String[] parts = property.split("/");
				propertyWithoutURI = parts[parts.length -1];
			}
			listMembers.add(endpoint +"#"+ propertyWithoutURI);

			//classesVisited.add(e)
		}
		try{
				_output = _output + writePropertiesDescription(classURI, "", endpoint+"#containerList", 0, 0);
			}	catch(Exception e){
			
		}
		return _output;
	}
	
	public static String writeMethodProperty(String method){
		String _output= "";
		_output ="<rdf:Description rdf:about=\""+method+"\">\n"+
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#method\"></rdf:type>\n"+
				"</rdf:Description>\n";
		return _output;
	}
	
	/*
	 * @param _classURI is the domain class Whose properties we want to write.
	 * @param propertyURI is the property whose range is _classURI.
	 * @param uri is the algorithm bases URI for _classURI  
	 *  
	 * */

	public String writePropertiesDescription(String _classURI, String propertyURI, String uri, int dropCount, int level) throws Exception{

		level++; 
		_logger.info(" Class for description : " + _classURI);
		
		
		
		String output="";
		ArrayList<String> allURIs = new ArrayList<String>();
		HashMap<String, String> _subClasses = new HashMap<String, String>();
		_subClasses = getSubClasses(_classURI);
		
		String _property = "http://www.w3.org/2000/01/rdf-schema#label";
		String _range="";
		String _resourceURI = getResourceURI(_property, uri);
		String _output ="";

		if(_subClasses.size() > 1){
			System.out.println(" Inside check subClass clause");
			output = output + writeTextBoxProperty(_resourceURI, "label", _property, "", false , false, "DatatypeProperty", _range );
			output= output + writeListBoxProperty(_resourceURI+"_type", "rdf:type", _subClasses);
		    allURIs.add(_resourceURI);
			allURIs.add(_resourceURI+"_type");
		} else {
			output = output + writeTextBoxProperty(_resourceURI, "label", _property, "", false , false, "DatatypeProperty", _range );
		    allURIs.add(_resourceURI);
		}			
	    
	    
		ArrayList<PropertyModel> al = getRelevantPropertiesList(_classURI);
	
		
	//	int dropCount = 0;
		

	
		    for(int i = 0; i < al.size() ; i++){
			
			PropertyModel model = al.get(i);
			String datatype = model.getPropertyType();
			String property = model.getProperty();
			String range = model.getPropertyRange();
			String label = model.getPropertyLabel();
			
			String resourceURI = getResourceURI(property, uri);
			
			//System.out.println(range + " has SubClasses : " + getSubClasses(range));
			 /*  
			  * if there is some data type property then write a text box
			  * */
			if (range.equalsIgnoreCase("") && datatype.equalsIgnoreCase("ObjectProperty")){
				
			} else if (datatype.equalsIgnoreCase("DatatypeProperty") || getPropertiesCount(range) < 1 ){
				 System.out.println( property + " is a datatype Property"  + datatype);
				 System.out.println( property + " count is  : :  " + getPropertiesCount(range) );
				String comment="";
				String rangeClass="";
				if (datatype.equalsIgnoreCase("DatatypeProperty")) {
					comment = "DatatypeProperty";
					//rangeClass = range;
				} else {
					comment = "ObjectProperty";
					rangeClass=range;
				}
				if(isFunctional(property)){
					System.out.println( property + " is a functional Property" );
					 output = output + writeTextBoxProperty(resourceURI, label, property, "", false , false, comment, rangeClass );
					 allURIs.add(resourceURI);
				} else {
					System.out.println( property + " is a nonFunctional Property" );
					 output = output + writeTextBoxProperty(resourceURI, label, property, "",true, false, comment, rangeClass);
					 allURIs.add(resourceURI);
				}
			} 
			 /*  
			  * if there is some object property then write a text box
			  * 
			  * */
			else {
				
				boolean alreadyTraversed = false;
				
				HashMap<String, String> subClasses = new HashMap<String, String>();
				subClasses = getSubClasses(range);
				
				for (Entry<String,String> entry : subClasses.entrySet()){
					if(traversedNodes.containsKey(entry.getKey())){
						alreadyTraversed = true;	
						break;
					} else {}
				}
				
				if (isTransitive(property)){
									
					if(isFunctional(property)){
						System.out.println( property + " is Transitive and functional Property" );
						if(subClasses.size() > 1){
							System.out.println(" Inside check subClass clause");
							output= output + writeTextBoxProperty(resourceURI, label, property, "", false, true, "ObjectProperty", range);
							output= output + writeListBoxProperty(resourceURI+"_type", "rdf:type", subClasses);
							allURIs.add(resourceURI);
							allURIs.add(resourceURI+"_type");
						} else {
							output= output + writeTextBoxProperty(resourceURI, label, property, "", false, true , "ObjectProperty", range);
							allURIs.add(resourceURI);
						}			
					} else {
						System.out.println( property + " is transitive and nonFunctional Property" );
						if(subClasses.size() > 1){		
							output= output + writeTextBoxProperty(resourceURI, label, property, "", true, true, "ObjectProperty", range);
							output= output + writeListBoxProperty(resourceURI+"_type", "rdf:type", subClasses);
							allURIs.add(resourceURI);
							allURIs.add(resourceURI+"_type");
						} else {
							output= output + writeTextBoxProperty(resourceURI, label, property, "", true, true, "ObjectProperty", range);
							allURIs.add(resourceURI);
						}
					}
					
				} else if(isInverse(property, propertyURI)){
					System.out.println( "For domain :" + _classURI+ " this property :" + property + " is inverse property " );
					dropCount++;
				} 
			//	else if (alreadyTraversed == true) {
				/*	boolean found = false;
					for (Entry<String,HashMap<String, String>> entry : traversedNodes.entrySet()){
						if((entry.getKey().equalsIgnoreCase(_classURI)) && (entry.getValue().containsKey(property)) && (entry.getValue().containsValue(range))){
						System.out.println( "For domain :" + _classURI+ " this property :" + property + " and range "+ range+ " is already traversed " );				
							//output = output + writeGroupProperty(resourceURI, label, property, "");
							//output= output + writeGroupProperty(resourceURI, range, property, label  );
						//	output= output + writeTextBoxProperty(resourceURI, label, property, "", true, true , "ObjectProperty", range);
							//output = output + writeGroupProperty(resourceURI, range, property, label  );
						//	allURIs.add(resourceURI);
							 found = true;
						} else {
							System.out.println( "For domain :" + _classURI+ " this property :" + property + " is already traversed " );
							 
						}	
						if(found == true){
							break;
						}else{}
					}*/
				else if(alreadyTraversed == true){
					System.out.println( "For domain :" + _classURI+ " this property :" + property + " is already traversed " );
					dropCount++;
				} else {
					
					if(isFunctional(property)){
						System.out.println( property + " is Transitive and functional Property" );
						
						output = output + writeGroupProperty(resourceURI, range, property, label, range, level  );
						//output= output + writeGroupProperty(resourceURI, range, property, label  );
						 allURIs.add(resourceURI);
						
					} else {
						_logger.info( property + " needs Dynamic group  " );

						output= output + writeDynamicGroupProperty(resourceURI, range, property, label ,range, level );
						//output = output + writeTextBoxProperty(resourceURI, label, property, ""); 
						allURIs.add(resourceURI);
		
					}
				}
			}	
			
			System.out.println("DropValue: " + dropCount);
			
		}	
		if(dropCount == al.size() ){
			System.out.println("For this Class "+_classURI + " DropValue: " + dropCount +" is equal to total properties :" + traversedNodes.get(_classURI).size());
			System.out.println(allURIs.size());
		}
		else {
		_output = writeListProperty(uri, allURIs) + output; 
		}
		
		return _output;
	} 
	
		
	public String writeGroupProperty(String uri ,String rangeURI, String propertyURI, String label, String range, int level ) throws RepositoryException, QueryEvaluationException, MalformedQueryException {

		String _output = "";
		String id = uri.split("#")[1];
		String listURI = uri + "List";
		String valueURI = uri + "Value";
					
			try{
					_output = _output + "<rdf:Description rdf:about=\""+ uri +"\">\n"+
							"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Group\"></rdf:type>\n" +
							"	<raul:id>"+id+"</raul:id>\n"+
							"	<raul:list>"+listURI+"</raul:list>\n" +
							"	<raul:value>"+valueURI+"</raul:value>\n"+
							"	<rdfs:range>"+range+"</rdfs:range>\n"+
							"	<raul:level>"+level+"</raul:level>\n"+
							"</rdf:Description>\n";
									
					_output = _output + writeValueTriple(valueURI, propertyURI, endpoint+"/defaultInstanceGraph_"+id);
					String propertiesDescription = writePropertiesDescription(rangeURI, propertyURI, listURI,0, level); 
					if (propertiesDescription == ""){ 
						_output = writeTextBoxProperty(uri, label, propertyURI, "", false, true, "ObjectProperty", range); 
					} else {
					_output = _output +  propertiesDescription;}			
			}catch(Exception e){}
		return _output;
	}
	
	public String writeDynamicGroupProperty(String resourceURI ,String rangeURI, String propertyURI, String label, String range, int level) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
		
		String _output = "";
		
		String value = resourceURI.split("#")[1];
		String listURI = resourceURI + "List";
		ArrayList<String> listURIFields = new ArrayList<String>();
		listURIFields.add(listURI +"Fields");
		
		_output = _output + "<rdf:Description rdf:about=\""+resourceURI+"\">\n"+
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#DynamicGroup\"></rdf:type>\n" +
				"	<raul:id>"+value+"</raul:id>\n"+
				"	<raul:list>"+listURI+"</raul:list>\n"+
				"	<raul:level>"+level+"</raul:level>\n";
		_output = _output + "</rdf:Description>\n";
		_output = _output + writeListProperty(listURI , listURIFields);
		
		String groupString = writeGroupProperty(listURI +"Fields", rangeURI, propertyURI, label, range,level );
		
		if (groupString.contains("http://purl.org/NET/raul#Group")){
			_output = _output + groupString ;
		} else {
		    _output =  writeTextBoxProperty(resourceURI, label, propertyURI, "", true, true, "ObjectProperty", range);
		}
	return _output; 	
	}
	
	public String writeListBoxProperty(String resourceURI,String property,HashMap<String,String> listMembers) throws RepositoryException{
		
		//System.out.println(" Inside writeListBox clause");
		//System.out.println(resourceURI +"	"+ property+"	"+listMembers);
		String _output = "";
		String listMembersRDF = "";
		ArrayList<String> listMembersURIs = new ArrayList<String>();
		
 		String valueURI = resourceURI + "_Value";
		String listURI = resourceURI + "List";
		
		_output = "<rdf:Description rdf:about=\""+resourceURI+"\">\n" +
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Listbox\"></rdf:type>\n" +
				"	<raul:id>"+resourceURI.split("#")[1]+"</raul:id>\n" +
				"	<raul:name>"+resourceURI.split("#")[1]+"</raul:name>\n" +
				"	<raul:list>"+listURI+"</raul:list>\n" +
				"	<raul:value>"+valueURI+"</raul:value>\n" +
				"	<rdfs:comment rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"+"ObjectProperty"+"</rdfs:comment>\n";
				
		_output = _output + "</rdf:Description>\n";
		_output = _output + writeValueTriple(valueURI, property, endpoint+"/defaultInstanceGraph_"+resourceURI.split("#")[1]);
		
		//System.out.println(_output);
		
		    int index=0;
		    for (Entry<String, String> entry : listMembers.entrySet()) {
	        	//System.out.println(" Inside writeListBox while next iterator clause");    
	            String uri = resourceURI + "_"+index;
				listMembersRDF = listMembersRDF + writeListMembers(uri, entry.getKey(),  entry.getValue());
				listMembersURIs.add(uri);
				index++;
	        }  

		_output = _output + writeListProperty(listURI, listMembersURIs) + listMembersRDF;
		return _output;
	}
	
	public String writeListMembers(String resourceURI, String subClassURI, String label){
		//System.out.println(" Inside write List Members clause");
		String prefix,predicate;
		int startOfPrefix = subClassURI.lastIndexOf("/");
    	int endOfPrefix = subClassURI.lastIndexOf("#");
    	            	
    	prefix = subClassURI.substring(startOfPrefix+1, endOfPrefix);
    	predicate = subClassURI.substring(endOfPrefix+1);
    	
		String output = "<rdf:Description rdf:about=\""+resourceURI+"\">\n"+
			"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Listitem\"/>\n" +
			"	<raul:label>"+label+"</raul:label>\n" +
			"	<raul:value>"+prefix+":"+predicate+"</raul:value>\n" +
			"</rdf:Description>\n";
		
		return output;
	}
	
	
	public String writeTextBoxProperty(String resourceURI, String label,String property, String group, boolean multiple, boolean isIdentifier, String comment, String range){
		
		//System.out.println(" Inside writeTextBox clause");
		String _output = "";
		String valueURI = resourceURI + "_Value";
		_output = _output + "<rdf:Description rdf:about=\""+resourceURI+"\">\n"+
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Textbox\"></rdf:type>\n" +
				"	<raul:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"+ label + ": </raul:label>\n"+
				"	<raul:id>"+ resourceURI.split("#")[1] +"</raul:id>\n"+
				"	<raul:name>"+  resourceURI.split("#")[1] +"</raul:name>\n"+
				"	<raul:value>"+ valueURI +"</raul:value>\n"+
				"	<raul:multiple rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">"+multiple+"</raul:multiple>\n";
		
		if(isIdentifier == true){
			_output = _output + "	<raul:isIdentifier rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">"+isIdentifier+"</raul:isIdentifier>\n";
		}else{}
		
		if(group!=""){
			_output = _output + "	<raul:group>"+group+"</raul:group>\n";
		}else{}
		
		if(range!=""){
			_output = _output + "	<rdfs:range>"+range+"</rdfs:range>\n";
		}else{}
			
		_output = _output + "	<rdfs:comment rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"+comment+"</rdfs:comment>\n";
		_output = _output + "</rdf:Description>\n";
		
		try {
			_output = _output + writeValueTriple(valueURI, property,  endpoint+"/defaultInstanceGraph");
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _output;
	}
	
	public String writeValueTriple(String valueURI, String property, String subject) throws RepositoryException{
		String output = "";
		String prefix="";
		String predicate="";
		String predicateValue = "";
		
		if(property.contains("#")){
			int startOfPrefix = property.lastIndexOf("/");
			int endOfPrefix = property.lastIndexOf("#");  
			if(property.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
				prefix = "rdfs";
			} else if(property.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")){
				prefix = "rdf";
			} else {
				prefix = property.substring(startOfPrefix+1, endOfPrefix);
			}
			predicate = property.substring(endOfPrefix+1);
			predicateValue =prefix+":"+predicate; 
		} else {
			predicateValue = property;
		}
    	
		output = output + "<rdf:Description rdf:about=\""+valueURI+"\">\n"+
				"	<rdf:subject>"+subject+"</rdf:subject>\n"+
				"	<rdf:predicate>"+predicateValue+"</rdf:predicate>\n"+
				"	<rdf:object></rdf:object>\n";
		output = output + "</rdf:Description>\n";		
		return output;
	}
	
	public String writeCREATEOperation(String subject){
		String _output = "";
		
		_output = _output + "<rdf:Description rdf:about=\""+subject+"\">\n" +
				"	<rdf:type rdf:resource=\"http://purl.org/NET/raul#Button\"></rdf:type>\n"+
				"	<raul:command>submitDataWrap()</raul:command>\n"+
				"	<raul:id>Submit</raul:id>\n"+
				"	<raul:name>Submit</raul:name>\n"+
				"	<raul:value>Submit</raul:value>\n"+
				"</rdf:Description>";
		return _output;
	}
	
	
	public ArrayList<PropertyModel> getRelevantPropertiesList(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		ArrayList<PropertyModel> al = new ArrayList<PropertyModel>();
		HashMap<String, String> properties = new HashMap<String, String>();
		
		System.out.println("URI of CLASS :: : " + URI);
		try {	
			
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					
					"SELECT DISTINCT ?property ?label ?range ?type ?iProperty " +
					"WHERE {" +
					"{ {<"+URI+"> rdfs:subClassOf ?restriction." +
					"?restriction owl:onProperty ?property." +
					"{" +
						"{?restriction owl:allValuesFrom ?range.} " +
						"UNION " +
						"{?restriction owl:someValuesFrom ?range} " +
					"}" +
					"}" +
					" UNION " +
					"{?property rdfs:domain <"+URI+">." +
					"?property rdfs:range ?range." +
					"}" +
					" UNION " +
					"{<"+URI+"> owl:equivalentClass ?equClass." +
					"?equClass owl:intersectionOf ?collection." +
					"?collection ?collectionProperty ?member." +
					"?member ?subClassOf ?restriction." +
					"?restriction owl:onProperty ?property." +
					"OPTIONAL {?restriction owl:allValuesFrom ?range.} "+
					"OPTIONAL {?restriction owl:someValuesFrom ?range} " +
					"}" +
					
					"}" +
					"OPTIONAL {?property rdfs:label ?label.}" +
					
					"OPTIONAL {" +
						"?property rdf:type ?type. " +
						"FILTER ((?type = owl:ObjectProperty) || (?type = owl:DatatypeProperty))" +
					"}" +
					"OPTIONAL {?property owl:inverseOf ?iProperty.}" +
					"} ORDER BY (?label) ";
			
			
			System.out.println(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			
			BindingSet results = resultSet.next();
			String property = results.getValue("property").stringValue();
			String label="",type="",range="";
			if(results.hasBinding("label")){
				label = results.getValue("label").stringValue();} else {
					label = property.split("#")[1];
				}
			if(results.hasBinding("range")){
				range = results.getValue("range").toString();}
			if(results.hasBinding("type")) {
				type = results.getValue("type").stringValue().split("#")[1];
			} else if (range != ""){
				type = "ObjectProperty";
			} else {
				type = "DatatypeProperty";
			}
			String inverse="";
			if (results.hasBinding("iProperty")){
				inverse = results.getValue("iProperty").toString();}

				PropertyModel model = new PropertyModel(property, type , range, label, inverse);
				System.out.println("Property : " + property + " Label : " + label + " TYpe : " +type +" Range : "+ range ) ;
				properties.put(property, range);
				al.add(model); 
		 } 
		 
		traversedNodes.put(URI, properties);
		System.out.println("size : " + al.size());
		return al;
	}
	
	public ArrayList<PropertyModel> getRelevantPropertiesList2(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		ArrayList<PropertyModel> al = new ArrayList<PropertyModel>();
		HashMap<String, String> properties = new HashMap<String, String>();
		
		System.out.println("URI of CLASS :: : " + URI);
		try {	
			
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					
					"SELECT DISTINCT ?property ?label ?range ?type ?iProperty " +
					"WHERE {" +
					"{ ?property rdfs:domain <"+URI+">." +
					"OPTIONAL {?property rdfs:label ?label.}" +
					"OPTIONAL {?property rdfs:range ?range.} " +
					"OPTIONAL {" +
						"?property rdf:type ?type. " +
						"FILTER ((?type = owl:ObjectProperty) || (?type = owl:DatatypeProperty))" +
					"}" +
					"OPTIONAL {?property owl:inverseOf ?iProperty.}" +
					"} ORDER BY (?label) ";
			
			
			System.out.println(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();
		
		while(resultSet.hasNext()) {
			
			BindingSet results = resultSet.next();
			String property = results.getValue("property").stringValue();
			String label="",type="",range="";
			if(results.hasBinding("label")){
				label = results.getValue("label").stringValue();} else {
					label = property.split("#")[1];
				}
			if(results.hasBinding("range")){
				range = results.getValue("range").toString();}
			if(results.hasBinding("type")) {
				type = results.getValue("type").stringValue().split("#")[1];
			} else if (range != ""){
				type = "ObjectProperty";
			} else {
				type = "DatatypeProperty";
			}
			String inverse="";
			if (results.hasBinding("iProperty")){
				inverse = results.getValue("iProperty").toString();}
			
			PropertyModel model = new PropertyModel(property, type , range, label, inverse);
			System.out.println("Property : " + property + " Label : " + label + " TYpe : " +type +" Range : "+ range ) ;
			properties.put(property, range);
			al.add(model);
		} 
		traversedNodes.put(URI, properties);
		System.out.println("size : " + al.size());
		return al;
	}
	
	
	public boolean isTransitive(String propertyURI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		//BooleanQuery query1= null;
		try {	
			
			bQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"ASK  { " +
			"{ ?class rdfs:subClassOf ?restriction." +
			"?restriction owl:onProperty <"+propertyURI+">." +
			"{{?restriction owl:allValuesFrom ?class.} " +
			"UNION" +
			"{?restriction owl:someValesFrom ?class.}}}" +
			" UNION " +
			"{<"+propertyURI+"> rdfs:domain ?class." +
			"<"+propertyURI+"> rdfs:range ?class}" +
			"}");
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		boolean result = bQuery.evaluate();
		
		//System.out.println(result);
		return result;
	}
	
	
	public boolean isInverse(String property1, String property2) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		//BooleanQuery query1= null;
		boolean result = false;
		
		if (property2.equalsIgnoreCase("")) {
			
		} else {
			
			try {	
			
				bQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, 
						"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
						"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
				"ASK  { { ?domain rdfs:subClassOf ?restriction." +
				"?restriction owl:onProperty <"+property1+">." +
				"?restriction ?cardinality ?range." +
				"?range rdfs:subClassOf ?restriction2." +
				"?restriction2 owl:onProperty <"+property2+">." +
				"?restriction2 ?cardinality ?domain. " +
				"Filter ((?cardinality = owl:allValuesFrom) || (owl:someValuesFrom))} " +
				" UNION {" +
				"<"+property1+"> owl:inverseOf <"+property2+">}" +
				" UNION {" +
				"<"+property1+"> rdfs:domain ?domain." +
				"<"+property1+"> rdfs:range ?range." +
				"<"+property2+"> rdfs:domain ?range." +
				"<"+property2+"> rdfs:range ?domain.}" +
				"}");
			
			} catch (MalformedQueryException e1) {
				e1.printStackTrace();}
				result = bQuery.evaluate();
		}
		//System.out.println(result);
		return result;
	}
	
	public boolean isFunctional(String propertyURI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		//BooleanQuery query1= null;
		try {	
			
			bQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"ASK  { <"+propertyURI+"> rdf:type owl:FunctionalProperty .}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		boolean result = bQuery.evaluate();
		
		//System.out.println(result);
		return result;
	}
	
	public String getResourceURI(String property, String uri){
		
		String resourceURI ,valueWithoutURI;
		if(property.contains("#")){
			valueWithoutURI = property.split("#")[1];
		} else {
			String[] parts = property.split("/"); 
			valueWithoutURI = parts[parts.length-1];
		}
		
		if(uri.contains("#containerList")){
			resourceURI = uri.split("#")[0] + "#" +valueWithoutURI;
		} else if(uri.contains("#")){
			resourceURI = uri + "_" + valueWithoutURI ;
		} else{
			resourceURI = uri + "#" +valueWithoutURI;
		}
		
		//System.out.println("Resource URI : " + resourceURI);
		return resourceURI;
	}
	
	
	public int getPropertiesCount(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		int count= 0;
		try {	
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT (COUNT(?property) AS ?count) " +
			"WHERE {" +
			"{<"+URI+"> rdfs:subClassOf ?restriction." +
			"?restriction owl:onProperty ?property.}" +
			" UNION " +
			"{?property rdfs:domain <"+URI+">}" +
			"}" );
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			
			BindingSet results = resultSet.next();
			String countString = results.getValue("count").stringValue();
			count = Integer.parseInt(countString);
		}
		return count;
	}
	
	public HashMap<String,String> getSubClasses(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		HashMap<String,String> subClassList = new HashMap<String,String>();
		//HashMap<String, String> properties = new HashMap<String, String>();
		int count= 0;
		try {	
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT ?subClass ?label " +
			"WHERE {?subClass rdfs:subClassOf <"+URI+"> ." +
					"?subClass rdfs:label ?label." +
				//	"Filter (?subClass != <"+URI+">)" +
							"}" );
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			BindingSet results = resultSet.next();
			subClassList.put(results.getValue("subClass").toString(), results.getValue("label").stringValue());
		}	
		//System.out.println( URI + " has properties " + count);
		return subClassList;
	}
	
	public ArrayList<String> getSuperClasses(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		ArrayList<String> superClassList = new ArrayList<String>();
		try {	
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT ?superClass " +
			"WHERE {<"+URI+"> rdfs:subClassOf  ?superClass." +
					"Filter (!isblank(?superClass))}" );
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			BindingSet results = resultSet.next();
			superClassList.add(results.getValue("superClass").toString());
		}	
		//System.out.println( URI + " has properties " + count);
		return superClassList;
	}
}