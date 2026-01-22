package mapmaker.test

import mapmaker.mapdata.*
import mapmaker.jsonexport.*
import tools.jackson.module.kotlin.jacksonObjectMapper

/**
 * Test to verify that floor IDs are reindexed per building during export.
 * Each building's floors should have IDs starting from 0.
 */
fun testFloorReindexing(): Boolean {
    println("========== TESTING FLOOR REINDEXING PER BUILDING ==========\n")
    
    // Create a simple map with 2 buildings
    val map = MapData(
        name = "Test Map",
        version = "1.0.0",
        categories = arrayListOf("Room")
    )
    
    // Building 1: 3 floors
    val building1 = map.newBuilding("Building 1")
    building1.newFloor(0)
    building1.newFloor(1)
    building1.newFloor(2)
    
    // Add at least one node per floor for validation
    building1.floors[0].addNode("B1-F0-N1", 0, 0, 0)
    building1.floors[1].addNode("B1-F1-N1", 0, 0, 0)
    building1.floors[2].addNode("B1-F2-N1", 0, 0, 0)
    
    println("Created Building 1 with 3 floors")
    println("  Original floor IDs: ${building1.floors.map { it.id }}")
    
    // Building 2: 2 floors
    val building2 = map.newBuilding("Building 2")
    building2.newFloor(0)
    building2.newFloor(1)
    
    // Add at least one node per floor
    building2.floors[0].addNode("B2-F0-N1", 0, 0, 0)
    building2.floors[1].addNode("B2-F1-N1", 0, 0, 0)
    
    println("Created Building 2 with 2 floors")
    println("  Original floor IDs: ${building2.floors.map { it.id }}")
    
    // Export the map
    map.export("test_floor_reindex", false, null)
    println("\n✓ Exported map")
    
    // Read the exported JSON from the directory and verify floor IDs
    val objectMapper = jacksonObjectMapper()
    val exportDir = java.io.File("test_floor_reindex")
    val file = java.io.File(exportDir, "test_floor_reindex.json")
    
    if (!file.exists()) {
        println("✗ ERROR: Exported JSON file not found at ${file.absolutePath}")
        return false
    }
    
    val jsonTree = objectMapper.readTree(file)
    val buildings = jsonTree.get("mapData").get("buildings")
    
    // Check Building 1 floors
    val building1Floors = buildings.get(0).get("floors")
    println("\nBuilding 1 exported floor IDs:")
    for (i in 0 until building1Floors.size()) {
        val floorId = building1Floors.get(i).get("id").asInt()
        val floorLevel = building1Floors.get(i).get("level").asInt()
        println("  Floor level $floorLevel -> ID $floorId")
        
        if (floorId != i) {
            println("✗ ERROR: Building 1 floor $i has ID $floorId, expected $i")
            file.delete()
            return false
        }
    }
    
    // Check Building 2 floors
    val building2Floors = buildings.get(1).get("floors")
    println("\nBuilding 2 exported floor IDs:")
    for (i in 0 until building2Floors.size()) {
        val floorId = building2Floors.get(i).get("id").asInt()
        val floorLevel = building2Floors.get(i).get("level").asInt()
        println("  Floor level $floorLevel -> ID $floorId")
        
        if (floorId != i) {
            println("✗ ERROR: Building 2 floor $i has ID $floorId, expected $i")
            file.delete()
            return false
        }
    }
    
    // Clean up test files
    exportDir.deleteRecursively()
    val zipFile = java.io.File("test_floor_reindex.zip")
    zipFile.delete()
    
    println("\n✓ All floor IDs correctly reindexed per building!")
    println("  Building 1: floors 0, 1, 2")
    println("  Building 2: floors 0, 1")
    
    println("\n========== FLOOR REINDEXING TEST PASSED ==========\n")
    return true
}

fun main() {
    println("\n╔════════════════════════════════════════════════╗")
    println("║      FLOOR REINDEXING VERIFICATION TEST       ║")
    println("╚════════════════════════════════════════════════╝\n")
    
    val passed = testFloorReindexing()
    
    if (passed) {
        println("\n╔════════════════════════════════════════════════╗")
        println("║          ✓ TEST PASSED! ✓                    ║")
        println("╚════════════════════════════════════════════════╝\n")
    } else {
        println("\n╔════════════════════════════════════════════════╗")
        println("║          ✗ TEST FAILED ✗                     ║")
        println("╚════════════════════════════════════════════════╝\n")
    }
}
