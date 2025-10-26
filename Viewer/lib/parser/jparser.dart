import 'dart:convert';
import 'dart:typed_data';
import 'dart:io'; // TESTING TEMP

import 'i_parser.dart';
import '../map_data.dart';

// TODO: Test every edge case, use streams rather than Uint8List in case of large JSON files

typedef JsonObject = Map<String, dynamic>;
typedef MetaCallback = bool Function(JsonObject meta);
typedef BuildingCallback = int Function(JsonObject building);
typedef FloorCallback = int Function(JsonObject floor);
typedef NodeCallback = int Function(JsonObject node);
typedef EdgeCallback = bool Function(JsonObject edge);

class JParser extends IParser {

  @override
  MapData getMapData(Uint8List bytes) {
    late final MapData map;
    late final List<Node?> nodes;
    late String currentBuilding;
    late int currentFloor;
    
    _decodeJson(bytes, (meta) {
        nodes = List.filled(meta["mapData"]["nodeCount"], null);
        map = MapData(meta["mapName"], meta["mapVersion"], nodes);
        return true;
      }, (building) {
        currentBuilding = building["name"];
        return 0;
      }, (floor) {
        try {
          Base64Decoder().convert(floor["floorplan"]);
        } catch (e) {
          throw "Floorplan '${floor["floorplan"]}' is not valid Base64-encoded String!";
        }
        currentFloor = floor["level"];
        return 0;
      }, (node) {
        // Duplicate id check
        if (nodes[node["id"]] != null) {
          throw "Node '$node' has non-unique id!";
        }
        nodes[node["id"]] = Node(node["name"], currentBuilding, node["id"], node["x"], node["y"], node["category"], currentFloor, node["add"], List.empty(growable: true));
        return 0;
      }, (edge) {
        // TODO: Disallow duplicate edges
        nodes[edge["nodeId1"]]!.neighbors.add((edge["nodeId2"], edge["distance"], edge["add"]));
        nodes[edge["nodeId2"]]!.neighbors.add((edge["nodeId1"], edge["distance"], edge["add"]));
        return true;
      });

    return map;
  }

  @override
  Uint8List? getFloorPlan(Uint8List bytes, String building, int floor) {
    late String currentBuilding;
    Uint8List? imgBytes;

    _decodeJson(bytes,
      null,
      (build) {
        if (build["name"] == building) {
          currentBuilding = build["name"];
          return 0;
        }
        return -1;
      },
      (flo) {
        if (currentBuilding == building && flo["level"] == floor) {
          imgBytes = Base64Decoder().convert(flo["floorplan"]);
          return 1;
        }
        return -1;
      },
      null,
      null
    );

    return imgBytes;
  }

  /// Parses JSON bytes, checks correctness of format and data. Inserts callbacks at every step of the reading process.
  /// Inserting callbacks allows for the reusable parsing/validation code to run alongside any custom data fetching code.
  /// This allows for parsing/validation and data fetching to all happen within a single iterations, rather than two seperate iterations.
  ///
  /// Parameters:
  /// - [bytes] UTF-8 encoded JSON bytes
  /// - [metaFunc] Callback for metadata processing
  /// - [buildFunc] Callback for building processing
  /// - [floorFunc] Callback for floor processing
  /// - [nodeFunc] Callback for node processing
  /// - [edgeFunc] Callback for edge processing
  ///   * -1 to continue loop
  ///   *  0 to do nothing
  ///   *  1 to return
  ///   *  true to continue
  ///   *  false to return
  // TODO: Disallow duplicate building names, disallow duplicate floors within the same building
  void _decodeJson(Uint8List bytes, MetaCallback? metaFunc, BuildingCallback? buildFunc, FloorCallback? floorFunc, NodeCallback? nodeFunc, EdgeCallback? edgeFunc) {
    // JSON bytes are encoded in UTF-8
    String decodedString = Utf8Decoder().convert(bytes);
    var json = JsonDecoder().convert(decodedString);

    try {
      _validateMeta(json);
      JsonObject mapData = json["mapData"];
      if (metaFunc != null && !metaFunc(json)) {
        return;
      }

      // Buildings loop
      for (var building in mapData["buildings"]) {
        _validateBuilding(building);
        if (buildFunc != null) {
          int result = buildFunc(building);
          if (result < 0) {
            continue;
          } else if (result > 0) {
            return;
          }
        }
        for (var floor in building["floors"]) {
          _validateFloor(floor);
          if (floorFunc != null) {
            int result = floorFunc(floor);
            if (result < 0) {
              continue;
            } else if (result > 0) {
              return;
            }
          }
          for (var node in floor["nodes"]) {
            _validateNode(node, mapData["nodeCount"]);
            if (nodeFunc != null) {
              int result = nodeFunc(node);
              if (result < 0) {
                continue;
              } else if (result > 0) {
                return;
              }
            }
          }
        }
      }

      // Edges loop
      for (var edge in mapData["edges"]) {
        _validateEdge(edge);
        if (edgeFunc != null && !edgeFunc(edge)) {
          return;
        }
      }
      
    } on String catch (e) {
      throw FormatException(e);
    }
  }

