package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint;

import java.util.Arrays;

/**
 * A custom configurator that performs some Spring Security config tweaks required by ZOOMA.  One of the things this
 * configurator class does is to take any configured {@link org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint}
 * beans and replace their default redirect strategy setup by Spring Security, the predictably named {@link
 * org.springframework.security.web.DefaultRedirectStrategy}, with a custom {@link PermanentRedirectStrategy} that uses
 * the HTTP response code 308 for redirects, not 302
 *
 * @author Tony Burdett
 * @date 21/02/14
 */
public class ZoomaSecurityConfigurator implements BeanPostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // customize entry point - use permanent redirect strategy
        if (bean instanceof RetryWithHttpsEntryPoint) {
            getLog().debug("Customizing RetryWithHttpsEntryPoint to use PermanentRedirectStrategy");
            RetryWithHttpsEntryPoint entryPoint = (RetryWithHttpsEntryPoint) bean;
            entryPoint.setRedirectStrategy(new PermanentRedirectStrategy());
        }

        // customize channel decision manager - use secure channel behind load balancer
        if (bean instanceof ChannelDecisionManagerImpl) {
            getLog().debug("Customizing ChannelDecisionManagerImpl to support decisions when behind a load balancer");
            ChannelDecisionManagerImpl channelDecisionManager = (ChannelDecisionManagerImpl) bean;
            channelDecisionManager.setChannelProcessors(Arrays.asList(new SecureChannelBehindLoadBalancerProcessor(),
                                                                      new InsecureChannelProcessor()));
        }

        return bean;
    }

    @Override public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
