package uk.ac.ebi.fgpt.zooma.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks an interface as being an Service Provider Interface
 * (SPI).
 *
 * @author Tony Burdett
 * @author Matthew Pocock
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Spi {
}
