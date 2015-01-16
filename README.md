# gwt-wasync
GWT/Atmosphere wAsync Sample

This project demonstrates integration between the Google Web Toolkit (GWT) and
Atmosphere, a Real-time web communication framework supporting WebSockets. The primary purpose
is to show the use of the Atmosphere wAsync client library to connect to a compatible Atmosphere servlet/Managed Service.
The GWT examples in the atmosphere-extensions project support GWT-RPC communication only. This example supports JSON messages
from non-GWT clients and translation of broadcasts to either GWT-RPC (for connected GWT applications)
or JSON (for other clients).

This is a work in progress. I have only used atmosphere a small amount so do not expect this to be comprehensive or to
cover anything beyong the narrowest use case. It's intention is to get users up and running with a solution I have found
satisfactory for further development work. The bundled GWT web application is essentially "hello world" with the GWT-RPC
service call wired to send an Atmosphere message. It doesn't display received messages except through the GWT log
sent to the JavaScript console. It won't even let you send more than a single message.
