package mapmaker.test

import mapmaker.mapdata.*

fun testMapData(): Boolean {
    println("========== TESTING MAPDATA CLASS ==========\n")
    
    // Create MapData
    val mapData = MapData("AGH", "0.0.1", categories = arrayListOf("Entrance", "Exit", "Room"))
    println("✓ TestMapData: Successfully created MapData object.")

    // Test empty building name
    try {
        mapData.newBuilding("")
        println("✗ TestMapData: Failed to catch empty building name.")
        return false
    } catch (e: Exception) {
        if (e.message != "Building name cannot be empty!") {
            println("✗ TestMapData: Wrong error for empty building name: ${e.message}")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error for empty building name.")

    // Create new building
    try {
        mapData.newBuilding("testBuilding")
    } catch (_: Exception) {
        println("✗ TestMapData: Failed to create a building.")
        return false
    }
    println("✓ TestMapData: Successfully created building in mapdata object.")

    // Create a building that already exists
    try {
        mapData.newBuilding("testBuilding")
        println("✗ TestMapData: Failed to catch duplicate building name.")
        return false
    } catch (e: Exception) {
        if (e.message != "Building with name \"testBuilding\" already exists!") {
            println("✗ TestMapData: Wrong error message for duplicate building.")
            println("   Expected: Building with name \"testBuilding\" already exists!")
            println("   Got: ${e.message}")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error when creating a building that already exists.")

    // Delete a building
    if (!mapData.delBuilding("testBuilding")) {
        println("✗ TestMapData: Failed to delete a building.")
        return false
    }
    println("✓ TestMapData: Successfully deleted a building in MapData object.")

    // Delete a building that does not exist
    if (mapData.delBuilding("testBuildingFAKE")) {
        println("✗ TestMapData: Deleted a building that does not exist.")
        return false
    }
    println("✓ TestMapData: Successfully returned false when trying to delete a non-existent building.")

    // Update version
    try {
        mapData.updateVersion("1.7.3")
    } catch (_: Exception) {
        println("✗ TestMapData: Failed to update version from 0.0.1 to 1.7.3.")
        return false
    }
    println("✓ TestMapData: Successfully updated map version from 0.0.1 to 1.7.3")

    // Invalid version
    try {
        mapData.updateVersion("1,7,3")
        println("✗ TestMapData: Failed to catch invalid version string.")
        return false
    } catch (e: Exception) {
        if (e.message != "Version Code \"1,7,3\" has invalid character \',\'!") {
            println("✗ TestMapData: Wrong error for invalid version string.")
            println("   Expected: Version Code \"1,7,3\" has invalid character \',\'!")
            println("   Got: ${e.message}")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught an error for invalid version string.")

    // Outdated version
    try {
        mapData.updateVersion("9.9.9")
        mapData.updateVersion("0.0.1")
        println("✗ TestMapData: Failed to catch outdated version.")
        return false
    } catch (e: Exception) {
        if (e.message != "Version Code \"0.0.1\" is not newer than saved version \"9.9.9\"!") {
            println("✗ TestMapData: Wrong error for outdated version.")
            println("   Expected: Version Code \"0.0.1\" is not newer than saved version \"9.9.9\"!")
            println("   Got: ${e.message}")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught an error for outdated version.")

    // Test rename
    mapData.rename("New AGH Name")
    if (mapData.name != "New AGH Name") {
        println("✗ TestMapData: Failed to rename map.")
        return false
    }
    println("✓ TestMapData: Successfully renamed map.")

    // Test external nodes
    mapData.addExternalNode("Outside1", 0, 0, 0)
    println("✓ TestMapData: Successfully added external node.")
    
    // Test duplicate external node
    try {
        mapData.addExternalNode("Outside1", 1, 1, 0)
        println("✗ TestMapData: Failed to catch duplicate external node name.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already exists")) {
            println("✗ TestMapData: Wrong error for duplicate external node.")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error for duplicate external node.")
    
    // Test empty external node name
    try {
        mapData.addExternalNode("", 2, 2, 1)
        println("✗ TestMapData: Failed to catch empty external node name.")
        return false
    } catch (e: Exception) {
        if (e.message != "Node name cannot be empty!") {
            println("✗ TestMapData: Wrong error for empty external node name.")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error for empty external node name.")
    
    // Test invalid category
    try {
        mapData.addExternalNode("Outside2", 3, 3, 99)
        println("✗ TestMapData: Failed to catch invalid category index.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("Invalid category index")) {
            println("✗ TestMapData: Wrong error for invalid category: ${e.message}")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error for invalid category index.")
    
    // Add another external node and create edge
    mapData.addExternalNode("Outside2", 5, 5, 1)
    mapData.addExternalEdge("Outside1", "Outside2")
    println("✓ TestMapData: Successfully added external edge.")
    
    // Test duplicate external edge
    try {
        mapData.addExternalEdge("Outside1", "Outside2")
        println("✗ TestMapData: Failed to catch duplicate external edge.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already exists")) {
            println("✗ TestMapData: Wrong error for duplicate external edge.")
            return false
        }
    }
    println("✓ TestMapData: Successfully caught error for duplicate external edge.")
    
    // Delete external node (should also delete connected edges)
    if (!mapData.delExternalNode("Outside1")) {
        println("✗ TestMapData: Failed to delete external node.")
        return false
    }
    println("✓ TestMapData: Successfully deleted external node.")
    
    // Test getNodes and getEdges
    mapData.newBuilding("B1")
    mapData.buildings[0].newFloor(0)
    mapData.buildings[0].floors[0].addNode("N1", 0, 0, 0)
    mapData.buildings[0].floors[0].addNode("N2", 1, 1, 1)
    mapData.buildings[0].floors[0].addEdge("N1", "N2")
    
    val allNodes = mapData.getNodes()
    if (allNodes.size != 3) { // 2 floor nodes + 1 external node remaining
        println("✗ TestMapData: getNodes returned wrong count. Expected 3, got ${allNodes.size}")
        return false
    }
    println("✓ TestMapData: getNodes() returned correct node count.")
    
    val allEdges = mapData.getEdges()
    if (allEdges.size != 1) { // 1 floor edge (external edge was deleted)
        println("✗ TestMapData: getEdges returned wrong count. Expected 1, got ${allEdges.size}")
        return false
    }
    println("✓ TestMapData: getEdges() returned correct edge count.")

    println("\n========== ALL MAPDATA TESTS PASSED ==========\n")
    return true
}

fun testBuilding(): Boolean {
    println("========== TESTING BUILDING CLASS ==========\n")
    
    val mapData = MapData("Test", "1.0", categories = arrayListOf("Room"))
    val building = mapData.newBuilding("Building1")
    
    // Create floor
    building.newFloor(0)
    println("✓ TestBuilding: Successfully created floor 0.")
    
    // Test duplicate floor level (should fail)
    try {
        building.newFloor(0)
        println("✗ TestBuilding: Failed to catch duplicate floor level.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already exists")) {
            println("✗ TestBuilding: Wrong error for duplicate floor level: ${e.message}")
            return false
        }
    }
    println("✓ TestBuilding: Successfully caught error for duplicate floor level.")
    
    // Test negative floor numbers (basements)
    building.newFloor(-1)
    building.newFloor(-2)
    println("✓ TestBuilding: Successfully created negative floor levels (basements -1, -2).")
    
    // Test positive floor numbers
    building.newFloor(1)
    building.newFloor(2)
    building.newFloor(10)
    println("✓ TestBuilding: Successfully created positive floor levels (1, 2, 10).")
    
    // Verify we have correct number of floors
    if (building.floors.size != 6) { // 0, -1, -2, 1, 2, 10
        println("✗ TestBuilding: Wrong floor count. Expected 6, got ${building.floors.size}")
        return false
    }
    println("✓ TestBuilding: Floor count correct (6 floors: -2, -1, 0, 1, 2, 10).")
    
    // Delete floor
    if (!building.delFloor(0)) {
        println("✗ TestBuilding: Failed to delete floor.")
        return false
    }
    println("✓ TestBuilding: Successfully deleted floor 0.")
    
    // Delete negative floor
    if (!building.delFloor(-1)) {
        println("✗ TestBuilding: Failed to delete negative floor.")
        return false
    }
    println("✓ TestBuilding: Successfully deleted floor -1.")
    
    // Delete non-existent floor
    if (building.delFloor(99)) {
        println("✗ TestBuilding: Deleted non-existent floor.")
        return false
    }
    println("✓ TestBuilding: Correctly returned false for non-existent floor deletion.")
    
    // Test rename
    building.rename("NewBuildingName")
    if (building.name != "NewBuildingName") {
        println("✗ TestBuilding: Failed to rename building.")
        return false
    }
    println("✓ TestBuilding: Successfully renamed building.")
    
    println("\n========== ALL BUILDING TESTS PASSED ==========\n")
    return true
}

fun testFloor(): Boolean {
    println("========== TESTING FLOOR CLASS ==========\n")
    
    val mapData = MapData("Test", "1.0", categories = arrayListOf("Entrance", "Exit", "Room"))
    val building = mapData.newBuilding("Building1")
    val floor = building.newFloor(0)
    
    // Test change level to positive
    floor.changeLevel(5)
    if (floor.level != 5) {
        println("✗ TestFloor: Failed to change floor level to positive.")
        return false
    }
    println("✓ TestFloor: Successfully changed floor level to positive (5).")
    
    // Test change level to negative
    floor.changeLevel(-3)
    if (floor.level != -3) {
        println("✗ TestFloor: Failed to change floor level to negative.")
        return false
    }
    println("✓ TestFloor: Successfully changed floor level to negative (-3).")
    
    // Test change level to duplicate (should fail)
    val floor2 = building.newFloor(1)
    try {
        floor.changeLevel(1) // floor2 already has level 1
        println("✗ TestFloor: Failed to catch duplicate level when changing floor level.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already has level")) {
            println("✗ TestFloor: Wrong error for duplicate level change: ${e.message}")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error when changing to duplicate floor level.")
    
    // Change back to valid level
    floor.changeLevel(0)
    println("✓ TestFloor: Successfully changed floor level back to 0.")
    
    // Add node
    floor.addNode("Node1", 0, 0, 0)
    println("✓ TestFloor: Successfully added node to floor.")
    
    // Test duplicate node name
    try {
        floor.addNode("Node1", 1, 1, 1)
        println("✗ TestFloor: Failed to catch duplicate node name.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already exists")) {
            println("✗ TestFloor: Wrong error for duplicate node name.")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error for duplicate node name.")
    
    // Test empty node name
    try {
        floor.addNode("", 2, 2, 1)
        println("✗ TestFloor: Failed to catch empty node name.")
        return false
    } catch (e: Exception) {
        if (e.message != "Node name cannot be empty!") {
            println("✗ TestFloor: Wrong error for empty node name: ${e.message}")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error for empty node name.")
    
    // Test invalid category
    try {
        floor.addNode("Node2", 3, 3, 10)
        println("✗ TestFloor: Failed to catch invalid category index.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("Invalid category index")) {
            println("✗ TestFloor: Wrong error for invalid category: ${e.message}")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error for invalid category index.")
    
    // Add more nodes and edges
    floor.addNode("Node2", 5, 5, 1)
    floor.addNode("Node3", 10, 10, 2)
    floor.addEdge("Node1", "Node2")
    println("✓ TestFloor: Successfully added edge between nodes.")
    
    // Test duplicate edge
    try {
        floor.addEdge("Node1", "Node2")
        println("✗ TestFloor: Failed to catch duplicate edge.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("already exists")) {
            println("✗ TestFloor: Wrong error for duplicate edge.")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error for duplicate edge.")
    
    // Test edge with non-existent nodes
    try {
        floor.addEdge("Node1", "NonExistent")
        println("✗ TestFloor: Failed to catch edge with non-existent node.")
        return false
    } catch (e: Exception) {
        if (!e.message!!.contains("not present")) {
            println("✗ TestFloor: Wrong error for non-existent node in edge.")
            return false
        }
    }
    println("✓ TestFloor: Successfully caught error for edge with non-existent node.")
    
    // Test delete node (should also delete connected edges)
    val edgeCountBefore = floor.edges.size
    if (!floor.delNode("Node1")) {
        println("✗ TestFloor: Failed to delete node.")
        return false
    }
    if (floor.edges.size >= edgeCountBefore) {
        println("✗ TestFloor: Edges not removed when node deleted.")
        return false
    }
    println("✓ TestFloor: Successfully deleted node and its connected edges.")
    
    // Test delete edge
    floor.addNode("Node1", 0, 0, 0)
    floor.addEdge("Node1", "Node2")
    if (!floor.delEdge("Node1", "Node2")) {
        println("✗ TestFloor: Failed to delete edge.")
        return false
    }
    println("✓ TestFloor: Successfully deleted edge.")
    
    // Test external edge
    val extNode = mapData.addExternalNode("Outside", 20, 20, 0)
    floor.addExternalEdge("Node1", extNode)
    println("✓ TestFloor: Successfully added external edge.")
    
    // Test cross-floor edge
    val floor3 = building.newFloor(2)
    floor3.addNode("Node4", 0, 0, 0)
    floor.addCrossFloorEdge("Node1", "Node4")
    println("✓ TestFloor: Successfully added cross-floor edge.")
    
    println("\n========== ALL FLOOR TESTS PASSED ==========\n")
    return true
}

fun testNode(): Boolean {
    println("========== TESTING NODE CLASS ==========\n")
    
    val mapData = MapData("Test", "1.0", categories = arrayListOf("Room", "Exit"))
    val building = mapData.newBuilding("B1")
    val floor = building.newFloor(0)
    floor.addNode("TestNode", 5, 10, 0)
    val node = floor.nodes[0]
    
    // Test update coords
    node.updateCoords(15, 20)
    if (node.coords.first != 15 || node.coords.second != 20) {
        println("✗ TestNode: Failed to update coordinates.")
        return false
    }
    println("✓ TestNode: Successfully updated node coordinates.")
    
    // Test update category
    node.updateCategory(1)
    if (node.cat != 1) {
        println("✗ TestNode: Failed to update category.")
        return false
    }
    println("✓ TestNode: Successfully updated node category.")
    
    // Test rename
    node.rename("RenamedNode")
    if (node.name != "RenamedNode") {
        println("✗ TestNode: Failed to rename node.")
        return false
    }
    println("✓ TestNode: Successfully renamed node.")
    
    println("\n========== ALL NODE TESTS PASSED ==========\n")
    return true
}

fun testEdge(): Boolean {
    println("========== TESTING EDGE CLASS ==========\n")
    
    val mapData = MapData("Test", "1.0", categories = arrayListOf("Room"))
    val building = mapData.newBuilding("B1")
    val floor = building.newFloor(0)
    floor.addNode("N1", 0, 0, 0)
    floor.addNode("N2", 3, 4, 0)
    floor.addEdge("N1", "N2")
    val edge = floor.edges[0]
    
    // Test automatic distance calculation (3-4-5 triangle)
    if (edge.dist != 5.0f) {
        println("✗ TestEdge: Distance calculation incorrect. Expected 5.0, got ${edge.dist}")
        return false
    }
    println("✓ TestEdge: Distance automatically calculated correctly.")
    
    // Test getNodeIDs
    val nodeIds = edge.getNodeIDs()
    if (nodeIds.first != floor.nodes[0].id || nodeIds.second != floor.nodes[1].id) {
        println("✗ TestEdge: getNodeIDs returned incorrect IDs.")
        return false
    }
    println("✓ TestEdge: getNodeIDs returned correct node IDs.")
    
    // Test manual distance recalculation
    floor.nodes[1].updateCoords(6, 8)
    edge.calcDistance()
    if (edge.dist != 10.0f) {
        println("✗ TestEdge: Distance recalculation incorrect. Expected 10.0, got ${edge.dist}")
        return false
    }
    println("✓ TestEdge: Distance recalculated correctly after node moved.")
    
    println("\n========== ALL EDGE TESTS PASSED ==========\n")
    return true
}

fun main() {
    println("\n╔════════════════════════════════════════════════╗")
    println("║  COMPREHENSIVE MAP OBJECT CREATION TESTS      ║")
    println("╚════════════════════════════════════════════════╝\n")
    
    var allPassed = true
    
    allPassed = testMapData() && allPassed
    allPassed = testBuilding() && allPassed
    allPassed = testFloor() && allPassed
    allPassed = testNode() && allPassed
    allPassed = testEdge() && allPassed
    
    if (allPassed) {
        println("\n╔════════════════════════════════════════════════╗")
        println("║          ✓ ALL TESTS PASSED! ✓               ║")
        println("╚════════════════════════════════════════════════╝\n")
    } else {
        println("\n╔════════════════════════════════════════════════╗")
        println("║          ✗ SOME TESTS FAILED ✗               ║")
        println("╚════════════════════════════════════════════════╝\n")
    }
}