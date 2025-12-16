library;

import 'map_data.dart';
import 'trip.dart';
import 'package:image/image.dart' as img;

typedef FloorPlan = img.Image;

// ----- Private variables storing default values for unloading. -----------------------------------------------------------

MapData _defaultMap = MapData("DEFAULT", "0", <String, Building>{}, [], []);
List<String> _defaultCategories = [];
//Trip _defaultTrip = Trip();
FloorPlan _defaultFloorPlan = FloorPlan.empty();

// ----- Public global variables. ------------------------------------------------------------------------------------------

MapData loadedMap = _defaultMap;
List<String> categories = _defaultCategories;
Trip loadedTrip = _defaultTrip;
FloorPlan loadedFloorPlan = _defaultFloorPlan;

// ----- Public utility functions. -----------------------------------------------------------------------------------------

// Unload functions.

void unloadMap() {
  loadedMap = _defaultMap;
  categories = _defaultCategories;
  loadedTrip = _defaultTrip;
  loadedFloorPlan = _defaultFloorPlan;
}

void unloadTrip() {
  loadedTrip = _defaultTrip;
}

void unloadFloorPlan() {
  loadedFloorPlan = _defaultFloorPlan;
}

// Trip functions

/// TODO Implement function
/// Creates a new trip using a start node [nid1] and a destination node [nid2].
///
/// Use [segmentNumber] to specify which segment to return.
/// Format: (total segs, BN, fn, FP, coords list)
(int, String, int, img.Image, List<int>) tripFromTo(
  int nid1,
  int nid2,
  int segmentNumber,
) {
  return (0, "", 0, FloorPlan.empty(), []);
}

/// TODO Implement function.
/// Creates a new trip using a start node [nid1] and a destination category [cat].
///
/// Use [segmentNumber] to specify which segment to return.
/// Format: (total segs, BN, fn, FP, coords list)
(int, String, int, img.Image, List<int>) tripFromFind(
  int nid1,
  String cat, {
  int segmentNumber = 0,
}) {
  return (0, "", 0, FloorPlan.empty(), []);
}

// Map functions

// TODO Implement function.
bool openMap(String path) {
  return false;
}

// Gets the building names in the loaded Map.
Iterable<String> getBuildingNames() {
  return loadedMap.getBuildings().keys;
}

// Get list of available actegories in the map.
List<String> getCategories() {
  return categories;
}

/// Get all nodes in a selected building.
///
/// Format [(floor number, node name, category), ...]
List<(int, String, String)> getNodesInBuilding(String buildingName) {
  // If building name not in buildings, return empty list.
  if (!loadedMap.getBuildings().containsKey(buildingName)) {
    return [];
  }

  Building? selectedBuilding = loadedMap.getBuildings()[buildingName];
  List<(int, String, String)> output = [];

  if (selectedBuilding == null) {
    return output;
  }

  // Get nodes in format [(floor num, node name, category id), ...]
  List<(int, String, int)> obtainedNodes = selectedBuilding.getNodeIds(
    loadedMap.getNodes(),
  );

  // Convert category id to category
  for (final element in obtainedNodes) {
    output.add((element.$1, element.$2, categories[element.$3]));
  }

  return output;
}

/// Get all nodes in the map.
///
/// Format [(Building name, floor num, node name, category), ...]
List<(String, int, String, String)> getAllNodes() {
  List<(String, int, String, String)> output = [];

  List<Node> nodes = loadedMap.getNodes();

  for (final node in nodes) {
    output.add((
      node.getBuildingName(),
      node.getFloorId(),
      node.getName(),
      categories[node.getCategory()],
    ));
  }

  return output;
}
