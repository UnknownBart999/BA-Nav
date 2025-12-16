import 'map_data.dart';

class Trip {
  int totalSegments = 0;
  int startNodeID = 0;
  int endNodeID = 0;
  MapData parentMap = MapData("", "", {}, [], []);

  Trip(this.totalSegments, this.startNodeID, this.endNodeID, this.parentMap);
}
