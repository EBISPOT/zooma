package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.spi.Spi;

/**
 * A utility class that is capable of converting ZOOMA model objects into lightweight HTML views.  Renderers are
 * discoverable via a {@link java.util.ServiceLoader}, as they implement the service provider specification.
 *
 * @param <O> the type of object that can be rendered
 * @author Tony Burdett
 * @date 10/04/12
 */
@Spi
public interface HtmlRenderer<O> {
    /**
     * Returns the name of the view that is generated for this renderer.  This should be something human readable and
     * user friendly so environments can offer a sensible choice of renderings to users.
     *
     * @return the name of this renderer
     */
    String getName();

    /**
     * A short description of the rendering this renderer provides.  For example, "Freebase compatible views of
     * Annotations" would be a good description.
     *
     * @return the description of the rendering to provide user feeback
     */
    String getDescription();

    /**
     * Returns true if this renderer is capable of rendering the supplied object, false otherwise.  This is a matching
     * method that allows clients to check whether discovered renderers are capable of rendering known objects without
     * a-priori knowledge of the type of this renderer.
     *
     * @param o the object to check for rendering capability
     * @return true if this renderer can return an HTML view of this entity
     */
    boolean canRender(Object o);

    /**
     * Returns the class of entities that this renderer can render
     *
     * @return the class representing those entities that this renderer can render
     */
    Class<O> getRenderingType();

    /**
     * Renders an HTML view of the supplied entity.  The returned HTML can contain references to any desired CSS
     * classes, but whether the desired CSS is actually loaded is dependent on the environment in which the HTML is
     * ultimately drawn - this class makes no guarantees about this environment.
     * <p/>
     * The resulting HTML will be rendered as part of a larger document: the outer element for the response should be a
     * <code>&gt;div /&lt;</code> element
     *
     * @param entity the entity to render
     * @return a well formed HTML string that can be incorporated into an HTML document.
     */
    String renderHTML(O entity);
}
