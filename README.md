Welcome to GWEMS 
-------------------
Aka General Worldwide Executing Messaging System.

A worldwide distributed message system in AWS.

It's meant to eventually be the low level backbone of [The Metaverse](http://en.wikipedia.org/wiki/Metaverse). Since The Metaverse does not exist yet it mostly resembles a very fast international distributed messaging system with publish and subscribe. It is meant to be used through a simple JSON api and is available now for people to experiment with.

I am working on a functioning explanation of How The Metaverse Works.

Connections are being served on a [WebSocket](http://en.wikipedia.org/wiki/WebSocket). There is a very rough example of message delivery in the form of [an http page with javascript that operates the websocket api]( http://go2here.com) There will be worldwide endpoints soon. 

There is presently just the lowest layer with no micro-services or storage, just routing. 

See the [wiki](https://github.com/awootton/GeneralWorldwideExecutingMessagingSystem1/wiki) for demos, examples, and explanations.  

Only the java needs building. As far as installing goes there's a tomcat8 java8 servlet in the project that also starts the gwems service. 

When run in eclipse the tests do a simulation of multiple servers in different zones. And, of course, the tests (junit) are in the 'tests' folder. 

It is better for debugging to not start the tomcat server. It syncs awkwardly. Instead, start the python server using python-server.sh from within it's folder and then run StartOneGlobalServer.main() which will start up the ws server separately. Feel free to ask questions. 
