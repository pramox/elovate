package com.tuwien.elovate.entities.game;

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

import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "game_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
@Setter
@Getter
@Builder
public class GameAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private String uuid;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;

    @OneToOne(fetch = FetchType.EAGER)
    private Rating rating;

    @CreationTimestamp
    private Instant createdOn;

    private boolean isUnlocked;
}
