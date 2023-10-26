package minimaxBased;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.location.Location;
import other.move.Move;

import java.io.Console;
import java.util.*;

// Add a TranspositionTableEntry class to store the necessary information
class TranspositionTableEntry {
    int value;
    int type; // 0 for exact value, 1 for lower bound, 2 for upper bound
    int depth;
    Move bestMove;

    public TranspositionTableEntry(int value, int type, int depth, Move bestMove) {
        this.value = value;
        this.type = type;
        this.depth = depth;
        this.bestMove = bestMove;
    }
}

public class MiniMax extends AI
{
    private HashMap<Long, TranspositionTableEntry> transpositionTable;
    private Map<Integer, String> indexToCoordinate;
    private String[] centerCoords;

    //-------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;

    //-------------------------------------------------------------------------

    /**
     * Constructor
     */
    public MiniMax()
    {
        this.friendlyName = "MiniMax AI";
        this.transpositionTable = new HashMap<>();
        initializeTranspositionTable();
        initializeIndexToCoordinateMap();
        initializeCenterCoords();
    }

    private void initializeTranspositionTable() {
        // You can choose a reasonable size for your HashMap based on your game
        // The size should depend on the expected number of unique positions
        // in your game tree. A larger table can handle more positions but uses
        // more memory.
        int initialCapacity = 3000000; // Adjust this according to your game's needs
        transpositionTable = new HashMap<>(initialCapacity);
    }

    private void initializeCenterCoords() {
        centerCoords = new String[10];
        centerCoords[0] = "F7";
        centerCoords[1] = "E6";
        centerCoords[2] = "D5";
        centerCoords[3] = "G7";
        centerCoords[4] = "F6";
        centerCoords[5] = "E5";
        centerCoords[6] = "D4";
        centerCoords[7] = "G6";
        centerCoords[8] = "F5";
        centerCoords[9] = "E4";
    }

    private void initializeIndexToCoordinateMap() {
        indexToCoordinate = new TreeMap<>();

        // Fill in the mapping here
        indexToCoordinate.put(0, "F1");
        indexToCoordinate.put(1, "G2");
        indexToCoordinate.put(2, "H3");
        indexToCoordinate.put(3, "I4");
        indexToCoordinate.put(4, "J5");
        indexToCoordinate.put(5, "E1");
        indexToCoordinate.put(6, "F2");
        indexToCoordinate.put(7, "G3");
        indexToCoordinate.put(8, "H4");
        indexToCoordinate.put(9, "I5");
        indexToCoordinate.put(10, "J6");
        indexToCoordinate.put(11, "D1");
        indexToCoordinate.put(12, "E2");
        indexToCoordinate.put(13, "F3");
        indexToCoordinate.put(14, "G4");
        indexToCoordinate.put(15, "H5");
        indexToCoordinate.put(16, "I6");
        indexToCoordinate.put(17, "J7");
        indexToCoordinate.put(18, "C1");
        indexToCoordinate.put(19, "D2");
        indexToCoordinate.put(20, "E3");
        indexToCoordinate.put(21, "F4");
        indexToCoordinate.put(22, "G5");
        indexToCoordinate.put(23, "H6");
        indexToCoordinate.put(24, "I7");
        indexToCoordinate.put(25, "J8");
        indexToCoordinate.put(26, "B1");
        indexToCoordinate.put(27, "C2");
        indexToCoordinate.put(28, "D3");
        indexToCoordinate.put(29, "E4");
        indexToCoordinate.put(30, "F5");
        indexToCoordinate.put(31, "G6");
        indexToCoordinate.put(32, "H7");
        indexToCoordinate.put(33, "I8");
        indexToCoordinate.put(34, "J9");
        indexToCoordinate.put(35, "A1");
        indexToCoordinate.put(36, "B2");
        indexToCoordinate.put(37, "C3");
        indexToCoordinate.put(38, "D4");
        indexToCoordinate.put(39, "E5");
        indexToCoordinate.put(40, "F6");
        indexToCoordinate.put(41, "G7");
        indexToCoordinate.put(42, "H8");
        indexToCoordinate.put(43, "I9");
        indexToCoordinate.put(44, "J10");
        indexToCoordinate.put(45, "A2");
        indexToCoordinate.put(46, "B3");
        indexToCoordinate.put(47, "C4");
        indexToCoordinate.put(48, "D5");
        indexToCoordinate.put(49, "E6");
        indexToCoordinate.put(50, "F7");
        indexToCoordinate.put(51, "G8");
        indexToCoordinate.put(52, "H9");
        indexToCoordinate.put(53, "I10");
        indexToCoordinate.put(54, "A3");
        indexToCoordinate.put(55, "B4");
        indexToCoordinate.put(56, "C5");
        indexToCoordinate.put(57, "D6");
        indexToCoordinate.put(58, "E7");
        indexToCoordinate.put(59, "F8");
        indexToCoordinate.put(60, "G9");
        indexToCoordinate.put(61, "H10");
        indexToCoordinate.put(62, "A4");
        indexToCoordinate.put(63, "B5");
        indexToCoordinate.put(64, "C6");
        indexToCoordinate.put(65, "D7");
        indexToCoordinate.put(66, "E8");
        indexToCoordinate.put(67, "F9");
        indexToCoordinate.put(68, "G10");
        indexToCoordinate.put(69, "A5");
        indexToCoordinate.put(70, "B6");
        indexToCoordinate.put(71, "C7");
        indexToCoordinate.put(72, "D8");
        indexToCoordinate.put(73, "E9");
        indexToCoordinate.put(74, "F10");
        indexToCoordinate.put(75, "A6");
        indexToCoordinate.put(76, "B7");
        indexToCoordinate.put(77, "C8");
        indexToCoordinate.put(78, "D9");
        indexToCoordinate.put(79, "E10");

//        for (Map.Entry<Integer, String> entry : indexToCoordinate.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
    }



