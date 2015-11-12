package org.openrdf.rio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Statement;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;

/**
 * 
 * @author LIU07N temporary class, delete after experiment
 */
public class RaulRDFParser {

	private RDFParser parser;

	private RDFXMLReader handler;
	
	protected static Log log=LogFactory.getLog(RaulRDFParser.class);

	public RaulRDFParser() {
		RDFXMLParserFactory factory = new RDFXMLParserFactory();
		parser = factory.getParser();
		handler = new RDFXMLReader();
		parser.setRDFHandler(handler);
		parser.setStopAtFirstError(true);
	}

	public void parse(String content) {
		try {
			parser.parse(new StringReader(content), "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Boolean isRaulRDF() {
		return handler.isRaulRDF();
	}

	public String getRaulPageID() {
		return handler._pageId;
	}

	/**
	 * @param args
	 * @throws RDFHandlerException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws RDFHandlerException, IOException {
		RaulRDFParser parser=new RaulRDFParser();
		InputStream is=parser.getClass().getResourceAsStream("raul.rdf");
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer=new StringBuffer();
		String str="";
		for(;(str=br.readLine())!=null;){
			buffer.append(str);
		}
		parser.parse(buffer.toString());
		//System.out.println(parser.isRaulRDF());
	}

	private class RDFXMLReader implements RDFHandler {

		private boolean elementsContainRaul = false;

		private boolean namespaceContainRaul = false;

		private String raulNamespace;

		public static final String RAUL_PREFIX = "raul";

		private static final String RAUL_PAGE = "http://purl.org/NET/raul#Page";
		private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

		private String _pageId;

		@Override
		public void endRDF() throws RDFHandlerException {

		}

		@Override
		public void handleComment(String arg0) throws RDFHandlerException {

		}

		@Override
		public void handleNamespace(String prefix, String uri)
				throws RDFHandlerException {
			if (prefix.equals(RAUL_PREFIX)) {
				this.namespaceContainRaul = true;
				this.raulNamespace = uri;
			}
		}

		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			if (this.elementsContainRaul == false && st.getSubject() != null
					&& st.getPredicate().getNamespace().equals(raulNamespace))
				this.elementsContainRaul = true;
			if (RDF_TYPE.equals(st.getPredicate().stringValue())
					&& RAUL_PAGE.equals(st.getObject().stringValue())) {
				_pageId = st.getSubject().stringValue();
			}
		}

		@Override
		public void startRDF() throws RDFHandlerException {

		}

		public boolean isRaulRDF() {
			return namespaceContainRaul && elementsContainRaul;
		}

		public String getPageId() {
			return this._pageId;
		}

	}
}
