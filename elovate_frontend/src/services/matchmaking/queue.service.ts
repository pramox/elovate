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
import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';
import {AuthService} from '../users/auth.service';
import {Observable, Subject} from 'rxjs';
import {CustomWebSocketMessageDto} from "../../dtos/queue/customWebSocketMessageDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class QueueService {
  private stompClient: any;

  private publicMessages: Subject<CustomWebSocketMessageDto> | null = null;
  private privateMessages: Subject<CustomWebSocketMessageDto> | null = null;

  private readonly queueingRegex = /^QUEUED:\s+(\d+)$/;
  private readonly matchRegex = /^MATCH STARTING:\s+([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})$/;

  private static readonly QUEUE_JOIN_MESSAGE = "Queue-Requested";

  private apiUrl = environment.backendUrl + '/api/v1/queue/';

  constructor(private authService: AuthService) {

  }

  disconnect(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      if (this.stompClient?.connected) {
        try {

          this.publicMessages = null;
          this.privateMessages = null;

          // Now disconnect the STOMP client
          this.stompClient.disconnect(() => {
            resolve(true); // Resolve after successful disconnection
          }, {'Authorization': 'Bearer ' + this.authService.fetchToken()});
        } catch (error) {
          reject(error); // Reject with error if there's an exception
        }
      } else {
        resolve(true); // Resolve with true as there's no connection to disconnect
      }
    });
  }

  connect(): Promise<string> {
    return new Promise((resolve, reject) => {
      const socket = new SockJS(this.apiUrl);
      this.stompClient = Stomp.over(socket);

      this.stompClient.debug = () => {
      }; // The debug is kind of spammy - if you want debug remove this line

      this.publicMessages = new Subject<CustomWebSocketMessageDto>();
      this.privateMessages = new Subject<CustomWebSocketMessageDto>();

      this.stompClient.connect(
        {'Authorization': 'Bearer ' + this.authService.fetchToken()},
        (frame: string) => {
          this.subscribeToPrivateMessages();
          resolve(frame);
        },
        (error: string) => {
          reject(error);
        }
      );
    });
  }

  private subscribeToPublicMessages(gameId: number): void {
    this.stompClient.subscribe('/topic/message/' + gameId, (message: any) => {
      const messageObj: CustomWebSocketMessageDto = JSON.parse(message.body);
      this.getPublicMessages().next(messageObj);
    }, {'Authorization': 'Bearer ' + this.authService.fetchToken()});
  }

  private subscribeToPrivateMessages(): void {
    this.stompClient.subscribe('/user/queue/messages', (message: any) => {
      const privateMessageObj: CustomWebSocketMessageDto = JSON.parse(message.body);
      this.getPrivateMessages().next(privateMessageObj);
    }, {'Authorization': 'Bearer ' + this.authService.fetchToken()});
  }

  sendQueueJoinMessageForGame(gameId: number): Observable<void> {
    return new Observable<void>((observer) => {
      if (this.stompClient?.connected) {
        try {
          this.subscribeToPublicMessages(gameId);
          this.stompClient.publish({
            destination: '/api/v1/app/send/message',
            body: JSON.stringify({message: QueueService.QUEUE_JOIN_MESSAGE, gameId: gameId}),
            headers: {'Authorization': 'Bearer ' + this.authService.fetchToken()}
          });
          observer.complete(); // Complete the observable if message is sent successfully
        } catch (error) {
          observer.error(error); // Emit error if there's an exception
        }
      } else {
        observer.error(new Error('STOMP client is not connected.'));
      }
    });
  }

  private getPublicMessages(): Subject<CustomWebSocketMessageDto> {
    if (!this.publicMessages) {
      throw Error("ERROR: publicMessages is null");
    }
    return this.publicMessages;
  }

  private getPrivateMessages(): Subject<CustomWebSocketMessageDto> {
    if (!this.privateMessages) {
      throw Error("ERROR: privateMessages is null");
    }
    return this.privateMessages;
  }

  public getPublicMessagesAsObservable(): Observable<CustomWebSocketMessageDto> {
    if (!this.publicMessages) {
      throw Error("ERROR: publicMessages is null");
    }
    return this.publicMessages.asObservable();
  }

  public getPrivateMessagesAsObservable(): Observable<CustomWebSocketMessageDto> {
    if (!this.privateMessages) {
      throw Error("ERROR: privateMessages is null");
    }
    return this.privateMessages.asObservable();
  }

  isMatchStartingMessage(messageDto: CustomWebSocketMessageDto): boolean {
    return this.matchRegex.test(messageDto.message);
  }

  getMatchLobbyId(messageDto: CustomWebSocketMessageDto): string {
    return messageDto.message.substring(16);
  }

  isQueueingAmountMessage(messageDto: CustomWebSocketMessageDto): boolean {
    return this.queueingRegex.test(messageDto.message);
  }

  getQueueingAmount(publicMessage: CustomWebSocketMessageDto): number {
    return this.parseMessageForNumber(publicMessage.message, this.queueingRegex);
  }

  parseMessageForNumber(message: string, regexPattern: RegExp): number {
    const regex = new RegExp(regexPattern);
    const match = RegExp(regex).exec(message);
    if (match) {
      return parseInt(match[1], 10);
    }
    throw new Error("Could not parse."); // only happens if the isQueueingAmountMessage or isMatchStartingMessage are not called
  }
}
