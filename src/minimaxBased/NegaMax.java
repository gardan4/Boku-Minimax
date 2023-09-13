package minimaxBased;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.location.Location;
import other.move.Move;
import utils.AIUtils;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class NegaMax extends AI
{

    //-------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;

    //-------------------------------------------------------------------------

    /**
     * Constructor
     */
    public NegaMax()
    {
        this.friendlyName = "NegaMax AI";
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
        Move bestMove = null;

        int Maxdepthnew = 2;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestValue = Integer.MIN_VALUE;
        FastArrayList<Move> legalMoves = game.moves(context).moves();

//        System.out.println("Legal moves: " + legalMoves);

        for (Move move : legalMoves)
        {
            // Create a new context to simulate the move
            Context simulatedContext = new Context(context);
            simulatedContext.game().apply(simulatedContext, move);
//            System.out.println("Simulated context: " + simulatedContext.state().owned().sites(context.state().mover()));

            int value = - negamax(simulatedContext, Maxdepthnew - 1, -beta, -alpha);

            if (value > bestValue)
            {
                bestValue = value;
                bestMove = move;
            }

            if (value > alpha)
            {
                alpha = value;
            }

        }

    return bestMove;

    }

    private int negamax(Context context, int depth, int alpha, int beta) {

        int playerToMakeMove = 3- context.state().mover();

        if (depth == 0 || context.trial().over())
        {
            // Implement your evaluation function here
            int value = evaluate(context, playerToMakeMove);

            System.out.println("playerToMakeMove: " + playerToMakeMove);
            System.out.println("owned spaces terminal" + context.state().owned().sites(playerToMakeMove));
            System.out.println("value: " + value);

            return value;
        }

        int score = Integer.MIN_VALUE;

        FastArrayList<Move> nextLegalMoves = context.game().moves(context).moves();
        System.out.println("Simulated context: " + nextLegalMoves);

        for (Move nextMove : nextLegalMoves)
        {
            Context simulatedContext = new TempContext(context);
            simulatedContext.game().apply(simulatedContext, nextMove);

            int value = -negamax(simulatedContext, depth - 1, -beta, -alpha);

            if (value > score)
            {
                score = value;
            }

            if (score > alpha)
            {
                alpha = score; // Alpha-beta pruning
            }

            if (score >= beta)
            {
                break; // Alpha-beta pruning
            }
        }

        return score;
    }

    private int evaluate(Context context, int playerToMakeMove) {

        if (context.trial().over())
        {
            return -9999;
        }

        int playerLastMadeMove = 3 - playerToMakeMove; // Assuming a two-player game with player IDs 1 and 2
        int score = 0;


        TIntArrayList coordinatesOfCurrentPlayerPieces = context.state().owned().sites(playerToMakeMove);


        TIntArrayList coordinatesOfOpponentPieces = context.state().owned().sites(playerLastMadeMove);




        int currentPlayerPieceCount = coordinatesOfCurrentPlayerPieces.size();
        score += currentPlayerPieceCount;

        int opponentPieceCount = coordinatesOfOpponentPieces.size();
        score -= opponentPieceCount;

        // Define the center cell indices
        int[] centerIndices = {29, 30, 31, 38, 39, 40, 41, 48, 49, 50};


        // Count the number of pieces in the center for each player
        for (int centerIndex : centerIndices) {
            if (coordinatesOfCurrentPlayerPieces.contains(centerIndex)) {
                score += 5;
            }
            if (coordinatesOfOpponentPieces.contains(centerIndex)) {
                score -= 3;
            }
        }

        // Calculate the score based on the number of pieces for the current player and center pieces
        score += (currentPlayerPieceCount - opponentPieceCount);

        System.out.println("playerToMakeMove: " + playerToMakeMove);
        System.out.println("owned spaces" + coordinatesOfCurrentPlayerPieces);
        System.out.println("value: " + score);


        return -score;
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
