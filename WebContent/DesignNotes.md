New Feature List
---------------

Have sub, unsub, and pub.
We have js possible with a user map and also js Bindings. Arbitrary code can be run.
There is no way to launch Agents but that's coming.

I need a demo case for the agent start/install & keep alive && restore and ownership.

We can do an IoT demo without advanced features. Secrecy is weird though. 

Things that need to be limited:

	space used by JS in agents.
	access to system from js.
	no looping or blocking allowed.
	bandwidth of upload/publish.
	number of subscribes.
	spamming of channels
	ownership of permanent agents.
	
features:

	Some channels are owned and cannot be published.
	State needs to be accumulated
		 and then requested through API
	Some channels need to pass through a filter, or agent. Same as owning?
	
ideas:

	rate limit subscriptions with proof of work. ? no - just count them,
	rate limit ownership of channels. at least require identity.
	require entity for permanent agents. Temp agents are free.
	
	All publish go to agent if present. Ownership would mean pushing agents down trees - blech.
		always push all messages through root? - horrible.
	Have separate types of channels. Some open, some owned. The owned one can only have one
		subscriber ever. (how do we do that?).  
		
	Some channels can have only one writer. Some can have only one reader. ?? 
	
	Is there such a thing a distributed room manager or does it always have to go through a master?
	
	how to limit pub: use secret channel. But, how do we limit to 8? go through db lazily and check perms
	
	how to limit sub: use secret channel. or, go through db when sub happens. 
	
	