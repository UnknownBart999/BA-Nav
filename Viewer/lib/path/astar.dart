import 'dart:math';

import 'package:ba_nav/mapdata/i_map_data.dart';
import 'package:ba_nav/path/trip.dart';
import 'package:ba_nav/path/shared.dart';
import 'package:collection/collection.dart';

/// Class for storing data relavant to a single node
class AstarEntry extends PathEntry implements Comparable {
  double g;       // Cost so far
  double f;       // g + heuristic

  AstarEntry(nid, this.g, this.f, parent) : super(nid, parent);

  @override
  int compareTo(other) {
    return (f - other.f).sign.toInt();
  }
}

/// Calculates the optimal path from [startID] to [endID]. Returns null if there is no path, or there is an error
Trip? astar(int startID, int goalID, IMapData mapdata) {
  final nodes = mapdata.getNodes();
  final edges = mapdata.getEdges();
  final buildings = mapdata.getBuildings();

  // Check inputs
  if (startID < 0 || goalID < 0 || startID >= nodes.length || goalID >= nodes.length) {
    return null;
  }

  var currnode = nodes[startID];
  var currLevel = buildings[currnode.getBuildingName()]!.getFloors()[currnode.getFloorId()].getLevel();
  final goalnode = nodes[goalID];
  final goalLevel = buildings[goalnode.getBuildingName()]!.getFloors()[goalnode.getFloorId()].getLevel();

  // Init
  final frontier = PriorityQueue<AstarEntry>();
  final explored = <AstarEntry>[];
  frontier.add(AstarEntry(startID, 0.0, _calcHeuristic(currnode.getCoords(), goalnode.getCoords(), currLevel, goalLevel), null));

  while (frontier.isNotEmpty) {
    // Pop cheapest node
    final node = frontier.removeFirst();

    // Goal check
    if (node.nid == goalID) {
      return createTrip(startID, goalID, node, mapdata);
    }

    // Add all neighbors to the frontier
    for (final neighbor in getNeighbors(node.nid, nodes, edges)) {
      currnode = nodes[neighbor.$1];
      if (currnode.getBuildingName() == "Outside") {
        currLevel = 0;
      } else {
        currLevel = buildings[currnode.getBuildingName()]!.getFloors()[currnode.getFloorId()].getLevel();
      }

      final gNew = node.g + neighbor.$2;
      final fNew = gNew + _calcHeuristic(currnode.getCoords(), goalnode.getCoords(), currLevel, goalLevel);

      final entry = AstarEntry(neighbor.$1, gNew, fNew, node);
      if (explored.contains(entry)) {
        continue;
      }
      final idx = frontier.toList().indexOf(entry);
      if (idx == -1 || frontier.toList()[idx].g > gNew) {
        frontier.remove(entry);
        frontier.add(entry);
      }
    }
  }

  return null;
}

double _calcHeuristic((int, int) nodePos, (int, int) goalPos, int nodeLevel, int goalLevel) {
  return sqrt(pow(nodePos.$1 - goalPos.$1, 2) + pow(nodePos.$2 - goalPos.$2, 2) + pow(nodeLevel - goalLevel, 2));
}