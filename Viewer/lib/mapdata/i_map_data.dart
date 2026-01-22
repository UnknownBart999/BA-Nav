import 'map_data.dart';

abstract class IMapData {
  const IMapData(
    String mapName,
    String mapVersion,
    Map<String, IBuilding> buildings,
    List<INode> nodes,
    List<IEdge> edges,
    List<ICategory> categories
  );

  String getMapName();
  String getMapVersion();
  Map<String, IBuilding> getBuildings();
  List<INode> getNodes();
  List<IEdge> getEdges();
  List<ICategory> getCategories();
}

abstract class IBuilding {
  const IBuilding(List<Floor> floors);

  List<IFloor> getFloors();
  List<(int, String, int)> getNodeIds(List<INode> nodes);
  List<(int, String, int)> getNodesIdsByCategory(List<INode> nodes, int cat);
}

abstract class IFloor {
  const IFloor(int level, String floorPlanPath, List<int> nids);

  int getLevel();
  String getFloorPlanPath();
  List<int> getNodeIds();
}

abstract class INode {
  const INode(
    String name,
    String buildingName,
    int fid,
    int x,
    int y,
    int cat,
    List<int> eids,
    Map<String, dynamic> add,
  );

  String getName();
  String getBuildingName();
  int getFloorId();
  (int, int) getCoords();
  int getCategory();
  List<int> getEdgeIds();
  Map<String, dynamic> getAdditional();
}

abstract class IEdge {
  const IEdge(int nid1, int nid2, double dist, Map<String, dynamic> add);

  (int, int) getNodeIds();
  double getDistance();
  Map<String, dynamic> getAdditional();
}

abstract class ICategory {
  const ICategory(String name);

  String getName();
}