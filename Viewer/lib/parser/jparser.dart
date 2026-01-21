import 'dart:convert';
import 'dart:io';

import 'package:ba_nav/astar/astar.dart';
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
    final elementCount = <JPContext, int> {};
    final contextStack = <JPContext> [];

    await for (final curr in stream) {
      final context = contextStack.lastOrNull;

      // Save data to vb, increment elementCount, throw if data is incorrect
      if (curr.type == JsonEventType.propertyValue && prev != null && prev.type == JsonEventType.propertyName && prev.value != null) {
        if (!JPContext.checkAttribute(context!, prev.value, curr.value)) {
          throw "Attribute ${prev.value} (value: ${curr.value}) doesn't belong to $context!";
        }
        vb[context][prev.value] = curr.value;
        // id is optional, so don't increment elementCount
        if (prev.value != "id" && prev.value != "add") {
          elementCount[context] = elementCount[context]! + 1;
        }

      } else if (curr.type == JsonEventType.arrayElement && curr.value != null) {
        if (!JPContext.checkAttribute(context!, null, curr.value)) {
          throw "Attribute ${curr.value} doesn't belong to $context!";
        }
        vb[context].add(curr.value);
        elementCount[context] = elementCount[context]! + 1;
      }

      // Create object from data in vb, increment elementCount of parent context, throw if buildingName isn't unique
      if (curr.type == JsonEventType.endObject || curr.type == JsonEventType.endArray) {
        final obj = JPContext.createObject(context!, vb, elementCount);
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
            final id = vb[context]["id"];
            if (id != null) {
              if (id >= vb[prevContext].length) {
                vb[prevContext].length = id + 1;
              }
              vb[prevContext][id] = obj;
            } else {
              vb[prevContext].add(obj);
            }
          }
        }
        // add is optional, so don't increment elementCount of parent context
        if (context != JPContext.addedge && context != JPContext.addnode) {
          final prevContext = contextStack.elementAt(contextStack.length-2);
          elementCount[prevContext] = elementCount[prevContext]! + 1;
        }
      }

      // Switch context, initialise buffer/elementCount for new context
      if (curr.type == JsonEventType.beginObject || curr.type == JsonEventType.beginArray || curr.type == JsonEventType.endObject || curr.type == JsonEventType.endArray) {
        _switchContext(contextStack, prev?.value, curr.type, vb, elementCount);
      }

      prev = curr;
    }
    return null;
  }

  void _switchContext(List<JPContext> stack, dynamic prevValue, JsonEventType currType, Map<JPContext, dynamic> vb, Map<JPContext, int> elementCount) {
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
      elementCount[nextContext] = 0;

    } else {
      stack.removeLast();
    }
  }
}


// TESTING CODE
void main() {
  var json = File("../../../MapMaker/University\ Campus_1.0.0.json").openRead();
  var parser = JParser();
  var mapdata = parser.getMapData(json);
  mapdata.then((value) {
    // final start = value!.getNodes()[12];
    // final goal = value.getNodes()[32];
    // var path = astar(start, goal, value.getNodes(), value.getEdges(), value.getBuildings());
    // for (var a in path) {
    //   print(value.getNodes().indexOf(a));
    // }
    print(value!.getBuildings()["Library"]!.getFloors()[0]);
  }
  );
}