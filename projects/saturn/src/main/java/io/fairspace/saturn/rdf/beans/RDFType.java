package io.fairspace.saturn.rdf.beans;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = TYPE)
public @interface RDFType {
    /**
     * @return The rdf:type IRI
     */
    String value();
}
