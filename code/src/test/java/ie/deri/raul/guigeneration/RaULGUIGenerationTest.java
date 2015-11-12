package ie.deri.raul.guigeneration;



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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.rio.RDFFormat;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.test.framework.JerseyTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;




public class RaULGUIGenerationTest extends JerseyTest {

	private static Log _logger = LogFactory.getLog(RaULGUIGenerationTest.class);
	public static String instanceURI = "http://w3c.org.au/raul/service/public/forms/foafedit";
	private Hashtable<String,String> seenPredicates = new Hashtable<String,String>();
	
	public RaULGUIGenerationTest() throws Exception {
		super("ie.deri.raul.guigeneration");
	}

	@Test
	public void testFOAFRaULGUIGeneration() throws Exception {		

		Repository myRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		//Repository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();

		URL myRDFData = Thread.currentThread().getContextClassLoader().getResource("foaf.rdf");	
		RepositoryConnection con = myRepository.getConnection();
		URL url = myRDFData;
		con.add(url, url.toString(), RDFFormat.RDFXML);

		// Get all Triples
//		TupleQuery query = null;
//		try {
//			query = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?s ?p ?o WHERE {?s ?p ?o.}");
//		} catch (MalformedQueryException e1) {
//			e1.printStackTrace();
//		}				
//		TupleQueryResult queryResult = query.evaluate();
//		while (queryResult.hasNext()) {
//			BindingSet TripleSet = queryResult.next();
//			System.out.println(TripleSet.getValue("s") + " " + TripleSet.getValue("p") + " " + TripleSet.getValue("o"));
//		}
		
		RaULGUIGeneration foafgraph = new RaULGUIGeneration();

		LinkedHashMap<String, LinkedHashMap<String, List<String>>> uri = foafgraph.generateGUI(myRepository);


		for (Entry<String, LinkedHashMap<String, List<String>>> entry : uri.entrySet()) {
			System.out.println("Superclass:  " + entry.getKey());
			LinkedHashMap<String, List<String>> predicatevalue = entry.getValue();
			for (Entry<String, List<String>> subClassEntry : predicatevalue.entrySet()) {
				System.out.println("  Subclass:  " + subClassEntry.getKey());
				List<String> predicatevalue1 = subClassEntry.getValue();

				Iterator<String> iterator = predicatevalue1.iterator();
				while (iterator.hasNext()) {
					System.out.println("     " + iterator.next());
				}
			}

		}

		for (Entry<String, LinkedHashMap<String, List<String>>> entry : uri.entrySet()) {
			int i = 1;
			String fragmentID = "";
			if (entry.getKey().contains("#")) { fragmentID = entry.getKey().substring(entry.getKey().indexOf("#")+1); }
			else { fragmentID = entry.getKey().substring(entry.getKey().lastIndexOf("/")+1); }
			System.out.println("\n" +
					"<rdf:Description rdf:about=\""+ instanceURI +"#" + fragmentID + "\">\n" + //Superclass instance
					"  <rdf:type rdf:resource=\"" + entry.getKey() + "\"></rdf:type>");  //Type of Superclass
			LinkedHashMap<String, List<String>> subclassvalue = entry.getValue();
			for (Entry<String, List<String>> subClassEntry : subclassvalue.entrySet()) {
				if (subClassEntry.getKey().contains("#")) { fragmentID = subClassEntry.getKey().substring(subClassEntry.getKey().indexOf("#")+1); }
				else { fragmentID = subClassEntry.getKey().substring(subClassEntry.getKey().lastIndexOf("/")+1); }
				System.out.println("  <rdf:_" + i + " rdf:resource=\""+ instanceURI + "#" + fragmentID +"_container\"/>"); //Subclass container
				i++;
			}
			System.out.println("</rdf:Description>");

			for (Entry<String, List<String>> subClassEntry : subclassvalue.entrySet()) {
				if (subClassEntry.getKey().contains("#")) { fragmentID = subClassEntry.getKey().substring(subClassEntry.getKey().indexOf("#")+1); }
				else { fragmentID = subClassEntry.getKey().substring(subClassEntry.getKey().lastIndexOf("/")+1); }
				System.out.println("<rdf:Description rdf:about=\""+ instanceURI + "#" + fragmentID +"_container\"/>\n" +			
						"  <rdf:type rdf:resource=\"http://purl.org/NET/raul#WidgetContainer\"></rdf:type>\n" +
						"  <raul:list>" + instanceURI + "#" + fragmentID +"</raul:list>\n" +
				"</rdf:Description>");

				Iterator<String> iterator = subClassEntry.getValue().iterator();
				int j=1;
				System.out.println("<rdf:Description rdf:about=\"" + instanceURI + "#" + fragmentID +"\">\n" +
				"  <rdf:type rdf:resource=\"" + subClassEntry.getKey() + "\"></rdf:type>");  //Type of Subclass
				while (iterator.hasNext()) {
				    String[] elements = iterator.next().split(",");
				    // make sure that the URI in the substring method is actually the URI of the ontology
				    System.out.println("  <rdf:_" + j + " rdf:resource=\"" + instanceURI + "#" + elements[4].substring(elements[4].lastIndexOf("/")+1) + "\"/>");
				    j++;
				}
				System.out.println("</rdf:Description>");
				iterator = subClassEntry.getValue().iterator();
				while (iterator.hasNext()) {
					String[] elements = iterator.next().split(",");
					//create the predicate (need to check if it was already created)
					if(!seenPredicates.containsKey(elements[4])) {
						seenPredicates.put(elements[4], "");
						System.out.println("<raul:Textbox rdf:about=\"" + instanceURI + "#" + elements[4].substring(elements[4].lastIndexOf("/")+1) + ">\n" +
								"  <raul:label>" + elements[3] +"</raul:label>\n" +
								"  <raul:value>" + instanceURI + "#value_" + elements[4].substring(elements[4].lastIndexOf("/")+1) + "</raul:value>\n" + 
								"</raul:Textbox>\n" +
								"<rdf:Description rdf:about="+ instanceURI + "#value_" + elements[4].substring(elements[4].lastIndexOf("/")+1) + ">\n" +
								"  <rdf:subject>" + instanceURI + "/defaultInstanceGraph" + "</rdf:subject>\n" +
								"  <rdf:predicate>" + elements[4] + "</rdf:predicate>\n" +
								"  <rdf:object/>\n" +
						"</rdf:Description>");
					}
				}
			}
		}

		
	}
}
