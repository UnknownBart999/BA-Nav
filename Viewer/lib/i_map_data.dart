abstract class IMapData {
  IMapData(String mapName, String mapVersion, Map<String, IBuilding> buildings, List<INode> nodes, List<IEdge> edges);

  String getMapName();
  String getMapVersion();
  Map<String, IBuilding> getBuildings();
  List<INode> getNodes();
  List<IEdge> getEdges();
}

abstract class IBuilding {
  IBuilding(List<IFloor> floors);

  List<IFloor> getFloors();
  Map<String, int> getNodeIds();
  Map<String, int> getNodesIdsByCategory(int cat);
}

abstract class IFloor {
  IFloor(int level, List<int> nids);

  int getLevel();
  List<int> getNodeIds();
}

abstract class INode {
  INode(String name, String buildingName, int fid, int x, int y, int cat, List<int> eids, Map<String, dynamic> add);

  String getName();
  String getBuildingName();
  int getFloorId();
  (int, int) getCoords();
  int getCategory();
  List<int> getEdgeIds();
  Map<String, dynamic> getAdditional();
}

abstract class IEdge {
  IEdge(int nid1, int nid2, double dist, Map<String, dynamic> add);

  (int, int) getNodeIds();
  double getDistance();
  Map<String, dynamic> getAdditional();
}