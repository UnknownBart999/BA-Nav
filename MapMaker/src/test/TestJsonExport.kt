package mapmaker.test

import mapmaker.mapdata.*

/**
 * Test Map 1: University Campus
 * Features:
 * - 3 buildings (Library, Student Center, Science Building)
 * - Multiple floors per building
 * - Staircase connections between floors
 * - External campus nodes (parking, quad, entrance)
 * - Building-to-building connections (walkway)
 */
fun createCampusMap(): MapData {
    println("\n========== Creating Campus Map ==========")
    
    val map = MapData(
        name = "University Campus", 
        version = "1.0.0",
        categories = arrayListOf("Entrance", "Exit", "Room", "Staircase", "Hallway", "Walkway", "Parking")
    )
    
    // Create external campus nodes
    map.addExternalNode("Campus Main Entrance", 0, 0, 0)
    map.addExternalNode("Quad Center", 50, 50, 4)
    map.addExternalNode("Parking Lot A", -20, 10, 6)
    map.addExternalNode("Parking Lot B", 100, 80, 6)
    
    // Connect external areas
    map.addExternalEdge("Campus Main Entrance", "Quad Center")
    map.addExternalEdge("Quad Center", "Parking Lot A")
    map.addExternalEdge("Quad Center", "Parking Lot B")
    
    // Building 1: Library (3 floors)
    map.newBuilding("Library")
    val library = map.buildings[0]
    
    // Library Ground Floor (0)
    library.newFloor(0)
    library.floors[0].addNode("Library Entrance", 10, 10, 0)
    library.floors[0].addNode("Circulation Desk", 15, 15, 2)
    library.floors[0].addNode("Reading Room 1", 20, 20, 2)
    library.floors[0].addNode("Library Stairwell F0", 25, 10, 3)
    library.floors[0].addNode("Library to Student Center", 30, 15, 5)
    
    library.floors[0].addEdge("Library Entrance", "Circulation Desk")
    library.floors[0].addEdge("Circulation Desk", "Reading Room 1")
    library.floors[0].addEdge("Circulation Desk", "Library Stairwell F0")
    library.floors[0].addEdge("Circulation Desk", "Library to Student Center")
    library.floors[0].addExternalEdge("Library Entrance", map.externalNodes[1]) // Connect to Quad
    
    // Library Floor 1
    library.newFloor(1)
    library.floors[1].addNode("Library Stairwell F1", 25, 10, 3)
    library.floors[1].addNode("Study Area", 20, 15, 2)
    library.floors[1].addNode("Computer Lab", 30, 15, 2)
    library.floors[1].addNode("Research Room", 25, 20, 2)
    
    library.floors[0].addCrossFloorEdge("Library Stairwell F0", "Library Stairwell F1")
    library.floors[1].addEdge("Library Stairwell F1", "Study Area")
    library.floors[1].addEdge("Study Area", "Computer Lab")
    library.floors[1].addEdge("Study Area", "Research Room")
    
    // Library Floor 2
    library.newFloor(2)
    library.floors[2].addNode("Library Stairwell F2", 25, 10, 3)
    library.floors[2].addNode("Archives", 20, 15, 2)
    library.floors[2].addNode("Special Collections", 30, 20, 2)
    
    library.floors[1].addCrossFloorEdge("Library Stairwell F1", "Library Stairwell F2")
    library.floors[2].addEdge("Library Stairwell F2", "Archives")
    library.floors[2].addEdge("Archives", "Special Collections")
    
    // Building 2: Student Center (2 floors)
    map.newBuilding("Student Center")
    val studentCenter = map.buildings[1]
    
    // Student Center Ground Floor (0)
    studentCenter.newFloor(0)
    studentCenter.floors[0].addNode("Student Center Entrance", 40, 15, 0)
    studentCenter.floors[0].addNode("Cafeteria", 45, 20, 2)
    studentCenter.floors[0].addNode("Student Lounge", 50, 15, 2)
    studentCenter.floors[0].addNode("SC Stairwell F0", 55, 10, 3)
    studentCenter.floors[0].addNode("Walkway to Library", 35, 15, 5)
    
    studentCenter.floors[0].addEdge("Student Center Entrance", "Cafeteria")
    studentCenter.floors[0].addEdge("Student Center Entrance", "Student Lounge")
    studentCenter.floors[0].addEdge("Cafeteria", "SC Stairwell F0")
    studentCenter.floors[0].addEdge("Student Center Entrance", "Walkway to Library")
    
    // Connect the walkway between buildings
    studentCenter.floors[0].addExternalEdge("Walkway to Library", library.floors[0].nodes[4]) // Library to Student Center node
    
    // Student Center Floor 1
    studentCenter.newFloor(1)
    studentCenter.floors[1].addNode("SC Stairwell F1", 55, 10, 3)
    studentCenter.floors[1].addNode("Meeting Room A", 50, 15, 2)
    studentCenter.floors[1].addNode("Meeting Room B", 50, 20, 2)
    studentCenter.floors[1].addNode("Office Area", 60, 15, 2)
    
    studentCenter.floors[0].addCrossFloorEdge("SC Stairwell F0", "SC Stairwell F1")
    studentCenter.floors[1].addEdge("SC Stairwell F1", "Meeting Room A")
    studentCenter.floors[1].addEdge("Meeting Room A", "Meeting Room B")
    studentCenter.floors[1].addEdge("SC Stairwell F1", "Office Area")
    
    // Building 3: Science Building (4 floors)
    map.newBuilding("Science Building")
    val science = map.buildings[2]
    
    // Science Building Ground Floor (0)
    science.newFloor(0)
    science.floors[0].addNode("Science Entrance", 70, 70, 0)
    science.floors[0].addNode("Lab 101", 75, 75, 2)
    science.floors[0].addNode("Lab 102", 80, 75, 2)
    science.floors[0].addNode("Science Stairwell F0", 85, 70, 3)
    
    science.floors[0].addEdge("Science Entrance", "Lab 101")
    science.floors[0].addEdge("Lab 101", "Lab 102")
    science.floors[0].addEdge("Lab 102", "Science Stairwell F0")
    science.floors[0].addExternalEdge("Science Entrance", map.externalNodes[3]) // Connect to Parking Lot B
    
    // Science Building Floors 1-3
    for (floor in 1..3) {
        science.newFloor(floor)
        val floorIndex = science.floors.size - 1
        science.floors[floorIndex].addNode("Science Stairwell F$floor", 85, 70, 3)
        science.floors[floorIndex].addNode("Lab ${floor}01", 75, 75, 2)
        science.floors[floorIndex].addNode("Lab ${floor}02", 80, 75, 2)
        science.floors[floorIndex].addNode("Faculty Office $floor", 90, 75, 2)
        
        // Connect to floor below
        science.floors[floorIndex - 1].addCrossFloorEdge(
            "Science Stairwell F${floor - 1}",
            "Science Stairwell F$floor"
        )
        
        science.floors[floorIndex].addEdge("Science Stairwell F$floor", "Lab ${floor}01")
        science.floors[floorIndex].addEdge("Lab ${floor}01", "Lab ${floor}02")
        science.floors[floorIndex].addEdge("Lab ${floor}02", "Faculty Office $floor")
    }
    
    println("✓ Campus Map created: 3 buildings, ${map.getNodes().size} nodes, ${map.getEdges().size} edges")
    return map
}

