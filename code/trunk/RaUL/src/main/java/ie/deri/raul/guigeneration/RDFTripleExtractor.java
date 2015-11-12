package ie.deri.raul.guigeneration;

import java.util.ArrayList;


import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.persistence.RDFRepositoryFactory;
import ie.deri.raul.resources.RaULResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;



public class RDFTripleExtractor {

	private static Log _logger = LogFactory.getLog(RaULResource.class);
	//private RDFRepository _repository= null;
	private RDFRepository _dataGraph= null;
	private RepositoryConnection _con;
	private TupleQuery query= null;
	
	public RDFTripleExtractor(RDFRepository repository) {
		// TODO Auto-generated constructor stub
		try{ 
			this._con = repository.createConnection();
			this._dataGraph =RDFRepositoryFactory.createInMemoryRepository();
			
		}catch(Exception e){}
	}


	public RDFTripleExtractor(Repository repository) {
		// TODO Auto-generated constructor stub
		try{ 
			this._con = repository.getConnection();
			this._dataGraph = RDFRepositoryFactory.createInMemoryRepository();
			
		}catch(Exception e){}
	}
	
	public RDFRepository addTriples(){
		URI type = _dataGraph.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		URI label = _dataGraph.URIref("http://www.w3.org/2000/01/rdf-schema#label");
		
		try{
		
		ArrayList<RDFBean> _list =getResourcesValues() ;

		ArrayList<RDFBean> list =getCompleteInfo(_list) ;
		
		for (int i=0; i <list.size(); i++){
			RDFBean bb = new RDFBean();
			bb = list.get(i);
			//_logger.info(bb.getURI() + "	"+ bb.getType() + "	" + bb.getRange()+ "	"+ bb.getSubject()+ "	"+ bb.getProperty()+ "	"+ bb.getObject());
			
			//
			String uri = bb.getURI();
			String uriType = bb.getType();
			String range=bb.getRange();
			String subject=bb.getSubject();
			String property = bb.getProperty();
			String object = bb.getObject();
			
			if(object.equalsIgnoreCase("")){
				//System.out.println(bb.getURI() + " Object is null for this uri	");
				 
			} else {

				if(uriType.contains("DatatypeProperty") ){
					 //System.out.println(bb.getURI() + " A Data type uiri	");
					 
					 URI s = _dataGraph.URIref(subject);
					 URI p = _dataGraph.URIref(property);
					 
					 if ( property.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#label") ){
						 if(!(subject.contains("defaultInstanceGraph"))) {
							 URI r = _dataGraph.URIref(range);
							 _dataGraph.add(s, type, r);
						 }
					 } else  {					 
					 }
					 
					 Literal l = _dataGraph.Literal(object);
					_dataGraph.add(s, p, l); 

				}else{
					//System.out.println(bb.getURI() + " Object URI	");
					//System.out.println(bb.getURI() + "	"+ bb.getType() + "	" + bb.getRange()+ "	"+ bb.getSubject()+ "	"+ bb.getProperty()+ "	"+ bb.getObject());
					 
					 URI s = _dataGraph.URIref(subject);
					 URI o = _dataGraph.URIref(object);
					 URI p = _dataGraph.URIref(property);

					//if(range.equalsIgnoreCase("")){
						URI r = _dataGraph.URIref(range);
						// Literal l = _dataGraph.Literal(bb.getObject().replaceAll("\"", ""));
						 _dataGraph.add(o, type, r);
					//}
		
					_dataGraph.add(s, p, o);

					//_dataGraph.add(o, label, l);
				}	
			}
		}
		} catch(Exception e){
			
		}
		return _dataGraph;	
	} 
	
	
	public ArrayList<RDFBean> getCompleteInfo(ArrayList<RDFBean> rdfList) {
		
    	ArrayList<RDFBean>  resourcesURIs = new ArrayList<RDFBean>();      
    	
    	//System.out.println(rdfList.size() + "LIST SIZE");
    	
		for (int i=0 ; i< rdfList.size() ; i++) {
			
		  // System.out.println("INSIDE " + i);
       	   RDFBean rdf = new RDFBean();
       	   
       	   rdf = rdfList.get(i);
       	   String uri = rdf.getURI().replaceAll("\"", "");
       	   String range = rdf.getRange();
       	   String type = rdf.getType();
//
       //	System.out.println("Value :" + uri + "	Type : " +type+" Range " + range );
       	  // queryString = ; 
       	   try{
       		TupleQuery query = _con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
       				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
    				"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
    				"PREFIX raul:<http://purl.org/NET/raul#> " +
    				
    				"SELECT ?subject ?property ?object " +

    				"WHERE {" +
    					"<"+uri+"> rdf:subject ?subject." +
    					"<"+uri+"> rdf:predicate ?property." +
    					"<"+uri+"> rdf:object ?object." +    					
    				"}");
       		
        	TupleQueryResult resultSet = query.evaluate();
       		   
       		   	while(resultSet.hasNext()) {
       	       	
       		   		BindingSet results = resultSet.next();
       			    RDFBean rdf2 = new RDFBean();
     	       	   
       		   		String subject = results.getValue("subject").toString().replaceAll("\"", "");
       		   		String property = results.getValue("property").toString().replaceAll("\"", "");
       		   		String object="";
       		   		
       		   		if(results.hasBinding("object")) {
       		   			object = results.getValue("object").toString().replaceAll("\"", "");
       		   		}
           	       	   
          	       	   	rdf2.setURI(uri);
          	       	   	rdf2.setType(type);
          	       	   	rdf2.setRange(range);
          		   		rdf2.setSubject(subject);
          		   		rdf2.setProperty(property);
          		   		rdf2.setObject(object);
          		   		
          		   		resourcesURIs.add(rdf2);
          		   		
       		   		// _logger.info( "SUBJECT : : :" + subject + "	PROPERTY :::	" + property +	"	OBJECT :: : " + object ) ;
       		   	} 
       		  }catch (Exception e ){}
			}
          return resourcesURIs; 
         }
                
	
	 public ArrayList<RDFBean> getResourcesValues(){
		 
	    	ArrayList<RDFBean>  resourcesURIs = new ArrayList<RDFBean>();      

	         try{
	        	   
	   			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
						"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
						"PREFIX raul:<http://purl.org/NET/raul#> " +
						
						"SELECT ?value ?range ?type " +

						"WHERE {" +
						"?uri raul:value ?value." +
						//"?uri rdf:type ?textbox." +
						"OPTIONAL {?uri rdfs:range ?range.}" +
						"OPTIONAL {?uri rdfs:comment ?type.} " +
				      //  "FILTER regex(?uri, defaultInstanceGraph, \"i\")"+

						"}";
	        	   
	        	   TupleQuery tupleQuery =_con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
	        	   TupleQueryResult result = tupleQuery.evaluate();
	        	 
	        	   try{
	        		   
	        		   while(result.hasNext())
	        		   {
	        			   RDFBean rdf = new RDFBean();
	        			   BindingSet results=result.next();
	        			   String value="", range="", type="";
	        			   
	        			   value = results.getValue("value").toString().replaceAll("\"", "");
	        			   rdf.setURI(value);
	        			   if (results.hasBinding("range")){
	        			      range = results.getValue("range").toString().replaceAll("\"", "");
	        			      rdf.setRange(range);
	        			   }
	        			   
	        			   if (results.hasBinding("type")){
		        			      type = results.getValue("type").toString().replaceAll("\"", "");
		        			      rdf.setType(type);
	        			   }
	        			   
	        			  // System.out.println("Value :" + rdf.getURI() + "	Type : " +rdf.getType()+" Range " + rdf.getRange() );
	        			   resourcesURIs.add(rdf);
	        		 }
	        	   }catch(Exception e){
	        		   System.out.println(e);
	        	   }
	        	   finally{
	        		   result.close();
	        	   }
	        	   
	           } catch(Exception e){
	        	   System.out.println(e);
	           } finally {
	        	   //con.close();
	           }
	         
	         //System.out.println(resourcesURIs.size());
	         return resourcesURIs;
	 }
}

