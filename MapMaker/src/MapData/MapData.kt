package mapmaker.mapdata

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * MapData class. This class stores all data for a given map.
 *
 * @author Bartosz Wolek
 *
 * @property name Common name of the map. Will be used for the file name after export.
 * @property version Current version code of map. Useful for differentiating between similar map files.
 * @property buildings ArrayList of Building objects. Stores the buildings that belong to this map.
 */
class MapData(
    override var name: String = "", override var version: String = "",
    override val buildings: ArrayList<Building> = ArrayList(),
    override val externalNodes: ArrayList<Node> = ArrayList(),
    override val externalEdges: ArrayList<Edge> = ArrayList()
) : IMapData {
    /**
     * Creates a new Building with name [name] and adds it to Maps's building list.
     *
     * @return Reference to the new Building object.
     */
    override fun newBuilding(name: String): Building {

        // Check if building name already exists
        for (building in this.buildings) {
            if (building.name == name) {
                throw Exception("Building with name \"${name}\" already exists!")
            }
        }

        val building = Building(name = name, parentMap = this)
        this.buildings.add(building)

        return building

    }

    /**
     * Attempts to find a building with name [name] and remove it from the Map's building list.
     *
     * @return true or false on whether the building was removed successfully.
     */
    override fun delBuilding(name: String): Boolean {

        for (building in this.buildings) {
            if (building.name == name) {
                this.buildings.remove(building)
                return true
            }
        }

        return false

    }

    /**
     * Updates the version code using [versionCode].
     * Format: ###.###.###... Can have numbers separated by periods. Can have any amount of these number period groups.
     */
    override fun updateVersion(versionCode: String) {

        val acceptedChars = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.')

        // Check if version code has valid characters
        for (c in versionCode) {
            if (c !in acceptedChars) {
                throw Exception("Version Code \"${versionCode}\" has invalid character \'${c}\'!")
            }
        }

        // Version code is valid, version not set
        if (this.version == "") {
            this.version = versionCode
            return
        }

        // Check if new version code uses decimal split
        val newUsesSplit: Boolean = '.' in versionCode
        val oldUsesSplit: Boolean = '.' in versionCode

        if (newUsesSplit and oldUsesSplit) {
            // Convert both into lists without decimals
            val newSplitVersion = versionCode.split('.').toMutableList()
            val oldSplitVersion = this.version.split('.').toMutableList()

            // If new version code is longer than old and has all elements of old at beginning, it is deemed newer.
            // Ex: old = 2.3, new = 2.3.1
            if ((newSplitVersion.size > oldSplitVersion.size) and (newSplitVersion.subList(0, oldSplitVersion.size)
                    .containsAll(oldSplitVersion))
            ) {
                this.version = versionCode
                return
            }

            // Pad the version codes to the same length
            // Ex: old: 2.3, new: 2.3.1 -> old: 2.3.0, new: 2.3.1
            if (newSplitVersion.size > oldSplitVersion.size) {
                val spotsToPad = newSplitVersion.size - oldSplitVersion.size

                for (i in 1..spotsToPad) {
                    oldSplitVersion.add("0")
                }
            } else if (oldSplitVersion.size > newSplitVersion.size) {
                val spotsToPad = oldSplitVersion.size - newSplitVersion.size

                for (i in 1..spotsToPad) {
                    newSplitVersion.add("0")
                }
            }

            if (comparePaddedVersions(oldSplitVersion, newSplitVersion)) {
                this.version = versionCode
                return
            } else {
                throw Exception("Version Code \"${versionCode}\" is not newer than saved version \"${this.version}\"!")
            }

            // At least one code does not contain periods.
        } else {
            // If both don't use periods, compare int value
            if (!oldUsesSplit and !newUsesSplit) {
                if (versionCode.toInt() >= this.version.toInt()) {
                    this.version = versionCode
                    return
                } else {
                    throw Exception("Version Code \"${versionCode}\" is not newer than saved version \"${this.version}\"!")
                }
                // Only old uses split format
            } else if (oldUsesSplit) {
                val oldSplitVersion = this.version.split('.').toMutableList()
                val newSplitVersion = mutableListOf<String>(versionCode)

                // Pad zeros
                val spotsToPad = oldSplitVersion.size - newSplitVersion.size

                for (i in 1..spotsToPad) {
                    newSplitVersion.add("0")
                }

                if (comparePaddedVersions(oldSplitVersion, newSplitVersion)) {
                    this.version = versionCode
                    return
                } else {
                    throw Exception("Version Code \"${versionCode}\" is not newer than saved version \"${this.version}\"!")
                }
                // Only new uses split format
            } else {
                val newSplitVersion = this.version.split('.').toMutableList()
                val oldSplitVersion = mutableListOf<String>(versionCode)

                // Pad zeros
                val spotsToPad = newSplitVersion.size - oldSplitVersion.size

                for (i in 1..spotsToPad) {
                    oldSplitVersion.add("0")
                }

                if (comparePaddedVersions(oldSplitVersion, newSplitVersion)) {
                    this.version = versionCode
                    return
                } else {
                    throw Exception("Version Code \"${versionCode}\" is not newer than saved version \"${this.version}\"!")
                }
            }

        }

    }

    /**
     * Helper function for comparing padded version codes
     *
     * @return true if new code is newer or the same as old code. false otherwise.
     */
    private fun comparePaddedVersions(oldCode: MutableList<String>, newCode: MutableList<String>): Boolean {

        oldCode.reverse()
        newCode.reverse()

        /*
                Go through the first X elements of the reversed version code. X is the smaller length between the two.
                If we find a section in the new that is less than the current, set the flag.
                If the flag is true at the end, that means there is an unresolved smaller part in the new version
                code (the new code is an older version than the current).

                Ex. old is 2.3.1, new is 2.3.0
                Both are reversed. old is 1.3.2, new is 0.3.2
                0 < 1 flag set
                3 = 3 nothing changes
                2 = 2 nothing changes
                flag still active, old is better than new

                Ex2. old is 2.3.1, new is 2.4.0
                Both are reversed: old is 1.3.2, new is 0.4.2
                0 < 1, flag is set
                4 > 3 flag is unset
                2 = 2 nothing changes.
                Version code is decided to be better than old.
             */
        var flag = false
        for (i in 0..<oldCode.size) {

            if (newCode[i].toInt() < oldCode[i].toInt()) flag = true
            else if (newCode[i].toInt() > oldCode[i].toInt()) flag = false

        }

        // Flag set, old code is <= new code, return false
        return !flag
    }

    /**
     * Renames the map using name [name].
     */
    override fun rename(name: String) {
        this.name = name
    }

    /**
     * Exports the map to a JSON file.
     * TODO
     */
    override fun export() {

    }

    /**
     * Helper function for export. Gets the nodes present in the map in a single ArrayList.
     */
    override fun getNodes(): ArrayList<Node> {
        val nodesList = ArrayList<Node>()

        for (building in buildings) {
            for (floor in building.floors) {
                nodesList.addAll(floor.nodes)
            }
        }

        return nodesList
    }

    /**
     * Helper function for export. Gets the edges present in the map in a single ArrayList.
     */
    override fun getEdges(): ArrayList<Edge> {
        val edgesList = ArrayList<Edge>()

        for (building in buildings) {
            for (floor in building.floors) {
                edgesList.addAll(floor.edges)
            }
        }

        return edgesList
    }
}

