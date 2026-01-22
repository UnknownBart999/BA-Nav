import 'package:ba_nav/mapdata/i_map_data.dart';
import 'package:ba_nav/path/shared.dart';
import 'package:ba_nav/path/trip.dart';
import 'package:collection/collection.dart';

class UCSEntry extends PathEntry implements Comparable {
  double costSoFar;

  UCSEntry(nid, this.costSoFar, parent) : super(nid, parent);

  @override
  int compareTo(other) {
    return (costSoFar - other.costSoFar).sign.toInt();
  }
}

Trip? ucs(int startID, String cat, IMapData mapdata) {
  final nodes = mapdata.getNodes();
  final edges = mapdata.getEdges();
  final categories = mapdata.getCategories();

  // Check inputs
  if (startID < 0 || startID >= nodes.length) {
    return null;
  }
  var containsCat = false;
  for (final c in categories) {
    if (c.getName() == cat) {
      containsCat = true;
    }
  }
  if (!containsCat) {
    return null;
  }

  // Init
  final frontier = PriorityQueue<UCSEntry>();
  final explored = <UCSEntry>[];
  frontier.add(UCSEntry(startID, 0, null));

  while (frontier.isNotEmpty) {
    // Pop cheapest node
    final node = frontier.removeFirst();
    
    // Skip if it's already explored for cheaper, otherwise explore it
    final idx = explored.indexOf(node);
    if (idx != -1) {
      if (explored[idx].costSoFar < node.costSoFar) {
        continue;
      }
      explored[idx] = node;
    } else {
      explored.add(node);
    }

    // Goal check
    final nextCat = nodes[node.nid].getCategory();
    if (categories[nextCat].getName() == cat) {
      return createTrip(startID, node.nid, node, mapdata);
    }

    // Add all neighbors to the frontier
    for (final neighbor in getNeighbors(node.nid, nodes, edges)) {
      frontier.add(UCSEntry(neighbor.$1, neighbor.$2 + node.costSoFar, node));
    }
  }

  return null;
}