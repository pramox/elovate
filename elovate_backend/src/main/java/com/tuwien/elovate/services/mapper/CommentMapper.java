package com.tuwien.elovate.services.mapper;

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

import com.tuwien.elovate.dtos.gameapplication.comment.CommentResponseDto;
import com.tuwien.elovate.entities.gameapplication.comment.Comment;
import com.tuwien.elovate.services.users.UserService;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserService.class)
public interface CommentMapper {

    @Named("toCommentResponseDto")
    @Mapping(target = "nickName", source = "comment.user.nickName")
    @Mapping(target = "content", source = "comment.content")
    @Mapping(target = "timestamp", source = "comment.timestamp")
    CommentResponseDto commentToGameApplicationCommentDTO(Comment comment);

    @IterableMapping(qualifiedByName = "toCommentResponseDto")
    List<CommentResponseDto> commentsToGameApplicationCommentsResponseDTO(List<Comment> comments);
}