/**
 * Test Map 2: Hospital Complex
 * Features:
 * - 2 main buildings connected by elevated walkway on floor 2
 * - Main Hospital has basement level (-1) with morgue and storage
 * - Emergency area (external)
 * - Multiple departments across floors (basement to floor 3)
 * - Elevator connections including basement
 * - Floor plan image upload for ground floor
 */
fun createHospitalMap(): MapData {
    println("\n========== Creating Hospital Map ==========")
    
    val map = MapData(
        name = "City Hospital",
        version = "2.1.0",
        categories = arrayListOf("Entrance", "Exit", "Department", "Elevator", "Hallway", "Bridge", "Emergency")
    )
    
    // External nodes
    map.addExternalNode("Main Hospital Entrance", 0, 0, 0)
    map.addExternalNode("Emergency Entrance", -10, 5, 6)
    map.addExternalNode("Ambulance Bay", -15, 10, 6)
    map.addExternalNode("Visitor Parking", 10, -10, 0)
    
    map.addExternalEdge("Main Hospital Entrance", "Emergency Entrance")
    map.addExternalEdge("Emergency Entrance", "Ambulance Bay")
    map.addExternalEdge("Main Hospital Entrance", "Visitor Parking")
    
    // Building 1: Main Hospital (4 floors including basement)
    map.newBuilding("Main Hospital")
    val mainHospital = map.buildings[0]
    
    // Main Hospital Basement -1
    mainHospital.newFloor(-1)
    mainHospital.floors[0].addNode("Basement Entrance", 5, -5, 0)
    mainHospital.floors[0].addNode("Morgue", 10, -5, 2)
    mainHospital.floors[0].addNode("Storage", 15, -5, 2)
    mainHospital.floors[0].addNode("Mechanical Room", 20, -5, 2)
    mainHospital.floors[0].addNode("Basement Elevator", 20, 0, 3)
    
    mainHospital.floors[0].addEdge("Basement Entrance", "Morgue")
    mainHospital.floors[0].addEdge("Morgue", "Storage")
    mainHospital.floors[0].addEdge("Storage", "Mechanical Room")
    mainHospital.floors[0].addEdge("Basement Entrance", "Basement Elevator")
    
    // Main Hospital Floor 1 (Ground)
    mainHospital.newFloor(1)
    mainHospital.floors[1].addNode("Main Lobby", 5, 5, 0)
    mainHospital.floors[1].addNode("Reception", 10, 5, 4)
    mainHospital.floors[1].addNode("Emergency Room", -5, 5, 2)
    mainHospital.floors[1].addNode("Radiology", 15, 10, 2)
    mainHospital.floors[1].addNode("Pharmacy", 10, 15, 2)
    mainHospital.floors[1].addNode("Main Elevator F1", 20, 5, 3)
    
    mainHospital.floors[1].addEdge("Main Lobby", "Reception")
    mainHospital.floors[1].addEdge("Main Lobby", "Emergency Room")
    mainHospital.floors[1].addEdge("Reception", "Radiology")
    mainHospital.floors[1].addEdge("Reception", "Pharmacy")
    mainHospital.floors[1].addEdge("Reception", "Main Elevator F1")
    mainHospital.floors[1].addExternalEdge("Main Lobby", map.externalNodes[0])
    mainHospital.floors[1].addExternalEdge("Emergency Room", map.externalNodes[1])
    
    // Connect basement to ground floor
    mainHospital.floors[0].addCrossFloorEdge("Basement Elevator", "Main Elevator F1")
    
    // Upload floor plan to Main Hospital Floor 1
    try {
        mainHospital.floors[1].uploadFloorPlan("sample_floor_plan.jpg")
        
        // Verify upload succeeded
        if (mainHospital.floors[1].floorPlan.isEmpty()) {
            throw Exception("Floor plan upload succeeded but data is empty!")
        }
        
        println("✓ Floor plan uploaded successfully for Main Hospital Floor 1")
        println("  Floor plan data size: ${mainHospital.floors[1].floorPlan.length} characters")
        
    } catch (e: java.io.FileNotFoundException) {
        // File not found is acceptable - test can continue without floor plan
        println("⚠ Warning: sample_floor_plan.jpg not found - skipping floor plan upload")
        println("  Place sample_floor_plan.jpg in MapMaker directory to test floor plan upload")
    } catch (e: Exception) {
        // Any other error should fail the test
        println("✗ ERROR: Floor plan upload failed: ${e.message}")
        throw e
    }
    
    // Main Hospital Floor 2
    mainHospital.newFloor(2)
    mainHospital.floors[2].addNode("Main Elevator F2", 20, 5, 3)
    mainHospital.floors[2].addNode("Surgery Wing", 15, 10, 2)
    mainHospital.floors[2].addNode("ICU", 25, 10, 2)
    mainHospital.floors[2].addNode("Recovery Room", 20, 15, 2)
    mainHospital.floors[2].addNode("Bridge to Medical Center", 30, 10, 5)
    
    mainHospital.floors[1].addCrossFloorEdge("Main Elevator F1", "Main Elevator F2")
    mainHospital.floors[2].addEdge("Main Elevator F2", "Surgery Wing")
    mainHospital.floors[2].addEdge("Main Elevator F2", "ICU")
    mainHospital.floors[2].addEdge("ICU", "Recovery Room")
    mainHospital.floors[2].addEdge("Main Elevator F2", "Bridge to Medical Center")
    
    // Main Hospital Floor 3
    mainHospital.newFloor(3)
    mainHospital.floors[3].addNode("Main Elevator F3", 20, 5, 3)
    mainHospital.floors[3].addNode("Cardiology", 15, 10, 2)
    mainHospital.floors[3].addNode("Neurology", 25, 10, 2)
    mainHospital.floors[3].addNode("Patient Rooms 301-310", 20, 15, 2)
    
    mainHospital.floors[2].addCrossFloorEdge("Main Elevator F2", "Main Elevator F3")
    mainHospital.floors[3].addEdge("Main Elevator F3", "Cardiology")
    mainHospital.floors[3].addEdge("Main Elevator F3", "Neurology")
    mainHospital.floors[3].addEdge("Main Elevator F3", "Patient Rooms 301-310")
    
    // Building 2: Medical Center (3 floors)
    map.newBuilding("Medical Center")
    val medCenter = map.buildings[1]
    
    // Medical Center Floor 1
    medCenter.newFloor(1)
    medCenter.floors[0].addNode("Med Center Entrance", 40, 5, 0)
    medCenter.floors[0].addNode("Outpatient Clinic", 45, 10, 2)
    medCenter.floors[0].addNode("Lab Services", 50, 10, 2)
    medCenter.floors[0].addNode("Med Center Elevator F1", 55, 5, 3)
    
    medCenter.floors[0].addEdge("Med Center Entrance", "Outpatient Clinic")
    medCenter.floors[0].addEdge("Outpatient Clinic", "Lab Services")
    medCenter.floors[0].addEdge("Outpatient Clinic", "Med Center Elevator F1")
    
    // Medical Center Floor 2 (connects to Main Hospital via bridge)
    medCenter.newFloor(2)
    medCenter.floors[1].addNode("Med Center Elevator F2", 55, 5, 3)
    medCenter.floors[1].addNode("Physical Therapy", 50, 10, 2)
    medCenter.floors[1].addNode("Occupational Therapy", 60, 10, 2)
    medCenter.floors[1].addNode("Bridge from Main Hospital", 40, 10, 5)
    
    medCenter.floors[0].addCrossFloorEdge("Med Center Elevator F1", "Med Center Elevator F2")
    medCenter.floors[1].addEdge("Med Center Elevator F2", "Physical Therapy")
    medCenter.floors[1].addEdge("Med Center Elevator F2", "Occupational Therapy")
    medCenter.floors[1].addEdge("Med Center Elevator F2", "Bridge from Main Hospital")
    
    // Connect the bridge between buildings on Floor 2
    medCenter.floors[1].addExternalEdge("Bridge from Main Hospital", mainHospital.floors[2].nodes[4]) // Bridge to Medical Center node
    
    // Medical Center Floor 3
    medCenter.newFloor(3)
    medCenter.floors[2].addNode("Med Center Elevator F3", 55, 5, 3)
    medCenter.floors[2].addNode("Research Lab", 50, 10, 2)
    medCenter.floors[2].addNode("Conference Room", 60, 10, 2)
    medCenter.floors[2].addNode("Admin Offices", 55, 15, 2)
    
    medCenter.floors[1].addCrossFloorEdge("Med Center Elevator F2", "Med Center Elevator F3")
    medCenter.floors[2].addEdge("Med Center Elevator F3", "Research Lab")
    medCenter.floors[2].addEdge("Med Center Elevator F3", "Conference Room")
    medCenter.floors[2].addEdge("Med Center Elevator F3", "Admin Offices")
    
    println("✓ Hospital Map created: 2 buildings, ${map.getNodes().size} nodes, ${map.getEdges().size} edges")
    return map
}

