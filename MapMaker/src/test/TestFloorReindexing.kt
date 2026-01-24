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
    
    // Read the exported JSON from the ZIP archive and verify floor IDs
    val objectMapper = jacksonObjectMapper()
    val zipFile = java.io.File("test_floor_reindex.zip")
    
    if (!zipFile.exists()) {
        println("✗ ERROR: Exported ZIP file not found at ${zipFile.absolutePath}")
        return false
    }
    
    // Extract and read map.json from the ZIP
    val jsonTree = java.util.zip.ZipFile(zipFile).use { zip ->
        val jsonEntry = zip.entries().toList().find { it.name == "map.json" }
            ?: throw Exception("map.json not found in ZIP archive")
        
        zip.getInputStream(jsonEntry).use { inputStream ->
            objectMapper.readTree(inputStream)
        }
    }
    
    val buildings = jsonTree.get("mapData").get("buildings")
    
    // Verify Outside building is at index 0
    val outsideBuildingName = buildings.get(0).get("name").asString()
    if (outsideBuildingName != "Outside") {
        println("✗ ERROR: First building should be 'Outside', found '$outsideBuildingName'")
        zipFile.delete()
        return false
    }
    println("\n✓ Outside building at index 0")
    
    // Check Building 1 floors (at index 1 since Outside is at 0)
    val building1Floors = buildings.get(1).get("floors")
    println("\nBuilding 1 exported floor IDs:")
    for (i in 0 until building1Floors.size()) {
        val floorId = building1Floors.get(i).get("id").asInt()
        val floorLevel = building1Floors.get(i).get("level").asInt()
        println("  Floor level $floorLevel -> ID $floorId")
        
        if (floorId != i) {
            println("✗ ERROR: Building 1 floor $i has ID $floorId, expected $i")
            zipFile.delete()
            return false
        }
    }
    
    // Check Building 2 floors (at index 2 since Outside is at 0)
    val building2Floors = buildings.get(2).get("floors")
    println("\nBuilding 2 exported floor IDs:")
    for (i in 0 until building2Floors.size()) {
        val floorId = building2Floors.get(i).get("id").asInt()
        val floorLevel = building2Floors.get(i).get("level").asInt()
        println("  Floor level $floorLevel -> ID $floorId")
        
        if (floorId != i) {
            println("✗ ERROR: Building 2 floor $i has ID $floorId, expected $i")
            zipFile.delete()
            return false
        }
    }
    
    // Clean up test files
    zipFile.delete()
    
    println("\n✓ All floor IDs correctly reindexed per building!")
    println("  Building 1: floors 0, 1, 2")
    println("  Building 2: floors 0, 1")
    
    println("\n========== FLOOR REINDEXING TEST PASSED ==========\n")
    return true
}

fun main() {
    println("\n╔════════════════════════════════════════════════╗")
    println("║       FLOOR REINDEXING VERIFICATION TEST       ║")
    println("╚════════════════════════════════════════════════╝\n")
    
    val passed = testFloorReindexing()
    
    if (passed) {
        println("\n╔════════════════════════════════════════════════╗")
        println("║            ✓ TEST PASSED! ✓                    ║")
        println("╚════════════════════════════════════════════════╝\n")
    } else {
        println("\n╔════════════════════════════════════════════════╗")
        println("║            ✗ TEST FAILED ✗                     ║")
        println("╚════════════════════════════════════════════════╝\n")
    }
}