    //-------------------------------------------------------------------------

    @Override
    public Move selectAction
            (
                    final Game game,
                    final Context context,
                    final double maxSeconds,
                    final int maxIterations,
                    final int maxDepth
            )
    {
        System.out.println("transpositionTable before search: " + transpositionTable.size());
        Move bestMove = null;
        int Maxdepthnew =100;
        int bestScore;
        int alpha;
        int beta;
        FastArrayList<Move> legalMoves = game.moves(context).moves();
        int maxPlayerId = context.state().mover();

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (long) (maxSeconds * 1000);;

        Move previousBestMove = null;

        // Create a two-dimensional array to store killer moves for each depth and player
        FastArrayList<Move>[][] killerMoves = new FastArrayList[Maxdepthnew][2];
        for (int i = 0; i < Maxdepthnew; i++) {
            killerMoves[i][0] = new FastArrayList<>();
            killerMoves[i][1] = new FastArrayList<>();
        }

        // Loop through the depths while game is not over
        for (int depth = 1; depth <= Maxdepthnew; depth++) {

            bestScore = Integer.MIN_VALUE;
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;

            // Create a list to store legal moves ordered by principal variation
            FastArrayList<Move> orderedMoves = new FastArrayList<>();

            boolean pcpfilled = false;
            for (Move move : legalMoves) {
                // PCP move first
                if (move.equals(previousBestMove) || previousBestMove != null) {
                    orderedMoves.add(0, previousBestMove);
                    pcpfilled = true;
                }
                //Killer moves second
                else if (killerMoves[depth][context.state().mover()-1].contains(move) && !pcpfilled) {
                    orderedMoves.add(0, move);
                }
                //Killer moves second
                else if (killerMoves[depth][context.state().mover()-1].contains(move) && pcpfilled) {
                    orderedMoves.add(1, move);
                }
                else {
                    orderedMoves.add(move);
                }
            }

            // Create a new context to simulate the move
            Context simulatedContext = new TempContext(context);

            for (Move move : orderedMoves) {


                //apply the move
                simulatedContext.game().apply(simulatedContext, move);

                // Check if the game is over
                int score;
                if (simulatedContext.trial().over()) {
                    score = Integer.MAX_VALUE;
                    bestMove = move;
                    return bestMove;
                }
                else
                {
                    score = minimax(simulatedContext, depth - 1, alpha, beta, false, maxPlayerId, killerMoves);
                }
                // undo the move
                simulatedContext.game().undo(simulatedContext);

                // Check if the score is better than the best score
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                // Update alpha and beta
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    // This is a beta cutoff; store the move as a killer move
                    if (!killerMoves[depth][context.state().mover()-1].contains(move)) {
                        killerMoves[depth][context.state().mover()-1].add(move);
                    }
                    break;
                }
            }

            // Store the best move found in this iteration
            previousBestMove = bestMove;
            System.out.println("Done searching depth: " + depth);

            // Check if time limit has been reached
            if (System.currentTimeMillis() >= endTime) {
                System.out.println("time delta: " + ((System.currentTimeMillis() - startTime) / 1000.0));
                break;
            }
        }

