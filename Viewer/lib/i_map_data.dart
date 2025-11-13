abstract class IMapData {
  const IMapData(
    String mapName,
    String mapVersion,
    Map<String, IBuilding> buildings,
    List<INode> nodes,
    List<IEdge> edges,
  );

  String getMapName();
  String getMapVersion();
  Map<String, IBuilding> getBuildings();
  List<INode> getNodes();
  List<IEdge> getEdges();
}

abstract class IBuilding {
  const IBuilding(List<IFloor> floors);

  List<IFloor> getFloors();
  Map<String, int> getNodeIds(List<INode> nodes);
  Map<String, int> getNodesIdsByCategory(List<INode> nodes, int cat);
}

abstract class IFloor {
  const IFloor(int level, List<int> nids);

  int getLevel();
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
