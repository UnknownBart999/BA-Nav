package mapmaker.test

import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.util.zip.ZipFile

data class NodeInfo(val id: Int, val buildingName: String, val fid: Int)
data class EdgeInfo(val id: Int, val nid1: Int, val nid2: Int, val dist: Float)

fun validateEdgeDistances(zipFilePath: String): Boolean {
    println("\n========== Validating Edge Distances in $zipFilePath ==========")
    
    val objectMapper = jacksonObjectMapper()
    val zipFile = ZipFile(File(zipFilePath))
    
    // Read map.json from the ZIP archive
    val jsonTree = zipFile.use { zip ->
        val jsonEntry = zip.entries().toList().find { it.name == "map.json" }
            ?: throw Exception("map.json not found in ZIP archive")
        
        zip.getInputStream(jsonEntry).use { inputStream ->
            objectMapper.readTree(inputStream)
        }
    }
    
    // Parse nodes to build a map of node ID -> building & floor
    val nodeMap = mutableMapOf<Int, NodeInfo>()
    val nodesArray = jsonTree.get("mapData").get("nodes")
    for (i in 0 until nodesArray.size()) {
        val node = nodesArray.get(i)
        val id = node.get("id").asInt()
        val buildingName = node.get("buildingName").asText()
        val fid = node.get("fid").asInt()
        nodeMap[id] = NodeInfo(id, buildingName, fid)
    }
    
    // Parse edges
    val edgesArray = jsonTree.get("mapData").get("edges")
    val crossFloorEdges = mutableListOf<EdgeInfo>()
    val externalEdges = mutableListOf<EdgeInfo>()
    val regularEdges = mutableListOf<EdgeInfo>()
    
    for (i in 0 until edgesArray.size()) {
        val edge = edgesArray.get(i)
        val id = edge.get("id").asInt()
        val nid1 = edge.get("nid1").asInt()
        val nid2 = edge.get("nid2").asInt()
        val dist = edge.get("dist").asDouble().toFloat()
        
        val node1 = nodeMap[nid1]!!
        val node2 = nodeMap[nid2]!!
        
        val edgeInfo = EdgeInfo(id, nid1, nid2, dist)
        
        // Categorize edge
        if (node1.buildingName == "Outside" || node2.buildingName == "Outside") {
            // One node is external
            if (node1.buildingName != node2.buildingName) {
                externalEdges.add(edgeInfo)
            }
        } else if (node1.buildingName == node2.buildingName && node1.fid != node2.fid) {
            // Same building, different floors
            crossFloorEdges.add(edgeInfo)
        } else {
            // Regular edge (same floor or both external)
            regularEdges.add(edgeInfo)
        }
    }
    
    println("\nEdge Statistics:")
    println("  Total edges: ${edgesArray.size()}")
    println("  Cross-floor edges: ${crossFloorEdges.size}")
    println("  External edges (building ↔ outside): ${externalEdges.size}")
    println("  Regular edges (same floor): ${regularEdges.size}")
    
    var allPassed = true
    
    // Validate cross-floor edges
    println("\n--- Cross-Floor Edges (should all have dist=1.0) ---")
    val badCrossFloorEdges = crossFloorEdges.filter { it.dist != 1.0f }
    if (badCrossFloorEdges.isEmpty()) {
        println("✓ All ${crossFloorEdges.size} cross-floor edges have dist=1.0")
    } else {
        println("✗ Found ${badCrossFloorEdges.size} cross-floor edges with incorrect dist:")
        for (edge in badCrossFloorEdges.take(10)) {
            val node1 = nodeMap[edge.nid1]!!
            val node2 = nodeMap[edge.nid2]!!
            println("  Edge ${edge.id}: node ${edge.nid1} (${node1.buildingName} floor ${node1.fid}) ↔ " +
                    "node ${edge.nid2} (${node2.buildingName} floor ${node2.fid}), dist=${edge.dist}")
        }
        if (badCrossFloorEdges.size > 10) {
            println("  ... and ${badCrossFloorEdges.size - 10} more")
        }
        allPassed = false
    }
    
    // Validate external edges
    println("\n--- External Edges (should all have dist=1.0) ---")
    val badExternalEdges = externalEdges.filter { it.dist != 1.0f }
    if (badExternalEdges.isEmpty()) {
        println("✓ All ${externalEdges.size} external edges have dist=1.0")
    } else {
        println("✗ Found ${badExternalEdges.size} external edges with incorrect dist:")
        for (edge in badExternalEdges.take(10)) {
            val node1 = nodeMap[edge.nid1]!!
            val node2 = nodeMap[edge.nid2]!!
            println("  Edge ${edge.id}: node ${edge.nid1} (${node1.buildingName}) ↔ " +
                    "node ${edge.nid2} (${node2.buildingName}), dist=${edge.dist}")
        }
        if (badExternalEdges.size > 10) {
            println("  ... and ${badExternalEdges.size - 10} more")
        }
        allPassed = false
    }
    
    // Show some examples of correct edges
    if (crossFloorEdges.isNotEmpty()) {
        println("\n--- Example Cross-Floor Edges ---")
        for (edge in crossFloorEdges.take(3)) {
            val node1 = nodeMap[edge.nid1]!!
            val node2 = nodeMap[edge.nid2]!!
            println("  Edge ${edge.id}: node ${edge.nid1} (${node1.buildingName} floor ${node1.fid}) ↔ " +
                    "node ${edge.nid2} (${node2.buildingName} floor ${node2.fid}), dist=${edge.dist}")
        }
    }
    
    if (externalEdges.isNotEmpty()) {
        println("\n--- Example External Edges ---")
        for (edge in externalEdges.take(3)) {
            val node1 = nodeMap[edge.nid1]!!
            val node2 = nodeMap[edge.nid2]!!
            println("  Edge ${edge.id}: node ${edge.nid1} (${node1.buildingName}) ↔ " +
                    "node ${edge.nid2} (${node2.buildingName}), dist=${edge.dist}")
        }
    }
    
    if (allPassed) {
        println("\n✓ All edge distances are correct!")
    } else {
        println("\n✗ Some edge distances are incorrect!")
    }
    
    return allPassed
}

fun main() {
    println("\n╔════════════════════════════════════════════════╗")
    println("║       EDGE DISTANCE VALIDATION TEST            ║")
    println("╚════════════════════════════════════════════════╝")
    
    val files = listOf(
        "University Campus_1.0.0.zip",
        "City Hospital_2.1.0.zip",
        "Metro Shopping Mall_3.0.2.zip"
    )
    
    var allPassed = true
    for (file in files) {
        try {
            val passed = validateEdgeDistances(file)
            allPassed = allPassed && passed
        } catch (e: Exception) {
            println("\n✗ ERROR processing $file: ${e.message}")
            e.printStackTrace()
            allPassed = false
        }
    }
    
    println("\n╔════════════════════════════════════════════════╗")
    if (allPassed) {
        println("║          ✓ ALL TESTS PASSED! ✓               ║")
    } else {
        println("║          ✗ SOME TESTS FAILED ✗               ║")
    }
    println("╚════════════════════════════════════════════════╝\n")
}
