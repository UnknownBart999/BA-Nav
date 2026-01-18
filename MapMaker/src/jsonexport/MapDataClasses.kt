package mapmaker.jsonexport

data class DNode (val id: Int, val name: String, val buildingName: String, val fid: Int, val x: Int, val y: Int, val cat: Int, val eids: ArrayList<Int>, val add: Map<String, Any>?)

data class DEdge (val id: Int, val nid1: Int, val nid2: Int, val dist: Float, val add: Map<String, Any>?)

data class DFloor (val id: Int, val level: Int, val floorPlan: String, val nids: ArrayList<Int>)

data class DBuilding (val name: String, val floors: ArrayList<DFloor>)

data class DCat(val name: String)

data class DMapData(val buildings: ArrayList<DBuilding>, val nodes: ArrayList<DNode>, val edges: ArrayList<DEdge>, val categories: ArrayList<DCat>)

data class DMapExport(val mapName: String, val mapVersion: String, val mapData: DMapData)
