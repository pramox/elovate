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
export enum GameApplicationStatus {
  ACCEPTED = 'ACCEPTED',
  PENDING = 'PENDING',
  REJECTED = 'REJECTED'
}

export const GameApplicationStatus2LabelMapping: Record<GameApplicationStatus, string> = {
  [GameApplicationStatus.ACCEPTED]: "Accepted",
  [GameApplicationStatus.PENDING]: "Pending",
  [GameApplicationStatus.REJECTED]: "Rejected"
};
