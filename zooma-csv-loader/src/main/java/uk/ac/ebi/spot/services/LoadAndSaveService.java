package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.service.LoadService;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.service.SaveService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * The {@link LoadAndSaveService} will use the {@link TaskExecutor} to initiate a thread for each {@link LoadService} in the loaders collection.
 * The loaders collection is a set of loader beans that are created on runtime and are autowired by type.
 * The {@link LoadService}s load() method will be called and then each loaded annotation will be saved via the {@link Save2Mongo}
 *
 * Created by olgavrou on 10/08/2016.
 */
@Service
public class LoadAndSaveService {

    @Autowired
    private Collection<LoadService<Annotation>> loaders;

    @Autowired
    SaveService saveToDb;

    @Autowired
    private TaskExecutor taskExecutor;

    public void init(){
        for (LoadService loadService : loaders){
            this.execute(loadService);
        }
    }

    public void execute(LoadService loadService){
        this.taskExecutor.execute(new LoadDatasource(loadService));

    }

    private class LoadDatasource implements Runnable{
        private LoadService loadService;

        public LoadDatasource(LoadService loadService){
            this.loadService = loadService;
        }

        @Override
        public void run() {
            try{
                this.loadService.load();
                List<Annotation> annotations = this.loadService.returnLoaded();
                for (Annotation annotation : annotations){
                    saveToDb.save(annotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
