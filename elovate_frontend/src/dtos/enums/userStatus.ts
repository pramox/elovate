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
export enum UserStatus {
  NORMAL = "NORMAL",
  PREMIUM = "PREMIUM",
  BANNED = "BANNED"
}

export const UserStatus2LabelMapping: Record<UserStatus, string> = {
  [UserStatus.NORMAL]: "Normal",
  [UserStatus.PREMIUM]: "Premium",
  [UserStatus.BANNED]: "Banned"
};
