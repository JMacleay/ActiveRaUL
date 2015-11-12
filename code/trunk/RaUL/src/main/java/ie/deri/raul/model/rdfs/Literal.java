package ie.deri.raul.model.rdfs;

public class Literal {

	private String value;
	
	public Literal(String value) {
		this.value = value.replaceAll("\"", ""); }

	public static Literal valueOf(String value) {
		return new Literal(value); }


	public String toString() {
		return value.replaceAll("\"", ""); }

	public int hashCode() {
		return value.hashCode(); }

	public boolean equals(Object o) {
		return getClass().equals(o.getClass()) && toString().equals(o.toString().replaceAll("\"", ""));
	}
}