/**
 * Building class. Stores the data for a building.
 *
 * @author Bartosz Wolek
 *
 * @property name Name of the building.
 * @property floors ArrayList storing all the floors belonging to this building.
 * @property parentMap Reference to the MapData object that this building is a part of. Useful for renaming.
 */
class Building(
    override var name: String = "",
    override val floors: ArrayList<Floor> = ArrayList(),
    override val parentMap: MapData
) : IBuilding {

    /**
     * Creates a new floor in this Building with floor number [num].
     */
    override fun newFloor(num: Int): Floor {
        val floor = Floor(level = num, parentBuilding = this)
        this.floors.add(floor)
        return floor
    }

    /**
     * Attempts to delete a floor with floor number [num] from this Building.
     *
     * @return true or false on whether the floor was removed successfully.
     */
    override fun delFloor(num: Int): Boolean {
        for (floor in this.floors) {
            if (floor.level == num) {
                this.floors.remove(floor)
                return true
            }
        }
        return false
    }

    /**
     * Renames the Building with name [name]. Verifies that the new name does not already exist in parent map.
     */
    override fun rename(name: String) {
        this.name = name
    }

}

/**
 * Floor class. Stores relevant info for a floor.
 *
 * @author Bartosz Wolek
 *
 * @property id Nonmutable id for this floor that is automatically generated upon creating the object.
 * @property level Floor number of this floor.
 * @property floorPlan String version of uploaded floor plan jpeg.
 * @property nodes ArrayList storing nodes in this floor.
 * @property parentBuilding Reference to the parent building. Useful for changing the level and creating Nodes.
 */
