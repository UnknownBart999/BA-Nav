//import 'dart:nativewrappers/_internal/vm/lib/ffi_patch.dart';

import 'package:flutter/material.dart';
import 'package:ba_nav/backend_api.dart';

void main() {
  openMap("C:\\Users\\troja\\Documents\\Uni\\BA-Nav\\Viewer\\lib\\City\ Hospital_2.1.0.zip").then((value) {
    if (value) {
      runApp(const ENSYC());
    }
  });
}

class ENSYC extends StatelessWidget {
  const ENSYC({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ENSYC',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color.fromARGB(255, 58, 154, 183), brightness: Brightness.dark),
      ),
      home: const TripPage(title: 'Trip'),
      routes: {
        '/menu': (context) => const MenuPage(),
        '/location-select': (context) {
          final selectedFrom = ModalRoute.of(context)?.settings.arguments as String?;
          return LocationSelectPage(selectedBuilding: selectedFrom);
        },
        '/navigation': (context) {
          final args = ModalRoute.of(context)?.settings.arguments as (int, dynamic)?;
          if (args != null) {
            return NavigationPage(fromLocationId: args.$1, toLocationOrCategory: args.$2);
          }
          return const NavigationPagePlaceholder();
        },
        '/find': (context) => const SearchPage(),
      },
    );
  }
}

class TripPage extends StatefulWidget {
  const TripPage({super.key, required this.title});
  final String title;
  @override
  State<TripPage> createState() => _TripPageState();
}