  void _validateMeta(var root) {
    // Metadata check
    if (root is! JsonObject || root["mapName"] is! String || root["mapVersion"] is! String || root["mapData"] is! JsonObject) {
      throw "File is missing metadata and/or metadata has incorrent types!";
    }
    // mapData check
    if (root["mapData"]["buildings"] is! List<dynamic> || root["mapData"]["edges"] is! List<dynamic> || root["mapData"]["nodeCount"] is! int) {
      throw "mapData is missing attributes and/or attributes have incorrect data types!";
    }
  }

  void _validateBuilding(var building) {
    // Building type check
    if (building is! JsonObject) {
      throw "Building '$building' must be an object!";
    }
    // Attribute check
    if (building["name"] is! String || building["floors"] is! List<dynamic>) {
      throw "Building '$building' is missing attributes and/or attributes have incorrect data types!";
    }
  }

  void _validateFloor(var floor) {
    // Floor type check
    if (floor is! JsonObject) {
      throw "Floor '$floor' must be an object!";
    }
    // Attribute check
    if (floor["level"] is! int || floor["nodes"] is! List<dynamic> || floor["floorplan"] is! String) {
      throw "Floor '$floor' is missing attributes and/or attributes have incorrect data types!";
    }
  }

  void _validateNode(var node, int nodeCount) {
    // Node type check
    if (node is! JsonObject) {
      throw "Node '$node' must be an object!";
    }
    // Attribute check
    if (node["name"] is! String || node["id"] is! int || node["x"] is! int || node["y"] is! int || node["category"] is! int) {
      throw "Node '$node' is missing attributes and/or attributes have incorrect data types!";
    }
    // Check if id is larger than nodeCount
    if (node["id"] >= nodeCount) {
      throw "Node ID '${node["id"]}' is larger that nodeCount '$nodeCount'";
    }
    // Set add to null if it's empty (less memory) or not a dictionary
    if (node["add"] is! JsonObject || node["add"].isEmpty) {
      node["add"] = null;
    }
  }

  void _validateEdge(var edge) {
    // Edge type check
    if (edge is! JsonObject) {
      throw "Edge '$edge' must be an object!";
    }
    // Attribute check
    if (edge["nodeId1"] is! int || edge["nodeId2"] is! int || edge["distance"] is! double) {
      throw "Edge '$edge' is missing attributes and/or attributes have incorrect data types!";
    }
    // Set add to null if it's empty (less memory) or not a dictionary
    if (edge["add"] is! JsonObject || edge["add"].isEmpty) {
      edge["add"] = null;
    }
  }
  
}

// TESTING CODE
Future<void> main() async {
  JParser parser = JParser();
  File file = File('test.json');
  Uint8List? m = parser.getFloorPlan(await file.readAsBytes(), "C1", 0);
  print(m);
}