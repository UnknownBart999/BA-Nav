package mapmaker.jsonexport
import kotlinx.serialization.*

@Serializable
data class DNode (val id: Int, val name: String, val buildingName: String, val fid: Int, val x: Int, val y: Int, val cat: Int, val eids: ArrayList<Int>, val add: Map<String, Any>?)

@Serializable
data class DEdge (val id: Int, val nid1: Int, val nid2: Int, val dist: Float, val add: Map<String, Any>?)

@Serializable
data class DFloor (val id: Int, val level: Int, val floorPlan: String, val nids: ArrayList<Int>)

@Serializable
data class DBuilding (val name: String, val floors: ArrayList<DFloor>)

@Serializable
data class DCat(val catName: String)
