abstract class IMapData {
  IMapData(String mapName, String mapVersion, Map<String, IBuilding> buildings, List<INode> nodes, List<IEdge> edges);
  Map<String, IBuilding> getBuildingNames();
}

abstract class IBuilding {
  IBuilding(List<IFloor> floors);
  Map<int, String> getNodes();
  Map<int, String> getNodesByCategory(int cat);
}

abstract class IFloor {
  IFloor(int level, List<int> nids);
}

abstract class INode {
  INode(String name, String buildingName, int fid, int x, int y, int cat, List<int> eids, Map<String, dynamic> add);
}

abstract class IEdge {
  IEdge(int nid1, int nid2, double distance, Map<String, dynamic> add);
}