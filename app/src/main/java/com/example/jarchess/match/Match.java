package com.example.jarchess.match;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.jarchess.match.activity.MatchActivity;
import com.example.jarchess.match.clock.MatchClock;
import com.example.jarchess.match.clock.MatchClockChoice;
import com.example.jarchess.match.clock.MatchClockObserver;
import com.example.jarchess.match.move.PieceMovement;
import com.example.jarchess.match.participant.LocalParticipant;
import com.example.jarchess.match.participant.LocalParticipantController;
import com.example.jarchess.match.participant.MatchParticipant;
import com.example.jarchess.match.pieces.Bishop;
import com.example.jarchess.match.pieces.Knight;
import com.example.jarchess.match.pieces.Pawn;
import com.example.jarchess.match.pieces.Piece;
import com.example.jarchess.match.pieces.Queen;
import com.example.jarchess.match.pieces.Rook;
import com.example.jarchess.match.result.CheckmateResult;
import com.example.jarchess.match.result.Result;
import com.example.jarchess.match.result.StalemateDrawResult;
import com.example.jarchess.match.result.TimeoutResult;
import com.example.jarchess.match.turn.Turn;

import java.util.Collection;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.jarchess.match.ChessColor.BLACK;
import static com.example.jarchess.match.ChessColor.WHITE;

//TODO javadocs
public abstract class Match {
    private final MatchHistory matchHistory;
    private final MatchParticipant blackPlayer;
    private final MatchParticipant whitePlayer;
    private final Chessboard chessboard;
    private final MoveExpert moveExpert;
    private ChessColor winner;
    private boolean isDone;
    private Result matchResult = null;
    private final MatchClock matchClock;
    //    private LocalParticipantController localParticipantController;
    private String gameToken;

    // TODO send the username of the winner in result

    public Match(@NonNull MatchParticipant participant1, @NonNull MatchParticipant participant2, MatchClockChoice matchClockChoice, MatchClockObserver matchClockObserver) {

        chessboard = new Chessboard();
        chessboard.reset();
        this.blackPlayer = participant1.getColor() == BLACK ? participant1 : participant2;
        this.whitePlayer = participant1.getColor() == WHITE ? participant1 : participant2;

        matchHistory = new MatchHistory(whitePlayer, blackPlayer);
        moveExpert = MoveExpert.getInstance();
        moveExpert.setMatchHistory(matchHistory);
        matchClock = matchClockChoice.makeMatchClock(matchClockObserver);
    }

    public Piece capture(Coordinate destination) {
        return chessboard.remove(destination);
    }

    public void checkForGameEnd(ChessColor nextTurnColor) {

        if (matchResult == null) {

            checkForTimeout();
            if (matchResult == null) {


                if (matchClock.flagHasFallen()) {
                    ChessColor colorOfWinner;
                    if (matchClock.getDisplayedTimeMillis(WHITE) <= 0L) {
                        colorOfWinner = BLACK;
                    } else if (matchClock.getDisplayedTimeMillis(BLACK) <= 0L) {
                        colorOfWinner = WHITE;
                    } else {
                        String msg = "checkForGameEnd: clock flag has fallen, but neither color is at 0";
                        Log.wtf(TAG, msg);
                        throw new IllegalStateException(msg);
                    }
                    matchResult = new TimeoutResult(colorOfWinner);
                } else if (!moveExpert.hasMoves(nextTurnColor, chessboard)) {
                    if (moveExpert.isInCheck(nextTurnColor, chessboard)) {
                        isDone = true;
                        matchResult = new CheckmateResult(ChessColor.getOther(nextTurnColor));
                    } else {
                        isDone = true;
                        matchResult = new StalemateDrawResult();
                    }
                } else {
                    // TODO handle our implementation of repeated board state draw
                    // I have a tendency to want to do the 5 time version that requires no

                    // TODO handle 50 move draw

                    // TODO imposibility of check draw handling

                }
            }
        }
    }

    public void checkForTimeout() {
        if (matchClock.flagHasFallen()) {
            isDone = true;
            ChessColor colorOfWinner;
            if (matchClock.getDisplayedTimeMillis(WHITE) <= 0L) {
                colorOfWinner = BLACK;
            } else if (matchClock.getDisplayedTimeMillis(BLACK) <= 0L) {
                colorOfWinner = WHITE;
            } else {
                String msg = "checkForGameEnd: clock flag has fallen, but neither color is at 0";
                Log.wtf(TAG, msg);
                throw new IllegalStateException(msg);
            }
            matchResult = new TimeoutResult(colorOfWinner);
        }
    }

    public MatchParticipant getBlackPlayer() {
        return blackPlayer;
    }

    public Turn getFirstTurn() throws MatchActivity.MatchOverException, InterruptedException {
        return whitePlayer.getFirstTurn();
    }

    public Collection<? extends PieceMovement> getLegalCastleMovements(Coordinate origin, Coordinate destination) {
        return moveExpert.getLegalCastleMovements(origin, destination, chessboard);
    }

    public MatchClock getMatchClock() {
        return matchClock;
    }

    public MatchHistory getMatchHistory() {
        return matchHistory;
    }

    public Result getMatchResult() {
        return matchResult;
    }

    public MatchParticipant getParticipant(ChessColor color) {
        switch (color) {

            case BLACK:
                return blackPlayer;
            case WHITE:
                return whitePlayer;
            default:
                throw new IllegalStateException("Unexpected color value: " + color);
        }
    }

    public Piece getPieceAt(@NonNull Coordinate coordinate) {
        return chessboard.getPieceAt(coordinate);
    }

    public Collection<Coordinate> getPossibleMoves(Coordinate origin) {
        return moveExpert.getLegalDestinations(origin, chessboard);
    }

    public Turn getTurn(@NonNull Turn turn) throws MatchActivity.MatchOverException, InterruptedException {
        return getParticipant(ChessColor.getOther(turn.getColor())).getNextTurn(turn);
    }

    public MatchParticipant getWhitePlayer() {
        return whitePlayer;
    }

    public boolean isDone() {
        return isDone;
    }

    public void move(Coordinate origin, Coordinate destination) {
        chessboard.move(origin, destination);
    }

    public void promote(Coordinate coordinate, Piece.PromotionChoice choice) {
        Log.d(TAG, "promote is running on thread: " + Thread.currentThread().getName());
        Log.d(TAG, "promote() called with: coordinate = [" + coordinate + "], choice = [" + choice + "]");
        Piece oldPiece = chessboard.getPieceAt(coordinate);

        if (oldPiece instanceof Pawn) {
            Pawn pawn = (Pawn) oldPiece;
            Piece newPiece;

            switch (choice.getPieceType()) {
                case ROOK:
                    newPiece = new Rook(pawn);
                    break;
                case KNIGHT:
                    newPiece = new Knight(pawn);
                    break;
                case BISHOP:
                    newPiece = new Bishop(pawn);
                    break;
                case QUEEN:
                    newPiece = new Queen(pawn);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value piece type from promotion choice: " + choice.getPieceType());
            }

            chessboard.remove(coordinate);
            chessboard.add(newPiece, coordinate);
        }
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public void setLocalParticipantController(LocalParticipantController localParticipantController) {
        if (blackPlayer instanceof LocalParticipant) {
            ((LocalParticipant) blackPlayer).setController(localParticipantController);
        }
        if (whitePlayer instanceof LocalParticipant) {
            ((LocalParticipant) whitePlayer).setController(localParticipantController);
        }
    }

    public void setWinner(ChessColor color) {

        if (winner == null) {

            winner = color;
            notifyAll();
        }

    }
}
