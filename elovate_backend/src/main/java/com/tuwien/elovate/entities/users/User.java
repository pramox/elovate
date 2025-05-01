package com.tuwien.elovate.entities.users;

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
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.gameapplication.comment.Comment;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(unique = true)
    @ToString.Include
    private String email;

    @Column(unique = true)
    @ToString.Include
    private String nickName;

    @Column
    private String password;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Set<Role> roles;

    @OneToMany(mappedBy = "developer")
    private Set<GameApplication> gameApplications;

    @OneToMany(mappedBy = "user")
    private Set<Comment> comments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @ToString.Include
    private UserStatus status;

    @Column(name = "country_code")
    @ToString.Include
    private String countryCode;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Game> connectedGames;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> friends;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> blocked;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> friendRequests;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return authorities;
    }

    public Set<Game> getConnectedGames() {
        if (connectedGames == null) {
            this.connectedGames = new HashSet<>();
        }
        return this.connectedGames;
    }

    public Set<Role> getRoles() {
        if (roles == null) {
            return Set.of();
        }
        return new HashSet<>(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isAdmin() {
        return getRoles().contains(Role.ADMIN);
    }

    public boolean isDeveloper() {
        return getRoles().contains(Role.GAME_DEVELOPER);
    }
}
