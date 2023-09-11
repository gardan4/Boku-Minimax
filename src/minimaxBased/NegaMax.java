package minimaxBased;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
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

        int Maxdepthnew = 5;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestValue = Integer.MIN_VALUE;
        FastArrayList<Move> legalMoves = AIUtils.extractMovesForMover(game.moves(context).moves(), player);


        for (Move move : legalMoves)
        {

            // Create a new context to simulate the move
            Context simulatedContext = new Context(context);
            TIntArrayList coordinatesofpiecesnew = context.state().owned().sites(1);
            System.out.println("coordinates of player 1 pieces: " + coordinatesofpiecesnew);

            simulatedContext.game().apply(simulatedContext, move);

            int value = -negamax(simulatedContext, Maxdepthnew - 1, -beta, -alpha);

            if (value > bestValue)
            {
                bestValue = value;
                bestMove = move;
            }

            alpha = Math.max(alpha, value);
        }

        // Apply the selected move to the actual game context
        context.game().apply(context, bestMove);

    return bestMove;

    }

    private int negamax(Context context, int depth, int alpha, int beta) {
        if (depth <= 0 || context.trial().over())
        {
            // Implement your evaluation function here
            return evaluate(context);
        }

        int bestValue = Integer.MIN_VALUE;
        FastArrayList<Move> legalMoves = AIUtils.extractMovesForMover(context.game().moves(context).moves(), context.state().mover());

        for (Move nextMove : legalMoves)
        {
            context.game().apply(context, nextMove);
            int value = -negamax(context, depth - 1, -beta, -alpha);

            bestValue = Math.max(bestValue, value);
            alpha = Math.max(alpha, value);

            if (alpha >= beta)
            {
                break; // Alpha-beta pruning
            }
        }

        return bestValue;
    }

    private int evaluate(Context context) {
//        int cellOwner = context.game().numComponents();

//        int player = context.state().mover();
//        int opponent = 3 - player; // Assuming a two-player game with player IDs 1 and 2

        TIntArrayList coordinatesOfPlayer1Pieces = context.state().owned().sites(1);
        TIntArrayList coordinatesOfPlayer2Pieces = context.state().owned().sites(2);

        int player1PieceCount = coordinatesOfPlayer1Pieces.size();
        int player2PieceCount = coordinatesOfPlayer2Pieces.size();

        // Define the center cell indices
        int[] centerIndices = {29, 30, 31, 38, 39, 40, 41, 48, 49, 50};

        int player1CenterPieceCount = 0;
        int player2CenterPieceCount = 0;

        // Count the number of pieces in the center for each player
        for (int centerIndex : centerIndices) {
            if (coordinatesOfPlayer1Pieces.contains(centerIndex)) {
                player1CenterPieceCount++;
            }
            if (coordinatesOfPlayer2Pieces.contains(centerIndex)) {
                player2CenterPieceCount++;
            }
        }

        // Calculate the score based on the number of pieces for each player and center pieces
        int score = (player1PieceCount - player2PieceCount) + (player1CenterPieceCount - player2CenterPieceCount);
        System.out.println("score: " + score);

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
