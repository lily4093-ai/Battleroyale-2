package com.battleroyale.game;

public enum GameState {
    WAITING,      // 게임 시작 대기 중
    STARTING,     // 게임 시작 중 (카운트다운)
    ACTIVE,       // 게임 진행 중
    DEATH_TIME,   // 데스타임 단계 (마지막 5분)
    ENDING        // 게임 종료 중
}
