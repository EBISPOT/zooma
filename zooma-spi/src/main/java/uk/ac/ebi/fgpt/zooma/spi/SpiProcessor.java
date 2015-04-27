package uk.ac.ebi.fgpt.zooma.spi;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Process Spi and SpiProcessor annotations, generating appropriate files in
 * META-INF/services.
 *
 * @author Tony Burdett
 * @author Matthew Pocock
 */
public class SpiProcessor extends AbstractProcessor {
  private volatile TypeElement typeElement;

  /**
   * Process @ServiceProvider annotations.  This will generate appropriate
   * entries in the META-INF/services file, making classes discoverable at
   * runtime for anything handling these annotations.
   */
  public boolean process(Set<? extends TypeElement> typeElements,
                         RoundEnvironment roundEnv) {
    processingEnv.getMessager().printMessage(
        Diagnostic.Kind.NOTE, "There are " +
            roundEnv.getElementsAnnotatedWith(Spi.class).size() +
            " Spi interfaces and " +
            roundEnv.getElementsAnnotatedWith(ServiceProvider.class).size() +
            " ServiceProvider implementations");

    // setup output options
    Map<String, PrintWriter> outputs = new HashMap<>();

    // check all elements annotated with @ServiceProvider
    for (Element element :
        roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
      ElementVisitor<Void, Void> visitor =
          new SimpleElementVisitor6<Void, Void>() {
            public Void visitType(TypeElement e, Void aVoid) {
              typeElement = e;
              return super.visitType(e, aVoid);
            }
          };
      element.accept(visitor, null);

      // visiting this type - work out the superclass (i.e. the @Spi)
      Name result = findSpiAnnotation(typeElement);
      if (result != null) {
        Name spiName = findSpiAnnotation(typeElement);
        if (spiName == null) {
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.NOTE,
              "Type annotated with @ServiceProvider but this type does not " +
                  "implement an SPI");
        }
        else {
          // found the SPI, insert the service file
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.NOTE,
              "Generating service entry for " + typeElement.getQualifiedName() +
                  " for service " + spiName);

          String spiNameStr = spiName.toString();
          PrintWriter pw = outputs.get(spiNameStr);
          // lazily instantiate pw if it is null
          if (pw == null) {
            try {
              processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.NOTE,
                  "Generating service file for " + spiName);

              FileObject fo = processingEnv.getFiler().createResource(
                  StandardLocation.CLASS_OUTPUT,
                  "",
                  "META-INF/services/" + spiName);

              pw = new PrintWriter(fo.openWriter());
              outputs.put(spiNameStr, pw);
            }
            catch (IOException e) {
              e.printStackTrace();
              processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Problem creating services file for " + spiName);
            }
          }

          // pw should now have defo be non-null, unless something went wrong
          if (pw != null) {
            pw.println(typeElement.getQualifiedName());
          }
        }
      }
      else {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            typeElement.getQualifiedName() + ": Classes annotated with " + ServiceProvider.class.getName() + " " +
                "MUST implement an interface marked with an " +
                Spi.class.getName() + " annotation");
        return false;
      }
    }

    // done all elements, so make sure all writers are closed
    for (PrintWriter pw : outputs.values()) {
      pw.close();
    }

    return true;
  }

  public Set<String> getSupportedAnnotationTypes() {
    Set<String> result = new HashSet<>();
    result.add(Spi.class.getName());
    result.add(ServiceProvider.class.getName());
    return result;
  }

  public Set<String> getSupportedOptions() {
    // no supported options
    return Collections.emptySet();
  }

  private Name findSpiAnnotation(TypeElement element) {
    // elements should be @ServiceProviders
    if (element.getKind().isClass()) {
      // look for interfaces implemented by this class
      for (TypeMirror mirror : element.getInterfaces()) {
        if (mirror instanceof DeclaredType) {
          Element superElement = ((DeclaredType) mirror).asElement();
          if (superElement instanceof TypeElement) {
            // recurse - normally these checks would all pass
            return findSpiAnnotation((TypeElement) superElement);
          }
        }
      }

      // if we haven't found an SpiAnnotation, look for superclasses
      TypeMirror mirror = element.getSuperclass();
      if (mirror instanceof DeclaredType) {
        Element superElement = ((DeclaredType) mirror).asElement();
        if (superElement instanceof TypeElement) {
          // recurse - normally these checks would all pass
          return findSpiAnnotation((TypeElement) superElement);
        }
      }
    }
    else {
      // is the type declaration an SPI?
      Spi spi = element.getAnnotation(Spi.class);
      if (spi != null) {
        return element.getQualifiedName();
      }
    }
    return null;
  }
}
