package mapmaker.test
import mapmaker.mapdata.*

fun createDemoMap(): MapData {

    // initialize the map
    val map = MapData(
        name = "Demo Map",
        version = "1.0.0",
        categories = arrayListOf("Entrance", "Exit", "Room", "Staircase", "Hallway", "Walkway")
    )

    // Get outside building, upload floorplan to floor 0
    val outside = map.buildings.find { it.name == "Outside" }!!
    val outside_floor = outside.floors.find { it.level == 0 }!!
    outside_floor.uploadFloorPlan("src/test/DemoMapImages/OutsideFloorPlan.jpeg")

    // Add building 1 entrance and some walkway nodes
    outside_floor.addNode("B1 Entrance", 1237, 415, 0)
    outside_floor.addNode("Walkway 1", 1000, 415, 5)
    outside_floor.addNode("Walkway 2", 1000, 702, 5)

    outside_floor.addEdge("B1 Entrance", "Walkway 1")
    outside_floor.addEdge("Walkway 1", "Walkway 2")

    // Add building 1
    map.newBuilding("B1")
    val b1 = map.buildings.find { it.name == "B1" }!!
    b1.newFloor(0)
    b1.floors[0].uploadFloorPlan("src/test/DemoMapImages/B1F0FloorPlan.jpeg")

    // Add floor 0 nodes
    b1.floors[0].addNode("B1 Exit", 51, 702, 1)
    b1.floors[0].addNode("F0 Hallway 1", 349, 702, 4)
    b1.floors[0].addNode("F0 Hallway 2", 345, 466, 4)
    b1.floors[0].addNode("F0 Hallway 3", 937, 466, 4)
    b1.floors[0].addNode("F0 Hallway 4", 1409, 466, 4)
    b1.floors[0].addNode("F0 Hallway 5", 1645, 466, 4)
    b1.floors[0].addNode("F0 Hallway 6", 1409, 288, 4)
    b1.floors[0].addNode("F0 Hallway 7", 937, 700, 4)
    b1.floors[0].addNode("F0 Hallway 8", 1411, 700, 4)
    b1.floors[0].addNode("F0 Hallway 9", 347, 940, 4)
    b1.floors[0].addNode("F0 Hallway 10", 937, 936, 4)
    b1.floors[0].addNode("F0 Hallway 11", 1409, 940, 4)
    b1.floors[0].addNode("F0 Hallway 12", 1643, 934, 4)
    b1.floors[0].addNode("Room 0", 467, 406, 2)
    b1.floors[0].addNode("Room 1", 877, 404, 2)
    b1.floors[0].addNode("Room 2", 1531, 290, 2)
    b1.floors[0].addNode("Room 3.1", 347, 1000, 2)
    b1.floors[0].addNode("Room 3.2", 937, 996, 2)
    b1.floors[0].addNode("Room 4", 1643, 1000, 2)
    b1.floors[0].addNode("Staircase F0", 1645, 710, 3)

    // Add floor 0 edges
    b1.floors[0].addEdge("B1 Exit", "F0 Hallway 1")
    b1.floors[0].addEdge("B1 Exit", "F0 Hallway 2")
    b1.floors[0].addEdge("B1 Exit", "F0 Hallway 9")
    b1.floors[0].addEdge("F0 Hallway 1", "F0 Hallway 2")
    b1.floors[0].addEdge("F0 Hallway 2", "F0 Hallway 3")
    b1.floors[0].addEdge("F0 Hallway 3", "F0 Hallway 4")
    b1.floors[0].addEdge("F0 Hallway 4", "F0 Hallway 5")
    b1.floors[0].addEdge("F0 Hallway 4", "F0 Hallway 6")
    b1.floors[0].addEdge("F0 Hallway 3", "F0 Hallway 7")
    b1.floors[0].addEdge("F0 Hallway 4", "F0 Hallway 8")
    b1.floors[0].addEdge("F0 Hallway 5", "Staircase F0")
    b1.floors[0].addEdge("F0 Hallway 1", "F0 Hallway 9")
    b1.floors[0].addEdge("F0 Hallway 9", "F0 Hallway 10")
    b1.floors[0].addEdge("F0 Hallway 10", "F0 Hallway 11")
    b1.floors[0].addEdge("F0 Hallway 11", "F0 Hallway 12")
    b1.floors[0].addEdge("F0 Hallway 7", "F0 Hallway 10")
    b1.floors[0].addEdge("F0 Hallway 8", "F0 Hallway 11")
    b1.floors[0].addEdge("F0 Hallway 8", "Staircase F0")
    b1.floors[0].addEdge("F0 Hallway 12", "Staircase F0")
    b1.floors[0].addEdge("F0 Hallway 4", "Staircase F0")
    b1.floors[0].addEdge("F0 Hallway 11", "Staircase F0")

    // Add floor 1
    b1.newFloor(1)
    val b1_floor1 = b1.floors.find { it.level == 1 }!!
    b1_floor1.uploadFloorPlan("src/test/DemoMapImages/B1F1FloorPlan.jpeg")

    // Add floor 1 nodes
    b1_floor1.addNode("F1 Hallway 1", 1541, 645, 4)
    b1_floor1.addNode("F1 Hallway 2", 1541, 1007, 4)
    b1_floor1.addNode("F1 Hallway 3", 1007, 1005, 4)
    b1_floor1.addNode("F1 Hallway 4", 1004, 645, 4)
    b1_floor1.addNode("F1 Hallway 5", 1007, 356, 4)
    b1_floor1.addNode("F1 Hallway 6", 502, 352, 4)
    b1_floor1.addNode("F1 Hallway 7", 235, 999, 4)
    b1_floor1.addNode("F1 Hallway 8", 502, 352, 4)
    b1_floor1.addNode("Staircase F1", 1718, 645, 3)
    b1_floor1.addNode("Room F1 0", 1421, 706, 2)
    b1_floor1.addNode("Room F1 1", 884, 651, 2)
    b1_floor1.addNode("Room F1 2", 233, 877, 2)

    // Add floor 1 edges
    b1_floor1.addEdge("F1 Hallway 1", "Staircase F1")
    b1_floor1.addEdge("F1 Hallway 1", "F1 Hallway 2")
    b1_floor1.addEdge("F1 Hallway 2", "F1 Hallway 3")
    b1_floor1.addEdge("F1 Hallway 3", "F1 Hallway 4")
    b1_floor1.addEdge("F1 Hallway 4", "F1 Hallway 5")
    b1_floor1.addEdge("F1 Hallway 5", "F1 Hallway 6")
    b1_floor1.addEdge("F1 Hallway 6", "F1 Hallway 7")
    b1_floor1.addEdge("F1 Hallway 7", "F1 Hallway 8")
    b1_floor1.addEdge("F1 Hallway 1", "Room F1 0")
    b1_floor1.addEdge("F1 Hallway 4", "Room F1 1")
    b1_floor1.addEdge("F1 Hallway 8", "Room F1 2")

    return map
}

fun main() {
    val map = createDemoMap()
    map.export("demo_map", true)
}