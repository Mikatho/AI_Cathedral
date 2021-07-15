package JavaFX.ki.cathedral;

import JavaFX.ki.SmartyAI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Game {
    private List<Turn> turns = new ArrayList<>();
    private boolean debug = false;

    public Game() {
        Map<Building, Integer> initalBuildings = new HashMap<>();

        initalBuildings.put(new Building(1, "Tavern", Color.Black, Turnable.No, new int[][]{{0, 0}}), 2);
        initalBuildings.put(new Building(2, "Stable", Color.Black, Turnable.Half, new int[][]{{0, 0}, {1, 0}}), 2);
        initalBuildings.put(new Building(3, "Inn", Color.Black, Turnable.Full, new int[][]{{0, 0}, {1, 0}, {1, 1}}), 2);
        initalBuildings.put(new Building(4, "Bridge", Color.Black, Turnable.Half, new int[][]{{0, 0}, {0, 1}, {0, 2}}), 1);
        initalBuildings.put(new Building(5, "Manor", Color.Black, Turnable.Full, new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}), 1);
        initalBuildings.put(new Building(6, "Square", Color.Black, Turnable.No, new int[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(7, "Abbey", Color.Black, Turnable.Half, new int[][]{{-1, 0}, {0, 0}, {0, 1}, {1, 1}}), 1);
        initalBuildings.put(new Building(8, "Infirmary", Color.Black, Turnable.No, new int[][]{{-1, 0}, {0, -1}, {0, 0}, {0, 1}, {1, 0}}), 1);
        initalBuildings.put(new Building(9, "Castle", Color.Black, Turnable.Full, new int[][]{{-1, 0}, {-1, 1}, {0, 0}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(10, "Tower", Color.Black, Turnable.Full, new int[][]{{-1, -1}, {0, -1}, {0, 0}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(11, "Academy", Color.Black, Turnable.Full, new int[][]{{-1, 0}, {0, -1}, {0, 0}, {0, 1}, {1, -1}}), 1);

        initalBuildings.put(new Building(12, "Tavern", Color.White, Turnable.No, new int[][]{{0, 0}}), 2);
        initalBuildings.put(new Building(13, "Stable", Color.White, Turnable.Half, new int[][]{{0, 0}, {1, 0}}), 2);
        initalBuildings.put(new Building(14, "Inn", Color.White, Turnable.Full, new int[][]{{0, 0}, {1, 0}, {1, 1}}), 2);
        initalBuildings.put(new Building(15, "Bridge", Color.White, Turnable.Half, new int[][]{{0, 0}, {0, 1}, {0, 2}}), 1);
        initalBuildings.put(new Building(16, "Manor", Color.White, Turnable.Full, new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}), 1);
        initalBuildings.put(new Building(17, "Square", Color.White, Turnable.No, new int[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(18, "Abbey", Color.White, Turnable.Half, new int[][]{{-1, 1}, {0, 0}, {0, 1}, {1, 0}}), 1);
        initalBuildings.put(new Building(19, "Infirmary", Color.White, Turnable.No, new int[][]{{-1, 0}, {0, -1}, {0, 0}, {0, 1}, {1, 0}}), 1);
        initalBuildings.put(new Building(20, "Castle", Color.White, Turnable.Full, new int[][]{{-1, 0}, {-1, 1}, {0, 0}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(21, "Tower", Color.White, Turnable.Full, new int[][]{{-1, -1}, {0, -1}, {0, 0}, {1, 0}, {1, 1}}), 1);
        initalBuildings.put(new Building(22, "Academy", Color.White, Turnable.Full, new int[][]{{-1, -1}, {0, -1}, {0, 0}, {0, 1}, {1, 0}}), 1);

        initalBuildings.put(new Building(23, "Cathedral", Color.Blue, Turnable.Full, new int[][]{{-1, 0}, {0, -1}, {0, 0}, {0, 1}, {0, 2}, {1, 0}}), 1);

        turns.add(new Turn(0, new Board(initalBuildings), null));
    }


    public Game copy() {
        Game gameCopy = new Game();
        //undoLastTurn();
        gameCopy.turns.clear();

        turns.forEach(turn -> gameCopy.turns.add(turn.copy()));

        return gameCopy;
    }

    public boolean takeTurn(Placement placement) {
        if (!debug) {
            if (turns.size() == 1 && placement.getBuilding().getId() != 23) {
                return false;
            }
            try {
                if (placement.getBuilding().getColor() != Color.Blue) {
                    if (turns.size() % 2 == 0 && placement.getBuilding().getColor() != Color.Black) {
                        return false;
                    }
                    if (turns.size() % 2 == 1 && placement.getBuilding().getColor() != Color.White) {
                        return false;
                    }
                }
            } catch (NullPointerException np) {
                np.printStackTrace();
            }
        }
        Board nextBoardState = lastTurn().getBoard().copy();

        if (!nextBoardState.placeBuilding(placement)) {
            return false;
        }

        turns.add(new Turn(turns.size(), nextBoardState, placement));

        return true;
    }

    public int getTurns() {
        return turns.size();
    }

    public Turn lastTurn() {
        return turns.get(turns.size() - 1);
    }

    public void undoLastTurn() {
        turns.remove(turns.size() - 1);
    }

    public void forfeitTurn() {
        System.out.print("skipped his turn");
        turns.add(new Turn(turns.size(), lastTurn().getBoard().copy(), null));
    }

    public Color getCurrentPlayer() {
        return turns.size() == 1 ? Color.Blue : turns.size() % 2 == 0 ? Color.Black : Color.White;
    }

    public List<Building> getPlaceableBuildings() {
        return lastTurn().getBoard().getBuildings().stream()
                .filter(building -> lastTurn().getBoard().getNumberOfFreeBuildings(building) > 0)
                .collect(Collectors.toList());
    }

    public List<Building> getPlaceableBuildingsByColor(Color color) {
        return getPlaceableBuildings().stream().filter(building -> building.getColor().equals(color)).collect(Collectors.toList());
    }

    public List<Placement> checkPossibleMovesForBuilding(Game game, List<Building> buildings) {
        List<Placement> possibleTurns = new ArrayList<>();

        for (Building building : buildings) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    Placement tempPlacement = new Placement(x, y, Direction._0, building);
                    if (checkIfPlacementIsValid(game, tempPlacement)) possibleTurns.add(tempPlacement);

                    if (building.getTurnable() == Turnable.Half) {
                        tempPlacement = new Placement(x, y, Direction._90, building);
                        if (checkIfPlacementIsValid(game, tempPlacement)) possibleTurns.add(tempPlacement);
                    } else if (building.getTurnable() == Turnable.Full) {
                        tempPlacement = new Placement(x, y, Direction._90, building);
                        if (checkIfPlacementIsValid(game, tempPlacement)) possibleTurns.add(tempPlacement);

                        tempPlacement = new Placement(x, y, Direction._180, building);
                        if (checkIfPlacementIsValid(game, tempPlacement)) possibleTurns.add(tempPlacement);

                        tempPlacement = new Placement(x, y, Direction._270, building);
                        if (checkIfPlacementIsValid(game, tempPlacement)) possibleTurns.add(tempPlacement);
                    }
                }
            }
        }
        return possibleTurns;
    }

    public List<Placement> getPossibleMovesBySize(Game game, int puffer) {
        List<Building> buildings = game.getPlaceableBuildingsByColor(game.getCurrentPlayer());
        List<Building> tempBuildings = SmartyAI.getLargestBuildings(buildings, puffer);
        List<Placement> possibleTurns = new ArrayList<>();


        if (tempBuildings != null) {
            possibleTurns = checkPossibleMovesForBuilding(game, tempBuildings);
        }

        if (possibleTurns.size() == 0) {
            possibleTurns = checkPossibleMovesForBuilding(game, buildings);
            //possibleTurns = getPossibleMovesBySize(game, puffer + 1);
        }

         return possibleTurns;
    }

    public boolean checkIfPlacementIsValid(Game game, Placement placement) {
        Game tempCopy = game.copy();
        return tempCopy.takeTurn(placement);
    }

    public Board getBoard() {
        return lastTurn().getBoard();
    }

    public Map<Color, Integer> score() {
        return lastTurn().score();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Game.class.getSimpleName() + "[", "]")
                .add("turns=" + turns)
                .add("debug=" + debug)
                .toString();
    }


}