/**
 * Test Map 3: Shopping Mall
 * Features:
 * - Large mall with 3 buildings (Main Mall, Food Court Wing, Entertainment Wing)
 * - Multiple floors with escalators
 * - Outdoor plaza area
 * - Parking structures
 * - Cross-building connections on multiple levels
 */
fun createMallMap(): MapData {
    println("\n========== Creating Shopping Mall Map ==========")
    
    val map = MapData(
        name = "Metro Shopping Mall",
        version = "3.0.2",
        categories = arrayListOf("Entrance", "Exit", "Store", "Escalator", "Corridor", "Connector", "Parking", "Plaza")
    )
    
    // External outdoor areas
    map.addExternalNode("Main Plaza", 0, 0, 7)
    map.addExternalNode("North Entrance", 0, -10, 0)
    map.addExternalNode("South Entrance", 0, 50, 0)
    map.addExternalNode("East Parking", 50, 0, 6)
    map.addExternalNode("West Parking", -30, 20, 6)
    map.addExternalNode("Food Court Patio", 30, 40, 7)
    
    map.addExternalEdge("North Entrance", "Main Plaza")
    map.addExternalEdge("Main Plaza", "South Entrance")
    map.addExternalEdge("Main Plaza", "East Parking")
    map.addExternalEdge("Main Plaza", "West Parking")
    map.addExternalEdge("South Entrance", "Food Court Patio")
    
    // Building 1: Main Mall (3 floors)
    map.newBuilding("Main Mall")
    val mainMall = map.buildings[0]
    
    // Main Mall Floor 1
    mainMall.newFloor(1)
    mainMall.floors[0].addNode("Main Entrance", 5, 0, 0)
    mainMall.floors[0].addNode("Central Court F1", 10, 10, 4)
    mainMall.floors[0].addNode("North Corridor F1", 10, 0, 4)
    mainMall.floors[0].addNode("South Corridor F1", 10, 20, 4)
    mainMall.floors[0].addNode("Store 101", 5, 10, 2)
    mainMall.floors[0].addNode("Store 102", 15, 5, 2)
    mainMall.floors[0].addNode("Store 103", 15, 15, 2)
    mainMall.floors[0].addNode("Central Escalator F1", 10, 10, 3)
    mainMall.floors[0].addNode("To Food Court F1", 10, 25, 5)
    
    mainMall.floors[0].addEdge("Main Entrance", "North Corridor F1")
    mainMall.floors[0].addEdge("North Corridor F1", "Central Court F1")
    mainMall.floors[0].addEdge("Central Court F1", "South Corridor F1")
    mainMall.floors[0].addEdge("Central Court F1", "Store 101")
    mainMall.floors[0].addEdge("North Corridor F1", "Store 102")
    mainMall.floors[0].addEdge("South Corridor F1", "Store 103")
    mainMall.floors[0].addEdge("Central Court F1", "Central Escalator F1")
    mainMall.floors[0].addEdge("South Corridor F1", "To Food Court F1")
    mainMall.floors[0].addExternalEdge("Main Entrance", map.externalNodes[0]) // Main Plaza
    
    // Main Mall Floor 2
    mainMall.newFloor(2)
    mainMall.floors[1].addNode("Central Court F2", 10, 10, 4)
    mainMall.floors[1].addNode("Central Escalator F2", 10, 10, 3)
    mainMall.floors[1].addNode("North Corridor F2", 10, 0, 4)
    mainMall.floors[1].addNode("South Corridor F2", 10, 20, 4)
    mainMall.floors[1].addNode("Store 201", 5, 10, 2)
    mainMall.floors[1].addNode("Store 202", 15, 5, 2)
    mainMall.floors[1].addNode("Store 203", 15, 15, 2)
    mainMall.floors[1].addNode("Store 204", 5, 20, 2)
    mainMall.floors[1].addNode("To Food Court F2", 10, 25, 5)
    
    mainMall.floors[0].addCrossFloorEdge("Central Escalator F1", "Central Escalator F2")
    mainMall.floors[1].addEdge("Central Court F2", "North Corridor F2")
    mainMall.floors[1].addEdge("Central Court F2", "South Corridor F2")
    mainMall.floors[1].addEdge("Central Court F2", "Store 201")
    mainMall.floors[1].addEdge("North Corridor F2", "Store 202")
    mainMall.floors[1].addEdge("South Corridor F2", "Store 203")
    mainMall.floors[1].addEdge("South Corridor F2", "Store 204")
    mainMall.floors[1].addEdge("South Corridor F2", "To Food Court F2")
    mainMall.floors[1].addEdge("Central Court F2", "Central Escalator F2")
    
    // Main Mall Floor 3
    mainMall.newFloor(3)
    mainMall.floors[2].addNode("Central Court F3", 10, 10, 4)
    mainMall.floors[2].addNode("Central Escalator F3", 10, 10, 3)
    mainMall.floors[2].addNode("Store 301", 5, 10, 2)
    mainMall.floors[2].addNode("Store 302", 15, 10, 2)
    mainMall.floors[2].addNode("Cinema Complex", 10, 20, 2)
    mainMall.floors[2].addNode("To Entertainment Wing F3", 20, 10, 5)
    
    mainMall.floors[1].addCrossFloorEdge("Central Escalator F2", "Central Escalator F3")
    mainMall.floors[2].addEdge("Central Court F3", "Store 301")
    mainMall.floors[2].addEdge("Central Court F3", "Store 302")
    mainMall.floors[2].addEdge("Central Court F3", "Cinema Complex")
    mainMall.floors[2].addEdge("Central Court F3", "Central Escalator F3")
    mainMall.floors[2].addEdge("Central Court F3", "To Entertainment Wing F3")
    
    // Building 2: Food Court Wing (2 floors)
    map.newBuilding("Food Court Wing")
    val foodCourt = map.buildings[1]
    
    // Food Court Floor 1
    foodCourt.newFloor(1)
    foodCourt.floors[0].addNode("From Main Mall F1", 20, 25, 5)
    foodCourt.floors[0].addNode("Food Court Seating", 25, 30, 2)
    foodCourt.floors[0].addNode("Restaurant 1", 20, 35, 2)
    foodCourt.floors[0].addNode("Restaurant 2", 25, 35, 2)
    foodCourt.floors[0].addNode("Restaurant 3", 30, 35, 2)
    foodCourt.floors[0].addNode("Food Court Escalator F1", 25, 25, 3)
    foodCourt.floors[0].addNode("Patio Exit", 25, 40, 1)
    
    foodCourt.floors[0].addEdge("From Main Mall F1", "Food Court Seating")
    foodCourt.floors[0].addEdge("Food Court Seating", "Restaurant 1")
    foodCourt.floors[0].addEdge("Food Court Seating", "Restaurant 2")
    foodCourt.floors[0].addEdge("Food Court Seating", "Restaurant 3")
    foodCourt.floors[0].addEdge("Food Court Seating", "Food Court Escalator F1")
    foodCourt.floors[0].addEdge("Food Court Seating", "Patio Exit")
    foodCourt.floors[0].addExternalEdge("From Main Mall F1", mainMall.floors[0].nodes[8]) // To Food Court F1
    foodCourt.floors[0].addExternalEdge("Patio Exit", map.externalNodes[5]) // Food Court Patio
    
    // Food Court Floor 2
    foodCourt.newFloor(2)
    foodCourt.floors[1].addNode("From Main Mall F2", 20, 25, 5)
    foodCourt.floors[1].addNode("Food Court Escalator F2", 25, 25, 3)
    foodCourt.floors[1].addNode("Restaurant 4", 25, 30, 2)
    foodCourt.floors[1].addNode("Bar & Lounge", 30, 30, 2)
    
    foodCourt.floors[0].addCrossFloorEdge("Food Court Escalator F1", "Food Court Escalator F2")
    foodCourt.floors[1].addEdge("From Main Mall F2", "Food Court Escalator F2")
    foodCourt.floors[1].addEdge("Food Court Escalator F2", "Restaurant 4")
    foodCourt.floors[1].addEdge("Food Court Escalator F2", "Bar & Lounge")
    foodCourt.floors[1].addExternalEdge("From Main Mall F2", mainMall.floors[1].nodes[8]) // To Food Court F2
    
    // Building 3: Entertainment Wing (3 floors)
    map.newBuilding("Entertainment Wing")
    val entertainment = map.buildings[2]
    
    // Entertainment Floor 1
    entertainment.newFloor(1)
    entertainment.floors[0].addNode("Entertainment Entrance", 35, 5, 0)
    entertainment.floors[0].addNode("Arcade", 40, 10, 2)
    entertainment.floors[0].addNode("Bowling Alley", 45, 10, 2)
    entertainment.floors[0].addNode("Entertainment Escalator F1", 40, 5, 3)
    
    entertainment.floors[0].addEdge("Entertainment Entrance", "Arcade")
    entertainment.floors[0].addEdge("Arcade", "Bowling Alley")
    entertainment.floors[0].addEdge("Entertainment Entrance", "Entertainment Escalator F1")
    entertainment.floors[0].addExternalEdge("Entertainment Entrance", map.externalNodes[3]) // East Parking
    
    // Entertainment Floor 2
    entertainment.newFloor(2)
    entertainment.floors[1].addNode("Entertainment Escalator F2", 40, 5, 3)
    entertainment.floors[1].addNode("VR Zone", 40, 10, 2)
    entertainment.floors[1].addNode("Karaoke Rooms", 45, 10, 2)
    
    entertainment.floors[0].addCrossFloorEdge("Entertainment Escalator F1", "Entertainment Escalator F2")
    entertainment.floors[1].addEdge("Entertainment Escalator F2", "VR Zone")
    entertainment.floors[1].addEdge("VR Zone", "Karaoke Rooms")
    
    // Entertainment Floor 3 (connects to Main Mall)
    entertainment.newFloor(3)
    entertainment.floors[2].addNode("Entertainment Escalator F3", 40, 5, 3)
    entertainment.floors[2].addNode("From Main Mall F3", 30, 10, 5)
    entertainment.floors[2].addNode("Game Center", 40, 10, 2)
    entertainment.floors[2].addNode("Kids Play Area", 45, 10, 2)
    
    entertainment.floors[1].addCrossFloorEdge("Entertainment Escalator F2", "Entertainment Escalator F3")
    entertainment.floors[2].addEdge("Entertainment Escalator F3", "From Main Mall F3")
    entertainment.floors[2].addEdge("Entertainment Escalator F3", "Game Center")
    entertainment.floors[2].addEdge("Game Center", "Kids Play Area")
    entertainment.floors[2].addExternalEdge("From Main Mall F3", mainMall.floors[2].nodes[5]) // To Entertainment Wing F3
    
    println("✓ Mall Map created: 3 buildings, ${map.getNodes().size} nodes, ${map.getEdges().size} edges")
    return map
}

