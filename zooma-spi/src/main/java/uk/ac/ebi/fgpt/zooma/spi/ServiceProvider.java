package uk.ac.ebi.fgpt.zooma.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class as being a provider of an SPI. This may be used to create
 * the services manifest entry.
 *
 * @author Tony Burdett
 * @author Matthew Pocock
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ServiceProvider {
}
