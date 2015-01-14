package us.heptet.samples.gwtwasync.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.gwt20.server.GwtRpcUtil;
import org.atmosphere.gwt20.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.heptet.samples.gwtwasync.common.RPCEvent;

import java.io.IOException;

/**
 * Created by kay on 6/14/2014.
 *
 * * This class appepars to be used for non-gwt-rpc cloents in order to process their json messages and place them into the message_object attribute of the request
 *  (gwt_deserialized_object). Should be renamed to something better than MyInterceptor.
 */
public class MyInterceptor extends AtmosphereInterceptorAdapter {
    private static Logger logger = LoggerFactory.getLogger(MyInterceptor.class);
    private ObjectMapper mapper = new ObjectMapper();//.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    MyBroadcastFilter filter = new MyBroadcastFilter();

    @Override
    public void configure(AtmosphereConfig config) {
        config.framework().broadcasterFilters(filter);
    }

    @Override
    public Action inspect(AtmosphereResource r) {
        AtmosphereRequest request = r.getRequest();
        if (request.getMethod().equals("POST")) {
            String ctype = request.getContentType();
            if(ctype == null || ctype.contains("x-gwt-rpc") == false)
            {
                try {
                    logger.debug("!!! reading json value");
                    String s = GwtRpcUtil.readerToString(request.getReader());
                    logger.debug("got json value {}", s);
                    Object object = mapper.readValue(s, RPCEvent.class);
                    r.getRequest().setAttribute(Constants.MESSAGE_OBJECT, object);
                } catch(IOException ex)
                {
                    logger.warn("!!! exception " + ex);
                }

            }
        }
        return Action.CONTINUE;
    }
}
