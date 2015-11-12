package ie.deri.raul.model.owl;

import java.lang.Class;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openrdf.repository.object.annotations.iri;

@iri("http://www.w3.org/2002/07/owl#unionOf")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE})
public @interface IUnionOf {
	Class[] value();

}
