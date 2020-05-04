package com.example.jarchess.match.participant;

import com.example.jarchess.match.ChessColor;
import com.example.jarchess.match.DrawResponse;
import com.example.jarchess.match.MatchOverException;
import com.example.jarchess.match.history.MatchHistory;
import com.example.jarchess.jaraccount.styles.avatar.AvatarStyle;
import com.example.jarchess.match.turn.Turn;

/**
 * A match participant represents a entity participating in a chess match.
 *
 * @author Joshua Zierman
 */
public interface MatchParticipant {

    /**
     * Gets the avatar style for this participant
     *
     * @return the avatar style for this participant
     */
    AvatarStyle getAvatarStyle();

    /**
     * Gets the color of this participant.
     *
     * @return the color of this participant
     */
    ChessColor getColor();

    /**
     * Takes the first turn from stating position.
     *
     * @return the turn that this participant takes
     */
    Turn getFirstTurn(MatchHistory matchHistory) throws InterruptedException, MatchOverException;

    /**
     * Gets the name of this participant.
     *
     * @return the name of this participant
     */
    String getName();

    /**
     * Responds to a draw request
     *
     * @return a response
     */
    DrawResponse getDrawResponse(MatchHistory matchHistory) throws InterruptedException;

    /**
     * Takes a turn in response to the last turn from the other participant.
     *
     * @param matchHistory the matchHistory recorded so far
     * @return the turn that this participant takes
     */
    Turn getNextTurn(MatchHistory matchHistory) throws InterruptedException, MatchOverException;
}
