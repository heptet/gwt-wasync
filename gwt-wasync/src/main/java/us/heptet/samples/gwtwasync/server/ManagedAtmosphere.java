package us.heptet.samples.gwtwasync.server;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Get;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.*;
import org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.heptet.samples.gwtwasync.common.RPCEvent;

/**
 * Prototype managed service implementation for atmosphere handling.
 *
 * Created by kay on 6/13/2014.
 */


@ManagedService(path = "/*",
		interceptors = {
		    AtmosphereResourceLifecycleInterceptor.class,
		    //TrackMessageSizeInterceptor.class,
		    MyGwtRpcInterceptor.class,
		    SuspendTrackerInterceptor.class,
		    MyInterceptor.class,
		    //AtmosphereMessageInterceptor.class,
		    //MyBroadcastOnPostAtmosphereInterceptor.class,
		    IdleResourceInterceptor.class
		})

public class ManagedAtmosphere {
    private static Logger logger = LoggerFactory.getLogger(ManagedAtmosphere.class);

//    @Get
    public void onGet(final AtmosphereResource r)
    {

        AtmosphereRequest req = r.getRequest();
        if(req.getHeader("Upgrade") != null)
        {
            return;
        }
        logger.debug("in GET");

        AtmosphereResponse response = r.getResponse();
        response.setContentType("text/html");
        response.write("Hello from " + ManagedAtmosphere.class.getName());
    }

    @Ready
    public void onReady(final AtmosphereResource r) {
        logger.info("Received RPC GET");

        Broadcaster myBroadcaster = r.getAtmosphereConfig().getBroadcasterFactory().lookup("MyBroadcaster", true);
        logger.debug("myBroadcaster = " + myBroadcaster);
        logger.debug("adding " + r + " to " + myBroadcaster);
        myBroadcaster.addAtmosphereResource(r);
	myBroadcaster.broadcast(new RPCEvent());
    }

    @Disconnect
    public void disconnected(AtmosphereResourceEvent event){
        // isCancelled == true. means the client didn't send the close event, so an unexpected network glitch or browser
        // crash occurred.
        if (event.isCancelled()) {
            logger.info("User:" + event.getResource().uuid() + " unexpectedly disconnected");
        } else if (event.isClosedByClient()) {
            logger.info("User:" + event.getResource().uuid() + " closed the connection");
        }
    }

    //ServerSerializationStreamReader is something we might be able to use to
    // deserialize a payload

    @Post
    public void post(AtmosphereResource r) {
        // Don't need to do anything, the interceptor took care of it for us.
        logger.debug("POST received with transport + " + r.transport());
        Object messageObject = r.getRequest().getAttribute(Constants.MESSAGE_OBJECT);
        logger.debug("mo = {}", messageObject);
        if(messageObject instanceof RPCEvent)
        {
            RPCEvent rpcEvent = (RPCEvent)messageObject;

	    /* do stuff here/dispatch */
        }
    }


    @Message
    public void message(Object o)
    {
        logger.info("got @Message {} {}", o.getClass().getName(), o);
    }

}
