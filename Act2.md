# Second Activity

* Goal
Analysis of the cooperation mechanisms between agents.


* Description
In this activity, the groups must study the theoretical concepts described in the lectures about cooperation mechanisms between agents and apply these concepts to the practical exercise of the MAS course.

* Each group has to analyze the different cooperation mechanisms that the practical exercise may present based on the lectures given, including voting, auctions, coalition’s formation, etc.

* Determine the advantages / disadvantages that each different cooperation mechanism could entail. Consider the architectures defined in the first activity.

* Once the different cooperation mechanisms have been analyzed for each agent, justify the chosen mechanism for the coordination of the agents.

* Explain as detailed as possible the application of the chosen mechanism for each agent depending on the task to fulfill.

# Ideas for the practical exercise

##  Practical exercise – distributed planning (PGP style)* If a harvester plans to pick up a garbage at a certain location, it will put this information its local plan, and send it to the garbage coordinator (GC).* The GC can build a PGP with the local plans received from all harvesters.* The GC can detect conflicts (e.g. several harvesters going to the same place) and solve them by modifying the PGP and sending it back to the harvesters. 
## Practical exercise – distributed planning (GPGP style)* Make a task structure* A subtask Collect i for each garbage* Subsubtasks of Collect i: Allocate harvester to garbage i and Decide recycling centre for garbage i
* For example, if two harvesters commit to pick up the same garbage, they will notice it and one of them will be selected to do it## Practical exercise – Coalition formation  * Each discovered garbage could be a pending task. Harvesters could decide how to join in subgroups of 2- 3 units (coalitions) to collect all the units of garbage on a certain location.  * They could take into account their present position, the position and quantity of the garbage to be collected, their capacity, their present state (idle/on the way to a garbage collection/on the way to a recycling centre), ...  * There could even be scouts + harvesters coalitions ...In how many coalitions of 2-3 units can a harvester participate? (e.g. if there are 21 )

## Practical exercise – Auction
* The harvester coordinator could make an auction for each group of garbage units to determine which harvester(s) should go there. The bids of the harvesters could depend on their current position, the position of the garbage, its current state (idle, on its way to another garbage collection, going to the recycling centre, pending garbages, ...)* It could be for example a FPSB* There could be multi-unit auctions, if several garbages are located close to one another and they are auctioned together.* There could be a combinatorial auction of all the garbages at a certain point in time.* Collected garbage could also be auctioned between recycling centres, if they were autonomous agents ...

## Practical exercise – Voting* The harvesters could vote which garbage to collect first, then the second one, etc.* The harvester coordinator could take the votes.* If some harvesters vote for the same garbage they could be asked to join in a group (coalition) to collect it.* The vote of each harvester agent could depend on its actual position, its current state (idle, on the way to a garbage-collection, going to a recycling centre, etc.), the position of the garbage, the amount of garbage units in each building, etc.  The working groups of the course are encouraged to use different coordination mechanisms for the different elements of the problem (e.g. to use a coordination strategy to control the movement of the scouts and another one to assign harvesters to garbages). 
 
 ## TASKS SOLVE
 
 * Scout initialization
 
 * Harvester intialization
 

 
 