class Floor(
    override var level: Int = 0, override var floorPlan: String = "",
    override val nodes: ArrayList<Node> = ArrayList(),
    override val edges: ArrayList<Edge> = ArrayList(),
    override val parentBuilding: Building
) : IFloor {

    companion object {

        var uniqueID: Int = 0

        fun getNewID(): Int {
            return uniqueID++
        }

    }

    override val id: Int = getNewID()

    /**
     * Changes the current floor's floor number [level].
     */
    override fun changeLevel(level: Int) {
        this.level = level
    }

    /**
     * Creates a new node in this floor using [name], [x], [y], and [cat].
     * [x] and [y] are the x-y coordinates that the node has.
     * [cat] is the category number for this node. This is useful to differentiate between entrances, exits, rooms, elevators, etc.
     *
     * @return Reference to the newly created Node.
     */
    override fun addNode(name: String, x: Int, y: Int, cat: Int): Node {
        val node = Node(name = name, coords = Pair(x, y), cat = cat, floor = this, building = this.parentBuilding)
        this.nodes.add(node)
        return node
    }

    /**
     * Attempts to delete the node from the current floor using [name].
     *
     * @return true or false on whether the node was removed successfully.
     */
    override fun delNode(name: String): Boolean {

        for (node in this.nodes) {
            if (node.name == name) {
                this.nodes.remove(node)
                return true
            }
        }
        return false

    }

    /**
     * Creates a new Edge using [nodeName1] and [nodeName2].
     *
     * @return Reference to the newly created edge.
     */
    override fun addEdge(nodeName1: String, nodeName2: String): Edge {

        // Check if edge already exists
        for (edge in this.edges) {
            if ((edge.nodes.first.name == nodeName1) and (edge.nodes.second.name == nodeName2)) {
                throw Exception("Edge already exists in this floor!")
            } else if ((edge.nodes.first.name == nodeName2) and (edge.nodes.second.name == nodeName1)) {
                throw Exception("Edge already exists in this floor!")
            }
        }

        // Check if nodes exist
        var node1: Node? = null
        var node2: Node? = null
        for (node in this.nodes) {
            if ((node1 != null) and (node2 != null)) {
                break
            }

            if (node.name == nodeName1) {
                node1 = node
            } else if (node.name == nodeName2) {
                node2 = node
            }
        }

        if ((node1 == null) or (node2 == null)) {
            throw Exception("One of the passed Nodes is not present in this floor's Nodes")
        }

        val edge = Edge(nodes = Pair(node1 as Node, node2 as Node))
        this.edges.add(edge)

        node1.addEdge(edge)
        node2.addEdge(edge)

        return edge
    }

    /**
     * Creates a new Edge using [intNodeName] and [extNode].
     * [intNodeName] is the String name of the node in this floor's node array.
     * [extNode] is the external node to connect to.
     *
     * @return Reference to the newly created edge.
     */
    override fun addExternalEdge(intNodeName: String, extNode: Node): Edge {

        // Check if edge already exists
        for (edge in this.edges) {
            if ((edge.nodes.first.name == intNodeName) and (edge.nodes.second == extNode)) {
                throw Exception("Edge already exists in this floor!")
            } else if ((edge.nodes.first == extNode) and (edge.nodes.second.name == intNodeName)) {
                throw Exception("Edge already exists in this floor!")
            }
        }

        var intNode: Node? = null

        // Locate internal node
        for (node in this.nodes) {
            if (node.name == intNodeName) {
                intNode = node
                break
            }
        }

        if (intNode == null) {
            throw Exception("Internal node does not exists in this floor's nodes array.")
        }

        val edge = Edge(nodes = Pair(intNode, extNode))
        this.edges.add(edge)

        intNode.addEdge(edge)
        extNode.addEdge(edge)

        return edge
    }

    /**
     * Attempts to delete the edge from the nodes [nodeName1] and [nodeName2].
     *
     * @return true or false on whether the edge was removed successfully.
     */
    override fun delEdge(nodeName1: String, nodeName2: String): Boolean {
        for (edge in this.edges) {
            if ((edge.nodes.first.name == nodeName1) and (edge.nodes.second.name == nodeName2)) {
                edge.nodes.first.delEdge(edge)
                edge.nodes.second.delEdge(edge)
                this.edges.remove(edge)
                return true
            } else if ((edge.nodes.first.name == nodeName2) and (edge.nodes.second.name == nodeName1)) {
                edge.nodes.first.delEdge(edge)
                edge.nodes.second.delEdge(edge)
                this.edges.remove(edge)
                return true
            }
        }
        throw Exception("Edge not found in this floor!")
    }

    /**
     * Uploads a new floor plan and converts it to a String.
     * TODO
     */
    override fun uploadFloorPlan() {

    }
}