fun main() {
    println("\n╔════════════════════════════════════════════════╗")
    println("║      COMPREHENSIVE MAP EXPORT TESTS           ║")
    println("╚════════════════════════════════════════════════╝")
    
    // Create and export all three test maps
    val campusMap = createCampusMap()
    campusMap.export()
    println("✓ Exported: University Campus_1.0.0.json\n")
    
    val hospitalMap = createHospitalMap()
    hospitalMap.export()
    println("✓ Exported: City Hospital_2.1.0.json\n")
    
    val mallMap = createMallMap()
    mallMap.export()
    println("✓ Exported: Metro Shopping Mall_3.0.2.json\n")
    
    println("╔════════════════════════════════════════════════╗")
    println("║          ✓ ALL MAPS EXPORTED! ✓              ║")
    println("╚════════════════════════════════════════════════╝")
    
    println("\nSummary:")
    println("• Campus Map: ${campusMap.buildings.size} buildings, ${campusMap.getNodes().size} nodes, ${campusMap.getEdges().size} edges")
    println("• Hospital Map: ${hospitalMap.buildings.size} buildings, ${hospitalMap.getNodes().size} nodes, ${hospitalMap.getEdges().size} edges (with floor plan)")
    println("• Mall Map: ${mallMap.buildings.size} buildings, ${mallMap.getNodes().size} nodes, ${mallMap.getEdges().size} edges")
    println("\nTotal: ${campusMap.getNodes().size + hospitalMap.getNodes().size + mallMap.getNodes().size} nodes across all maps")
}