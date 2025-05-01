package com.tuwien.elovate.services;

import com.tuwien.elovate.entities.queue.QueuePlayer;
import com.tuwien.elovate.services.queue.QueuePlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MatchmakingTest {

    @Autowired
    private QueuePlayerService queuePlayerService;

    private static final double MAX_TEAM_RATING_DISCREPANCY = 50.0;

    @Test
    void add4Players_withSimpleRatings_expectValidPartitioning() {
        List<QueuePlayer> queuePlayers = new ArrayList<>();
        QueuePlayer queuePlayer1 = new QueuePlayer(null, null, null, null, null, 1D);
        QueuePlayer queuePlayer2 = new QueuePlayer(null, null, null, null, null, 2D);
        QueuePlayer queuePlayer3 = new QueuePlayer(null, null, null, null, null, 3D);
        QueuePlayer queuePlayer4 = new QueuePlayer(null, null, null, null, null, 4D);

        queuePlayers.add(queuePlayer1);
        queuePlayers.add(queuePlayer2);
        queuePlayers.add(queuePlayer3);
        queuePlayers.add(queuePlayer4);

        Pair<List<QueuePlayer>, List<QueuePlayer>> result = queuePlayerService.findValidTeamPartitioning(queuePlayers, MAX_TEAM_RATING_DISCREPANCY);

        assertTrue(result.getFirst().contains(queuePlayer2));
        assertTrue(result.getFirst().contains(queuePlayer3));
        assertTrue(result.getSecond().contains(queuePlayer1));
        assertTrue(result.getSecond().contains(queuePlayer4));
        assertEquals(2, result.getFirst().size());
        assertEquals(2, result.getSecond().size());
    }

    @Test
    void add4Players_withHighRatings_expectValidPartitioning() {
        List<QueuePlayer> queuePlayers = new ArrayList<>();
        QueuePlayer queuePlayer1 = new QueuePlayer(null, null, null, null, null, 3200D);
        QueuePlayer queuePlayer2 = new QueuePlayer(null, null, null, null, null, 3300D);
        QueuePlayer queuePlayer3 = new QueuePlayer(null, null, null, null, null, 3100D);
        QueuePlayer queuePlayer4 = new QueuePlayer(null, null, null, null, null, 3400D);

        queuePlayers.add(queuePlayer1);
        queuePlayers.add(queuePlayer2);
        queuePlayers.add(queuePlayer3);
        queuePlayers.add(queuePlayer4);

        Pair<List<QueuePlayer>, List<QueuePlayer>> result = queuePlayerService.findValidTeamPartitioning(queuePlayers, MAX_TEAM_RATING_DISCREPANCY);

        assertTrue(result.getFirst().contains(queuePlayer1));
        assertTrue(result.getFirst().contains(queuePlayer2));
        assertTrue(result.getSecond().contains(queuePlayer3));
        assertTrue(result.getSecond().contains(queuePlayer4));
        assertEquals(2, result.getFirst().size());
        assertEquals(2, result.getSecond().size());
    }

    @Test
    void add4Players_withNonPossibleRatings_tryPartitioning_expectNull() {
        List<QueuePlayer> queuePlayers = new ArrayList<>();
        QueuePlayer queuePlayer1 = new QueuePlayer(null, null, null, null, null, 1D);
        QueuePlayer queuePlayer2 = new QueuePlayer(null, null, null, null, null, 2D);
        QueuePlayer queuePlayer3 = new QueuePlayer(null, null, null, null, null, 3D);
        QueuePlayer queuePlayer4 = new QueuePlayer(null, null, null, null, null, 110D);

        queuePlayers.add(queuePlayer1);
        queuePlayers.add(queuePlayer2);
        queuePlayers.add(queuePlayer3);
        queuePlayers.add(queuePlayer4);

        Pair<List<QueuePlayer>, List<QueuePlayer>> result = queuePlayerService.findValidTeamPartitioning(queuePlayers, MAX_TEAM_RATING_DISCREPANCY);

        assertNull(result);
    }

    @Test
    void addUnevenAmountOfPlayers_tryPartitioning_expectException() {
        List<QueuePlayer> queuePlayers = new ArrayList<>();
        QueuePlayer queuePlayer1 = new QueuePlayer(null, null, null, null, null, 1D);
        QueuePlayer queuePlayer2 = new QueuePlayer(null, null, null, null, null, 2D);
        QueuePlayer queuePlayer3 = new QueuePlayer(null, null, null, null, null, 3D);
        QueuePlayer queuePlayer4 = new QueuePlayer(null, null, null, null, null, 110D);
        QueuePlayer queuePlayer5 = new QueuePlayer(null, null, null, null, null, 110D);

        queuePlayers.add(queuePlayer1);
        queuePlayers.add(queuePlayer2);
        queuePlayers.add(queuePlayer3);
        queuePlayers.add(queuePlayer4);
        queuePlayers.add(queuePlayer5);

        assertThrows(IllegalStateException.class, () -> queuePlayerService.findValidTeamPartitioning(queuePlayers, MAX_TEAM_RATING_DISCREPANCY));
    }

    @Test
    void add10Players_withSimpleRatings_expectValidPartitioning() {
        List<QueuePlayer> queuePlayers = new ArrayList<>();
        QueuePlayer queuePlayer1 = new QueuePlayer(1L, null, null, null, null, 1D);
        QueuePlayer queuePlayer2 = new QueuePlayer(2L, null, null, null, null, 1D);
        QueuePlayer queuePlayer3 = new QueuePlayer(3L, null, null, null, null, 1D);
        QueuePlayer queuePlayer4 = new QueuePlayer(4L, null, null, null, null, 1D);
        QueuePlayer queuePlayer5 = new QueuePlayer(5L, null, null, null, null, 1D);
        QueuePlayer queuePlayer6 = new QueuePlayer(6L, null, null, null, null, 1D);
        QueuePlayer queuePlayer7 = new QueuePlayer(7L, null, null, null, null, 1D);
        QueuePlayer queuePlayer8 = new QueuePlayer(8L, null, null, null, null, 1D);
        QueuePlayer queuePlayer9 = new QueuePlayer(9L, null, null, null, null, 10D);
        QueuePlayer queuePlayer10 = new QueuePlayer(10L, null, null, null, null, 10D);

        queuePlayers.add(queuePlayer1);
        queuePlayers.add(queuePlayer2);
        queuePlayers.add(queuePlayer3);
        queuePlayers.add(queuePlayer4);
        queuePlayers.add(queuePlayer5);
        queuePlayers.add(queuePlayer6);
        queuePlayers.add(queuePlayer7);
        queuePlayers.add(queuePlayer8);
        queuePlayers.add(queuePlayer9);
        queuePlayers.add(queuePlayer10);

        Pair<List<QueuePlayer>, List<QueuePlayer>> result = queuePlayerService.findValidTeamPartitioning(queuePlayers, MAX_TEAM_RATING_DISCREPANCY);

        assertTrue(result.getFirst().contains(queuePlayer10));
        assertTrue(result.getSecond().contains(queuePlayer9));
        assertEquals(5, result.getFirst().size());
        assertEquals(5, result.getSecond().size());
    }

}
