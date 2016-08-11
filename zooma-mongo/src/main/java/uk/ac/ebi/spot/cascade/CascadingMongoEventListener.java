package uk.ac.ebi.spot.cascade;

import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Enables auto saving of fields that have a collection in the database, withought having to explicitly save them
 * Uses the CascadeSave annotation
 *
 * Created by olgavrou on 05/08/2016.
 */
@Component
public class CascadingMongoEventListener extends AbstractMongoEventListener {
    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public void onBeforeConvert(BeforeConvertEvent event) {

        final Object source = event.getSource();

        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {

            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);

                if (field.isAnnotationPresent(DBRef.class) && field.isAnnotationPresent(CascadeSave.class)) {
                    final Object fieldValue = field.get(source);

                    if (fieldValue instanceof Collection){
                        for (Object o : (Collection) fieldValue){
                            DbRefFieldCallback callback = new DbRefFieldCallback();

                            ReflectionUtils.doWithFields(o.getClass(), callback);

                            if (!callback.isIdFound()) {
                                throw new MappingException("Cannot perform cascade save on child object without id set");
                            }

                            mongoOperations.save(o);
                        }
                    } else {

                        DbRefFieldCallback callback = new DbRefFieldCallback();

                        ReflectionUtils.doWithFields(fieldValue.getClass(), callback);

                        if (!callback.isIdFound()) {
                            throw new MappingException("Cannot perform cascade save on child object without id set");
                        }

                        mongoOperations.save(fieldValue);
                    }
                }
            }
        });
    }


    private static class DbRefFieldCallback implements ReflectionUtils.FieldCallback {
        private boolean idFound;

        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
            ReflectionUtils.makeAccessible(field);

            if (field.isAnnotationPresent(Id.class)) {
                idFound = true;
            }
        }

        public boolean isIdFound() {
            return idFound;
        }
    }
}
