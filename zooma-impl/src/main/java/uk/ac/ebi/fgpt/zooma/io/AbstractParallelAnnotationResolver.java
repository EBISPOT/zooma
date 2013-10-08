package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.concurrent.WorkloadScheduler;
import uk.ac.ebi.fgpt.zooma.concurrent.ZoomaThreadFactory;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaResolutionException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An abstract implementation of an annotation resolver service that can resolve annotations in parallel.  This provides
 * a default implementation of {@link #resolve(String, java.util.Collection)} that delegates the work to
 * {@link #resolve(uk.ac.ebi.fgpt.zooma.model.Annotation)} for each annotation, but performs requests in a
 * parallelized manner using a configurable number of worker threads.
 * <p/>
 * You should always make sure you call {@link #shutdown()} on all classes that extend this implementation once you have
 * finished with it, in order to correctly terminate any worker threads that are running.
 *
 * @author Tony Burdett
 * @date 05/06/13
 */
public abstract class AbstractParallelAnnotationResolver extends AbstractAnnotationResolver {
    private final ExecutorService executor;

    public AbstractParallelAnnotationResolver(int numberOfThreads) {
        this.executor = new ThreadPoolExecutor(1,
                                               numberOfThreads,
                                               30,
                                               TimeUnit.SECONDS,
                                               new LinkedBlockingQueue<Runnable>(numberOfThreads),
                                               new ZoomaThreadFactory("ZOOMA-Resolver"));
    }

    public void shutdown() {
        getLog().info("Shutting down " + getClass().getSimpleName() + "...");
        executor.shutdown();
        getLog().info("Shut down " + getClass().getSimpleName() + " OK.");
    }

    @Override public Collection<Annotation> resolve(String datasourceName,
                                                    Collection<Annotation> annotationsToResolve)
            throws ZoomaResolutionException {
        // check this service hasn't been shutdown
        if (executor.isShutdown()) {
            throw new IllegalStateException("Cannot resolve annotations - resolver service has been shutdown");
        }

        List<Annotation> annotations;
        if (annotationsToResolve instanceof List) {
            annotations = (List<Annotation>) annotationsToResolve;
        }
        else {
            annotations = new ArrayList<>();
            annotations.addAll(annotationsToResolve);
        }
        final List<Annotation> syncedAnnotations = Collections.synchronizedList(annotations);

        // first, resolve single annotations
        final Collection<Annotation> resolvedAnnotations = Collections.synchronizedSet(new HashSet<Annotation>());
        final WorkloadScheduler scheduler =
                new WorkloadScheduler(executor, annotationsToResolve.size(), "Annotation-Resolver-" + datasourceName) {
                    @Override protected void executeTask(int iteration) throws Exception {
                        Annotation annotationToResolve = syncedAnnotations.get(iteration - 1);
                        getLog().trace("Executing runnable to resolve annotation " + annotationToResolve.getURI());
                        Annotation resolvedAnnotation = resolve(annotationToResolve);
                        if (resolvedAnnotation != null) {
                            resolvedAnnotations.add(resolvedAnnotation);
                        }
                        getLog().trace("Resolved annotation " + annotationToResolve.getURI());
                    }
                };
        scheduler.start();

        int count = -1;
        int total = annotationsToResolve.size();
        try {
            while (true) {
                try {
                    scheduler.waitUntilComplete();
                    break;
                }
                catch (InterruptedException e) {
                    getLog().debug("Interrupted whilst waiting for annotations resolution to finish, continuing");
                }
            }
        }
        catch (RuntimeException e) {
            getLog().error("Execution of annotation resolving failed (" + e.getMessage() + ").  " +
                                   "Completed " + count + "/" + total + ".", e);
            if (e.getCause() instanceof ZoomaResolutionException) {
                throw (ZoomaResolutionException) e.getCause();
            }
            else {
                throw new ZoomaResolutionException("A resolver task failed", e);
            }
        }

        // then filter out annotations with no semantic tag
        return filter(resolvedAnnotations);
    }
}
