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
import {GameApplicationResponseDto} from "./gameApplicationResponseDto";
import {CommentResponseDto} from "./Comment/commentResponseDto";

export interface GameApplicationResponseDetailDto extends GameApplicationResponseDto {
  comments: Array<CommentResponseDto>
}
