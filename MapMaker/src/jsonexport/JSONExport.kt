package mapmaker.jsonexport
import mapmaker.mapdata.*
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

class JSONExport {

    val exportData: DMapExport

    constructor(map: MapData) {
        val mapName = map.name
        val mapVersion = map.version

        val categories = ArrayList<DCat>()
        for (cat in map.categories) {
            categories.add(DCat(cat))
        }

        val nodes = ArrayList<DNode>()
        for (node in map.getNodes()) {

            val eids = ArrayList<Int>()

            for (edge in node.edges) {
                eids.add(edge.id)
            }

            nodes.add(DNode(node.id, node.name, node.building?.name ?: "Outside", node.floor?.id ?: 0, node.coords.first, node.coords.second, node.cat, eids, node.additionalInfo))
        }

        val edges = ArrayList<DEdge>()
        for (edge in map.getEdges()) {
            edges.add(DEdge(edge.id, edge.nodes.first.id, edge.nodes.second.id, edge.dist, edge.additionalInfo))
        }

        val buildings = ArrayList<DBuilding>()
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

        val mapData = DMapData(buildings, nodes, edges, categories)
        this.exportData = DMapExport(mapName, mapVersion, mapData)

    }

    fun export() {

        val objectMapper = jacksonObjectMapper()
        objectMapper.writeValue(File(this.exportData.mapName+"_"+this.exportData.mapVersion+".json"), this.exportData)

    }

    fun exportToPath(filePath: String) {

        val objectMapper = jacksonObjectMapper()
        objectMapper.writeValue(File(filePath), this.exportData)

    }

}