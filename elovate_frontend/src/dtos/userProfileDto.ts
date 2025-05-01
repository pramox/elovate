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
import {UserResponseDto} from "./userResponseDto";

export interface UserProfileDto extends UserResponseDto {

  friend: boolean;

  requested: boolean;

  requesting: boolean;

  self: boolean;

  blocked: boolean;

}
