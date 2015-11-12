package ie.deri.raul;

// THIS CLASS IS OBSOLETE AND UNUSED (just for testing and demonstration stuff)
//import foaf.Person;
import ie.deri.raul.persistence.RDFRepository;
import ie.deri.raul.persistence.RDFRepositoryFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.openrdf.rio.RDFFormat;

public class Main {

	public static void main(String[] args) throws Exception {
		
		RDFRepository repository = RDFRepositoryFactory.createRepository();		
		repository.addURI("http://richard.cyganiak.de/foaf.rdf");
		
		System.out.println("Dump:");
		repository.dumpRDF(System.out, RDFFormat.N3);		

		
		
		
		ObjectConnection conn = repository.createObjectConnection();
		ValueFactory vf = conn.getValueFactory();
		org.openrdf.model.URI id = vf.createURI("http://richard.cyganiak.de/foaf.rdf#cygri");
		
		System.out.println(conn.getObject(id));

//		Person andreas = conn.getObject(Person.class, id);
//		System.out.println("Andreas: " + andreas.getFoafFirstNames().size());
//		for (Object o : andreas.getFoafSurnames()) {
//			System.out.println(o);
//		}
//				
//		Result<Person> persons = conn.getObjects(Person.class);
//		while (persons.hasNext()) {
//			System.out.println(persons.next());
//		}
//		
//		
		
		
		
//		conn.add(new URL("http://richard.cyganiak.de/foaf.rdf"), "http://richard.cyganiak.de/foaf.rdf", RDFFormat.RDFXML, vf.createURI("<http://richard.cyganiak.de/foaf.rdf>"));
		
//		andreas.setFoafAccounts(null);
//		andreas.setFoafSurnames(null);
//		andreas.setFoafKnows(null);
//		repo.getConnection().removeDesignation(andreas, Person.class);
		
		
		//repo.getConnection().remove(id, vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), vf.createURI("http://xmlns.com/foaf/0.1/Person"), null); 
		
		
//		Person deleted = repo.getConnection().getObject(Person.class, id);		
//		System.out.println("Deleted: " + deleted);
	
	}
}
