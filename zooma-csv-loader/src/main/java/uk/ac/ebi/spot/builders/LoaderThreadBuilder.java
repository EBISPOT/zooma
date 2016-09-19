package uk.ac.ebi.spot.builders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.datasource.LoaderDAO;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.controllers.SaveToMongoController;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * The {@link LoaderThreadBuilder} will use the {@link TaskExecutor} to initiate a thread for each {@link LoaderDAO} in the loaders collection.
 * The loaders collection is a set of loader beans that are created on runtime and are autowired by type.
 * The {@link LoaderDAO}s load() method will be called and then each loaded annotation will be saved via the {@link SaveToMongoController}
 *
 * Created by olgavrou on 10/08/2016.
 */
@Component
public class LoaderThreadBuilder {

    @Autowired
    private Collection<LoaderDAO> loaders;

    @Autowired
    SaveToMongoController saveToMongoController;

    @Autowired
    private TaskExecutor taskExecutor;

    public void init(){
        for (LoaderDAO loaderDAO : loaders){
            this.execute(loaderDAO);
        }
    }

    public void execute(LoaderDAO loaderDAO){
        this.taskExecutor.execute(new LoadDatasource(loaderDAO));

    }

    private class LoadDatasource implements Runnable{
        private LoaderDAO loaderDAO;

        public LoadDatasource(LoaderDAO loaderDAO){
            this.loaderDAO = loaderDAO;
        }

        @Override
        public void run() {
            try{
                this.loaderDAO.load();
                List<Annotation> annotations = this.loaderDAO.returnLoaded();
                for (Annotation annotation : annotations){
                    saveToMongoController.save(annotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
