import 'i_map_data.dart';

// MapData class that implements IMapData interface
class MapData extends IMapData {
  final String _mapName, _mapVersion;
  final Map<String, Building> _buildings;
  final List<Node> _nodes;
  final List<Edge> _edges;

  @override
  const MapData(
    this._mapName,
    this._mapVersion,
    this._buildings,
    this._nodes,
    this._edges,
  ) : super(_mapName, _mapVersion, _buildings, _nodes, _edges);

  @override
  String getMapName() {
    return _mapName;
  }

  @override
  String getMapVersion() {
    return _mapVersion;
  }

  @override
  Map<String, Building> getBuildings() {
    return Map.unmodifiable(_buildings);
  }

  @override
  List<Node> getNodes() {
    return List.unmodifiable(_nodes);
  }

  @override
  List<Edge> getEdges() {
    return List.unmodifiable(_edges);
  }
}

// Building class that implements IBuilding interface
class Building extends IBuilding {
  final List<Floor> _floors;

  @override
  Building(this._floors) : super(_floors);

  @override
  List<Floor> getFloors() {
    return List.unmodifiable(_floors);
  }

  @override
  List<(int, String, int)> getNodeIds(List<Node> nodes) {
    return getNodesIdsByCategory(nodes, null);
  }

  @override
  List<(int, String, int)> getNodesIdsByCategory(List<Node> nodes, int? cat) {
    List<(int, String, int)> mappedNids = [];
    List<int> nids = [];

    // Get all node ids in each floor, combine to list.
    for (Floor floor in _floors) {
      nids.addAll(floor.getNodeIds());
    }

    // Go through each node id, get the name for that node, add to map
    for (int nid in nids) {
      Node node = nodes.elementAt(nid);

      if (cat == null || node.getCategory() == cat) {
        mappedNids.add((node.getFloorId(), node.getName(), node.getCategory()));
      }
    }

    nids.clear();
    return List.unmodifiable(mappedNids);
  }
}

// Floor class that implements IFloor interface
class Floor extends IFloor {
  final int _level;
  final List<int> _nids;

  @override
  Floor(this._level, this._nids) : super(_level, _nids);

  @override
  int getLevel() {
    return _level;
  }

  @override
  List<int> getNodeIds() {
    return List.unmodifiable(_nids);
  }
}

// Node class that implements INode interface
class Node extends INode {
  final String _name, _buildingName;
  final int _fid, _x, _y, _cat;
  final List<int> _eids;
  final Map<String, dynamic> _add;

  @override
  Node(
    this._name,
    this._buildingName,
    this._fid,
    this._x,
    this._y,
    this._cat,
    this._eids,
    this._add,
  ) : super(_name, _buildingName, _fid, _x, _y, _cat, _eids, _add);

  @override
  String getName() {
    return _name;
  }

  @override
  String getBuildingName() {
    return _buildingName;
  }

  @override
  int getFloorId() {
    return _fid;
  }

  @override
  (int, int) getCoords() {
    return (_x, _y);
  }

  @override
  int getCategory() {
    return _cat;
  }

  @override
  List<int> getEdgeIds() {
    return List.unmodifiable(_eids);
  }

  @override
  Map<String, dynamic> getAdditional() {
    return Map.unmodifiable(_add);
  }
}

// Edge class that implements IEdge interface
class Edge extends IEdge {
  final int _nid1, _nid2;
  final double _dist;
  final Map<String, dynamic> _add;

  @override
  Edge(this._nid1, this._nid2, this._dist, this._add)
    : super(_nid1, _nid2, _dist, _add);

  @override
  (int, int) getNodeIds() {
    return (_nid1, _nid2);
  }

  @override
  double getDistance() {
    return _dist;
  }

  @override
  Map<String, dynamic> getAdditional() {
    return Map.unmodifiable(_add);
  }
}

class Category extends ICategory {
  final String _name;

  @override
  Category(this._name) : super(_name);

  @override
  String getName() {
    return _name;
  }
}