import 'dart:convert';
import 'dart:io';

import 'package:ba_nav/i_map_data.dart';
import 'package:ba_nav/parser/i_parser.dart';
import 'package:ba_nav/parser/jp_context.dart';
import 'package:json_events/json_events.dart';

class JParser extends IParser {
  @override
  Future<IMapData?> getMapData(Stream<List<int>> byteStream) async {
    Stream<JsonEvent> stream = byteStream
        .transform(const Utf8Decoder())
        .transform(const JsonEventDecoder())
        .flatten();

    JsonEvent? prev;
    final vb = <JPContext, dynamic> {}; // value buffer
    final checkSum = <JPContext, int> {};
    final contextStack = <JPContext> [];

    await for (final curr in stream) {
      final context = contextStack.lastOrNull;

      // Save data to vb, increment checksum, throw if data is incorrect
      if (curr.type == JsonEventType.propertyValue && prev != null && prev.type == JsonEventType.propertyName) {
        if (!JPContext.checkAttribute(context!, prev.value, curr.value)) {
          throw "Attribute ${prev.value} (value: ${curr.value}) doesn't belong to $context!";
        }
        vb[context][prev.value] = curr.value;
        // id is optional, so don't increment checksum
        if (prev.value != "id") {
          checkSum[context] = (checkSum[context] != null) ? checkSum[context]! + 1 : 1;
        }

      } else if (curr.type == JsonEventType.arrayElement && curr.value != null) {
        if (!JPContext.checkAttribute(context!, null, curr.value)) {
          throw "Attribute ${curr.value} doesn't belong to $context!";
        }
        vb[context].add(curr.value);
      }

      // Create object from data in vb, increment checksum of parent context, throw if buildingName isn't unique
      if (curr.type == JsonEventType.endObject || curr.type == JsonEventType.endArray) {
        final obj = JPContext.createObject(context!, vb, checkSum);
        if (obj is IMapData) {
          return obj;
        }

        if (obj != null) {
          final prevContext = contextStack.elementAt(contextStack.length-2);

          if (obj is IBuilding) {
            // Checks building name uniquness
            final buildingName = vb[context]["name"];
            if (vb[prevContext][buildingName] != null) {
              throw "Building name $buildingName must be unique!";
            }
            vb[prevContext][buildingName] = obj;
          } else {
            // Inserts at specified index, it id is not null
            vb[prevContext].add(obj);
          }
        }

        // add is optional, so don't increment checksum of parent context
        if (context != JPContext.addedge || context != JPContext.addnode) {
          final prevContext = contextStack.elementAt(contextStack.length-2);
          checkSum[prevContext] = checkSum[prevContext]! + 1;
        }
      }

      // Switch context, initialise buffer/checksum for new context
      if (curr.type == JsonEventType.beginObject || curr.type == JsonEventType.beginArray || curr.type == JsonEventType.endObject || curr.type == JsonEventType.endArray) {
        _switchContext(contextStack, prev?.value, curr.type, vb, checkSum);
      }

      prev = curr;
    }

    return null;
  }

  void _switchContext(List<JPContext> stack, dynamic prevValue, JsonEventType currType, Map<JPContext, dynamic> vb, Map<JPContext, int> checkSum) {
    final context = stack.lastOrNull;
    if (currType == JsonEventType.beginObject || currType == JsonEventType.beginArray) {
      // Ternary converts non-string labels to null, since non-string means no label is present
      final nextContext = JPContext.getNextContext((prevValue is String) ? prevValue : null, context, currType); 
      if (nextContext == null) {
        throw "Context switch in $context (label: $prevValue) is invalid!";
      }
      stack.add(nextContext);

      if (nextContext.openingSymbol == JsonEventType.beginObject) {
        vb[nextContext] = <String, dynamic> {};
      } else if (nextContext == JPContext.buildings) {
        vb[nextContext] = <String, IBuilding> {};
      } else if (nextContext.openingSymbol == JsonEventType.beginArray) {
        vb[nextContext] = [];
      }
      checkSum[nextContext] = 0;

    } else {
      stack.removeLast();
    }
  }
}



void main() {
  var json = File("test.json").openRead();
  var parser = JParser();
  var mapdata = parser.getMapData(json);
  mapdata.then((value) => 
    print(value!.getBuildings()["C2"]!.getFloors())
  );
}