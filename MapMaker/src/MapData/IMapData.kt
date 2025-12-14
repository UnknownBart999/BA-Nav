package mapmaker.mapdata

/**
 * Interface for MapData class.
 *
 * @author Bartosz Wolek
 *
 * @property name Common name of the map. Will be used for the file name after export.
 * @property version Current version code of map. Useful for differentiating between similar map files.
 * @property buildings ArrayList of Building objects. Stores the buildings that belong to this map.
 */
internal interface IMapData {

    var name: String
    var version: String
    val buildings: ArrayList<Building>
    val externalNodes: ArrayList<Node>
    val externalEdges: ArrayList<Edge>

    /**
     * Creates a new Building with name [name] and adds it to Maps's building list.
     *
     * @return Reference to the new Building object.
     */
    fun newBuilding(name: String): Building

    /**
     * Attempts to find a building with name [name] and remove it from the Map's building list.
     *
     * @return true or false on whether the building was removed successfully.
     */
    fun delBuilding(name: String): Boolean

    /**
     * Updates the version code using [versionCode].
     * Format: ###.###.###... Can have numbers separated by periods. Can have any amount of these number period groups.
     */
    fun updateVersion(versionCode: String)

    /**
     * Renames the map using name [name].
     */
    fun rename(name: String)

    /**
     * Exports the map to a JSON file.
     */
    fun export()

    /**
     * Helper function for export. Gets the nodes present in the map in a single ArrayList.
     */
    fun getNodes(): ArrayList<Node>

    /**
     * Helper function for export. Gets the edges present in the map in a single ArrayList.
     */
    fun getEdges(): ArrayList<Edge>

}

/**
 * Interface for Building class.
 *
 * @author Bartosz Wolek
 *
 * @property name Name of the building.
 * @property floors ArrayList storing all the floors belonging to this building.
 * @property parentMap Reference to the MapData object that this building is a part of. Useful for renaming.
 */
internal interface IBuilding {

    var name: String
    val floors: ArrayList<Floor>
    val parentMap: MapData

    /**
     * Creates a new floor in this Building with floor number [num].
     */
    fun newFloor(num: Int): Floor

    /**
     * Attempts to delete a floor with floor number [num] from this Building.
     *
     * @return true or false on whether the floor was removed successfully.
     */
    fun delFloor(num: Int): Boolean

    /**
     * Renames the Building with name [name]. Verifies that the new name does not already exist in parent map.
     */
    fun rename(name: String)

}

/**
 * Interface for Floor class.
 *
 * @author Bartosz Wolek
 *
 * @property id Nonmutable id for this floor that is automatically generated upon creating the object.
 * @property level Floor number of this floor.
 * @property floorPlan String version of uploaded floor plan jpeg.
 * @property nodes ArrayList storing nodes in this floor.
 * @property edges ArrayList storing edges in this floor.
 * @property parentBuilding Reference to the parent building. Useful for changing the level and creating Nodes.
 */
internal interface IFloor {

    val id: Int
    var level: Int
    var floorPlan: String
    val nodes: ArrayList<Node>
    val edges: ArrayList<Edge>
    val parentBuilding: Building

    /**
     * Changes the current floor's floor number [level].
     */
    fun changeLevel(level: Int)

    /**
     * Creates a new node in this floor using [name], [x], [y], and [cat].
     * [x] and [y] are the x-y coordinates that the node has.
     * [cat] is the category number for this node. This is useful to differentiate between entrances, exits, rooms, elevators, etc.
     *
     * @return Reference to the newly created Node.
     */
    fun addNode(name: String, x: Int, y: Int, cat: Int): Node

    /**
     * Attempts to delete the node from the current floor using [name].
     *
     * @return true or false on whether the node was removed successfully.
     */
    fun delNode(name: String): Boolean

    /**
     * Creates a new Edge using [nodeName1] and [nodeName2].
     *
     * @return Reference to the newly created edge.
     */
    fun addEdge(nodeName1: String, nodeName2: String): Edge

    /**
     * Creates a new Edge using [intNodeName] and [extNode].
     * [intNodeName] is the String name of the node in this floor's node array.
     * [extNode] is the external node to connect to.
     *
     * @return Reference to the newly created edge.
     */
    fun addExternalEdge(intNodeName: String, extNode: Node): Edge

    /**
     * Attempts to delete the edge from the nodes [nodeName1] and [nodeName2].
     *
     * @return true or false on whether the edge was removed successfully.
     */
    fun delEdge(nodeName1: String, nodeName2: String): Boolean

    /**
     * Uploads a new floor plan and converts it to a String.
     */
    fun uploadFloorPlan()

}

/**
 * Interface for Node class.
 *
 * @author Bartosz Wolek
 *
 * @property id Nonmutable id that is automatically generated upon creating the node.
 * @property name Common name for the node.
 * @property building Reference to the parent building. Useful for export.
 * @property floor Reference to the parent floor. Useful for renaming.
 * @property coords X-Y coordinate data for the node.
 * @property cat Category id for the node.
 * @property edges ArrayList of edges that use this node.
 * @property additionalInfo Nullable ArrayList of strings to store future modifications to the node.
 */
internal interface INode {

    val id: Int
    var name: String
    val building: Building
    val floor: Floor
    var coords: Pair<Int, Int>
    var cat: Int
    val edges: ArrayList<Edge>
    val additionalInfo: ArrayList<String>?

    /**
     * Updates the nodes coordinates with [x] and [y].
     */
    fun updateCoords(x: Int, y: Int)

    /**
     * Updates the node category number with [cat].
     */
    fun updateCategory(cat: Int)

    /**
     * Renames the node with [name]. Checks parent floor if new name is not already present.
     */
    fun rename(name: String)

    /**
     * Adds [edge] to node's edge list.
     */
    fun addEdge(edge: Edge)

    /**
     * Attempts to find and remove Edge [edge].
     *
     * @return true or false on whether the Edge was removed successfully.
     */
    fun delEdge(edge: Edge): Boolean

}

/**
 * Interface for Edge class.
 *
 * @author Bartosz Wolek
 *
 * @property id Nonmutable id that is automatically generated upon creating the edge.
 * @property nodes Pair of Node for this edge.
 * @property dist Distance between the two Nodes.
 * @property additionalInfo Nullable ArrayList of strings to store future modifications to the edge.
 */
internal interface IEdge {

    val id: Int
    val nodes: Pair<Node, Node>
    var dist: Float
    val additionalInfo: ArrayList<String>?

    /**
     * Calculates the distance between the two nodes.
     */
    fun calcDistance()

    /**
     * Gets the ids from the pair of nodes.
     */
    fun getNodeIDs(): Pair<Int, Int>

}