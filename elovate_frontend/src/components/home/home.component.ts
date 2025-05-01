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
import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/users/auth.service";
import {GameDetailDto} from "../../dtos/Game/gameDetailDto";
import {GameService} from "../../services/game/game.service";
import {ToastService} from "../../services/shared/toast.service";
import {Router} from "@angular/router";
import {Genre} from "../../dtos/enums/genre";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  username: string | null;

  // THIS DUMMY DATA IS INTENDED - PLEASE DO NOT REMOVE
  // THIS IS TOO UGLY FOR YOU? PROGRAM AN EVENT LISTENER WHICH SUBSCRIBES TO THE GAME FETCHING
  // AND RELOADS THE IMAGES LAZILY OVER THE SHIMMER IMAGE - HAVE FUN :)!
  games: GameDetailDto[] = Array(5).fill(    {
      id: 0, name: "", playersPerTeam: 0, genre: Genre.OTHER, image: "shimmer", developerDetails: {
        id: 0,
        nickname: "",
        email: ""
      }})

  myGames: GameDetailDto[] = Array(5).fill(    {
    id: 0, name: "", playersPerTeam: 0, genre: Genre.OTHER, image: "shimmer", developerDetails: {
      id: 0,
      nickname: "",
      email: ""
    }})

  constructor(public authService: AuthService,
              private gameService: GameService,
              private toastService: ToastService,
              private router: Router) {
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.username = this.authService.getLoggedInUsername()
      this.getAllGames();
    }
  }

  private getAllGames() {
    this.gameService.getAllGames(true).subscribe({
      next: data => {
        this.myGames = data
        if (this.myGames.length < 5 && this.myGames.length > 0) {
          for (let i = this.myGames.length; i < 5; i++)
            this.myGames.push({
              id: 0, name: "", playersPerTeam: 0, genre: Genre.OTHER, image: "empty", developerDetails: {
                id: 0,
                nickname: "",
                email: ""
              }})
        }
        console.log(JSON.stringify(data))
      },
      error: err => {
        this.toastService.addError(err)
      }
    });

    this.gameService.getAllGames(false).subscribe({
      next: data => {
        this.games = data
        if (this.games.length < 5 && this.games.length > 0) {
          for (let i = this.games.length; i < 5; i++)
          this.games.push({
            id: 0, name: "", playersPerTeam: 0, genre: Genre.OTHER, image: "empty", developerDetails: {
              id: 0,
              nickname: "",
              email: ""
            }})
        }
        console.log(data.map)
      },
      error: err => {
        this.toastService.addError(err)
      }
    });
  }

  onCardClick(game: GameDetailDto) {
    let gameId = game.id;
    this.router.navigate(['/queue', gameId])
  }

    protected readonly Math = Math;
}
