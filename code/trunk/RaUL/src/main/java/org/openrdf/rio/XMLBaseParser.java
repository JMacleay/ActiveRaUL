package org.openrdf.rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLBaseParser {

	public static String XML_BASE="base";
	
	private XMLStreamReader reader;
	
	public XMLBaseParser(Reader reader){
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			this.reader=factory.createXMLStreamReader(reader);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public String getXMLBase(){
		try {
                    String firstAbout="";
			for(;reader.hasNext();){
				if(reader.getEventType()==XMLStreamReader.START_ELEMENT){
					int count=reader.getAttributeCount();
					for(int i=0;i<count;i++){
                                                String attributeName=reader.getAttributeLocalName(i);
						if(attributeName.toLowerCase().equals(XML_BASE)){
							return reader.getAttributeValue(i);
						}
                                                if("about".equals(attributeName)&&"".equals(firstAbout)){
                                                    firstAbout=reader.getAttributeValue(i);
                                                }
                                        }
				}
				reader.next();
			}
                        return firstAbout;
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}finally{
			try {
				reader.close();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		String filePath = "/home/u5096831/PhDResearch/FOAF/foaf.rdf";
		File file = new File(filePath);
		XMLBaseParser parser=new XMLBaseParser(new InputStreamReader(new FileInputStream(file)));
		System.out.println(parser.getXMLBase());
	}
	
}
