package minimaxBased;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        int score = 0;

        // Get the coordinates of the pieces for each player
        TIntArrayList coordinatesOfMaxPlayerPieces = context.state().owned().sites(maxPlayerID);
//        System.out.println("coordinatesOfMaxPlayerPieces: " + coordinatesOfMaxPlayerPieces);
        TIntArrayList coordinatesOfMinPlayerPieces = context.state().owned().sites(3 - maxPlayerID);
//        System.out.println("coordinatesOfMinPlayerPieces: " + coordinatesOfMinPlayerPieces);


        if (context.trial().over() && isMaximizingPlayer) {
//            System.out.println("Game over maximizer wins, -9999");
            return Integer.MIN_VALUE;
        }
        else if (context.trial().over() && !isMaximizingPlayer) {
//            System.out.println("Game over minimizer wins, 9999");
            return Integer.MAX_VALUE;
        }

        int[] centerIndices = {29, 30, 31, 38, 39, 40, 41, 48, 49, 50};



        int maxPlayerPieceCount = coordinatesOfMaxPlayerPieces.size();
        int minPlayerPieceCount = coordinatesOfMinPlayerPieces.size();

        score += maxPlayerPieceCount;
        score -= minPlayerPieceCount;

        // Count the number of pieces in the center for each player
        for (int centerIndex : centerIndices) {
            if (coordinatesOfMaxPlayerPieces.contains(centerIndex)) {
                score += 5;
            }
            if (coordinatesOfMinPlayerPieces.contains(centerIndex)) {
                score -= 5;
            }
        }
//        System.out.println("Score: " + score);

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

    //-------------------------------------------------------------------------

}
