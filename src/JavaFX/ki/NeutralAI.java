package JavaFX.ki;

import JavaFX.ki.cathedral.Color;
import JavaFX.ki.cathedral.Game;
import JavaFX.ki.cathedral.Placement;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class NeutralAI {

    public static Placement getBestPlacement(Game game, int rounds, Color player, int maxDepth) {

        List<Placement> possiblePlacements = game.getPossibleMovesBySize(game,0);

        HashMap<Placement, Integer[]> predictedPlacements = new HashMap<>();
        Random rand = new Random();

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < rounds; i++) {
            if (possiblePlacements.size() == 0) {
                continue;
            }
            Game tempGame = game.copy();
            Placement tempPlacement;
            if (possiblePlacements.size() == 1) {
                tempPlacement = possiblePlacements.get(0);
            } else {
                tempPlacement = possiblePlacements.remove(rand.nextInt(possiblePlacements.size() - 1));
            }

            Thread thread = new Thread(() -> {
                Integer[] tempScore =  getBestScore(tempGame, rounds / 2, maxDepth, player, 0);
                predictedPlacements.put(tempPlacement, tempScore);
            });
            threads.add(thread);
        }

        long startTime = System.nanoTime();

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println("Time for last turn: " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.NANOSECONDS) + "s");

        for (Map.Entry<Placement, Integer[]> entry : predictedPlacements.entrySet()) {
            System.out.println(entry.getKey().toString() + "Score:  " + entry.getValue()[0] + " Boardscore:  " +  entry.getValue()[1]);
        }


        return selectBestPlacement(predictedPlacements);
    }

    private static Placement selectBestPlacement(HashMap<Placement, Integer[]> predictedPlacements) {
        Placement bestPlacement = null;
        int bestBoardScore = 0;
        for (Map.Entry<Placement, Integer[]> entry : predictedPlacements.entrySet()) {

            if (bestPlacement == null) {
                bestPlacement = entry.getKey();
                bestBoardScore = entry.getValue()[1];
            } else {
                if (entry.getValue()[1] > bestBoardScore) {
                    bestPlacement = entry.getKey();
                    bestBoardScore = entry.getValue()[1];
                }
            }
        }

        return bestPlacement;
    }

    public static Integer[] getBestScore(Game game, int rounds, int maxDepth, Color player, int bs) {
        List<Placement> possiblePlacements = game.getPossibleMovesBySize(game,0);
        List<Placement> predictedPlacements = new ArrayList<>();
        Random rand = new Random();

        if (possiblePlacements.size() == 0 || maxDepth == 0) {
            return new Integer[]{game.score().get(player) , bs};
        }

        for (int i = 0; i < rounds; i++) {
            Placement tempPlacement;
            if (possiblePlacements.size() == 1) {
                tempPlacement = possiblePlacements.get(0);
            } else {
                tempPlacement = possiblePlacements.get(rand.nextInt(possiblePlacements.size() - 1));
            }
            predictedPlacements.add(tempPlacement);
        }

        for (Placement p : predictedPlacements) {
            Game tempGame = game.copy();
            bs = bs + calculateBoardScore(game, player);
            tempGame.takeTurn(p);
            return getBestScore(tempGame, rounds, maxDepth - 1, player, bs);
        }
        return null;
    }

    private static int calculateBoardScore(Game game, Color currentPlayer) {
        int scoreBlack = 0, scoreWhite = 0;
        int piece = 1, ownedArea = 2;
        Color[][] board = game.getBoard().getBoardAsColorArray();

        for (int y = 0; y < 10; ++y) {
            for (int x = 0; x < 10; ++x) {
            switch (board[y][x]) {
                case Black:
                    scoreBlack += piece;
                    break;
                case Black_Owned:
                    scoreBlack += ownedArea;
                    break;
                case White:
                    scoreWhite += piece;
                    break;
                case White_Owned:
                    scoreWhite += ownedArea;
                    break;
                default:
                    break;
            }
            }
        }
        //TODO Check if vielleicht nicht anders herum
        return (currentPlayer == Color.Black) ? scoreBlack - scoreWhite : scoreWhite - scoreBlack;
    }
}
