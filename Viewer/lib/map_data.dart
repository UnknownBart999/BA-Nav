import 'dart:typed_data';

// TODO: Make nodes more compact by setting fixed string size, fixed size add dictionaries, fixed size neighbors list
// TODO: Immutable lists members??? (not required, but makes code unable to modify list members, which makes sense since they're read-only data)
// TODO : Consider using an edge list instead of a neighbor list for each node (no edge data duplication among nodes, easier to disallow duplicate edges)

class MapData {
  final String mapName;
  final String version;
  final List<Node> nodes;
  // TODO: Figure out floorplan datatype

  MapData(this.mapName, this.version, this.nodes);

  @override
  String toString() {
    return "[Map $mapName: Version $version, No. Nodes ${nodes.length}]";
  }
}

class Node {
  final String name;
  final String buildingName;
  final Map<String, dynamic>? add;
  final List<(int, double, Map<String, dynamic>?)> neighbors;
  
  // 0-3 ID, 4-5 x, 6-7 y, 8 - category, 9 - floor
  final ByteData _data = ByteData(10);

  Node(this.name, this.buildingName, int id, int x, int y, int category, int floor, this.add, this.neighbors) {
    _data.setUint32(0, id);
    _data.setUint16(4, x);
    _data.setUint16(6, y);
    _data.setUint8(8, category);
    _data.setUint8(9, floor);
  }

  int getID() {
    return _data.getUint32(0);
  }

  (int, int) getCoords() {
    return (_data.getUint16(4), _data.getUint16(6));
  }

  int getCategory() {
    return _data.getUint8(8);
  }

  int getFloor() {
    return _data.getUint8(9);
  }

  @override
  String toString() {
    return "[Node $name: ID ${getID()}, Building $buildingName, Coords ${getCoords()}, Category: ${getCategory()}, Floor: ${getFloor()}, Additional: $add, Neighbors: $neighbors]";
  }
}