import 'dart:math';

import 'package:ba_nav/i_map_data.dart';

class AstarEntry {
  double g;       // Cost so far
  double f;       // g + heuristic
  INode? parent;  // Previous node

  AstarEntry(this.g, this.f, this.parent);
}

List<INode> astar(INode start, INode goal, List<INode> nodes, List<IEdge> edges, Map<String, IBuilding> buildings) {
  final goalLevel = buildings[goal.getBuildingName()]!.getFloors()[goal.getFloorId()].getLevel();
  var currLevel = buildings[start.getBuildingName()]!.getFloors()[start.getFloorId()].getLevel();

  final explored = <INode, INode?> {};
  final frontier = <INode, AstarEntry> {start: AstarEntry(0.0, _calcHeuristic(start, goal, currLevel, goalLevel), null)};

  while (frontier.isNotEmpty) {
    final parent = _getCheapestFrontNode(frontier);
    final g = frontier[parent]!.g;

    // Remove from frontier
    explored[parent] = frontier[parent]!.parent;
    frontier.remove(parent);

    // Goal Test
    if (parent == goal) {
      final result = <INode>[];

      INode? node = goal;
      while (node != null) {
        result.add(node);
        node = explored[node];
      }
      return result;
    }

    for (final eid in parent.getEdgeIds()) {
      final edge = edges[eid];
      final child = _getNeighbor(parent, edge, nodes);

      if (child.getBuildingName() == "Outside") {
        currLevel = 0;
      } else {
        currLevel = buildings[child.getBuildingName()]!.getFloors()[child.getFloorId()].getLevel();
      }

      final gNew = g + edge.getDistance();
      final fNew = gNew + _calcHeuristic(child, goal, currLevel, goalLevel);

      if (explored.containsKey(child)) {
        continue;
      }
      else if (!frontier.containsKey(child) || frontier[child]!.g > gNew) {
        frontier[child] = AstarEntry(gNew, fNew, parent);
      }
    }
  }

  return [];
}

INode _getCheapestFrontNode(Map<INode, AstarEntry> frontier) {
  var leastScore = 100000000.0;
  var leastScoreKey;
  for (final n in frontier.entries) {
    if (leastScore > n.value.f) {
      leastScore = n.value.f;
      leastScoreKey = n.key;
    }
  }
  return leastScoreKey;
}

INode _getNeighbor(INode node, IEdge edge, List<INode> nodes) {
  final nids = edge.getNodeIds();
  if (nodes[nids.$1] == node) {
    return nodes[nids.$2];
  } else {
    return nodes[nids.$1];
  }
}

double _calcHeuristic(INode node, INode goal, int nodeLevel, int goalLevel) {
  final n1Pos = node.getCoords();
  final n2Pos = goal.getCoords();
  return sqrt(pow(n1Pos.$1 - n2Pos.$1, 2) + pow(n1Pos.$2 - n2Pos.$2, 2) + pow(nodeLevel - goalLevel, 2));
}