package ie.deri.raul.persistence;

import ie.deri.raul.resources.RaULResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.sun.jersey.api.container.MappableContainerException;

public class RDFRepository {

	static RDFFormat NTRIPLES = RDFFormat.NTRIPLES;
	static RDFFormat N3 = RDFFormat.N3;
	static RDFFormat RDFXML = RDFFormat.RDFXML;
	public static String RDFTYPE = RDF.TYPE.toString();

	private Repository _repository = null;
	private static Log _logger = LogFactory.getLog(RaULResource.class);

	public RDFRepository(Repository repository) {		
		_repository = repository;
	}

	/**
	 * Creates a new {@link ObjectConnection} to work with Alibaba. 
	 * @return The newly created {@link ObjectConnection}.
	 * @throws RepositoryConfigException
	 * @throws RepositoryException
	 */
	/*public ObjectConnection createObjectConnection()
			throws RepositoryConfigException, RepositoryException, MappableContainerException {
		ObjectRepository objectrepository = new ObjectRepositoryFactory().createRepository(_repository);
		ObjectConnection con = objectrepository.getConnection();		
		return con;
	}*/

	
	public RepositoryConnection createConnection()
			throws RepositoryConfigException, RepositoryException, MappableContainerException {
		//ObjectRepository objectrepository = new ObjectRepositoryFactory().createRepository(_repository);
		RepositoryConnection con = _repository.getConnection();		
		return con;
	}
	/**
	 * Literal factory
	 * 
	 * @param s the literal value
	 * @param typeuri uri representing the type (generally xsd)
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s, URI typeuri) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				if (typeuri == null) {
					return vf.createLiteral(s);
				} else {
					return vf.createLiteral(s, typeuri);
				}
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Untyped Literal factory
	 * 
	 * @param s the literal
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s) {
		return Literal(s, null);
	}

	/**
	 * URIref factory
	 * 
	 * @param uri
	 * @return
	 */
	public URI URIref(String uri) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createURI(uri);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * BNode factory
	 * 
	 * @return
	 */
	public BNode bnode() {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createBNode();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Insert Triple/Statement into graph
	 * 
	 * @param s subject uriref
	 * @param p predicate uriref
	 * @param o value object (URIref or Literal)
	 */
	public void add(URI s, URI p, Value o) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				ValueFactory myFactory = con.getValueFactory();
				Statement st = myFactory.createStatement((Resource) s, p,
						(Value) o);
				con.add(st);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			// handle exception
		}
	}

	/**
	 * Import RDF data from a string
	 * 
	 * @param rdfstring string with RDF data
	 * @param format RDF format of the string (used to select parser)
	 * @throws RepositoryException 
	 * @throws RepositoryException 
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	public void addString(String rdfstring, String baseURI, RDFFormat format, String context) throws RepositoryException, RDFParseException, IOException {
			RepositoryConnection con = _repository.getConnection();
			try {
				StringReader sr = new StringReader(rdfstring);
				con.add(sr, baseURI, format, URIref(context));
			} finally {
				con.close();
			}
	}
	

	public void deleteContextAndTriples(String context) throws RepositoryException {
			RepositoryConnection con = _repository.getConnection();
			try {
				con.clear(URIref(context));
			} finally {
				con.close();
			}
	}

	/**
	 * Import RDF data from an other RDF graph
	 * 
	 * @param RDF repository
	 */
	public void addGraph(RDFRepository _tempGraph) {
		try {
			RepositoryConnection _tempCon = _tempGraph.createConnection();
			RepositoryConnection con = _repository.getConnection();
			RepositoryResult<Statement> statements = _tempCon.getStatements(null, null, null, true, (Resource)null);
			try {
				while(statements.hasNext()){
					Statement st = statements.next();
					con.add(st, (Resource)null);
				}
			} finally {
				con.close();_tempCon.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Import RDF data from a file
	 * 
	 * @param location of file (/path/file) with RDF data
	 * @param format RDF format of the string (used to select parser)
	 */
	public void addFile(String filepath, RDFFormat format) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				con.add(new File(filepath), "", format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import data from URI source Request is made with proper HTTP ACCEPT
	 * header and will follow redirects for proper LOD source negotiation
	 * 
	 * @param urlstring absolute URI of the data source
	 * @param format RDF format to request/parse from data source
	 */
	public void addURI(String urlstring, RDFFormat format) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				URL url = new URL(urlstring);
				URLConnection uricon = (URLConnection) url.openConnection();
				uricon.addRequestProperty("accept", format.getDefaultMIMEType());
				InputStream instream = uricon.getInputStream();
				con.add(instream, urlstring, format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * dump RDF graph
	 * 
	 * @param out output stream for the serialization
	 * @param outform the RDF serialization format for the dump
	 * @return
	 */
	public void dumpRDF(OutputStream out, RDFFormat outform) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				RDFWriter w = Rio.createWriter(outform, out);
				con.export(w);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//public void dumpRDF(RDFWriter w) {	//added by pcc 1, Dec 11
	public void dumpRDF(RDFWriter w, String context) {	//added by pcc 9, Jan 12
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				if(context.equals(""))
					con.export(w);
				else
					con.export(w, URIref(context));
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clearRepository() throws RepositoryException{
		RepositoryConnection con = _repository.getConnection();
		con.clear();
	}
	
	public Set<String> listContexts() throws RepositoryException{
		Set<String> set = new HashSet<String>();
        
		RepositoryConnection con = _repository.getConnection();
		Resource tmpContextID;
		RepositoryResult<Resource> 	_repositoryContexts = con.getContextIDs();
		while (_repositoryContexts.hasNext()) {
			tmpContextID = _repositoryContexts.next();
			set.add(tmpContextID.toString());
		}
		return set;
		
	}
	

	/**
	 * Convenience URI import for RDF/XML sources
	 * 
	 * @param urlstring absolute URI of the data source
	 */
	public void addURI(String urlstring) {
		addURI(urlstring, RDFFormat.RDFXML);
	}

	/**
	 * Tuple pattern query - find all statements with the pattern, where null is
	 * a wildcard
	 * 
	 * @param s subject (null for wildcard)
	 * @param p predicate (null for wildcard)
	 * @param o object (null for wildcard)
	 * @return serialized graph of results
	 */
	public List tuplePattern(URI s, URI p, Value o) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				RepositoryResult repres = con.getStatements(s, p, o, true);
				ArrayList reslist = new ArrayList();
				while (repres.hasNext()) {
					reslist.add(repres.next());
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute a CONSTRUCT/DESCRIBE SPARQL query against the graph
	 * 
	 * @param qs CONSTRUCT or DESCRIBE SPARQL query
	 * @param format the serialization format for the returned graph
	 * @return serialized graph of results
	 */
	public String runSPARQL(String qs, RDFFormat format) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				GraphQuery query = con.prepareGraphQuery(
						org.openrdf.query.QueryLanguage.SPARQL, qs);
				StringWriter stringout = new StringWriter();
				RDFWriter w = Rio.createWriter(format, stringout);
				query.evaluate(w);
				return stringout.toString();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute a SELECT SPARQL query against the graph
	 * 
	 * @param qs SELECT SPARQL query
	 * @return list of solutions, each containing a hashmap of bindings
	 */
	public List<String> runSPARQL(String qs) {
		try {
			RepositoryConnection con = _repository.getConnection();
			try {
				TupleQuery query = con.prepareTupleQuery(
						org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult qres = query.evaluate();
				ArrayList reslist = new ArrayList();
				while (qres.hasNext()) {
					BindingSet b = qres.next();
					Set names = b.getBindingNames();
					HashMap hm = new HashMap();
					for (Object n : names) {
						hm.put((String) n, b.getValue((String) n));
					}
					reslist.add(hm);
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public RepositoryResult<Statement> getStatements(URI uri, boolean includeInferred) throws RepositoryException {
		return _repository.getConnection().getStatements(uri, null, null, includeInferred);
	}

}