class _TripPageState extends State<TripPage> {
  int? selectedFromLocation = -1;
  String? selectedFromLocationName = '...';
  String? selectedFromBuilding = "";
  int? selectedToLocation = -1;
  String? selectedToLocationName = '...';
  String? selectedToBuilding = "";
  String? selectedCategory = "";
  bool isUsingFind = false;
  List<DropdownMenuItem<String>> dropDownItems=[];
  List<DropdownMenuItem<String>> categoryItems=[];
  _TripPageState(){
    List<String> building_names=getBuildingNames();
    selectedFromBuilding = building_names[0];
    selectedToBuilding = building_names[0];
    for(int i=0; i<building_names.length; ++i){dropDownItems.add(DropdownMenuItem(value: building_names[i], child: Text(building_names[i])));}
    List<String> categories = getCategories();
    selectedCategory = categories[0];
    for(int i=0; i<categories.length; ++i){categoryItems.add(DropdownMenuItem(value: categories[i], child: Text(categories[i])));}
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                IconButton(onPressed: (){Navigator.of(context).pushNamed('/menu');}, icon: const Icon(Icons.menu), iconSize: 27,),
                Text("ENSYC"),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Container(
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(8),
              ),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const Text('From'),
                  const SizedBox(height: 8),
                  SizedBox(
                    width: double.infinity,
                    child: DropdownButton<String>(
                      isExpanded: true,
                      value: selectedFromBuilding,
                      hint: const Text('Select Building'),
                      items: dropDownItems,
                      onChanged: (value) {
                        setState(() {
                          selectedFromBuilding = value;
                          selectedFromLocation=-1;
                          selectedFromLocationName="...";
                        });
                      },
                    ),
                  ),
                  const SizedBox(height: 8),
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: OutlinedButton(
                      style: OutlinedButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.zero),),
                      onPressed: () async {
                          final result = await Navigator.of(context).pushNamed('/location-select', arguments: selectedFromBuilding);
                          if (result is (int, String, String)) {
                            setState(() {
                              selectedFromLocation = result.$1;
                              selectedFromLocationName = result.$2;
                            });
                          }
                        },
                      child: Text(selectedFromLocationName ?? "..."),
                    ),
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  onPressed: () {
                    setState(() {
                      isUsingFind = false;
                    });
                  },
                  icon: const Icon(Icons.arrow_back),
                  iconSize: 24,
                ),
                IconButton(
                  onPressed: () {
                    setState(() {
                      isUsingFind = true;
                    });
                  },
                  icon: const Icon(Icons.arrow_forward),
                  iconSize: 24,
                ),
              ],
            ),
          ),
          if (!isUsingFind)
            Padding(
              padding: const EdgeInsets.all(16),
              child: Container(
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey),
                  borderRadius: BorderRadius.circular(8),
                ),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const Text('To'),
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      child: DropdownButton<String>(
                        isExpanded: true,
                        value: selectedToBuilding,
                        hint: const Text('Select Building'),
                        items: dropDownItems,
                        onChanged: (value) {
                          setState(() {
                            selectedToBuilding = value;
                            selectedToLocation=-1;
                            selectedToLocationName="...";
                          });
                        },
                      ),
                    ),
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      height: 48,
                      child: OutlinedButton(
                        style: OutlinedButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.zero),),
                        onPressed: () async {
                            final result = await Navigator.of(context).pushNamed('/location-select', arguments: selectedToBuilding);
                            if (result is (int, String, String)) {
                              setState(() {
                                selectedToLocation = result.$1;
                                selectedToLocationName = result.$2;
                              });
                            }
                          },
                        child: Text(selectedToLocationName ?? "..."),
                      ),
                    ),
                  ],
                ),
              ),
            )
          else
            Padding(
              padding: const EdgeInsets.all(16),
              child: Container(
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey),
                  borderRadius: BorderRadius.circular(8),
                ),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const Text('Find'),
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      child: DropdownButton<String>(
                        isExpanded: true,
                        value: selectedCategory,
                        hint: const Text('Select Category'),
                        items: categoryItems,
                        onChanged: (value) {
                          setState(() {
                            selectedCategory = value;
                          });
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color.fromARGB(255, 58, 154, 183),
                ),
                onPressed: (){
                  if (selectedFromLocation == -1) return;
                  if (isUsingFind) {
                    Navigator.of(context).pushReplacementNamed('/navigation', arguments: (selectedFromLocation, selectedCategory));
                  } else {
                    if (selectedToLocation == -1) return;
                    Navigator.of(context).pushReplacementNamed('/navigation', arguments: (selectedFromLocation, selectedToLocation));
                  }
                },
                icon: const Icon(Icons.navigation, color: Color.fromARGB(255, 220, 220, 220)),
                label: const Text('Go', style: TextStyle(color: Color.fromARGB(255, 220, 220, 220))),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.route),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {
                  if (selectedFromLocation == -1 || selectedToLocation == -1) {
                      Navigator.of(context).pushReplacementNamed('/navigation');
                    } else {
                      Navigator.of(context).pushReplacementNamed('/navigation', arguments: (selectedFromLocation, selectedToLocation));
                    }
                  },
                  icon: const Icon(Icons.navigation),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/find');
                  },
                  icon: const Icon(Icons.search),
                  iconSize: 28,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class LocationSelectPage extends StatefulWidget {
  final String? selectedBuilding;
  const LocationSelectPage({super.key, required this.selectedBuilding});

  @override
  State<LocationSelectPage> createState() => _LocationSelectPageState();
}

class _LocationSelectPageState extends State<LocationSelectPage> {
  late TextEditingController _searchController;
  String searchQuery = '';

