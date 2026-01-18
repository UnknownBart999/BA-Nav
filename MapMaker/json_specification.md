# JSON MapData Specification

### Object IDs are optional because they may be implicitly defined by their index in their surrounding array
**If IDs are explicitly defined, they must start from 0 and be sequential (no gaps)**

```javascript
{
    "mapName": string,
    "mapVersion": string,
    "mapData": {
        "buildings": [
            {
                "name": string,
                "floors": [
                    {
                        "id": int, <optional>
                        "level": int,
                        "floorPlan": string,
                        "nids": [int]
                    },
                    ...
                ]
            },
            ...
        ],
        "nodes": [
            {
                "id": int, <optional>
                "name": string,
                "buildingName": string
                "fid": int,
                "x": int,
                "y": int,
                "cat": int,
                "eids": [int],
                "add": { <optional>
                    string: any,
                    ...
                }
            },
            ...
        ],
        "edges": [
            {
                "id": int, <optional>
                "nid1": int,
                "nid2": int,
                "dist": float,
                "add": { <optional>
                    string: any,
                    ...
                }
            },
            ...
        ],
        "categories": [
            {
                "name": str
            },
            ...
        ]
    }
}
```