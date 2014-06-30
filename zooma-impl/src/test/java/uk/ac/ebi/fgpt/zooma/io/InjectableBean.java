package uk.ac.ebi.fgpt.zooma.io;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/06/14
 */
@Component
public class InjectableBean {
    private InjectedBean injectedBean;

    public InjectedBean getInjectedBean() {
        return injectedBean;
    }

    @Autowired
    public void setInjectedBean(InjectedBean injectedBean) {
        this.injectedBean = injectedBean;
    }
}
