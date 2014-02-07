package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.AmbiguousResourceException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSourceService;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;

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


    public AnnotationSourceService getAnnotationSourceService() {
        return annotationSourceService;
    }

    @Autowired
    public void setAnnotationSourceService(AnnotationSourceService annotationSourceService) {
        this.annotationSourceService = annotationSourceService;
    }

    public DataLoadingService<Annotation> getDataLoadingService() {
        return dataLoadingService;
    }

    @Autowired
    public void setDataLoadingService(DataLoadingService<Annotation> dataLoadingService) {
        this.dataLoadingService = dataLoadingService;
    }

    @RequestMapping(value = "/sources", method = RequestMethod.GET)
    public @ResponseBody Collection<AnnotationSource> getDatasources() {
        return getAnnotationSourceService().getAnnotationSources();
    }

    @RequestMapping(value = "/loaders", method = RequestMethod.GET)
    public @ResponseBody Collection<String> getLoaders() {
        Collection<String> results = new ArrayList<>();
        Collection<ZoomaDAO<Annotation>> datasources = getDataLoadingService().getAvailableDatasources();
        for (ZoomaDAO dao : datasources) {
            results.add(dao.getDatasourceName());
        }
        return results;
    }

    @RequestMapping(value = "/loaders/{loaderName}", method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasource(@PathVariable String loaderName) {
        ZoomaDAO<Annotation> loaderDAO = lookupLoader(loaderName);
        if (loaderDAO != null) {
            getLog().debug("Loading " + loaderDAO.count() + " data items from " + loaderDAO.getDatasourceName());
            DataLoadingService.Receipt receipt = getDataLoadingService().load(loaderDAO);
            getLog().debug("Load request sent, received receipt ID " + receipt.getID());
            return receipt;
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + loaderName + "' exists");
        }
    }

    @RequestMapping(value = "/loaders/{loaderName}/study/{studyFilter}", method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasourceWithStudyFilter(
            @PathVariable String loaderName,
            @PathVariable String studyFilter) {
        ZoomaDAO<Annotation> loaderDAO = lookupLoader(loaderName);
        if (loaderDAO != null) {
            if (loaderDAO instanceof AnnotationDAO) {
                Study study = new SimpleStudy(null, studyFilter);
                getLog().debug("Fetching data from datasource '" + loaderName + "', study '" + study + "'");
                Collection<Annotation> annotationsToLoad = ((AnnotationDAO) loaderDAO).readByStudy(study);
                getLog().debug("Loading " + annotationsToLoad.size() + " data items from " +
                                       loaderDAO.getDatasourceName());
                String datasetName = loaderName.concat(".").concat(studyFilter);
                DataLoadingService.Receipt receipt = getDataLoadingService().load(annotationsToLoad, datasetName);
                getLog().debug("Load request sent, received receipt ID " + receipt.getID());
                return receipt;
            }
            else {
                throw new UnsupportedOperationException(
                        "The datasource '" + loaderName + "' does not support study filtering");
            }
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + loaderName + "' exists");
        }
    }

    @RequestMapping(value = "/loaders/{loaderName}/study/{studyFilter}/biologicalEntity/{bioentityFilter}",
                    method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasourceWithStudyAndBioentityFilter(
            @PathVariable String loaderName,
            @PathVariable String studyFilter,
            @PathVariable String bioentityFilter) {
        ZoomaDAO<Annotation> loaderDAO = lookupLoader(loaderName);
        if (loaderDAO != null) {
            if (loaderDAO instanceof AnnotationDAO) {
                Study study = new SimpleStudy(null, studyFilter);
                BiologicalEntity be = new SimpleBiologicalEntity(null, bioentityFilter, study);
                getLog().debug("Fetching data from datasource '" + loaderName + "', " +
                                       "study '" + study + "', bioentity '" + be + "'");
                Collection<Annotation> annotationsToLoad = ((AnnotationDAO) loaderDAO).readByBiologicalEntity(be);
                getLog().debug("Loading " + annotationsToLoad + " data items from " + loaderDAO.getDatasourceName());
                String datasetName = loaderName.concat(".").concat(studyFilter).concat(".").concat(bioentityFilter);
                DataLoadingService.Receipt receipt = getDataLoadingService().load(annotationsToLoad, datasetName);
                getLog().debug("Load request sent, received receipt ID " + receipt.getID());
                return receipt;
            }
            else {
                throw new UnsupportedOperationException(
                        "The datasource '" + loaderName + "' does not support study filtering");
            }
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + loaderName + "' exists");
        }
    }

    @RequestMapping(value = "/receipts/{receiptID}", method = RequestMethod.GET)
    public @ResponseBody DataLoadingService.ReceiptStatus getReceiptStatus(@PathVariable String receiptID) {
        // note that this exposes all receipts from this data loading service - ZoomaAnnotationLoaders also uses this class
        return getDataLoadingService().getReceiptStatus(receiptID);
    }

    private ZoomaDAO<Annotation> lookupLoader(String loaderName) {
        ZoomaDAO<Annotation> result = null;
        for (ZoomaDAO<Annotation> dao : getDataLoadingService().getAvailableDatasources()) {
            if (dao.getDatasourceName().equals(loaderName)) {
                if (result == null) {
                    result = dao;
                }
                else {
                    throw new AmbiguousResourceException(
                            "Could not uniquely identify datasource to load from: " +
                                    "more than one datasource with name  '" + loaderName + "' exists");
                }
            }
        }
        return result;
    }
}
