package ie.deri.raul.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ie.deri.raul.resources.RaULResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.sun.jersey.api.container.MappableContainerException;


public class RDFTripleExtractor {

	private static Log _logger = LogFactory.getLog(RaULResource.class);
	//private RDFRepository _repository= null;
	private RDFRepository _dataGraph= null;
	private RepositoryConnection _con;
	private HashMap<String, String>  resourcesURIs = new HashMap<String, String>();
	
	public RDFTripleExtractor(String contents, String formIri, RDFFormat format, String context){
		
		//_logger.info("Contents.... " + contents);

		try {
			//_repository = RDFRepositoryFactory.createRepository();
			_dataGraph = RDFRepositoryFactory.createInMemoryRepository();
			_dataGraph.addString(contents, formIri, format, context);
			_con = _dataGraph.createConnection();
			 resourcesURIs = getResourcesValues(_con);
			//addResources();
			//addTriples();
		} catch (MappableContainerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RDFRepository addTriples(){
        try{
           for (Entry<String, String> entry : resourcesURIs.entrySet()) {
        	   _logger.info(" Value ...... " + entry.getKey());
        	   _logger.info(" Range ...... " + entry.getValue());
        	   
           try{
        	   String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        			   "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
        			   "PREFIX raul:<http://purl.org/NET/raul#>"+
        			   
                       "SELECT DISTINCT ?subject ?predicate ?object " +
                       " Where {" +
                       "<"+entry.getKey()+"> rdf:subject ?subject." +
                       "<"+entry.getKey()+"> rdf:predicate ?predicate." +
                       "<"+entry.getKey()+"> rdf:object ?object." +
                       "FILTER ( ?object != \"\" )." +
                       "}";
            
                        TupleQuery tupleQuery =_con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                        TupleQueryResult result = tupleQuery.evaluate();
                        try{
                               // int count=0;
                        		URI type = _dataGraph.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                                List<String> bindingNames =result.getBindingNames();
                                while(result.hasNext())
                                {
                                    BindingSet bindingSet=result.next();
                                    Value subject = bindingSet.getValue(bindingNames.get(0));
                                    Value predicate = bindingSet.getValue(bindingNames.get(1));
                                    Value object = bindingSet.getValue(bindingNames.get(2));
                                  //  Value r = bindingSet.getValue(bindingNames.get(3));
                                    _logger.info( subject.toString() +"	"+ predicate.toString() + "	"+ object.toString()+ "	"+ entry.getValue());
                                    
                                    URI s = _dataGraph.URIref(subject.toString());
                    				URI p = _dataGraph.URIref(predicate.toString());
                    
                    				if(object.toString().contains("http://")){
                    					URI o = _dataGraph.URIref(object.toString());
                    					URI range = _dataGraph.URIref(entry.getValue());

                        				_dataGraph.add(s, p, o);
                    					_dataGraph.add(o, type, range);
                    				} else {
                    					Value o = _dataGraph.Literal(object.toString());
                        				_dataGraph.add(s, p, o);
                    				}	
                                }

                        }catch(Exception e){
   	                    	System.out.println(e);
   	                    } finally{
                           result.close();
                        }
          

                    } catch(Exception e){
                    	System.out.println(e);
                    }finally{ 
                    	//con.close(); 
                    } } } catch(Exception e ){}
		return _dataGraph;
	}
	
/*	private void addResources(){
		try{
            String queryString = "PREFIX raul:<http://purl.org/NET/raul#>"+
            					 "SELECT DISTINCT ?value ?list " +
                                 "Where {" +
                                 "?resourceURI raul:value ?value." +
                                 "OPTIONAL {?resourceURI raul:list ?list.}" +
                                 "}";

            TupleQuery tupleQuery =_con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = tupleQuery.evaluate();
            try{
                    List<String> bindingNames =result.getBindingNames();
                    while(result.hasNext())
                    {
                        BindingSet bindingSet=result.next();
                       
                        Value value = bindingSet.getValue(bindingNames.get(0)); 
                        _dataGraph.addURI(value.toString().replaceAll("\"", ""), RDFFormat.RDFXML);
                        
                        if (bindingSet.hasBinding("list")){
                            Value list = bindingSet.getValue(bindingNames.get(1));
                            _dataGraph.addURI(list.toString().replaceAll("\"", ""), RDFFormat.RDFXML);
                        }                    
                    }
               } finally{
                        result.close();
           }
		} catch(Exception e){
			
		}
	}
	*/
	 public HashMap<String, String> getResourcesValues(RepositoryConnection con){

		 HashMap<String, String> resources = new HashMap<String, String>(); 
	           
	           try{
	        	   String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	        			   "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
	        			   "PREFIX raul:<http://purl.org/NET/raul#>"+
	        			   "SELECT DISTINCT ?value ?range " +
	        			   " Where {" +
	        			   "?resourceURI raul:value ?value." +
	        			   "?resourceURI rdfs:range ?range." +	                                             
	        			   "}";
	        	   
	        	   TupleQuery tupleQuery =con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
	        	   TupleQueryResult result = tupleQuery.evaluate();
	        	   try{
	        		   // int count=0;
	        		   List<String> bindingNames =result.getBindingNames();
	        		   while(result.hasNext())
	        		   {
	        			   BindingSet bindingSet=result.next();
	        			   Value value = bindingSet.getValue(bindingNames.get(0));
	        			   Value range = bindingSet.getValue(bindingNames.get(1));
	        			   resources.put(value.toString().replace("\"", ""), range.toString().replace("\"", ""));
	        		   }
	        	   }catch(Exception e){
	        		   System.out.println(e);
	        	   }
	        	   finally{
	        		   result.close();
	        	   }
	        	   
	           } catch(Exception e){
	        	   System.out.println(e);
	           }
	           finally{
	        	   //con.close();
	           }
	        return resources;
	 }
	
}
