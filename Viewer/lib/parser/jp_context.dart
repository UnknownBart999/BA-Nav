import 'package:ba_nav/i_map_data.dart';
import 'package:ba_nav/map_data_factory.dart';
import 'package:json_events/json_events.dart';

/// Represents the nested structure of the JSON file. Each context has an [openingLabel?], [parentContext?], and [openingSymbol] ('[' or '{')
enum JPContext {
  root(null, null, JsonEventType.beginObject, 3),
  mapData("mapData", JPContext.root, JsonEventType.beginObject, 4),
  buildings("buildings", JPContext.mapData, JsonEventType.beginArray, null),
  building(null, JPContext.buildings, JsonEventType.beginObject, 2),
  floors("floors", JPContext.building, JsonEventType.beginArray, null),
  floor(null, JPContext.floors, JsonEventType.beginObject, 3),
  nids("nids", JPContext.floor, JsonEventType.beginArray, null),
  nodes("nodes", JPContext.mapData, JsonEventType.beginArray, null),
  node(null, JPContext.nodes, JsonEventType.beginObject, 7),
  eids("eids", JPContext.node, JsonEventType.beginArray, null),
  addnode("add", JPContext.node, JsonEventType.beginObject, null),
  edges("edges", JPContext.mapData, JsonEventType.beginArray, null),
  edge(null, JPContext.edges, JsonEventType.beginObject, 3),
  addedge("add", JPContext.edge, JsonEventType.beginObject, null),
  categories("categories", JPContext.mapData, JsonEventType.beginArray, null),
  category(null, JPContext.categories, JsonEventType.beginObject, 1);

  final String? openingLabel;
  final JPContext? parentContext;
  final JsonEventType openingSymbol;
  final int? numOfElements;
  const JPContext(this.openingLabel, this.parentContext, this.openingSymbol, this.numOfElements);

  /// Used for easy lookup of the next context. Returns null of there exists no valid context, given the parameters
  static JPContext? getNextContext(String? openingLabel, JPContext? parentContext, JsonEventType openingSymbol) {
    final matches = JPContext.values.where((f) => f.openingLabel == openingLabel && f.parentContext == parentContext && f.openingSymbol == openingSymbol);
    return matches.isEmpty ? null : matches.first;
  }

  /// Used to check if attribute is of correct type, and if it belongs to the [context]
  static bool checkAttribute(JPContext context, String? name, dynamic value) {
    switch (context) {
      case JPContext.root when (name == "mapName" || name == "mapVersion") && value is String:
      case JPContext.building when name == "name" && value is String:
      case JPContext.floor when (name == "id" || name == "level") && value is int || name == "floorPlan" && value is String:
      case JPContext.nids when value is int:
      case JPContext.node when (name == "name" || name == "buildingName") && value is String || (name == "id" || name == "fid" || name == "x" || name == "y" || name == "cat") && value is int || name == "add" && value == null:
      case JPContext.eids when value is int:
      case JPContext.edge when (name == "id" || name == "nid1" || name == "nid2") && value is int || name == "dist" && value is double || name == "add" && value == null:
      case JPContext.category when name == "name" && value is String:
      case JPContext.addnode:
      case JPContext.addedge:
        return true;
      default:
        return false;
    }
  }

  /// Creates object based on context, returns null if no object is created, throws if object doesn't have required parameters
  static Object? createObject(JPContext context, Map<JPContext, dynamic> vb, Map<JPContext, int> checkSum) {
    final vbc = vb[context];
    Object obj;

    // Check if all required attributes exist
    if (context.numOfElements != null && checkSum[context] != context.numOfElements) {
      throw "Object $context is missing attributes (only has ${checkSum[context]})!";
    }
    // Check if any of the object lists contain null (ids are not sequential)
    else if (context.numOfElements == null && checkSum[context] != vbc.length) {
      throw "IDs in $context must be sequential!";
    }

    switch (context) {
      case JPContext.root:
        obj = MapDataFactory.createMapData(vbc["mapName"], vbc["mapVersion"], vb[JPContext.buildings], vb[JPContext.nodes].cast<INode>(), vb[JPContext.edges].cast<IEdge>(), vb[JPContext.categories].cast<ICategory>());
        break;
      case JPContext.building:
        obj = MapDataFactory.createBuilding(vb[JPContext.floors].cast<IFloor>());
        break;
      case JPContext.floor:
        obj = MapDataFactory.createFloor(vbc["level"], vbc["floorPlan"], vb[JPContext.nids].cast<int>());
        break;
      case JPContext.node:
        obj = MapDataFactory.createNode(vbc["name"], vbc["buildingName"], vbc["fid"], vbc["x"], vbc["y"], vbc["cat"], vb[JPContext.eids].cast<int>(), vb[JPContext.addnode]);
        vb[JPContext.addnode] = null;
        break;
      case JPContext.edge:
        obj = MapDataFactory.createEdge(vbc["nid1"], vbc["nid2"], vbc["dist"], vb[JPContext.addedge]);
        vb[JPContext.addedge] = null;
        break;
      case JPContext.category:
        obj = MapDataFactory.createCategory(vbc["name"]);
        break;
      default:
        return null;
    }

    return obj;
  }
}