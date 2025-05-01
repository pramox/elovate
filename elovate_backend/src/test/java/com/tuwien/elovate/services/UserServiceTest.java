package com.tuwien.elovate.services;

import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.enums.UserStatus;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.mapper.UserMapper;
import com.tuwien.elovate.services.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private User mockDeveloper, mockPlayer, mockUser, mockAdmin, mockAdmin2;

    @Autowired
    private DataClearer dataClearer;

    @BeforeEach
    void setUp() {
        dataClearer.clear();

        mockDeveloper = User.builder()
                .id(1L)
                .nickName("developer")
                .email("developer@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(Set.of(Role.USER, Role.GAME_DEVELOPER))
                .password(getEncodedPassword())
                .build();

        mockPlayer = User.builder()
                .id(2L)
                .nickName("player")
                .email("player@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(Set.of(Role.USER))
                .password(getEncodedPassword())
                .build();

        mockUser = User.builder()
                .id(3L)
                .nickName("user")
                .email("user@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(Set.of(Role.USER))
                .password(getEncodedPassword())
                .build();

        mockAdmin = User.builder()
                .id(4L)
                .nickName("admin")
                .email("admin@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .password(getEncodedPassword())
                .build();

        mockAdmin2 = User.builder()
                .id(5L)
                .nickName("admin2")
                .email("admin2@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .password(getEncodedPassword())
                .build();

        userRepository.saveAll(List.of(mockDeveloper, mockPlayer, mockUser, mockAdmin, mockAdmin2));
    }

    String getEncodedPassword() {
        return passwordEncoder.encode("1234");
    }

    @Test
    void user_edits_own_profile_with_no_change_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        ExtendedUserResponseDto dtoAfterUpdate = userService.updateUserById(mockUser.getId(), dtoBeforeUpdate, mockUser);

        if (!dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }
    }

    @Test
    void user_edits_different_profile_throws_exception() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockDeveloper);
        Long id = mockDeveloper.getId();
        assertThrows(ValidationException.class, () -> userService.updateUserById(id, dtoBeforeUpdate, mockUser));
    }

    @Test
    void admin_edits_users_profile_with_no_change_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        ExtendedUserResponseDto dtoAfterUpdate = userService.updateUserById(mockUser.getId(), dtoBeforeUpdate, mockAdmin);

        if (!dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }
    }

    @Test
    void player_edits_own_profile_with_country_code_change_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockPlayer);
        dtoBeforeUpdate.setCountryCode("DE");
        ExtendedUserResponseDto dtoAfterUpdate = userService.updateUserById(mockPlayer.getId(), dtoBeforeUpdate, mockPlayer);

        if (!dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }
    }

    @Test
    void admin_edits_player_profile_with_country_code_change_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockPlayer);
        dtoBeforeUpdate.setCountryCode("DE");
        ExtendedUserResponseDto dtoAfterUpdate = userService.updateUserById(mockPlayer.getId(), dtoBeforeUpdate, mockAdmin);

        if (!dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }
    }

    @Test
    void admin_promotes_user_to_admin_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        ExtendedUserResponseDto dtoAfterUpdate = userService.promoteUserToAdmin(mockUser.getId());

        if (dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }

        if (!dtoAfterUpdate.getRoles().contains(Role.ADMIN)) {
            fail();
        }
    }

    @Test
    void admin_demotes_admin_to_user_leads_to_no_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockAdmin);
        ExtendedUserResponseDto dtoAfterUpdate = userService.demoteAdminToUser(mockAdmin.getId(), mockAdmin2);

        if (dtoBeforeUpdate.equals(dtoAfterUpdate)) {
            fail();
        }

        if (dtoAfterUpdate.getRoles().contains(Role.ADMIN)) {
            fail();
        }
    }

    @Test
    void admin_demotes_himself_leads_to_error() {
        Long id = mockAdmin.getId();
        assertThrows(ValidationException.class, () -> userService.demoteAdminToUser(id, mockAdmin));
    }

    @Test
    void user_tries_to_make_himself_admin_leads_to_error() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        dtoBeforeUpdate.setRoles(Set.of(Role.USER, Role.ADMIN));
        Long id = mockUser.getId();
        assertThrows(ValidationException.class, () -> userService.updateUserById(id, dtoBeforeUpdate, mockUser));
    }

    @Test
    void get_user_by_id_works() {
        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(mockUser);
        UserResponseDto userResponseDto1 = userService.getUserById(mockUser.getId());

        if (!userResponseDto.equals(userResponseDto1)) {
            fail();
        }
    }

    @Test
    void get_user_by_non_existant_id_throws() {
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(10000L));
    }

    @Test
    void get_extended_user_by_id_works() {
        ExtendedUserResponseDto userResponseDto = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        ExtendedUserResponseDto userResponseDto1 = userService.getExtendedUserById(mockUser.getId(), mockAdmin);

        if (!userResponseDto.equals(userResponseDto1)) {
            fail();
        }
    }

    @Test
    void get_extended_user_by_non_existant_id_throws() {
        assertThrows(EntityNotFoundException.class, () -> userService.getExtendedUserById(10000L, mockAdmin));
    }

    @Test
    void user_cant_change_nickname_to_used_nickname() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        dtoBeforeUpdate.setNickName(mockAdmin.getNickName());
        Long id = mockUser.getId();
        assertThrows(ValidationException.class, () -> userService.updateUserById(id, dtoBeforeUpdate, mockUser));
    }

    @Test
    void user_cant_change_email_to_used_email() {
        ExtendedUserResponseDto dtoBeforeUpdate = userMapper.mapUserToExtendedUserResponseDto(mockUser);
        dtoBeforeUpdate.setEmail(mockAdmin.getEmail());
        Long id = mockUser.getId();
        assertThrows(ValidationException.class, () -> userService.updateUserById(id, dtoBeforeUpdate, mockUser));
    }
}
