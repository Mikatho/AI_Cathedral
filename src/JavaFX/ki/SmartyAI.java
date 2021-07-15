package JavaFX.ki;

import JavaFX.ki.cathedral.*;

import java.util.*;
import java.util.stream.Collectors;

public class SmartyAI {
    int rounds = 100;
    int maxDepth = 10;
    int childNodesDivider = 2;


    public Placement takeTurn(Game game) {
        Random rand = new Random();
        Placement nextMove;

        if (game.getTurns() == 1) {
            nextMove = firstTurn(rand, game);
        } else if (game.getTurns() == 2 || game.getTurns() == 3) {
            // seite die am weitesten weg ist von cathedral mit geringstem abstand nach oben und unten
            //nextMove = secondTurn(rand, game);
            nextMove = NeutralAI.getBestPlacement(game, rounds / childNodesDivider, game.getCurrentPlayer() ,maxDepth);

        } else {
            nextMove = NeutralAI.getBestPlacement(game, rounds / childNodesDivider, game.getCurrentPlayer() ,maxDepth);
        }

        return nextMove;
    }

    private Placement randomTurn(Random rand, Game game) {
        return new Placement(
                rand.nextInt(10),
                rand.nextInt(10),
                Direction.values()[rand.nextInt(Direction.values().length)],
                getLargestBuilding(getActivePlayerBuildings(game)));
    }

    // erste zug random cathedral spielen
    private Placement firstTurn(Random rand, Game game) {
        Building cath = null;

        for (Building b : game.getPlaceableBuildings()) {
            if (b.getId() == 23) {
                cath = b;
            }
        }

        Placement placement;

        do {
            placement = new Placement(
                    rand.nextInt(10),
                    rand.nextInt(10),
                    Direction.values()[rand.nextInt(Direction.values().length)],
                    cath);
        } while (!game.checkIfPlacementIsValid(game, placement));

        return placement;
    }

    private Placement secondTurn(Random rand, Game game) {
        // x und y am weitesten weg von cathedral
        Direction direction = Direction.values()[rand.nextInt(Direction.values().length)];

        Building academy = null;

        for (Building b : getActivePlayerBuildings(game)) {
            if (b.getName().equals("Academy")) {
                academy = b;
            }
        }

        HashMap<Placement, Double> possible = new HashMap<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                Game tempCopy = game.copy();
                Position cath = game.getBoard().getPlacedBuildings().get(0).getPosition();
                Placement tempPlace = new Placement(x, y, direction, academy);
                if (tempCopy.takeTurn(tempPlace)) {
                    double distance = Math.sqrt(Math.pow(cath.x() - x, 2) + Math.pow(cath.y() - y, 2));
                    possible.put(tempPlace, distance);
                }
            }
        }

        Placement highestValue = null;
        double highestDistance = 0;

        for (Map.Entry<Placement, Double> entry : possible.entrySet()) {
            if (highestValue == null) {
                highestValue = entry.getKey();
                highestDistance = entry.getValue();
            } else {
                if (entry.getValue() > highestDistance) {
                    highestValue = entry.getKey();
                    highestDistance = entry.getValue();
                }
            }
        }
        return highestValue;
    }

    //TODO Randomize höchste Ergebnisse für Return
    public static Building getLargestBuilding(List<Building> buildings) {
        List<Building> sortedBuildings = buildings.stream().sorted(Comparator.comparingInt(Building::getBuildingsize))
                .collect(Collectors.toList());
        return sortedBuildings.get(sortedBuildings.size() - 1);
    }

    public static List<Building> getLargestBuildings(List<Building> buildings, int puffer) {
        if (buildings.size() == 0) return null;
        Building biggest = getLargestBuilding(buildings);
        List<Building> bestBuildings = new ArrayList<>();

        buildings.forEach(building -> {
            if (building.getBuildingsize() == biggest.getBuildingsize() - puffer) {
                bestBuildings.add(building);
            }
        });

        return bestBuildings;
    }

    public List<Building> getActivePlayerBuildings(Game game) {
        return game.getPlaceableBuildingsByColor(game.getCurrentPlayer());
    }
}
