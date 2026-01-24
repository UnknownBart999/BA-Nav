import 'package:ba_nav/backend_api.dart';
import 'package:flutter/widgets.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // TESTING CODE: Change the path when testing
  openMap("C:\\Users\\troja\\Documents\\Uni\\BA-Nav\\Viewer\\lib\\City\ Hospital_2.1.0.zip").then((a) {
    if (a) {
      //tripFromFind(12, "Outside", segmentNumber)
      tripFromTo(0, 9, 1).then((value) {
        print(value);
      });
      print(loadedTrip);
    } else {
      print("didnt work");
    }
  });
}