package org.openrdf.rio;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class HTMLInterceptor {

	private final static String HTML_ATTR_TYPEOF = "typeof";

	private final static String HTML_ATTR_ABOUT = "about";

	private final static String HTML_ELE_FIELDSET = "fieldset";

	private final static String FIELDSET_ATTR_COPIED = "copied";

	private XMLStreamReader reader;

	private XMLStreamWriter writer;

	private StringReader strReader;

	private StringWriter strWriter;

	public HTMLInterceptor(String htmlContent) {
		strReader = new StringReader(htmlContent);
		strWriter = new StringWriter();
		try {
			reader = XMLInputFactory.newInstance().createXMLStreamReader(
					strReader);
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
					strWriter);
			writer.setDefaultNamespace("http://www.w3.org/1999/xhtml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFieldSetContent() {
		String about = "";
		try {
			writer.writeStartElement("div");
			writer.writeAttribute("style", "display:none");
			writer.writeAttribute("id", "templates");
			boolean allowWriting = false;
			int fieldSetCount = 0;
			for (; reader.hasNext();) {
				String eleName = null;
				if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
					eleName = reader.getLocalName();
					String typeof = reader.getAttributeValue("",
							HTML_ATTR_TYPEOF);
					String copied = reader.getAttributeValue("",
							FIELDSET_ATTR_COPIED);
					if (typeof != null
							&& "raul:DynamicGroup".toLowerCase().equals(
									typeof.toLowerCase())) {
						about = reader.getAttributeValue("", HTML_ATTR_ABOUT);
					}
					if (HTML_ELE_FIELDSET.equalsIgnoreCase(eleName)
							&& "copied".equals(copied)) {
						allowWriting = true;
						writeXml(eleName, about);
						reader.next();
						fieldSetCount = 1;
						continue;
					}
					if (HTML_ELE_FIELDSET.equalsIgnoreCase(eleName)
							&& allowWriting == true) {
						fieldSetCount++;
					}
				}
				if (reader.getEventType() == XMLStreamReader.END_ELEMENT) {
					eleName = reader.getLocalName();
					if (eleName.equalsIgnoreCase(HTML_ELE_FIELDSET)) {
						if (fieldSetCount == 1) {
							allowWriting = false;
						}
						fieldSetCount--;
						writeXml("", "");
						reader.next();
						continue;
					}
				}
				if (allowWriting == true) {
					if ("fieldset".equalsIgnoreCase(eleName))
						writeXml("", about);
					else
						writeXml("", "");
				}
				reader.next();
			}
			writer.writeEndElement();
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				writer.close();
				strReader.close();
				strWriter.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return strWriter.toString();
	}

	private void writeXml(String eleName, String about) {
		try {
			if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
				if ("".equals(eleName))
					eleName = reader.getLocalName();
				writer.writeStartElement(eleName);
				int count = reader.getAttributeCount();
				for (int i = 0; i < count; i++) {
					writer.writeAttribute(reader.getAttributeLocalName(i),
							reader.getAttributeValue(i));
				}
				if (eleName.equalsIgnoreCase(HTML_ELE_FIELDSET))
					writer.writeAttribute("group", about);
			}
			if (reader.getEventType() == XMLStreamReader.CHARACTERS)
				writer.writeCharacters(reader.getText());
			if (reader.getEventType() == XMLStreamReader.END_ELEMENT)
				writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
