package mapmaker.test

import mapmaker.mapdata.*

fun testMapData(): Boolean {
    // Create MapData
    val mapData = MapData("AGH", "0.0.1")
    println("TestMapData: Successfully created MapData object.")

    // Create new building
    try {
        mapData.newBuilding("testBuilding")
    } catch (_: Exception) {
        println("TestMapData failed to create a building.")
        return false
    }
    println("TestMapData: Successfully created building in mapdata object.")

    // Create a building that already exists
    try {
        mapData.newBuilding("testBuilding")
    } catch (e: Exception) {
        if (e.message != "Building with name \"testBuilding\" already exists!") {
            println("TestMapData failed to catch correct error from creating an already existing building.")
            println("Error received: ${e.message}")
            println("Expected error: Building with name \"testBuilding\" already exists!")
            return false
        }
    }
    println("TestMapData: Successfully caught error when creating a building that already exists.")

    // Delete a building
    if (!mapData.delBuilding("testBuilding")) {
        println("TestMapData failed to delete a building.")
        return false
    }
    println("TestMapData: Successfully deleted a building in MapData object.")

    // Delete a building that does not exist
    if (mapData.delBuilding("testBuildingFAKE")) {
        println("TestMapData deleted a building that does not exist.")
        return false
    }
    println("TestMapData: Successfully caught an error when trying to delete a building that does not exist.")

    // Update version
    try {
        mapData.updateVersion("1.7.3")
    } catch (_: Exception) {
        println("TestMapData failed to update version from 0.0.1 to 1.7.3.")
        return false
    }
    println("TestMapData: Successfully updated map version from 0.0.1 to 1.7.3")

    // Invalid version
    try {
        mapData.updateVersion("1,7,3")
    } catch (e: Exception) {
        if (e.message != "Version Code \"1,7,3\" has invalid character \',\'!") {
            println("TestMapData failed to catch correct error from an invalid version string.")
            println("Error received: ${e.message}")
            println("Expected error: Version Code \"1,7,3\" has invalid character \',\'!")
            return false
        }
    }
    println("TestMapData: Successfully caught an error trying to update the map version with an invalid version string.")

    // Outdated version
    try {
        mapData.updateVersion("9.9.9")
        mapData.updateVersion("0.0.1")
    } catch (e: Exception) {
        if (e.message != "Version Code \"0.0.1\" is not newer than saved version \"9.9.9\"!") {
            println("TestMapData failed to catch correct error from an outdated version string.")
            println("Error received: ${e.message}")
            println("Expected error: Version Code \"0.0.1\" is not newer than saved version \"9.9.9\"!")
            return false
        }
    }
    println("TestMapData: Successfully caught an error trying to update the version code of the map to an older version.")


    // TODO Test getNodes
    // TODO Test getEdges
    // TODO Test export

    println("\nTestMapData: All tests passed.")
    return true

}

fun main() {
    println("Testing MapData class...\n")
    testMapData()
}