package minimaxBased;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.location.Location;
import other.move.Move;

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

    }

    private void initializeTranspositionTable() {
        // You can choose a reasonable size for your HashMap based on your game
        // The size should depend on the expected number of unique positions
        // in your game tree. A larger table can handle more positions but uses
        // more memory.
        int initialCapacity = 3000000; // Adjust this according to your game's needs
        transpositionTable = new HashMap<>(initialCapacity);
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
        System.out.println("transpositionTable: " + transpositionTable.size());
        Move bestMove = null;
        int Maxdepthnew =100;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        FastArrayList<Move> legalMoves = game.moves(context).moves();
        int maxPlayerId = context.state().mover();

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (long) (maxSeconds * 1000);;

        Move previousBestMove = null;

        // Loop through the depths while game is not over
        for (int depth = 1; depth <= Maxdepthnew; depth++) {

            bestScore = Integer.MIN_VALUE;
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;

            // Create a list to store legal moves ordered by principal variation
            FastArrayList<Move> orderedMoves = new FastArrayList<>();

            // If there's a previous best move, add it to the front of the ordered moves
            if (previousBestMove != null) {
                orderedMoves.add(previousBestMove);
            }

            for (Move move : legalMoves) {
                // Skip the move if it's the same as the previous best move
                if (move.equals(previousBestMove)) {
                    continue;
                }
                // Add the move to the ordered moves list
                orderedMoves.add(move);
            }

            System.out.println("Depth: " + depth);
            for (Move move : orderedMoves) {


                // Create a new context to simulate the move
                Context simulatedContext = new TempContext(context);
                simulatedContext.game().apply(simulatedContext, move);

                int score;
                if (simulatedContext.trial().over()) {
                    score = Integer.MAX_VALUE;
                    bestMove = move;
                    return bestMove;
                } else {
                    score = minimax(simulatedContext, depth - 1, alpha, beta, false, maxPlayerId);
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, score);

                if (beta <= alpha) {
                    break;
                }
            }

            // Store the best move found in this iteration
            previousBestMove = bestMove;
            System.out.println("Done searching depth: " + depth);

            // Check if time limit has been reached
            if (System.currentTimeMillis() >= endTime) {
                break;
            }
        }

        System.out.println("transpositionTable: " + transpositionTable.size());
        return bestMove;
    }


    private int minimax(Context context, int depth, int alpha, int beta, boolean isMaximizingPlayer, int maxPlayerID)
    {
        int olda = alpha;
        long hashKey = context.state().stateHash() + context.state().mover() + depth;
//        System.out.println("hashKey: " + hashKey);

        // Check if the current state is in the transposition table
        if (transpositionTable.containsKey(hashKey)) {
            TranspositionTableEntry entry = transpositionTable.get(hashKey);
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

        // Check If player has a back to back move to remove a piece
        if (nextLegalMoves.size() != 0) {
            boolean isRemoveMove = nextLegalMoves.get(0).actionType().toString().contains("Remove");
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

            for (Move nextMove : nextLegalMoves) {
                Context simulatedContext = new TempContext(context);
                simulatedContext.game().apply(simulatedContext, nextMove);

                int score = minimax(simulatedContext, depth - 1, alpha, beta, false, maxPlayerID);


//                System.out.println("Scoremax: " + score);

                alpha = Math.max(alpha, score);
                bestScore = Math.max(bestScore, score);
//                System.out.println("Bestscoremax: " + bestScore);

                if (beta <= alpha) {
                    break;
                }

                // Store the best move in the transposition table
                if (bestScore > olda) {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 0, depth, nextMove));
                }
                else {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 1, depth, nextMove));
                }
            }
            return bestScore;
        }
        // Minimizing player
        else
        {
            int bestScore = Integer.MAX_VALUE;

            for (Move nextMove : nextLegalMoves) {
                Context simulatedContext = new TempContext(context);
                simulatedContext.game().apply(simulatedContext, nextMove);

                int score = minimax(simulatedContext, depth - 1, alpha, beta, true, maxPlayerID);

                beta = Math.min(beta, score);
                bestScore = Math.min(bestScore, score);
//                System.out.println("Bestscoremin: " + bestScore);

                if (beta <= alpha) {
                    break;
                }

                // Store the best move in the transposition table
                if (bestScore < beta) {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 0, depth, nextMove));
                }
                else {
                    transpositionTable.put(hashKey, new TranspositionTableEntry(bestScore, 2, depth, nextMove));
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
        Map<Integer, String> indexToCoordinate = createIndexToCoordinateMap();
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
        String[] centerCoords = {"F7", "E6", "D5", "G7", "F6", "E5", "D4", "G6", "F5", "E4"};

        // Count the number of pieces for each player
        int maxPlayerPieceCount = coordinatesOfMaxPlayerPieces.size();
        int minPlayerPieceCount = coordinatesOfMinPlayerPieces.size();
        score += maxPlayerPieceCount;
        score -= minPlayerPieceCount;

        // Count the number of pieces in the center for each player
        for (String centerCoord : centerCoords) {
            if (coordinatesOfMaxPlayerPieces.contains(centerCoord)) {
                score += 5;
            }
            if (coordinatesOfMinPlayerPieces.contains(centerCoord)) {
                score -= 5;
            }
        }

        // detect if a player has 3 pieces in a row, by checking the coordinatesOfPlayerPieces in the 3 possible directions ( vertical, diagonal, anti-diagonal), on the hexagonal board
        // Do this by having 3 loops: 1 increasing the number, 1 increasing the letter, 1 increasing both
        // For each loop, check if the next coordinate is in the coordinatesOfPlayerPieces, if so, increase the counter
        // If the counter reaches 3, return the score
        // If the counter is 2, check if the next coordinate is empty, if so, increase the score by 1
        // If the counter is 1, check if the next coordinate is empty, if so, increase the score by 0.5
        // If the counter is 0, check if the next coordinate is empty, if so, increase the score by 0.25

        // Vertical

        // Loop over the letters
        for (int i = 0; i < 10; i++) {
            int counter = 0;
            // Loop over the numbers
            for (int j = 0; j < 10; j++) {
                String coordinate = Character.toString((char) (i + 65)) + Integer.toString(j + 1);
                if (coordinatesOfMaxPlayerPieces.contains(coordinate)) {
                    counter++;
                }
                else if (coordinatesOfMinPlayerPieces.contains(coordinate)) {
                    counter--;
                }
                else {
                    if (counter == 3) {
                        score += 100;
                        counter = 0;
                    }
                    else if (counter == 2) {
                        score += 1;
                        counter = 0;
                    }
                    else if (counter == 1) {
                        score += 0.5;
                        counter = 0;
                    }
                    else if (counter == 0) {
                        score += 0.25;
                        counter = 0;
                    }
                }
            }
        }

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

    private Map<Integer, String> createIndexToCoordinateMap() {
        Map<Integer, String> indexToCoordinate = new TreeMap<>();

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
        return indexToCoordinate;
    }

}
