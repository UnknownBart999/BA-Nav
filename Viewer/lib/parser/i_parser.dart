import 'dart:typed_data';

import '../map_data.dart';

abstract class IParser {
  MapData parse(Uint8List bytes);
}