import 'dart:collection';

import 'package:ba_nav/mapdata/i_map_data.dart';
import 'package:ba_nav/path/trip.dart';

class PathEntry {
  int nid;
  PathEntry? parent;

  PathEntry(this.nid, this.parent);

  @override
  bool operator ==(Object other) {
    return other is PathEntry && nid == other.nid;
  }
}

List<(int, double)> getNeighbors(int nodeID, List<INode> nodes, List<IEdge> edges) {
  final result = <(int, double)>[];

  for (final eid in nodes[nodeID].getEdgeIds()) {
    final edge = edges[eid];
    final nids = edge.getNodeIds();
    if (nids.$1 == nodeID) {
      result.add((nids.$2, edge.getDistance()));
    } else {
      result.add((nids.$1, edge.getDistance()));
    }
  }
  return result;
}

Trip createTrip(int startID, int goalID, PathEntry goal, IMapData mapdata) {
  final nodes = mapdata.getNodes();

  final result = Queue<List<INode>>();
  var numOfSegs = 1;

  var currSeg = Queue<INode>();
  var prevBName = nodes[goalID].getBuildingName();
  var prevFloor = nodes[goalID].getFloorId();

  PathEntry? nodeEntry = goal;
  while (nodeEntry != null) {
    final node = nodes[nodeEntry.nid];
    if (node.getBuildingName() != prevBName || node.getFloorId() != prevFloor) {
      numOfSegs++;
      result.addFirst(currSeg.toList());
      currSeg = Queue();
    }
    currSeg.addFirst(node);

    prevBName = node.getBuildingName();
    prevFloor = node.getFloorId();
    nodeEntry = nodeEntry.parent;
  }
  result.addFirst(currSeg.toList());
  return Trip(numOfSegs, startID, goalID, mapdata, result.toList());
}