/*public ArrayList<RDFBean> getRDFInfo() throws QueryEvaluationException, RepositoryException, MalformedQueryException{
ArrayList<RDFBean> rdfList = new ArrayList<RDFBean>();

try {	
	
	String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
			"PREFIX raul:<http://purl.org/NET/raul#> " +
			
			"SELECT DISTINCT ?value ?range ?type " +

			"WHERE {" +
			"?uri raul:value ?value." +
			"?uri rdf:type ?textbox." +
			"OPTIONAL {?uri rdfs:range ?range.}" +
			"OPTIONAL { ?uri rdfs:comment ?type.} " +
			"FILTER (?textbox = raul:Textbox )" +

			"}";
	query = _con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
} catch (MalformedQueryException e1) {
	//e1.printStackTrace();
	System.out.println(e1);
} catch (RepositoryException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

TupleQueryResult resultSet;

	resultSet = query.evaluate();
	
int count = 0;
while(resultSet.hasNext()) {

	RDFBean bean = new RDFBean();
	
	BindingSet results = resultSet.next();
	
	String label="", range =" ", type = "" , refiedInfo="";
	String[] refiedParts={" "," "," "};
	if(results.hasBinding("value")){
	label = results.getValue("value").toString();
	System.out.println(label);
	System.out.println(  " *   * * * * * * * * * * * ** * * *  * * **		"  ) ;
	refiedInfo = getRefiedTriples(label.replaceAll("\"", ""));
	System.out.println(refiedInfo);
	refiedParts = refiedInfo.split("\\|");
	}
	
	if(results.hasBinding("range")){
		range = results.getValue("range").toString();}
	if(results.hasBinding("type")){
		type = results.getValue("type").toString();}
	
	System.out.println(  label +"	" + range + "	"+ type) ;
	count++;
	

	bean.setType(type);
	bean.setRange(range);
	bean.setSubject(refiedParts[0]);
	bean.setProperty(refiedParts[1]);
	bean.setObject(refiedParts[2]);
	rdfList.add(bean);
}


return rdfList;

}


public String getRefiedTriples(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

try {	

String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
		"PREFIX raul:<http://purl.org/NET/raul#> " +
		
		"SELECT DISTINCT ?subject ?property ?object " +

		"WHERE {" +
			"<"+URI+"> rdf:subject ?subject." +
			"<"+URI+"> rdf:predicate ?property." +
			"<"+URI+"> rdf:object ?object." +
		"}";
query = _con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
} catch (MalformedQueryException e1) {
//e1.printStackTrace();
System.out.println(e1);
}

TupleQueryResult resultSet = query.evaluate();

int count = 0;
String reifiedInfo=" ";
while(resultSet.hasNext()) {

BindingSet results = resultSet.next();

String subject = results.getValue("subject").stringValue();
String property = results.getValue("property").stringValue();
String object=" ";
if(results.hasBinding("object")) {
object = results.getValue("object").stringValue();}

reifiedInfo =  subject + "|" + property +	"|" + object  ;

count++;
}

return reifiedInfo;
}*/



/* TupleQuery tupleQuery =_con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
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

*/


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