library;

import 'dart:io';
import 'package:archive/archive.dart';
import 'package:archive/archive_io.dart';
import 'package:ba_nav/mapdata/map_data.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

import 'package:ba_nav/path/astar.dart';
import 'package:ba_nav/mapdata/i_map_data.dart';
import 'package:ba_nav/path/ucs.dart';

import 'parser/jparser.dart';
import 'path/trip.dart';
import 'package:image/image.dart' as img;

typedef FloorPlan = img.Image;

// ----- Private variables storing default values for unloading. -----------------------------------------------------------

IMapData? _defaultMap;
String _defaultMapPath = "";
Trip? _defaultTrip;
FloorPlan _defaultFloorPlan = FloorPlan.empty();

// ----- Public global variables. ------------------------------------------------------------------------------------------

IMapData? loadedMap = _defaultMap;
String loadedMapPath = _defaultMapPath;
Trip? loadedTrip = _defaultTrip;
FloorPlan loadedFloorPlan = _defaultFloorPlan;

// ----- Public utility functions. -----------------------------------------------------------------------------------------

// Unload functions.

void unloadMap() {
  loadedMap = _defaultMap;
  loadedMapPath = _defaultMapPath;
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

/// Creates a new trip using a start node [nid1] and a destination node [nid2].
///
/// Use [segmentNumber] to specify which segment to return.
/// Format: (total segs, BN, fn, FP, coords list)
Future<(int, String, int, img.Image, List<(int, int)>)> tripFromTo(
  int nid1,
  int nid2,
  int segmentNumber
) async {
  // If current trip is null -> make new trip
  if (loadedTrip == _defaultTrip) {
    loadedTrip = loadedMap != _defaultMap ? astar(nid1, nid2, loadedMap!) : null;
  }
  return _getSegment(segmentNumber);
}

/// Creates a new trip using a start node [nid1] and a destination category [cat].
///
/// Use [segmentNumber] to specify which segment to return.
/// Format: (total segs, BN, fn, FP, coords list)
Future<(int, String, int, img.Image, List<(int, int)>)> tripFromFind(
  int nid1,
  String cat,
  int segmentNumber
) {
  if (loadedTrip == _defaultTrip) {
    loadedTrip = loadedMap != _defaultMap ? ucs(nid1, cat, loadedMap!) : null;
  }
  return _getSegment(segmentNumber);
}

Future<(int, String, int, img.Image, List<(int, int)>)> _getSegment(int segNum) async {
  if (loadedTrip == _defaultTrip || segNum < 0 || segNum >= loadedTrip!.segments.length) {
    return (0, "", 0, _defaultFloorPlan, <(int, int)>[]);
  }

  final seg = loadedTrip!.segments[segNum];
  final buildingName = seg[0].getBuildingName();
  final floor = loadedMap!.getBuildings()[buildingName]!.getFloors()[seg[0].getFloorId()];
  final floorLevel = floor.getLevel();

  final coordsList = <(int, int)>[];
  for (final node in seg) {
    coordsList.add(node.getCoords());
  }

  loadedFloorPlan = _defaultFloorPlan;
  final file = File("$loadedMapPath/${floor.getFloorPlanPath()}");
  if (file.existsSync()) {
    loadedFloorPlan = img.decodeImage(await file.readAsBytes()) ?? _defaultFloorPlan;
  }

  return (loadedTrip!.totalSegments, buildingName, floorLevel, loadedFloorPlan, coordsList);
}

// Map functions

// Loads the map file from path
Future<bool> openMap(String path) async {
  final zipFile = File(path);
  if (!zipFile.existsSync()) {
    loadedMap = _defaultMap;
    loadedMapPath = _defaultMapPath;
    return false;
  }

  final appDir = await getApplicationDocumentsDirectory();
  final zipName = p.basenameWithoutExtension(path);
  final targetDir = Directory(p.join(appDir.path, zipName));
  // Unzips file if the directory doesn't exist yet
  if (!targetDir.existsSync()) {
    final bytes = await zipFile.readAsBytes();
    final archive = ZipDecoder().decodeBytes(bytes);
    targetDir.createSync(recursive: true);

    for (final file in archive.files) {
      final filePath = p.join(targetDir.path, file.name);

      if (file.isFile) {
        final outFile = File(filePath);
        await outFile.parent.create(recursive: true);
        await outFile.writeAsBytes(file.content as List<int>);
      } else {
        await Directory(filePath).create(recursive: true);
      }
    }
  }

  final mapFile = File("${targetDir.path}/map.json");
  final parser = JParser();
  final mapData = await parser.getMapData(mapFile.openRead());
  if (mapData == null) {
    loadedMap = _defaultMap;
    loadedMapPath = _defaultMapPath;
    return false;
  }
  loadedMap = mapData;
  loadedMapPath = targetDir.path;
  return true;
}

// Gets the building names in the loaded Map.
Iterable<String> getBuildingNames() {
  return loadedMap?.getBuildings().keys ?? Iterable.empty();
}

// Get list of available actegories in the map.
List<String> getCategories() {
  return List.generate(loadedMap?.getCategories().length ?? 0, (i) {
    return loadedMap!.getCategories()[i].getName();
  });
}

/// Get all nodes in a selected building.
///
/// Format [(floor number, node name, category), ...]
List<(int, String, String)> getNodesInBuilding(String buildingName) {
  // If building name not in buildings, return empty list.
  IBuilding? selectedBuilding = loadedMap?.getBuildings()[buildingName];
  if (selectedBuilding == null) {
    return [];
  }

  List<(int, String, String)> output = [];

  // Get nodes in format [(floor num, node name, category id), ...]
  List<(int, String, int)> obtainedNodes = selectedBuilding.getNodeIds(
    loadedMap!.getNodes()
  );

  // Convert category id to category
  for (final element in obtainedNodes) {
    output.add((element.$1, element.$2, loadedMap!.getCategories()[element.$3].getName()));
  }

  return output;
}

/// Get all nodes in the map.
///
/// Format [(Building name, floor num, node name, category), ...]
List<(String, int, String, String)> getAllNodes() {
  List<(String, int, String, String)> output = [];

  List<INode> nodes = loadedMap?.getNodes() ?? [];

  for (final node in nodes) {
    output.add((
      node.getBuildingName(),
      node.getFloorId(),
      node.getName(),
      loadedMap!.getCategories()[node.getCategory()].getName(),
    ));
  }

  return output;
}

// TESTING CODE
// void main() {
//   openMap("University\ Campus_1.0.0").then((a) {
//     if (a) {
//       //tripFromFind(12, "Outside", segmentNumber)
//       tripFromTo(12, 3, 3).then((value) {
//         print(value);
//       });
//     }
//   });
// }