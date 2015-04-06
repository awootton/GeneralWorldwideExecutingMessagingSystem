Welcome to GWEMS 
-------------------
Aka GeneralWorldwideExecutingMessagingSystem.

A worldwide distributed message system in AWS.
Because every metaverse needs a strong backbone. 

Connections are being served on a [WebSocket](ws://go3here.com:8081/). There is a very rough example of message delivery in the form of [an http page with javascript that operates the websocket api]( http://go2here.com) There will be worldwide endpoints soon. 

There is presently just the lowest layer with no micro-services or storage, just routing. 

See [wiki](https://github.com/awootton/GeneralWorldwideExecutingMessagingSystem1/wiki) for demos, examples, and explanations.  

Only the java needs building. As far as installing goes there's a tomcat8 java8 server in the project that also starts the gwems service. The project requires redis, and the full system requires a modified redis. Several of them. I usually start those with start_redis.sh manually.

When run in eclipse the tests do a simulation of multiple servers in different zones. And, of course, the tests (junit) are in the 'tests' folder. 

It is better for debugging to not start the tomcat server. It syncs awkwardly. Instead, start the python server using python-server.sh from within it's folder and then run StartOneGlobalServer.main() which will start up the ws server separately. Feel free to ask questions. 


