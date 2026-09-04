USE CASE 1 - Planlegg rute

```mermaid

sequenceDiagram

actor Bruker
participant HomeScreen
participant HomeViewModel
participant Layers
participant ShipLayerRoute


loop Looper til bruker er fornøyd med ruta
Bruker ->> HomeScreen: Trykker på havområdet i kartet
HomeScreen ->> HomeViewModel: addWaypoint(lat, lon)
HomeViewModel -->> HomeScreen: oppdaterer route med et punkt
HomeScreen ->> Layers: addLayers(route)
Layers ->> ShipRouteLayer: addShipRouteLayer(route)
ShipRouteLayer -->> HomeScreen: visualiserer punktet
HomeScreen -->> Bruker: Ser punkt på kartet

alt
Bruker ->> HomeScreen: Trykker på landområde i kartet
HomeScreen ->> Bruker: Returnerer ingenting 
end

alt
Bruker ->> HomeScreen: Trykker på fjern punkt
HomeScreen ->> HomeViewModel: removeWaypoint(waypoint)
HomeScreen ->> Layers: addLayers(route)
Layers ->> ShipRouteLayer: addShipRouteLayer(route)
ShipRouteLayer -->> HomeScreen: fjerner visualiseringen av waypoint 
HomeScreen -->> Bruker: Bruker ser et mindre punkt på kartet
end

end

```