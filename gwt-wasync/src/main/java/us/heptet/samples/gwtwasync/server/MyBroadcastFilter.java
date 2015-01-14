package us.heptet.samples.gwtwasync.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.PerRequestBroadcastFilter;
import org.atmosphere.gwt20.server.GwtRpcSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.heptet.samples.gwtwasync.common.RPCEvent;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by kay on 6/14/2014.
 */
public class MyBroadcastFilter implements PerRequestBroadcastFilter {
    private static Logger logger = LoggerFactory.getLogger(MyBroadcastFilter.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public BroadcastAction filter(String broadcasterId, AtmosphereResource r, Object originalMessage, Object message) {
        logger.debug("filtering broadcast on {} to resource {}: message({}) = {}", broadcasterId, r.uuid(), message.getClass().getName(), message);
        ServletRequest servletRequest = r.getRequest();
        assert(r != null);

        String contentType1 = servletRequest.getContentType();
        if(contentType1 != null && contentType1.contains("x-gwt-rpc"))
        {
            if(!RPCEvent.class.isAssignableFrom(message.getClass()))
            {
                // this code does nothing good!
                RPCEvent newMessage = new RPCEvent();
                //newMessage.setMessage(message.toString());
                message = newMessage;
            }

            if (!(r.getSerializer() instanceof GwtRpcSerializer)) {
                String charEncoding = servletRequest.getCharacterEncoding();
                if (charEncoding == null) {
                    charEncoding = "UTF-8";
                }
                ServletResponse response = r.getResponse();
                response.setContentType(contentType1);
                response.setCharacterEncoding(charEncoding);
                r.setSerializer(new GwtRpcSerializer(r));
            }
        }
        else
        {
            if(!(message instanceof String)) {
                try {

                    logger.debug("type of message = {}", message.getClass().getName());
                    String newMessage = mapper.writeValueAsString(message);
                    logger.info("message is {}", newMessage);
                    message = newMessage;
                } catch (JsonProcessingException ex) {
                    logger.warn(ex.toString());
                }
            }
        }
        return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
    }

    @Override
    public BroadcastAction filter(String broadcasterId, Object originalMessage, Object message) {
        logger.debug("filter NOres o = {}; m = {}", originalMessage, message);

        return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
    }
}
