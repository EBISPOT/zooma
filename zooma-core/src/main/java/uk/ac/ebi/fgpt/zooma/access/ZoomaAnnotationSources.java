package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.AmbiguousResourceException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSourceService;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;
import uk.ac.ebi.fgpt.zooma.service.StudyService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Access information about the datasources ZOOMA utilizes.  This is a high level convenience class for accessing the
 * list of datasources that have contributed annotation data into ZOOMA and for loading and reloading data from
 * available datasources.
 * <p/>
 * Note that datasources can be shown but not loaded from in some instances.  ZOOMA can be configured in read-only
 * modes, and in these cases datasources can be shown in the listings but an attempt to reload data from that datasource
 * will fail.
 *
 * @author Tony Burdett
 * @date 27/01/14
 */
@Controller
public class ZoomaAnnotationSources {
    private AnnotationSourceService annotationSourceService;
    private DataLoadingService<Annotation> dataLoadingService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public ZoomaAnnotationSources(AnnotationSourceService annotationSourceService,
                                  DataLoadingService<Annotation> dataLoadingService) {
        this.annotationSourceService = annotationSourceService;
        this.dataLoadingService = dataLoadingService;
    }

    public AnnotationSourceService getAnnotationSourceService() {
        return annotationSourceService;
    }

    public DataLoadingService<Annotation> getDataLoadingService() {
        return dataLoadingService;
    }

    @RequestMapping(value = "/sources", method = RequestMethod.GET)
    public @ResponseBody Collection<AnnotationSource> getDatasources() {
        return getAnnotationSourceService().getAnnotationSources();
    }
}
