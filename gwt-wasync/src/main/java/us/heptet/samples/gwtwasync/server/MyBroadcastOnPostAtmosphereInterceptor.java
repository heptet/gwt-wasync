package us.heptet.samples.gwtwasync.server;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.atmosphere.util.IOUtils.isBodyEmpty;
import static org.atmosphere.util.IOUtils.readEntirely;

/**
 * Created by kay on 7/2/2014.
 */
public class MyBroadcastOnPostAtmosphereInterceptor extends AtmosphereInterceptorAdapter {

    private final static Logger logger = LoggerFactory.getLogger(MyBroadcastOnPostAtmosphereInterceptor.class);

    @Override
    public Action inspect(AtmosphereResource r) {
        return Action.CONTINUE;
    }


    @Override
    public void postInspect(AtmosphereResource r) {
        if (r.getRequest().getMethod().equalsIgnoreCase("POST")) {
            AtmosphereRequest request = r.getRequest();
            Object o = readEntirely(r);
            logger.info("got body of {} to broadcast", o);
            if (isBodyEmpty(o)) {
                logger.warn("{} received an empty body", request);
                return;
            }
            r.getBroadcaster().broadcast(o);
        }
    }
}
