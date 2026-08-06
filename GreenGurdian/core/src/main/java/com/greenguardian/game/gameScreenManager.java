package com.greenguardian.game;

public class gameScreenManager {
    public enum GameState {
        START,
        MENU,
        PLAY
    }
    
    private GameState currentState = GameState.START;
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public void setScreen(GameState state) {
        this.currentState = state;
    }
}
