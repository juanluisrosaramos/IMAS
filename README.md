# Practice description

You have to design and develop a decision making system for real complex situations based on a multi-agent system.
This exercise aims to simulate an efficient cleaning of a city. There are two kinds of entities:

* **scouts** that detects where the garbage is.
* **harvesters** that are responsible for harvesting the garbage and disposing it into recycling centers.

To make the problem **finite**, we locate this scenario in a **city seen as a grid of variable size**, where:

  * limited number of elements  
  * allowed interactions appear.
  * Both scouts and harvesters will have a visual range surrounding their position.

We will consider the **city divided into a set of grid cells**. Within this area there are:

  * **buildings** (which may contain garbage).
  * **streets** where scouts and harvesters may be located.
  * **recycling centers**.
  * **Contraint:** in a certain street cell there **can only be a unique vehicle** (scout or harvester) in each moment.

**Garbage** must be detected by exploring the map, then once is detected must be harvested and disposed in one of the available recycling centers. So that there are three basic kinds of **actions** that will have to be done:

  * garbage detecting
  * garbage harvesting
  * garbage recycling

A certain **building** may contain:

  * **some units of garbage** of different kinds:
  	* plastic
  	* glass
  	* paper
  * A concrete building **only contains one kind of garbage**
  *  For example, it could contain 5 units of plastic garbage

Each **recycling center**:

  * For each kind of garbage, there will be **at least** one recycling center in the city that accepts it
  * **only deals** with some of the kinds of garbage.
  * give a number of points for each unit of garbage disposed.
  * ** For example**:  2 points for each unit of plastic garbage and 3 points for each unit of paper.

The **location** of the garbage:

  * is not known a priori

The ** vehicle** will have:

  * a **visual range** limited to the cell where it is located plus the 8 cells surrounding it.
  * They will have to move, horizontally or vertically, through the street cells in order to explore the city.
  * They will communicate to combine the information found so that the position of the garbage is known as soon as possible.

Each **scout and harvester** will have its corresponding agent. These agents:

  * keep their current position (both) + their internal state (for example, a harvester keeps the number of garbage units it is carrying and their kind).
  * make their own decisions or wait for orders from their managers (depending on the design of the multi-agent system).

**Scouts:**

  * They explore the map discovering the buildings that have garbage.
  * They cannot harvest garbage.
  * They can move, horizontally or vertically, 1 cell per turn.

**Harvesters:**

  * They harvest garbage and bring it to a recycling center.
  * They can move, horizontally or vertically, 1 cell per turn.
  * In order to harvest garbage, they must be situated in a cell adjacent to the building containing garbage (horizontally, vertically or diagonally) and remain there for some time (1 turn per garbage unit).
  * Each harvester can harvest one or more kinds of garbage but
  * it can only carry one kind of garbage at the same time.
  * Moreover, harvesters will have a maximum number of units of garbage that they can carry.
  * When they have harvested garbage, they can:
    * go to harvest in another building if the maximum number of units has not been reached or
    * they can go to recycle this garbage. To do this, a harvester has to be situated in a cell adjacent to a recycling center (horizontally, vertically or diagonally) that allows the kind of garbage it is carrying and remain there for some time (1 turn per garbage unit).
  * Several harvesters can be harvesting garbage from the same building or disposing garbage in the same recycling center at the same time.

Coordinators:
  The agents representing the scouts and the harvesters can be coordinated using at least a coordinator, that is, a scout coordinator and a harvester coordinator. However you can decide, if necessary, design other coordination methods based on several coordinators of different levels (e.g., harvester coordinator, plastic harvester coordinator, glass harvester coordinator, etc.). The idea behind these coordinators is to have a map of the explored area and use it to make decisions.

  For example, harvester coordinator, knowing that there is a limited number of harvesters, must determine how to assign the different harvesters to each one of the buildings with garbage, depending on the kind and amount of garbage, the capacity of the harvesters and their location in the map. Several strategies can be followed, like assigning 1 harvester/building, sending some harvesters to the same building in order to harvest its garbage earlier, sending a harvester to a recycling center which gives less points than another one but it is very much closer, decide routes for the scouts that do not interfere in the harvesting tasks, etc.


  There is always a coordinator agent which centralizes the orders to be executed in each turn. This coordinator agent knows which are the changes that dynamically happen in the city (e.g., movement of the vehicles, garbage harvested, etc.). 
  In order to simulate and control what is happening, there is also a system agent which executes orders to update the state of the world. This agent is the one that, in a nutshell, keeps the state of the city and shows it to the user using a graphical interface.
