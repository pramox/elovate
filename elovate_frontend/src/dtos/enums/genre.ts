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
export enum Genre {

  MOBA = "MOBA",

  FIRST_PERSON_SHOOTER = "FIRST_PERSON_SHOOTER",

  FIGHTING = "FIGHTING",

  RACING = "RACING",

  REAL_TIME_STRATEGY = "REAL_TIME_STRATEGY",

  SPORTS = "SPORTS",

  THIRD_PERSON_SHOOTER = "THIRD_PERSON_SHOOTER",

  CARD_GAME = "CARD_GAME",

  OTHER = "OTHER"

}

export const Genre2LabelMapping: Record<Genre, string> = {
  [Genre.MOBA]: "elovate.genre.moba",
  [Genre.FIRST_PERSON_SHOOTER]: "elovate.genre.first-person-shooter",
  [Genre.FIGHTING]: "elovate.genre.fighting",
  [Genre.RACING]: "elovate.genre.racing",
  [Genre.REAL_TIME_STRATEGY]: "elovate.genre.real-time-strategy",
  [Genre.SPORTS]: "elovate.genre.sports",
  [Genre.THIRD_PERSON_SHOOTER]: "elovate.genre.third-person-shooter",
  [Genre.CARD_GAME]: "elovate.genre.card-game",
  [Genre.OTHER]: "elovate.genre.other"
};
