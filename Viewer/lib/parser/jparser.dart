import 'dart:convert';
import 'dart:typed_data';
import 'dart:io';

import 'i_parser.dart';
import '../map_data.dart';

// TODO: Test every edge case

class JParser extends IParser {

  @override
  MapData parse(Uint8List bytes) {
    // JSON bytes are encoded in UTF-8
    String decodedString = Utf8Decoder().convert(bytes);
    var json = JsonDecoder().convert(decodedString);
    MapData map;

    // Any incorrect data type (bad member access) or null violations (when creating Node) are caught
    // and indicate that either the data format is incorrect, or there is missing data
    try {
      List<Node?> nodes = List.filled(json["mapData"]["nodeCount"], null);

      for (var building in json["mapData"].entries) {
        // Skips over nodeCount. If not, building.value.entries fails
        // Skips over edges, they will be observed later
        if (building.key == "nodeCount" || building.key == "edges") {
          continue;
        }

        for (var floor in building.value.entries) {
          // Skips over floors without nodes. This allows floors without nodes (TBD)
          if (floor.value["nodes"] == null) {
            continue;
          }

          for (var node in floor.value["nodes"]) {
            // Fails if there are duplicate node IDs
            if (node[node["id"]] != null) {
              throw "Two nodes of the same ID! (${node["id"]})";
            }

            // If add is empty, store it as null rather than an empty map (less memory)
            if (node["add"] != null && node["add"].isEmpty) {
              node["add"] = null;
            }
            nodes[node["id"]] = Node(node["name"], building.key, node["id"], node["x"], node["y"], node["category"], 0, node["add"], List.empty(growable: true));
          }
        }
      }

      // TODO: Disallow duplicate edges
      for (var edge in json["mapData"]["edges"]) {
        int nodeId1 = edge["nodeId1"];
        int nodeId2 = edge["nodeId2"];
        // If add is empty, store it as null rather than an empty map (less memory)
        if (edge["add"] != null && edge["add"].isEmpty) {
          edge["add"] = null;
        }
        nodes[nodeId1]!.neighbors.add((nodeId2, edge["distance"], edge["add"]));
        nodes[nodeId2]!.neighbors.add((nodeId1, edge["distance"], edge["add"]));
      }

      map = MapData(json["mapName"], json["mapVersion"], nodes.cast<Node>());

    } catch (e) {
      // TODO: Make the exception more specific for easier error detection???
      throw "Invalid format and/or missing data!";
    }

    return map;
  }
  
}

Future<void> main() async {
  JParser parser = JParser();
  File file = File('test.json');
  MapData m = parser.parse(await file.readAsBytes());
  print(m.nodes);
}