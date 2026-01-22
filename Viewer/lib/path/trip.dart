import 'package:ba_nav/mapdata/i_map_data.dart';

class Trip {
  int totalSegments;
  int startNodeID;
  int endNodeID;
  IMapData parentMap;
  List<List<INode>> segments;

  Trip(this.totalSegments, this.startNodeID, this.endNodeID, this.parentMap, this.segments);

  @override
  String toString() {
    return "Trip: SegCount - $totalSegments, Start: $startNodeID, End: $endNodeID, Segments: $segments";
  }
}
