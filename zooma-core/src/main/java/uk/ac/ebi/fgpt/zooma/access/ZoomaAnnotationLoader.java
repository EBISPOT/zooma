package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.AmbiguousResourceException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Load data into ZOOMA from available datasources.
 * <p/>
 * This class is a high level convenience implementation for loading data.  It will work readily out of the box, but
 * requires configuration with underlying service implementations.  It is also a controller 'stereotype' that can be
 * used to construct a REST API.
 *
 * @author Tony Burdett
 * @date 12/06/13
 */
@Controller
public class ZoomaAnnotationLoader {
    private DataLoadingService<Annotation> dataLoadingService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public DataLoadingService<Annotation> getDataLoadingService() {
        return dataLoadingService;
    }

    @Autowired
    public void setDataLoadingService(DataLoadingService<Annotation> dataLoadingService) {
        this.dataLoadingService = dataLoadingService;
    }


    @RequestMapping(value = "/datasources", method = RequestMethod.GET)
    public @ResponseBody Collection<String> getAvailableDatasourceNames() {
        Collection<String> results = new ArrayList<>();
        Collection<ZoomaDAO<Annotation>> datasources = getDataLoadingService().getAvailableDatasources();
        for (ZoomaDAO dao : datasources) {
            results.add(dao.getDatasourceName());
        }
        return results;
    }

    @RequestMapping(value = "/datasources/{datasourceName}", method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasource(@PathVariable String datasourceName) {
        ZoomaDAO<Annotation> loaderDAO = lookupDatasource(datasourceName);
        if (loaderDAO != null) {
            getLog().debug("Loading " + loaderDAO.count() + " data items from " + loaderDAO.getDatasourceName());
            DataLoadingService.Receipt receipt = getDataLoadingService().load(loaderDAO);
            getLog().debug("Load request sent, received receipt ID " + receipt.getID());
            return receipt;
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + datasourceName + "' exists");
        }
    }

    @RequestMapping(value = "/{datasourceName}/study/{studyFilter}", method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasourceWithStudyFilter(
            @PathVariable String datasourceName,
            @PathVariable String studyFilter) {
        ZoomaDAO<Annotation> loaderDAO = lookupDatasource(datasourceName);
        if (loaderDAO != null) {
            if (loaderDAO instanceof AnnotationDAO) {
                Study study = new SimpleStudy(null, studyFilter);
                getLog().debug("Fetching data from datasource '" + datasourceName + "', study '" + study + "'");
                Collection<Annotation> annotationsToLoad = ((AnnotationDAO) loaderDAO).readByStudy(study);
                getLog().debug("Loading " + annotationsToLoad.size() + " data items from " +
                        loaderDAO.getDatasourceName());
                String datasetName = datasourceName.concat(".").concat(studyFilter);
                DataLoadingService.Receipt receipt = getDataLoadingService().load(annotationsToLoad, datasetName);
                getLog().debug("Load request sent, received receipt ID " + receipt.getID());
                return receipt;
            }
            else {
                throw new UnsupportedOperationException(
                        "The datasource '" + datasourceName + "' does not support study filtering");
            }
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + datasourceName + "' exists");
        }
    }

    @RequestMapping(value = "/{datasourceName}/study/{studyFilter}/biologicalEntity/{bioentityFilter}",
            method = RequestMethod.PUT)
    public @ResponseBody DataLoadingService.Receipt loadFromDatasourceWithStudyAndBioentityFilter(
            @PathVariable String datasourceName,
            @PathVariable String studyFilter,
            @PathVariable String bioentityFilter) {
        ZoomaDAO<Annotation> loaderDAO = lookupDatasource(datasourceName);
        if (loaderDAO != null) {
            if (loaderDAO instanceof AnnotationDAO) {
                Study study = new SimpleStudy(null, studyFilter);
                BiologicalEntity be = new SimpleBiologicalEntity(null, bioentityFilter, study);
                getLog().debug("Fetching data from datasource '" + datasourceName + "', " +
                        "study '" + study + "', bioentity '" + be + "'");
                Collection<Annotation> annotationsToLoad = ((AnnotationDAO) loaderDAO).readByBiologicalEntity(be);
                getLog().debug("Loading " + annotationsToLoad + " data items from " + loaderDAO.getDatasourceName());
                String datasetName = datasourceName.concat(".").concat(studyFilter).concat(".").concat(bioentityFilter);
                DataLoadingService.Receipt receipt = getDataLoadingService().load(annotationsToLoad, datasetName);
                getLog().debug("Load request sent, received receipt ID " + receipt.getID());
                return receipt;
            }
            else {
                throw new UnsupportedOperationException(
                        "The datasource '" + datasourceName + "' does not support study filtering");
            }
        }
        else {
            throw new NoSuchResourceException(
                    "Could not identify datasource to load from: " +
                            "no datasource with name '" + datasourceName + "' exists");
        }
    }

    private ZoomaDAO<Annotation> lookupDatasource(String datasourceName) {
        ZoomaDAO<Annotation> result = null;
        for (ZoomaDAO<Annotation> dao : getDataLoadingService().getAvailableDatasources()) {
            if (dao.getDatasourceName().equals(datasourceName)) {
                if (result == null) {
                    result = dao;
                }
                else {
                    throw new AmbiguousResourceException(
                            "Could not uniquely identify datasource to load from: " +
                                    "more than one datasource with name  '" + datasourceName + "' exists");
                }
            }
        }
        return result;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AmbiguousResourceException.class)
    private @ResponseBody String handleAmbiguousResourceException(AmbiguousResourceException e) {
        getLog().error("Ambiguous resource reference", e);
        return "Data loading failed - " + e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchResourceException.class)
    private @ResponseBody String handleNoSuchResourceException(NoSuchResourceException e) {
        getLog().error("Tried to access a resource that does not exist", e);
        return "Data loading failed - " + e.getMessage();
    }

    @ExceptionHandler(ZoomaUpdateException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody String handleException(ZoomaUpdateException exception) {
        getLog().error("Failed zooma update", exception);
        return exception.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody String handleException(IllegalArgumentException exception) {
        getLog().error("Caught illegal argument exception", exception);
        return exception.getMessage();
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public @ResponseBody String handleException(UnsupportedOperationException exception) {
        getLog().error("Caught unsupported operation exception", exception);
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    private @ResponseBody String handleException(Exception e) {
        getLog().error("Uncaught exception!", e);
        return "The server encountered an error it could not recover from - " + e.getMessage();
    }
}
