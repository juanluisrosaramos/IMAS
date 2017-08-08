1.Written report detailing the study and analysis of:
Characteristics of the environment.
Best kind of architecture to apply to each type of agents.
Properties that should be exhibited by each type of agents.
#
##Analytical reading of the exercise
Our first step was to identify from the description of the exercise the different properties, goals and entities provided.

We found some important points like the basic objective of our multi agent system: **an efficient cleaning of a city** Then we read that for this purpose we have two entities: **scouts and harversters**
 * Scouts detects and find harvest
 * Harversters collect it and dispose it in the recycling centers
We find then some keys about the **environment**:
 * x buildings in a grid of a non provided size
 * x recycling centers
 * streets (where there can only be an unique vehicle in each moment)
At this point we also find some important points about the
* we move by turns, we find this a very interesting point for the description of the environment (as we will see later)
 **characteristics** of this environment
 <blockquote>"a limited number of elements and allowed interactions appear."</blockquote>
 <blockquote>the idea behind coordinators is to have a map of the explored area and to use it to make decisions</blockquote>
 **characteristics and actions of the entities**
 * a limited visual range of his position inside his cell and the 8 cells surrounding it. Scouts communicate the position of the garbage so there must be a central coordinator.
  * harvester coordinators (1 to n)
  * scout coordinators (1 to n)
 * garbage detection
 * garbage harvesting
 * garbage recycling
 * garbage is expressed in units and there are three types: plastic, glass and paper
 * buildings only have one kind of garbage (x units of glass)
 * recycling centers only recycle one kind of garbage and they give to the harvester two points for each unit of garbage disposed (this measure unit can be useful for improve the system) *¿For example, there could be a recycling center that gives 2 points for each unit of plastic garbage and 3 points for each unit of paper. This recycling center does not deal with plastic and glass garbage?
 * scouts and harvesters can make their own decisions or wait orders from their managers.
 * harvesters
   * they collect garbage being close to a building and lose a turn each time they are collecting.
   * they only can collect a **maximum unit of one kind** of garbage despite the fact that they can collect different types of garbage
   * they can collect same kind of garbage from different buildings before going to recycling center
   * they lose one turn when the recycle garbage
   * they can act (collect and recycle) at the same time in the same place( without using the same street)
    
 
 
##Characteristics of our environment
Accessible --> According of the definition of the problem  because all the map information is accessible Even that the agents doesn't need it
Deterministic -->  There is no randomness in the action's agents. You will have the same effect if you repeat the action
Episodic --> It's episodic because it moves by turns.
Dinamic -- > "remain unchanged except by the performance of actions by the agent" But we consider the system agent the environment and not an agent by itself then the random garbage is produced by the environment. and viceversa (poner lo del static)
Discrete --> because the percepts of actions are integers and basically all actions are booleans
##Architecture of our agents
Scouts agents architecture -->  reactive architecture
Harverster agents --> reactive architecture
Scout -> reactive agent
Coordinators
but we plan to improve the reactive rules and change some of them to Hybrid 
##Properties of our agents


