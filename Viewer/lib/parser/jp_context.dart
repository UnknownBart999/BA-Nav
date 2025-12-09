import 'package:ba_nav/i_map_data.dart';
import 'package:ba_nav/map_data.dart';
import 'package:json_events/json_events.dart';

/// Represents the nested structure of the JSON file. Each context has an [openingLabel?], [parentContext?], and [openingSymbol] ('[' or '{')
enum JPContext {
  root(null, null, JsonEventType.beginObject),
  mapData("mapData", JPContext.root, JsonEventType.beginObject),
  buildings("buildings", JPContext.mapData, JsonEventType.beginArray),
  building(null, JPContext.buildings, JsonEventType.beginObject),
  floors("floors", JPContext.building, JsonEventType.beginArray),
  floor(null, JPContext.floors, JsonEventType.beginObject),
  nids("nids", JPContext.floor, JsonEventType.beginArray),
  nodes("nodes", JPContext.mapData, JsonEventType.beginArray),
  node(null, JPContext.nodes, JsonEventType.beginObject),
  eids("eids", JPContext.node, JsonEventType.beginArray),
  addnode("add", JPContext.node, JsonEventType.beginObject),
  edges("edges", JPContext.mapData, JsonEventType.beginArray),
  edge(null, JPContext.edges, JsonEventType.beginObject),
  addedge("add", JPContext.edge, JsonEventType.beginObject);

  final String? openingLabel;
  final JPContext? parentContext;
  final JsonEventType openingSymbol;
  const JPContext(this.openingLabel, this.parentContext, this.openingSymbol);

  /// Used for easy lookup of the next context. Returns null of there exists no valid context, given the parameters
  static JPContext? getNextContext(String? openingLabel, JPContext? parentContext, JsonEventType openingSymbol) {
    final matches = JPContext.values.where((f) => f.openingLabel == openingLabel && f.parentContext == parentContext && f.openingSymbol == openingSymbol);
    return matches.isEmpty ? null : matches.first;
  }

  /// Used to check if attribute is of correct type, and if it belongs to the [context]
  static bool checkAttribute(JPContext context, String? name, dynamic value) {
    switch (context) {
      case JPContext.root when (name == "mapName" || name == "mapVersion") && value is String:
      case JPContext.mapData when (name == "buildings" || name == "nodes" || name == "edges") && value is String:
      case JPContext.building when name == "name" && value is String:
      case JPContext.floor when (name == "id" || name == "level") && value is int || name == "floorPlan" && value is String:
      case JPContext.nids when value is int:
      case JPContext.node when (name == "name" || name == "buildingName") && value is String || (name == "id" || name == "fid" || name == "x" || name == "y" || name == "cat") && value is int:
      case JPContext.eids when value is int:
      case JPContext.edge when (name == "id" || name == "nid1" || name == "nid2") && value is int || name == "dist" || value is double:
        return true;
      default:
        return false;
    }
  }

  // TODO : Use mapdata factory?? Would be better than just calling the constructor here, reduces coupling
  static Object? createObject(List<JPContext> contextStack, Map<JPContext, dynamic> vb, Map<JPContext, int> checkSum) {
    final context = contextStack.last;
    final sum = checkSum[context];
    final vbc = vb[context];
    Object obj;

    switch (context) {
      case JPContext.root when sum == 3:
        obj = MapData(vbc["mapName"], vbc["mapVersion"], vb[JPContext.buildings], vb[JPContext.nodes].cast<INode>(), vb[JPContext.edges].cast<IEdge>());
        break;
      case JPContext.mapData when sum == 3:
        continue addChecksum;
      case JPContext.building when sum == 2:
        obj = Building(vb[JPContext.floors].cast<IFloor>());
        break;
      case JPContext.floor when sum == 3:
        obj = Floor(vbc["level"], vb[JPContext.nids].cast<int>());
        break;
      case JPContext.node when sum == 7:
        obj = Node(vbc["name"], vbc["buildingName"], vbc["fid"], vbc["x"], vbc["y"], vbc["cat"], vb[JPContext.eids], vb[JPContext.addnode]);
        break;
      case JPContext.edge when sum == 3:
        obj = Edge(vbc["nid1"], vbc["nid2"], vbc["dist"], vb[JPContext.addedge]);
        break;

      // Data from these contexts are already stored in their object (List, Map), so they don't need to be constructed, just add to the checksum
      addChecksum:
      case JPContext.buildings || JPContext.floors || JPContext.nodes || JPContext.edges || JPContext.nids || JPContext.eids || JPContext.addnode || JPContext.addedge:
        if (context != JPContext.addnode && context != JPContext.addedge) { // since addnode/addedge are optional
          final prevContext = contextStack.elementAt(contextStack.length-2);
          checkSum[prevContext] = checkSum[prevContext]! + 1;
        }
        return null;

      default:
        throw "Object $context is missing attributes (only has $sum)!";
    }

    return obj;
  }
}