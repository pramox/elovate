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
import {Role} from "./enums/role";
import {UserStatus} from "./enums/userStatus";

export interface ExtendedUserDto {

  id: number,
  email: string,
  nickName: string
  roles: Role[];
  status: UserStatus;
  countryCode: string;
  image: File | null,
  imageBase64: string
}
