package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.service.AnnotationLoadingService;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.service.AnnotationSavingService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * The {@link LoadAndSaveAnnotationsService} will use the {@link TaskExecutor} to initiate a thread for each {@link AnnotationLoadingService} in the loaders collection.
 * The loaders collection is a set of loader beans that are created on runtime and are autowired by type.
 * The {@link AnnotationLoadingService}s load() method will be called and then each loaded annotation will be saved via the {@link SaveAnnotationToMongo}
 *
 * Created by olgavrou on 10/08/2016.
 */
@Service
public class LoadAndSaveAnnotationsService {

    @Autowired
    private Collection<AnnotationLoadingService<Annotation>> loaders;

    @Autowired
    AnnotationSavingService saveToDb;

    @Autowired
    private TaskExecutor taskExecutor;

    public void init(){
        for (AnnotationLoadingService annotationLoadingService : loaders){
            this.execute(annotationLoadingService);
        }
    }

    public void execute(AnnotationLoadingService annotationLoadingService){
        this.taskExecutor.execute(new LoadDatasource(annotationLoadingService));

    }

    private class LoadDatasource implements Runnable{
        private AnnotationLoadingService annotationLoadingService;

        public LoadDatasource(AnnotationLoadingService annotationLoadingService){
            this.annotationLoadingService = annotationLoadingService;
        }

        @Override
        public void run() {
            try{
                this.annotationLoadingService.load();
                List<Annotation> annotations = this.annotationLoadingService.returnLoaded();
                for (Annotation annotation : annotations){
                    saveToDb.save(annotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
