import 'dart:typed_data';

import '../map_data.dart';

abstract class IParser {
  MapData getMapData(Uint8List bytes);
  Uint8List getFloorPlan(Uint8List bytes, String building, int floor);
}