import 'package:ba_nav/i_map_data.dart';

abstract class IParser {
  Future<IMapData?> getMapData(Stream<List<int>> byteStream);
}