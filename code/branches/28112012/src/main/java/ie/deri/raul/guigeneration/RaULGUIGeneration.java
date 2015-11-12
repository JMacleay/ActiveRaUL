package ie.deri.raul.guigeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.result.MultipleResultException;
import org.openrdf.result.NoResultException;

public class RaULGUIGeneration {
	// Set<Value> listsubjects = new LinkedHashSet<Value>();
	//private HashMap<String, List<String>> subjectMap = new HashMap<String, List<String>>();
	private TupleQuery query = null;
	private Hashtable<String,String> seenUrls = new Hashtable<String,String>();
	LinkedHashMap<String, LinkedHashMap<String, List<String>>> classProperties = new LinkedHashMap<String, LinkedHashMap<String,List<String>>>();

	public LinkedHashMap<String, LinkedHashMap<String, List<String>>> generateGUI(Repository graph) throws RepositoryConfigException, RepositoryException, NoResultException, MultipleResultException, QueryEvaluationException {
		// RDFFormat graph1 = RDFFormat.RDFXML;

		ObjectRepository objectrepository = new ObjectRepositoryFactory().createRepository(graph);
		ObjectConnection con = objectrepository.getConnection();

		// Query for the ontology URI

		try {
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + "SELECT ?s WHERE {{?s rdf:type owl:Ontology}}");
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult ontologyNameSet = query.evaluate();

		if(ontologyNameSet.hasNext()) {
			String ontologyName = ontologyNameSet.next().getValue("s").stringValue();

			try {
				//				query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + "SELECT ?cl WHERE {?cl rdf:type rdfs:Class . OPTIONAL {?cl rdfs:subClassOf ?sc. filter(?cl != ?sc && ?sc != owl:Thing && ?sc != rdfs:Resource)} filter (!bound(?sc) && isURI(?cl))}");
				//				query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + "SELECT ?s WHERE {{?s rdf:type rdfs:Class} UNION {?s rdf:type owl:Class} OPTIONAL { ?y rdfs:subClassOf ?z . FILTER (?y = ?s) . } FILTER ( !BOUND(?y) )}"); 
				//				query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT s FROM {s} rdf:type {y}; [rdfs:subClassOf {z} WHERE s != z ] WHERE NOT BOUND(z) AND isURI(s) AND y IN (rdfs:Class, owl:Class) AND s != owl:Thing AND s != rdfs:Resource USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>"); 

				// Get all superclasses that are not owl:Thing, rdfs:Resource or rdfs:Class
				query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT DISTINCT s FROM {s} rdf:type {y} WHERE y IN (rdfs:Class, owl:Class) AND NOT s LIKE \"http://www.w3.org/2000/01/rdf-schema*\" AND NOT s LIKE \"http://www.w3.org/2002/07/owl#*\" AND NOT s LIKE \"http://www.w3.org/1999/02/22-rdf-syntax-ns*\" AND NOT s LIKE \"http://www.w3.org/2001/XMLSchema#*\" MINUS SELECT DISTINCT s FROM {s} rdfs:subClassOf {y} WHERE s != y AND y != owl:Thing AND y != rdfs:Resource AND y != rdfs:Class AND y != owl:Class USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>"); 


				//				query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + "SELECT ?cl WHERE {?cl rdfs:subClassOf rdfs:Resource . OPTIONAL { ?cl rdfs:subClassOf ?sc. ?sc rdfs:subClassOf rdfs:Resource . filter(?cl != ?sc) } filter (!bound(?sc) && isURI(?cl) && ?cl != rdfs:Resource) }");


			} catch (MalformedQueryException e1) {
				e1.printStackTrace();
			}
			TupleQueryResult superClassResult = query.evaluate();

			while (superClassResult.hasNext()) {

				BindingSet superClassSet = superClassResult.next();
				String subjectString = superClassSet.getValue("s").toString();
				
				List<String> propertyList = new ArrayList<String>();

				// Query for all predicates of the Top Class
				try {
					// query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>" + "SELECT ?s WHERE {?s rdfs:domain rdfs:Resource.}");
					query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT s FROM {s} rdfs:domain {y} WHERE y IN (rdfs:Resource, owl:Thing) AND NOT s LIKE \"http://www.w3.org/2000/01/rdf-schema*\" AND NOT s LIKE \"http://www.w3.org/2002/07/owl#*\" AND NOT s LIKE \"http://www.w3.org/1999/02/22-rdf-syntax-ns*\" USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>"); 
				} catch (MalformedQueryException e1) {
					e1.printStackTrace();
				}
				TupleQueryResult topPredicates = query.evaluate();

				while (topPredicates.hasNext()) {
					BindingSet predicateDomainSet = topPredicates.next();
					try {
						query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdfs:label {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
					} catch (MalformedQueryException e1) {
						e1.printStackTrace();
					}				
					TupleQueryResult predicateLabel = query.evaluate();
					String labelString = "";
					if (predicateLabel.hasNext()) {
						labelString = predicateLabel.next().getValue("o").toString();
						labelString = labelString.substring(1, labelString.length()-1);
					}
					
					try {
						query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdf:type {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
					} catch (MalformedQueryException e1) {
						e1.printStackTrace();
					}				
					TupleQueryResult predicateType = query.evaluate();

					// add Top class properties
					while (predicateType.hasNext()) {
						BindingSet predicateTypeSet = predicateType.next();

						// For all DataTypeProperties determine the datatype
						if (predicateTypeSet.getValue("o").stringValue().equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
							try {
								query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdfs:range {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
							} catch (MalformedQueryException e1) {
								e1.printStackTrace();
							}
							TupleQueryResult dataType = query.evaluate();
							String dataTypes = "";
							while (dataType.hasNext()) {
								BindingSet dataTypeSet = dataType.next();
								dataTypes += " " + dataTypeSet.getValue("o").stringValue();
							}	
							propertyList.add("TopClass,owl:DatatypeProperty," + dataTypes + "," + labelString + "," + predicateDomainSet.getValue("s").stringValue());
						}

						// For all ObjectProperties create identifiers for the instance data
						if (predicateTypeSet.getValue("o").stringValue().equals("http://www.w3.org/2002/07/owl#ObjectProperty")) { 

							try {
								query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?o WHERE {<" + predicateDomainSet.getValue("s") + "> rdfs:range ?o.}");
							} catch (MalformedQueryException e1) {
								e1.printStackTrace();
							}
							TupleQueryResult propertyRange = query.evaluate();
							while (propertyRange.hasNext()) {
								BindingSet propertyRangeSet = propertyRange.next();
								propertyList.add("TopClass,owl:ObjectProperty," +  propertyRangeSet.getValue("o") + "," + labelString + "," + predicateDomainSet.getValue("s").stringValue()); 
								//ontologyName	
							}
						}
						System.out.println(propertyList);
					}
					// call recursive method getSubClassPropertyList for each Top class
					LinkedHashMap<String, List<String>> subClassProperties = getSubClassPropertyList(con,subjectString,subjectString,propertyList);
					classProperties.put(subjectString,subClassProperties);	
				}
			}			
		}
		return classProperties;

	}
	private LinkedHashMap<String, List<String>> getSubClassPropertyList(ObjectConnection con, String topClass, String superClass, List<String> propertyList) throws RepositoryException, QueryEvaluationException {

		//System.out.println(superClass);
		LinkedHashMap<String, List<String>> subClassProperties = new LinkedHashMap<String, List<String>>();	

		// query for properties of superClass
		try {
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?s WHERE {?s rdfs:domain <" + superClass + ">.}");
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult predicateSet = query.evaluate();
		List<String> predicateList = new ArrayList<String>();
		while (predicateSet.hasNext()) {
			BindingSet predicateDomainSet = predicateSet.next();
			try {
				query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdfs:label {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
			} catch (MalformedQueryException e1) {
				e1.printStackTrace();
			}				
			TupleQueryResult predicateLabel = query.evaluate();
			String labelString = "";
			if (predicateLabel.hasNext()) {
				labelString = predicateLabel.next().getValue("o").toString();
				labelString = labelString.substring(1, labelString.length()-1);
			}
			try {
				query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdf:type {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
			} catch (MalformedQueryException e1) {
				e1.printStackTrace();
			}				
			TupleQueryResult predicateType = query.evaluate();


			if (predicateType.hasNext()) {
				while (predicateType.hasNext()) {
					BindingSet predicateTypeSet = predicateType.next();

					// For all DataTypeProperties determine the datatype
					if (predicateTypeSet.getValue("o").stringValue().equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
						try {
							query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT o FROM {s} rdfs:range {o} WHERE s = <" + predicateDomainSet.getValue("s") + "> USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>");
						} catch (MalformedQueryException e1) {
							e1.printStackTrace();
						}
						TupleQueryResult dataType = query.evaluate();
						String dataTypes = "";
						while (dataType.hasNext()) {
							BindingSet dataTypeSet = dataType.next();
							dataTypes += " " + dataTypeSet.getValue("o").stringValue();
						}	
						predicateList.add("SubClass,owl:DatatypeProperty," + dataTypes + "," + labelString + "," + predicateDomainSet.getValue("s").stringValue());
					}

					// For all ObjectProperties create identifiers for the instance data
					if (predicateTypeSet.getValue("o").stringValue().equals("http://www.w3.org/2002/07/owl#ObjectProperty")) { 

						try {
							query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?o WHERE {<" + predicateDomainSet.getValue("s") + "> rdfs:range ?o.}");
						} catch (MalformedQueryException e1) {
							e1.printStackTrace();
						}
						TupleQueryResult propertyRange = query.evaluate();
						while (propertyRange.hasNext()) {
							BindingSet propertyRangeSet = propertyRange.next();
							predicateList.add("SubClass,owl:ObjectProperty," +  propertyRangeSet.getValue("o") + "," + labelString + "," + predicateDomainSet.getValue("s").stringValue()); 
							//ontologyName	
						}
					}

				}
			}
			else {predicateList.add(predicateDomainSet.getValue("s").stringValue());}
		}
		List<String> mergedPredicateList = new ArrayList<String>();
		mergedPredicateList.addAll(propertyList);
		mergedPredicateList.addAll(predicateList);
		subClassProperties.put(superClass, mergedPredicateList);

		// query for subclass and for every subclass call function

		try {
			query = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT s FROM {s} rdfs:subClassOf {y} WHERE (y = <" + superClass + "> AND s != y AND y != owl:Thing AND y != rdfs:Resource AND y != rdfs:Class AND y != owl:Class) USING NAMESPACE rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, owl = <http://www.w3.org/2002/07/owl#>, rdfs = <http://www.w3.org/2000/01/rdf-schema#>"); 
			//query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?s WHERE {?s rdfs:subClassOf <" + superClass + ">}");
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult subClassResult = query.evaluate();
		int k =0;
		// for every subclass call function
		while (subClassResult.hasNext()) {
			BindingSet subClassSet = subClassResult.next();
			String subClass = subClassSet.getValue("s").stringValue();
			//check classProperties instead
			
			if(!seenUrls.containsKey(subClass)) {

				subClassProperties.putAll(getSubClassPropertyList(con,topClass,subClass,mergedPredicateList));
				seenUrls.put(subClass, topClass);
			}
			else {
				String oldTopClass = seenUrls.get(subClass);
				LinkedHashMap<String, List<String>> addedSubclassProperties = classProperties.get(oldTopClass);
				if (addedSubclassProperties != null) {
					//problem with topclass
					
					List<String> part = addedSubclassProperties.get(subClass);
					List<String> mergedParts = new ArrayList<String>();
					
					Set<String> setboth = new HashSet<String>(part);
					setboth.addAll(mergedPredicateList);
					part.clear();
					part.addAll(setboth);
					
					mergedParts.addAll(part);
					addedSubclassProperties.put(subClass,mergedParts);
					subClassProperties.putAll(addedSubclassProperties);
//					subClassProperties.putAll(getSubClassPropertyList(con,topClass,subClass,mergedPredicateList));
				}
			}

		}

		return subClassProperties;

	}
}
