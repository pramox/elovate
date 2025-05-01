package com.tuwien.elovate.entities.gameapplication;

/*-
 * #%L
 * ELOvate
 * %%
 * Copyright (C) 2024 ELOvate GmbH.
 * %%
 * Copyright (C) 2024 ELOvate GmbH. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * #L%
 */

import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.comment.Comment;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Genre;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "game_application")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class GameApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    @Column(name = "name")
    private String name;

    @ToString.Include
    @Column(name = "draw_possible")
    private boolean drawPossible;

    @Column(name = "players_per_team")
    @ToString.Include
    private int playersPerTeam;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    @Column(name = "genre")
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    @Column(name = "game_application_status")
    private GameApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User developer;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(cascade = CascadeType.MERGE)
    private Game game;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "gameApplication")
    private List<Comment> comments;
}