  @override
  void initState() {
    super.initState();
    _searchController = TextEditingController();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    List<(int, String, String)> locations = getLocationsInBuilding(widget.selectedBuilding ?? "");

    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                IconButton(onPressed: (){Navigator.of(context).pop();}, icon: const Icon(Icons.arrow_back), iconSize: 27,),
                const SizedBox(width: 56),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Text('Building: ${widget.selectedBuilding}'),
                const SizedBox(height: 16),
                ...locations.map((loc) => ListTile(
                  title: Text("${loc.$2} (${loc.$3})"),
                  onTap: () {
                    Navigator.of(context).pop(loc);
                  },
                )),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class MenuPage extends StatelessWidget {
  const MenuPage({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                IconButton(onPressed: (){Navigator.of(context).pop();}, icon: const Icon(Icons.arrow_back), iconSize: 27,),
                const SizedBox(width: 56),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                ExpansionTile(
                  title: const Text('About us'),
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: Text('<PLACEHOLDER>'),
                    ),
                  ],
                ),
                ExpansionTile(
                  title: const Text('Help'),
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: Text('<PLACEHOLDER>'),
                    ),
                  ],
                ),
                ExpansionTile(
                  title: const Text('Contact us'),
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: Text('<PLACEHOLDER>'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class SearchPage extends StatelessWidget {
  const SearchPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text('Search page', style: TextStyle(fontSize: 18)),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/');
                  },
                  icon: const Icon(Icons.route),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/navigation');
                  },
                  icon: const Icon(Icons.navigation),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.search),
                  iconSize: 28,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class NavigationPagePlaceholder extends StatelessWidget {
  const NavigationPagePlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: const Text('Please plan a trip first', style: TextStyle(fontSize: 16)),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/');
                  },
                  icon: const Icon(Icons.route),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.navigation),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/find');
                  },
                  icon: const Icon(Icons.search),
                  iconSize: 28,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class NavigationPage extends StatefulWidget {
  final int fromLocationId;
  final dynamic toLocationOrCategory;

  const NavigationPage({super.key, required this.fromLocationId, required this.toLocationOrCategory});

  @override
  State<NavigationPage> createState() => _NavigationPageState();
}

class _NavigationPageState extends State<NavigationPage> {
  late int currentSection;
  late int totalSections;
  late String buildingName;
  late int floor;
  late Image? stepImage;
  late List<(int, int)> coordinates;

  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    currentSection = 0;
    _loadSection();
  }

  void _loadSection() async {
    _isLoading = true;
    late dynamic tripData;
    unloadTrip();
    if (widget.toLocationOrCategory is String) {
      tripData = await tripFromFind(widget.fromLocationId, widget.toLocationOrCategory as String, currentSection);
    } else {
      tripData = await tripFromTo(widget.fromLocationId, widget.toLocationOrCategory as int, currentSection);
    }
    _isLoading = false;
    setState(() {
      totalSections = tripData.$1;
      buildingName = tripData.$2;
      floor = tripData.$3;
      stepImage = tripData.$4;
      coordinates = tripData.$5;
    });
  }

  void _nextSection() {
    if (currentSection < totalSections - 1) {
      setState(() {
        currentSection++;
      });
      _loadSection();
    }
  }

  void _previousSection() {
    if (currentSection > 0) {
      setState(() {
        currentSection--;
      });
      _loadSection();
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('$buildingName - Floor $floor', style: const TextStyle(fontSize: 12)),
                Text('${currentSection + 1}/$totalSections', style: const TextStyle(fontSize: 12)),
                const SizedBox(height: 8),
              ],
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Stack(
                children: [
                  stepImage ?? SizedBox.shrink(),
                  CustomPaint(
                    painter: LinesPainter(coordinates: coordinates),
                    size: Size.infinite,
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  onPressed: _previousSection,
                  icon: const Icon(Icons.arrow_back),
                  iconSize: 28,
                ),
                const SizedBox(width: 16),
                IconButton(
                  onPressed: _nextSection,
                  icon: const Icon(Icons.arrow_forward),
                  iconSize: 28,
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/');
                  },
                  icon: const Icon(Icons.route),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.navigation),
                  iconSize: 28,
                ),
                IconButton(
                  onPressed: () {
                    Navigator.of(context).pushReplacementNamed('/find');
                  },
                  icon: const Icon(Icons.search),
                  iconSize: 28,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class LinesPainter extends CustomPainter {
  final List<(int, int)> coordinates;

  LinesPainter({required this.coordinates});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.red
      ..strokeWidth = 2.0;

    for (int i = 0; i < coordinates.length - 1; i++) {
      final start = coordinates[i];
      final end = coordinates[i + 1];

      canvas.drawLine(
        Offset(start.$1.toDouble(), start.$2.toDouble()),
        Offset(end.$1.toDouble(), end.$2.toDouble()),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(LinesPainter oldDelegate) {
    return oldDelegate.coordinates != coordinates;
  }
}


