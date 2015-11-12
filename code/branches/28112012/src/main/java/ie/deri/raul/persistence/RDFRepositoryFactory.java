package ie.deri.raul.persistence;

import ie.deri.raul.RaULProperties;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public abstract class RDFRepositoryFactory {

	private static RaULProperties _properties = RaULProperties.getProperties();
			
	/**
	 * Creates a {@link RDFRepository} by with the repository name specified in the raul.properties file.
	 * @return The newly created {@link RDFRepository}.
	 * @throws RepositoryException If an error occurs while creating the repository.
	 */
	public static RDFRepository createRepository() throws RepositoryException {
		String repository = _properties.getProperty("default.repository");
		if (repository == null || "".equals(repository)) {
			throw new RepositoryException("Property 'default.repository' undefined.");
		}
		return createRepository(repository);
	}
	
	public static RDFRepository createRepository(String repositoryId) throws RepositoryException {
		String repoType = _properties.getProperty("repository.type");
		
		if (repoType != null && "remote".equalsIgnoreCase(repoType)) {			
			String url = _properties.getProperty("sesame.url");
			if (url == null || "".equals(url)) {
				throw new RepositoryException("Please specify remote repository location in property 'repository.url'.");
			}						
			HTTPRepository repository = new HTTPRepository(url, repositoryId);
	    	repository.initialize();
	    	repository.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.SPARQL);
	    	return new RDFRepository(repository);
			
		} else if (repoType != null && "inmemory".equalsIgnoreCase(repoType)) {
			// we assume we always want inferencing - TODO put inferencing boolean flag in config
			boolean inferencing = true;
			try {
				Repository repository = null;
				if (inferencing) {
					repository = new SailRepository(
							new ForwardChainingRDFSInferencer(new MemoryStore()));

				} else {
					repository = new SailRepository(new MemoryStore());
				}
				repository.initialize();
				return new RDFRepository(repository);
			} catch (RepositoryException e) {
				throw new RepositoryException("Error while creating an in-memory SailRepository.");
			}		
		}
		
		// default action if no valid property found
		throw new RepositoryException("No valid property 'repository.type'. Please specify either 'remote' or 'inmemory'.");						
	}
	
	public static RDFRepository createInMemoryRepository() throws RepositoryException {		
		boolean inferencing = true;
		try {
			Repository repository = null;
			if (inferencing) {
				repository = new SailRepository(
						new ForwardChainingRDFSInferencer(new MemoryStore()));
			} else {
				repository = new SailRepository(new MemoryStore());
			}
			repository.initialize();
			return new RDFRepository(repository);
		} catch (RepositoryException e) {
			throw new RepositoryException("Error while creating an in-memory SailRepository.");
		}						
	}
	
}
