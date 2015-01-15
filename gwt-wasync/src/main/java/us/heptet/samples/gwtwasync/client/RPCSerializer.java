package us.heptet.samples.gwtwasync.client;
import com.google.gwt.user.client.rpc.SerializationException;
import org.atmosphere.gwt20.client.GwtRpcClientSerializer;
import org.atmosphere.gwt20.client.GwtRpcSerialTypes;
import us.heptet.samples.gwtwasync.common.RPCEvent;

import java.util.logging.Logger;

/**
 * Created by kay on 5/19/2014.
 */
@GwtRpcSerialTypes({RPCEvent.class})
public abstract class RPCSerializer extends GwtRpcClientSerializer {
    private static Logger logger = Logger.getLogger(RPCSerializer.class.getName());
    public Object deserialize(String raw) throws SerializationException {
        logger.fine("deserializing " + raw);
        Object o = null;
        //try {
            o = super.deserialize(raw);
        //} catch(SerializationException ex)
//        {
//            throw new RuntimeException(ex);
            //logger.severe("Unable to deserialize message");
//       }
        return o;
    }
}
