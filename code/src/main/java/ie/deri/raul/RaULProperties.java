package ie.deri.raul;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton implementation for getting the properties.
 */
public class RaULProperties {
	
	private static final String PROPERTIES_NAME = "raul.properties";
	
	private static Log _log = LogFactory.getLog(RaULProperties.class);	
	private static Properties _properties = new Properties();
	
	static {
		try {
			_log.info(String.format("Loading '%s'", PROPERTIES_NAME));
			_properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_NAME));
		} catch (IOException e) {
			final String msg = String.format("Could not load '%s'.", PROPERTIES_NAME);
			_log.fatal(msg,	e);
			new RuntimeException(msg, e);
		}
	}
	
	private static RaULProperties _raulProperties = null;
	
	public synchronized static RaULProperties getProperties() {
		if (_raulProperties == null) 
			_raulProperties = new RaULProperties();
		return _raulProperties;
	}
	
	private RaULProperties() {	}
	
	public void clear() {
		_properties.clear();
	}

	public Object clone() {
		return _properties.clone();
	}

	public boolean contains(Object value) {
		return _properties.contains(value);
	}

	public boolean containsKey(Object key) {
		return _properties.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return _properties.containsValue(value);
	}

	public Enumeration<Object> elements() {
		return _properties.elements();
	}

	public Set<Entry<Object, Object>> entrySet() {
		return _properties.entrySet();
	}

	public boolean equals(Object o) {
		return _properties.equals(o);
	}

	public Object get(Object key) {
		return _properties.get(key);
	}

	public String getProperty(String key, String defaultValue) {
		return _properties.getProperty(key, defaultValue);
	}

	public String getProperty(String key) {
		return _properties.getProperty(key);
	}

	public int hashCode() {
		return _properties.hashCode();
	}

	public boolean isEmpty() {
		return _properties.isEmpty();
	}

	public Enumeration<Object> keys() {
		return _properties.keys();
	}

	public Set<Object> keySet() {
		return _properties.keySet();
	}

	public void list(PrintStream out) {
		_properties.list(out);
	}

	public void list(PrintWriter out) {
		_properties.list(out);
	}

	public void load(InputStream inStream) throws IOException {
		_properties.load(inStream);
	}

	public void load(Reader reader) throws IOException {
		_properties.load(reader);
	}

	public void loadFromXML(InputStream in) throws IOException,
			InvalidPropertiesFormatException {
		_properties.loadFromXML(in);
	}

	public Enumeration<?> propertyNames() {
		return _properties.propertyNames();
	}

	public Object put(Object key, Object value) {
		return _properties.put(key, value);
	}

	public void putAll(Map<? extends Object, ? extends Object> t) {
		_properties.putAll(t);
	}

	public Object remove(Object key) {
		return _properties.remove(key);
	}

	public void save(OutputStream out, String comments) {
		_properties.save(out, comments);
	}

	public Object setProperty(String key, String value) {
		return _properties.setProperty(key, value);
	}

	public int size() {
		return _properties.size();
	}

	public void store(OutputStream out, String comments) throws IOException {
		_properties.store(out, comments);
	}

	public void store(Writer writer, String comments) throws IOException {
		_properties.store(writer, comments);
	}

	public void storeToXML(OutputStream os, String comment, String encoding)
			throws IOException {
		_properties.storeToXML(os, comment, encoding);
	}

	public void storeToXML(OutputStream os, String comment) throws IOException {
		_properties.storeToXML(os, comment);
	}

	public Set<String> stringPropertyNames() {
		return _properties.stringPropertyNames();
	}

	public String toString() {
		return _properties.toString();
	}

	public Collection<Object> values() {
		return _properties.values();
	}
	
	
	
	
}
