package mapmaker.jsonexport
import mapmaker.mapdata.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
class JSONExport {

    val mapName: String
    val mapVersion: String
    val buildings: ArrayList<DBuilding>
    val nodes: ArrayList<DNode>
    val edges: ArrayList<DEdge>
    val categories: ArrayList<DCat>

    constructor(map: MapData) {
        this.mapName = map.name
        this.mapVersion = map.version

        this.categories = ArrayList<DCat>()
        for (cat in map.categories) {
            this.categories.add(DCat(cat))
        }

        this.nodes = ArrayList<DNode>()
        for (node in map.getNodes()) {

            val eids = ArrayList<Int>()

            for (edge in node.edges) {
                eids.add(edge.id)
            }

            this.nodes.add(DNode(node.id, node.name, node.building.name, node.floor.id, node.coords.first, node.coords.second, node.cat, eids, node.additionalInfo))
        }

        this.edges = ArrayList<DEdge>()
        for (edge in map.getEdges()) {
            this.edges.add(DEdge(edge.id, edge.nodes.first.id, edge.nodes.second.id, edge.dist, edge.additionalInfo))
        }

        this.buildings = ArrayList<DBuilding>()
        for (building in map.buildings) {
            val dfloors = ArrayList<DFloor>()
            for (floor in building.floors) {
                val nids = ArrayList<Int>()

                for (node in floor.nodes) {
                    nids.add(node.id)
                }

                dfloors.add(DFloor(floor.id, floor.level, floor.floorPlan, nids))
            }

            buildings.add(DBuilding(building.name, dfloors))
        }

    }

}