        System.out.println("transpositionTable after search: " + transpositionTable.size());
        return bestMove;
    }


    private int minimax(Context context, int depth, int alpha, int beta, boolean isMaximizingPlayer, int maxPlayerID, FastArrayList<Move>[][] killerMoves)
    {
        Move bestMove = null;
        int olda = alpha;
        long hashKey = context.state().fullHash() ^ context.state().mover();
//        System.out.println("hashKey: " + hashKey);

        // Check if the current state is in the transposition table
        if (transpositionTable.containsKey(hashKey)) {
            TranspositionTableEntry entry = transpositionTable.get(hashKey);
            bestMove = entry.bestMove;

            if (entry.depth >= depth) {
                if (entry.type == 0) {
                    return entry.value;
                }
                else if (entry.type == 1) {
                    alpha = Math.max(alpha, entry.value);
                }
                else if (entry.type == 2) {
                    beta = Math.min(beta, entry.value);
                }
                if (alpha >= beta) {
                    return entry.value;
                }
            }
        }
        // give info about the transpositionTable, the length
//        System.out.println("transpositionTable: " + transpositionTable.size());

        if (depth == 0 || context.trial().over()) {
            // Implement your evaluation function here
            int value = evaluate(context, isMaximizingPlayer, maxPlayerID);
            return value;
        }

        FastArrayList<Move> nextLegalMoves = context.game().moves(context).moves();
        FastArrayList<Move> orderedMoves = new FastArrayList<>();

        boolean pcpfilled = false;
        for (Move nextMove : nextLegalMoves) {
            // PCP move first
            if (nextMove.equals(bestMove)) {
                orderedMoves.add(0, nextMove);
                pcpfilled = true;
            }
            // Check if the move is a killer move for the current depth and player
            else if (killerMoves[depth][context.state().mover()-1].contains(nextMove) && !pcpfilled) {
                // Try the killer move first
                orderedMoves.add(0, nextMove);
            }
            else if (killerMoves[depth][context.state().mover()-1].contains(nextMove) && pcpfilled) {
                orderedMoves.add(1, nextMove);
            }
            else {
                orderedMoves.add(nextMove);
            }
        }

        // Check If player has a back to back move to remove a piece
        if (orderedMoves.size() != 0) {
            boolean isRemoveMove = orderedMoves.get(0).actionType().toString().contains("Remove");
            if (isRemoveMove)
            {
                //flip isMaximizingPlayer boolean value
                isMaximizingPlayer = !isMaximizingPlayer;
                depth = depth + 1;
            }
        }

        if (isMaximizingPlayer)
        {
            int bestScore = Integer.MIN_VALUE;
            Context simulatedContext = new TempContext(context);


            // Loop through the legal moves
            for (Move nextMove : orderedMoves) {
                simulatedContext.game().apply(simulatedContext, nextMove);

                int score = minimax(simulatedContext, depth - 1, alpha, beta, false, maxPlayerID, killerMoves);

                // undo the move
                simulatedContext.game().undo(simulatedContext);

                alpha = Math.max(alpha, score);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = nextMove;
                }

                if (beta <= alpha) {
                    // Store the move as a killer move for the current depth and player
                    if (!killerMoves[depth][context.state().mover()-1].contains(nextMove)) {
                        killerMoves[depth][context.state().mover()-1].add(0, nextMove);
                    }
                    break;
                }

                // Store the best move in the transposition table
                if (bestScore > olda) {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 0, depth, bestMove));
                }
                else {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 1, depth, bestMove));
                }

            }

            return bestScore;
        }
        // Minimizing player
        else
        {
            int bestScore = Integer.MAX_VALUE;
            Context simulatedContext = new TempContext(context);

            for (Move nextMove : orderedMoves) {
                simulatedContext.game().apply(simulatedContext, nextMove);

                int score = minimax(simulatedContext, depth - 1, alpha, beta, true, maxPlayerID, killerMoves);

                // undo the move
                simulatedContext.game().undo(simulatedContext);

                beta = Math.min(beta, score);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = nextMove;
                }

                if (beta <= alpha) {
                    // Store the move as a killer move for the current depth and player
                    if (!killerMoves[depth][context.state().mover()-1].contains(nextMove)) {
                        killerMoves[depth][context.state().mover()-1].add(0, nextMove);
                    }
                    break;
                }

                // Store the best move in the transposition table
                if (bestScore < beta) {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 0, depth, bestMove));
                }
                else {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 2, depth, bestMove));
                }
            }
            return bestScore;
        }

    }

    private int evaluate(Context context, boolean isMaximizingPlayer, int maxPlayerID) {
        //Check if game is over
        if (context.trial().over() && isMaximizingPlayer) {
            return Integer.MIN_VALUE;
        }
        else if (context.trial().over() && !isMaximizingPlayer) {
            return Integer.MAX_VALUE;
        }

        // initialise score
        int score = 0;
        //get the coordinates of the pieces of each player
        TIntArrayList indicesOfMaxPlayerPieces = context.state().owned().sites(maxPlayerID);
        TIntArrayList indicesOfMinPlayerPieces = context.state().owned().sites(3 - maxPlayerID);
        ArrayList<String> coordinatesOfMaxPlayerPieces = new ArrayList<>();
        ArrayList<String> coordinatesOfMinPlayerPieces = new ArrayList<>();
        for (int i = 0; i < indicesOfMaxPlayerPieces.size(); i++) {
            coordinatesOfMaxPlayerPieces.add(indexToCoordinate.get(indicesOfMaxPlayerPieces.get(i)));
        }
        for (int i = 0; i < indicesOfMinPlayerPieces.size(); i++) {
            coordinatesOfMinPlayerPieces.add(indexToCoordinate.get(indicesOfMinPlayerPieces.get(i)));
        }

        // Count the number of pieces for each player
        int maxPlayerPieceCount = coordinatesOfMaxPlayerPieces.size();
        int minPlayerPieceCount = coordinatesOfMinPlayerPieces.size();
        score += maxPlayerPieceCount * 3;
        score -= minPlayerPieceCount * 3;

        // Count the number of pieces in the center for each player
        for (String centerCoord : centerCoords) {
            if (coordinatesOfMaxPlayerPieces.contains(centerCoord)) {
                score += 2;
            }
            if (coordinatesOfMinPlayerPieces.contains(centerCoord)) {
                score -= 2;
            }
        }

        // Check if the player has 3 or 4 pieces in a row
        //todo: lines are not that useful if they cant be finished
        //todo: make lines that need to be blocked in such a way that they match up with another opponent piece so that an opponent piece can possibly be removed
        score += checkThreeOrFourInARow(coordinatesOfMaxPlayerPieces);
        score -= (int) (checkThreeOrFourInARow(coordinatesOfMinPlayerPieces));

        return score;
    }

    @Override
    public void initAI(final Game game, final int playerID)
    {
        this.player = playerID;
    }

    @Override
    public boolean supportsGame(final Game game)
    {
        if (game.isStochasticGame())
            return false;

        if (!game.isAlternatingMoveGame())
            return false;

        return true;
    }

    private int checkThreeOrFourInARow(ArrayList<String> coordinatesOfPlayerPieces) {
        int score = 0;
        int threeInARow = 5;
        int fourInARow = 15;
        //loop over the coordinates of the player
        for (String coord : coordinatesOfPlayerPieces) {
            // get the letter and number of the coordinate
            char letter = coord.charAt(0);
            int number = Integer.parseInt(coord.substring(1));

            // get the coordinate above and if the player has that, then check one higher and if the player has that, then check one higher etc. in a loop
            int counter = 0;
            for (int i = 1; i < 4; i++) {
                String coordAbove = String.valueOf((char) (letter + i)) + number;
                if (coordinatesOfPlayerPieces.contains(coordAbove)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            // get the coordinate below and if the player has that, then check one lower and if the player has that, then check one lower etc. in a loop
            for (int i = 1; i < 4; i++) {
                String coordBelow = String.valueOf((char) (letter - i)) + number;
                if (coordinatesOfPlayerPieces.contains(coordBelow)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            // if the counter is 2 or higher, then the player has 3 pieces in a row give points for 3 and 4 in a row
            if (counter >= 2) {
                score += threeInARow;
            }
            if (counter >= 3) {
                score += fourInARow;
            }

            // reset the counter
            counter = 0;

            // Do the same but now increase the number
            for (int i = 1; i < 4; i++) {
                String coordAbove = String.valueOf(letter) + (number + i);
                if (coordinatesOfPlayerPieces.contains(coordAbove)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            for (int i = 1; i < 4; i++) {
                String coordBelow = String.valueOf(letter) + (number - i);
                if (coordinatesOfPlayerPieces.contains(coordBelow)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            if (counter >= 2) {
                score += threeInARow;
            }
            if (counter >= 3) {
                score += fourInARow;
            }

            // reset the counter
            counter = 0;

            // Do the same but now increase both
            for (int i = 1; i < 4; i++) {
                String coordAbove = String.valueOf((char) (letter + i)) + (number + i);
                if (coordinatesOfPlayerPieces.contains(coordAbove)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            for (int i = 1; i < 4; i++) {
                String coordBelow = String.valueOf((char) (letter - i)) + (number - i);
                if (coordinatesOfPlayerPieces.contains(coordBelow)) {
                    counter += 1;
                }
                else {
                    break;
                }
            }
            if (counter >= 2) {
                score += threeInARow;
            }
            if (counter >= 3) {
                score += fourInARow;
            }
        }

//        System.out.println("row Score: " + score);
        return score;
    }


}
