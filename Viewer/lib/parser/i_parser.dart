import 'package:ba_nav/mapdata/i_map_data.dart';

abstract class IParser {
  Future<IMapData?> getMapData(Stream<List<int>> byteStream);
}