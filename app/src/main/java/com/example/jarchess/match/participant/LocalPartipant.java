package com.example.jarchess.match.participant;

import com.example.jarchess.match.ChessColor;
import com.example.jarchess.match.resignation.ResignationEvent;
import com.example.jarchess.match.resignation.ResignationException;
import com.example.jarchess.match.styles.AvatarStyle;
import com.example.jarchess.match.turn.Move;
import com.example.jarchess.match.turn.Turn;
import com.example.jarchess.testmode.TestableCurrentTime;

public abstract class LocalPartipant implements MatchParticipant {
    private final String name;
    private final ChessColor color;
    private final AvatarStyle avatarStyle;
    private LocalParticipantController controller;

    /**
     * Creates a local participant.
     *
     * @param name        the name of the participant
     * @param color       the color of the participant
     * @param avatarStyle the style of the avatar for this participant
     */
    public LocalPartipant(String name, ChessColor color, AvatarStyle avatarStyle) {
        this.name = name;
        this.color = color;
        this.avatarStyle = avatarStyle;
    }

    /**
     * Gets the name of this participant.
     *
     * @return the name of this participant
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Takes the first turn from stating position.
     *
     * @return the turn that this participant takes
     * @throws ResignationException if a resignation was detected.
     */
    @Override
    public Turn takeFirstTurn() throws ResignationException {
        return takeTurn();
    }

    /**
     * Takes a turn in response to the last turn from the other participant.
     *
     * @param lastTurnFromOtherParticipant the turn that happened immediately before by the other participant
     * @return the turn that this participant takes
     * @throws ResignationException if a resignation was detected.
     */
    @Override
    public Turn takeTurn(Turn lastTurnFromOtherParticipant) throws ResignationException {
        return takeTurn();
    }

    private Turn takeTurn() {
        long start, end, elapsed;
        Move move = null;

        start = TestableCurrentTime.currentTimeMillis();
//        move = controller.getMove(this); // at the moment we assume the move is valid
        end = TestableCurrentTime.currentTimeMillis();

        elapsed = end - start;

        return new Turn(this.color, move, elapsed);
    }

    /**
     * Resigns from the match.
     */
    @Override
    public void resign() {

        //TODO
    }

    /**
     * Gets the color of this participant.
     *
     * @return the color of this participant
     */
    @Override
    public ChessColor getColor() {
        return color;
    }

    /**
     * Gets the avatar resource id for this participant
     *
     * @return the avatar resource id for this participant
     */
    @Override
    public AvatarStyle getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void observeResignationEvent(ResignationEvent resignationEvent) {
        //TODO
    }

    /**
     * sets the controller of this local participant
     *
     * @param controller
     */
    public void setController(LocalParticipantController controller) {
        this.controller = controller;
    }
}