package mapmaker.mapdata

import kotlin.math.pow
import kotlin.math.sqrt
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Base64
import javax.imageio.ImageIO

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
    override val externalEdges: ArrayList<Edge> = ArrayList(),
    override var categories: ArrayList<String> = ArrayList()
) : IMapData {
    /**
     * Creates a new Building with name [name] and adds it to Maps's building list.
     *
     * @return Reference to the new Building object.
     */
    override fun newBuilding(name: String): Building {

        // Check if name is empty
        if (name.isBlank()) {
            throw Exception("Building name cannot be empty!")
        }

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
     * Exports the map to a JSON file with reindexed IDs to eliminate gaps.
     * 
     * @param name Optional custom name for the file (without extension). If null, uses map name.
     * @param includeVersion If true, appends version to filename. Default is true.
     * @param path Optional directory path where file should be saved. If null, saves in current directory.
     */
    fun export(name: String? = null, includeVersion: Boolean = true, path: String? = null) {
        // Create reindexed copy of the map data
        val reindexedData = reindexMapData()
        
        // Build filename
        val baseName = name ?: this.name
        val fileName = if (includeVersion) {
            "${baseName}_${this.version}.json"
        } else {
            "${baseName}.json"
        }
        
        // Build full file path
        val fullPath = if (path != null) {
            if (path.endsWith("/") || path.endsWith("\\")) {
                path + fileName
            } else {
                path + "/" + fileName
            }
        } else {
            fileName
        }
        
        // Export using JSONExport
        val jsonExport = mapmaker.jsonexport.JSONExport(reindexedData)
        jsonExport.exportToPath(fullPath)
    }

    /**
     * Overload of export() to maintain backward compatibility.
     */
    override fun export() {
        export(null, true, null)
    }

    /**
     * Helper function to create a reindexed copy of the map data.
     * Eliminates gaps in IDs caused by deletions.
     * 
     * @return A new MapData object with sequential IDs starting from 0.
     */
    private fun reindexMapData(): MapData {
        // Create ID mappings
        val oldToNewNodeId = mutableMapOf<Int, Int>()
        val oldToNewEdgeId = mutableMapOf<Int, Int>()
        val oldToNewFloorId = mutableMapOf<Int, Int>()
        
        // Collect and sort all nodes by current ID
        val allNodes = getNodes().sortedBy { it.id }
        allNodes.forEachIndexed { index, node ->
            oldToNewNodeId[node.id] = index
        }
        
        // Collect and sort all edges by current ID
        val allEdges = getEdges().sortedBy { it.id }
        allEdges.forEachIndexed { index, edge ->
            oldToNewEdgeId[edge.id] = index
        }
        
        // Reindex floors per building (each building's floors start at 0)
        for (building in this.buildings) {
            val buildingFloors = building.floors.sortedBy { it.level }
            buildingFloors.forEachIndexed { index, floor ->
                oldToNewFloorId[floor.id] = index
            }
        }
        
        // Create new MapData with reindexed data
        val newMap = MapData(
            name = this.name,
            version = this.version,
            categories = ArrayList(this.categories)
        )
        
        // Create reindexed external nodes
        val externalNodeMap = mutableMapOf<Int, Node>()
        for (node in this.externalNodes) {
            val newId = oldToNewNodeId[node.id]!!
            val newNode = Node(
                name = node.name,
                coords = node.coords,
                cat = node.cat,
                additionalInfo = node.additionalInfo
            )
            newNode.id = newId
            newMap.externalNodes.add(newNode)
            externalNodeMap[node.id] = newNode
        }
        
        // Create reindexed buildings and their contents
        for (building in this.buildings) {
            val newBuilding = Building(
                name = building.name,
                parentMap = newMap
            )
            newMap.buildings.add(newBuilding)
            
            // Create reindexed floors for this building
            for (floor in building.floors) {
                val newFloor = Floor(
                    level = floor.level,
                    floorPlan = floor.floorPlan,
                    parentBuilding = newBuilding
                )
                val newFloorId = oldToNewFloorId[floor.id]!!
                newFloor.id = newFloorId
                newBuilding.floors.add(newFloor)
                
                // Create reindexed nodes for this floor
                for (node in floor.nodes) {
                    val newId = oldToNewNodeId[node.id]!!
                    val newNode = Node(
                        name = node.name,
                        building = newBuilding,
                        floor = newFloor,
                        coords = node.coords,
                        cat = node.cat,
                        additionalInfo = node.additionalInfo
                    )
                    newNode.id = newId
                    newFloor.nodes.add(newNode)
                    externalNodeMap[node.id] = newNode
                }
            }
        }
        
        // Create reindexed edges
        val processedEdges = mutableSetOf<Int>()
        
        // Process external edges
        for (edge in this.externalEdges) {
            if (edge.id in processedEdges) continue
            processedEdges.add(edge.id)
            
            val newId = oldToNewEdgeId[edge.id]!!
            val newNode1 = externalNodeMap[edge.nodes.first.id]!!
            val newNode2 = externalNodeMap[edge.nodes.second.id]!!
            
            val newEdge = Edge(
                nodes = Pair(newNode1, newNode2),
                dist = edge.dist,
                additionalInfo = edge.additionalInfo
            )
            newEdge.id = newId
            newMap.externalEdges.add(newEdge)
            newNode1.edges.add(newEdge)
            newNode2.edges.add(newEdge)
        }
        
        // Process building floor edges
        for (building in this.buildings) {
            val newBuilding = newMap.buildings.find { it.name == building.name }!!
            
            for (floor in building.floors) {
                val newFloor = newBuilding.floors.find { it.level == floor.level }!!
                
                for (edge in floor.edges) {
                    if (edge.id in processedEdges) continue
                    processedEdges.add(edge.id)
                    
                    val newId = oldToNewEdgeId[edge.id]!!
                    val newNode1 = externalNodeMap[edge.nodes.first.id]!!
                    val newNode2 = externalNodeMap[edge.nodes.second.id]!!
                    
                    val newEdge = Edge(
                        nodes = Pair(newNode1, newNode2),
                        dist = edge.dist,
                        additionalInfo = edge.additionalInfo
                    )
                    newEdge.id = newId
                    newFloor.edges.add(newEdge)
                    newNode1.edges.add(newEdge)
                    newNode2.edges.add(newEdge)
                }
            }
        }
        
        return newMap
    }

    /**
     * Helper function for export. Gets the nodes present in the map in a single ArrayList.
     */
    override fun getNodes(): ArrayList<Node> {
        val nodesList = ArrayList<Node>()

        for (building in this.buildings) {
            for (floor in building.floors) {
                nodesList.addAll(floor.nodes)
            }
        }

        nodesList.addAll(this.externalNodes)

        return nodesList
    }

    /**
     * Helper function for export. Gets the edges present in the map in a single ArrayList.
     */
    override fun getEdges(): ArrayList<Edge> {
        val edgesList = ArrayList<Edge>()

        for (building in this.buildings) {
            for (floor in building.floors) {
                edgesList.addAll(floor.edges)
            }
        }

        edgesList.addAll(this.externalEdges)

        return edgesList
    }

    /**
     * Creates a new external node in this map using [name], [x], [y], and [cat].
     * [x] and [y] are the x-y coordinates that the node has.
     * [cat] is the category number for this node. This is useful to differentiate between entrances, exits, rooms, elevators, etc.
     *
     * @return Reference to the newly created Node.
     */
    fun addExternalNode(name: String, x: Int, y: Int, cat: Int): Node {
        // Check if name is empty
        if (name.isBlank()) {
            throw Exception("Node name cannot be empty!")
        }
        
        // Check if external node name already exists
        for (node in this.externalNodes) {
            if (node.name == name) {
                throw Exception("External node with name \"${name}\" already exists!")
            }
        }
        
        // Validate category index
        if (cat < 0 || cat >= this.categories.size) {
            throw Exception("Invalid category index: $cat. Must be between 0 and ${this.categories.size - 1}.")
        }

        val node = Node(name = name, coords = Pair(x, y), cat = cat)
        this.externalNodes.add(node)
        return node
    }

    /**
     * Attempts to delete the node from the external map using [name].
     *
     * @return true or false on whether the node was removed successfully.
     */
    fun delExternalNode(name: String): Boolean {

        for (node in this.externalNodes) {
            if (node.name == name) {
                // Remove all edges connected to this node
                val edgesToRemove = ArrayList<Edge>()
                for (edge in this.externalEdges) {
                    if ((edge.nodes.first == node) or (edge.nodes.second == node)) {
                        edgesToRemove.add(edge)
                    }
                }
                
                for (edge in edgesToRemove) {
                    this.externalEdges.remove(edge)
                    // Remove edge from the other node
                    if (edge.nodes.first == node) {
                        edge.nodes.second.delEdge(edge)
                    } else {
                        edge.nodes.first.delEdge(edge)
                    }
                }

                this.externalNodes.remove(node)
                return true
            }
        }
        return false

    }

    /**
     * Creates a new Edge between two external nodes using [nodeName1] and [nodeName2].
     *
     * @return Reference to the newly created edge.
     */
    fun addExternalEdge(nodeName1: String, nodeName2: String): Edge {

        // Check if edge already exists
        for (edge in this.externalEdges) {
            if ((edge.nodes.first.name == nodeName1) and (edge.nodes.second.name == nodeName2)) {
                throw Exception("Edge already exists in external edges!")
            } else if ((edge.nodes.first.name == nodeName2) and (edge.nodes.second.name == nodeName1)) {
                throw Exception("Edge already exists in external edges!")
            }
        }

        // Check if nodes exist
        var node1: Node? = null
        var node2: Node? = null
        for (node in this.externalNodes) {
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
            throw Exception("One of the passed Nodes is not present in external nodes: (" + node1?.name + "," + node2?.name + ")")
        }

        val edge = Edge(nodes = Pair(node1 as Node, node2 as Node))
        this.externalEdges.add(edge)

        node1.addEdge(edge)
        node2.addEdge(edge)

        return edge
    }

    /**
     * Attempts to delete the edge between external nodes [nodeName1] and [nodeName2].
     *
     * @return true or false on whether the edge was removed successfully.
     */
    fun delExternalEdge(nodeName1: String, nodeName2: String): Boolean {
        for (edge in this.externalEdges) {
            if ((edge.nodes.first.name == nodeName1) and (edge.nodes.second.name == nodeName2)) {
                edge.nodes.first.delEdge(edge)
                edge.nodes.second.delEdge(edge)
                this.externalEdges.remove(edge)
                return true
            } else if ((edge.nodes.first.name == nodeName2) and (edge.nodes.second.name == nodeName1)) {
                edge.nodes.first.delEdge(edge)
                edge.nodes.second.delEdge(edge)
                this.externalEdges.remove(edge)
                return true
            }
        }
        return false
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
     * Floor numbers can be negative (e.g., -1 for basement levels).
     */
    override fun newFloor(num: Int): Floor {
        // Check if floor with this level already exists
        for (floor in this.floors) {
            if (floor.level == num) {
                throw Exception("Floor with level $num already exists in building \"${this.name}\"!")
            }
        }
        
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

    override var id: Int = getNewID()

    /**
     * Changes the current floor's floor number [level].
     * Floor numbers can be negative (e.g., -1 for basement levels).
     */
    override fun changeLevel(level: Int) {
        // Check if another floor in the same building already has this level
        for (floor in this.parentBuilding.floors) {
            if (floor != this && floor.level == level) {
                throw Exception("Another floor in building \"${this.parentBuilding.name}\" already has level $level!")
            }
        }
        
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
        // Check if name is empty
        if (name.isBlank()) {
            throw Exception("Node name cannot be empty!")
        }
        
        // Check if node name already exists in this floor
        for (node in this.nodes) {
            if (node.name == name) {
                throw Exception("Node with name \"${name}\" already exists in this floor!")
            }
        }
        
        // Validate category index
        if (cat < 0 || cat >= this.parentBuilding.parentMap.categories.size) {
            throw Exception("Invalid category index: $cat. Must be between 0 and ${this.parentBuilding.parentMap.categories.size - 1}.")
        }
        
        val node = Node(name = name, coords = Pair(x, y), cat = cat, floor = this, building = this.parentBuilding)
        this.nodes.add(node)
        return node
    }

    /**
     * Attempts to delete the node from the current floor using [name].
     * Also removes all edges connected to this node.
     *
     * @return true or false on whether the node was removed successfully.
     */
    override fun delNode(name: String): Boolean {

        for (node in this.nodes) {
            if (node.name == name) {
                // Remove all edges connected to this node
                val edgesToRemove = ArrayList<Edge>()
                for (edge in this.edges) {
                    if ((edge.nodes.first == node) or (edge.nodes.second == node)) {
                        edgesToRemove.add(edge)
                    }
                }
                
                for (edge in edgesToRemove) {
                    this.edges.remove(edge)
                    // Remove edge from the other node
                    if (edge.nodes.first == node) {
                        edge.nodes.second.edges.remove(edge)
                    } else {
                        edge.nodes.first.edges.remove(edge)
                    }
                }
                
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
            throw Exception("One of the passed Nodes is not present in this floor's Nodes: (" + node1?.name + "," + node2?.name + ")")
        }

        val edge = Edge(nodes = Pair(node1 as Node, node2 as Node))
        this.edges.add(edge)

        node1.addEdge(edge)
        node2.addEdge(edge)

        return edge
    }

    /**
     * Creates a new Edge using [nodeName1] and [nodeName2] where the nodes can be on different floors within the same building.
     * Searches all floors in the parent building for the nodes.
     *
     * @return Reference to the newly created edge.
     */
    fun addCrossFloorEdge(nodeName1: String, nodeName2: String): Edge {

        // Search for both nodes across all floors in the building
        var node1: Node? = null
        var node2: Node? = null

        for (floor in this.parentBuilding.floors) {
            for (node in floor.nodes) {
                if (node.name == nodeName1) {
                    node1 = node
                }
                if (node.name == nodeName2) {
                    node2 = node
                }
                if ((node1 != null) and (node2 != null)) {
                    break
                }
            }
            if ((node1 != null) and (node2 != null)) {
                break
            }
        }

        if ((node1 == null) or (node2 == null)) {
            throw Exception("One of the passed Nodes is not present in this building: ($nodeName1, $nodeName2)")
        }

        // Check if edge already exists in any floor
        for (floor in this.parentBuilding.floors) {
            for (edge in floor.edges) {
                if ((edge.nodes.first == node1) and (edge.nodes.second == node2)) {
                    throw Exception("Edge already exists in this building!")
                } else if ((edge.nodes.first == node2) and (edge.nodes.second == node1)) {
                    throw Exception("Edge already exists in this building!")
                }
            }
        }

        val edge = Edge(nodes = Pair(node1 as Node, node2 as Node), dist = 1f)
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

        val edge = Edge(nodes = Pair(intNode, extNode), dist = 1f)
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
     * Supports PNG, JPG, and JPEG formats. Converts to PNG and encodes as Base64 for JSON storage.
     * 
     * @param path File path to the floor plan image.
     * @throws java.io.FileNotFoundException if the file does not exist
     * @throws Exception if the file is corrupted or cannot be processed
     */
    override fun uploadFloorPlan(path: String) {
        val file = File(path)
        
        // Check if file exists
        if (!file.exists()) {
            throw java.io.FileNotFoundException("Floor plan file not found: $path")
        }
        
        // Check if file is readable
        if (!file.canRead()) {
            throw Exception("Floor plan file exists but cannot be read: $path")
        }
        
        // Check if file is empty
        if (file.length() == 0L) {
            throw Exception("Floor plan file is empty: $path")
        }
        
        // Check file extension
        val extension = file.extension.lowercase()
        if (extension !in listOf("png", "jpg", "jpeg")) {
            throw Exception("Unsupported image format: $extension. Only PNG, JPG, and JPEG are supported.")
        }
        
        try {
            // Read the image file
            val bufferedImage = ImageIO.read(file)
                ?: throw Exception("Failed to read image file. The file may be corrupted or in an unsupported format.")
            
            // Validate image has dimensions
            if (bufferedImage.width <= 0 || bufferedImage.height <= 0) {
                throw Exception("Invalid image dimensions: ${bufferedImage.width}x${bufferedImage.height}")
            }
            
            // Convert to PNG format and write to byte array
            val outputStream = ByteArrayOutputStream()
            val success = ImageIO.write(bufferedImage, "png", outputStream)
            
            if (!success) {
                throw Exception("Failed to encode image as PNG.")
            }
            
            // Convert byte array to Base64 string
            val imageBytes = outputStream.toByteArray()
            
            // Verify we have data
            if (imageBytes.isEmpty()) {
                throw Exception("Image conversion produced no data.")
            }
            
            val base64String = Base64.getEncoder().encodeToString(imageBytes)
            
            // Verify Base64 encoding worked
            if (base64String.isEmpty()) {
                throw Exception("Base64 encoding failed - empty string produced.")
            }
            
            // Store in floorPlan field
            this.floorPlan = base64String
            
        } catch (e: java.io.IOException) {
            throw Exception("IO error while processing floor plan image: ${e.message}", e)
        } catch (e: Exception) {
            // Re-throw if already our exception
            if (e.message?.startsWith("Failed to") == true || 
                e.message?.startsWith("Invalid") == true ||
                e.message?.startsWith("Image") == true ||
                e.message?.startsWith("Base64") == true) {
                throw e
            }
            // Wrap other exceptions
            throw Exception("Unexpected error processing floor plan image: ${e.message}", e)
        }
    }
    
    /**
     * Helper function to get the decoded floor plan image as a ByteArray.
     * Useful for extracting the image from the Base64 string.
     * 
     * @return ByteArray of the PNG image data, or null if no floor plan is set.
     * @throws Exception if floor plan data is corrupted
     */
    fun getFloorPlanBytes(): ByteArray? {
        if (this.floorPlan.isEmpty()) {
            return null
        }
        
        return try {
            val decoded = Base64.getDecoder().decode(this.floorPlan)
            if (decoded.isEmpty()) {
                throw Exception("Floor plan data decoded to empty array")
            }
            decoded
        } catch (e: IllegalArgumentException) {
            throw Exception("Floor plan data is not valid Base64: ${e.message}", e)
        } catch (e: Exception) {
            throw Exception("Failed to decode floor plan data: ${e.message}", e)
        }
    }
    
    /**
     * Helper function to save the floor plan to a file.
     * 
     * @param outputPath Path where the PNG file should be saved.
     * @throws Exception if no floor plan data exists or file cannot be written
     */
    fun saveFloorPlan(outputPath: String) {
        if (outputPath.isBlank()) {
            throw Exception("Output path cannot be empty")
        }
        
        val imageBytes = getFloorPlanBytes()
            ?: throw Exception("No floor plan data to save. Upload a floor plan first using uploadFloorPlan().")
        
        try {
            val outputFile = File(outputPath)
            
            // Check if parent directory exists
            outputFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    throw Exception("Parent directory does not exist: ${parent.absolutePath}")
                }
                if (!parent.canWrite()) {
                    throw Exception("Cannot write to directory: ${parent.absolutePath}")
                }
            }
            
            outputFile.writeBytes(imageBytes)
            
            // Verify file was written
            if (!outputFile.exists() || outputFile.length() == 0L) {
                throw Exception("File was not written successfully")
            }
        } catch (e: IOException) {
            throw Exception("IO error while saving floor plan: ${e.message}", e)
        }
    }
}

class Node(
    override var name: String = "", override val building: Building? = null,
    override val floor: Floor? = null, override var coords: Pair<Int, Int> = Pair(0, 0),
    override var cat: Int = 0, override val edges: ArrayList<Edge> = ArrayList(),
    override val additionalInfo: Map<String, Any>? = null
) : INode {

    companion object {

        var uniqueID: Int = 0

        fun getNewID(): Int {
            return uniqueID++
        }

    }

    override var id: Int = getNewID()

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
    override var dist: Float = 0F, override var additionalInfo: Map<String, Any>? = null
) : IEdge {

    companion object {

        var uniqueID: Int = 0

        fun getNewID(): Int {
            return uniqueID++
        }

    }

    override var id: Int = getNewID()

    init {
        if (this.dist == 0F) {
            calcDistance()
        }
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