import 'package:ba_nav/i_map_data.dart';
import 'package:ba_nav/map_data.dart';

/// Contains factory methods for the map data classes
class MapDataFactory {

  static IMapData createMapData(String mapName, String mapVersion, Map<String, IBuilding> buildings, List<INode> nodes, List<IEdge> edges, List<ICategory> categories) {
    return MapData(mapName, mapVersion, buildings.cast(), nodes.cast(), edges.cast(), categories.cast());
  }

  static IBuilding createBuilding(List<IFloor> floors) {
    return Building(floors.cast());
  }

  static IFloor createFloor(int level, String floorPlanPath, List<int> nids) {
    return Floor(level, floorPlanPath, nids);
  }

  static INode createNode(String name, String buildingName, int fid, int x, int y, int cat, List<int> eids, Map<String, dynamic>? add) {
    return Node(name, buildingName, fid, x, y, cat, eids, add ?? {});
  }

  static IEdge createEdge(int nid1, int nid2, double dist, Map<String, dynamic>? add) {
    return Edge(nid1, nid2, dist, add ?? {});
  }

  static ICategory createCategory(String name) {
    return Category(name);
  }

}