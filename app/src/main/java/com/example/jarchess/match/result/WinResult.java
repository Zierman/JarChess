package com.example.jarchess.match.result;

import com.example.jarchess.match.ChessColor;

abstract class WinResult extends Result {
    public WinResult(ChessColor winnerColor) {
        super(winnerColor);
    }

    public ChessColor getWinnerColor() {
        return winnerColor;
    }

    public ChessColor getLoserColor() {
        return ChessColor.getOther(winnerColor);
    }

    @Override
    protected String getMessage() {
        return winnerColor.toString() + " wins by " + winTypeString() + ".";
    }

    protected abstract String winTypeString();
}
