package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.channel.SecureChannelProcessor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * A subclass of the spring security {@link org.springframework.security.web.access.channel.ChannelProcessor} that
 * determines whether the request channel is secure based on request headers added by a load balancer.  In this way, URL
 * based requiresChannel config can be used on requests that have been decrypted and forwarded by a load balancer.
 *
 * @author Tony Burdett
 * @date 07/03/14
 */
public class SecureChannelBehindLoadBalancerProcessor extends SecureChannelProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public void decide(FilterInvocation invocation, Collection<ConfigAttribute> config)
            throws IOException, ServletException {
        if (getLog().isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n********** Checking for load balancer forwarded protocol headers ***********\n");
            Enumeration headerNames = invocation.getHttpRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement().toString();
                sb.append("\t")
                        .append(header)
                        .append(":    ")
                        .append(invocation.getHttpRequest().getHeader(header))
                        .append("\n");
            }
            sb.append("*********** End of header check **********\n");
            getLog().trace(sb.toString());
        }

        // can we detect a header added by load balancer?
        if (invocation.getHttpRequest().getHeader("x-https") != null) {
            // if x-https = 1, this is acceptable, so do nothing.  Otherwise, revert to default channel processor
            if (!invocation.getHttpRequest().getHeader("x-https").equals("1")) {
                super.decide(invocation, config);
            }
        }
        else {
            // if not, revert to default channel processor
            super.decide(invocation, config);
        }
    }
}
