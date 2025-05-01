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
export enum Role {
  ADMIN = "ADMIN",
  USER = "USER",
  GAME_DEVELOPER = "GAME_DEVELOPER"
}

export const Role2LabelMapping: Record<Role, string> = {
  [Role.ADMIN]: "Admin",
  [Role.USER]: "User",
  [Role.GAME_DEVELOPER]: "Developer"
};