class Node(
    override var name: String = "", override val building: Building,
    override val floor: Floor, override var coords: Pair<Int, Int> = Pair(0, 0),
    override var cat: Int = 0, override val edges: ArrayList<Edge> = ArrayList(),
    override val additionalInfo: ArrayList<String>? = null
) : INode {

    companion object {

        var uniqueID: Int = 0

        fun getNewID(): Int {
            return uniqueID++
        }

    }

    override val id: Int = getNewID()

    /**
     * Updates the nodes coordinates with [x] and [y].
     */
    override fun updateCoords(x: Int, y: Int) {
        this.coords = Pair(x, y)
    }

    /**
     * Updates the node category number with [cat].
     */
    override fun updateCategory(cat: Int) {
        this.cat = cat
    }

    /**
     * Renames the node with [name]. Checks parent floor if new name is not already present.
     */
    override fun rename(name: String) {
        this.name = name
    }

    /**
     * Adds [edge] to node's edge list.
     */
    override fun addEdge(edge: Edge) {
        // Check if edge already exists
        if (edge in this.edges) {
            throw Exception("Edge already exists in this node!")
        }

        this.edges.add(edge)
    }

    /**
     * Attempts to find and remove Edge [edge].
     *
     * @return true or false on whether the Edge was removed successfully.
     */
    override fun delEdge(edge: Edge): Boolean {

        if (edge in this.edges) {
            this.edges.remove(edge)
            return true
        }

        throw Exception("Edge does not exist in this node!")

    }

}

class Edge(
    override val nodes: Pair<Node, Node>,
    override var dist: Float = 0F, override var additionalInfo: ArrayList<String>? = null
) : IEdge {

    companion object {

        var uniqueID: Int = 0

        fun getNewID(): Int {
            return uniqueID++
        }

    }

    override val id: Int = getNewID()

    init {
        calcDistance()
    }

    /**
     * Calculates the distance between the two nodes.
     */
    override fun calcDistance() {
        val deltaX: Float = (this.nodes.first.coords.first - this.nodes.second.coords.first).toFloat()
        val deltaY: Float = (this.nodes.first.coords.second - this.nodes.second.coords.second).toFloat()
        this.dist = sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    /**
     * Gets the ids from the pair of nodes.
     */
    override fun getNodeIDs(): Pair<Int, Int> {
        return Pair(this.nodes.first.id, this.nodes.second.id)
    }
}