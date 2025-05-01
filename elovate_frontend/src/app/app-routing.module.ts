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
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginRegisterComponent} from "../components/login-register/login-register.component";
import {AuthGuardService} from "../services/guards/auth-guard.service";
import {HomeComponent} from "../components/home/home.component";
import {
  GameApplicationCreateComponent
} from "../components/game-application/game-application-create/game-application-create.component";
import {
  GameApplicationDetailViewComponent
} from "../components/game-application/game-application-detail-view/game-application-detail-view.component";
import {
  GameApplicationOverviewComponent
} from "../components/game-application/game-application-overview/game-application-overview.component";
import {AuthGuardAdminService} from "../services/guards/auth-guard-admin.service";
import {
  GameApplicationAdminOverviewComponent
} from "../components/game-application/game-application-admin-overview/game-application-admin-overview.component";
import {
  GameApplicationAdminDetailViewComponent
} from "../components/game-application/game-application-admin-detail-view/game-application-admin-detail-view.component";
import {GameDetailComponent} from "../components/game/game-detail/game-detail.component";
import {GameAccessComponent} from "../components/game/game-access/game-access.component";
import {GameAccessValidateComponent} from "../components/game/game-access-validate/game-access-validate.component";
import {LobbyComponent} from "../components/lobby/lobby.component";
import {UserDetailComponent} from "../components/users/detail/user-detail.component";
import {PasswordResetComponent} from "../components/password-reset/password-reset.component";
import {AuthGuardUserService} from "../services/guards/auth-guard-user.service";
import {InQueueViewComponent} from "../components/in-queue-view/in-queue-view.component";
import {
  Glicko2RatingParametersComponent
} from "../components/rating-parameters/glicko2/glicko2-rating-parameters.component";
import {DeveloperDashboardComponent} from "../components/developer-dashboard/developer-dashboard.component";
import {AdminDashboardComponent} from "../components/admin-dashboard/admin-dashboard.component";
import {FriendDashboardComponent} from "../components/friend-dashboard/friend-dashboard.component";
import {UserViewComponent} from "../components/users/user-view/user-view.component";
import {PrivacyPolicyComponent} from "../components/privacy-policy/privacy-policy.component";
import {ImprintComponent} from "../components/imprint/imprint.component";
import {StatisticsComponent} from "../components/statistics/statistics.component";
import {LanguageService} from "../services/i18n/LanguageService";
import {DeveloperGameListComponent} from "../components/game/developer-game-list/developer-game-list.component";
import {PostGameViewComponent} from "../components/post-game-view/post-game-view.component";

const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [LanguageService]},
  {path: 'login', component: LoginRegisterComponent, data: {isLogin: true}, canActivate: [LanguageService]},
  {path: 'register', component: LoginRegisterComponent, data: {isLogin: false}, canActivate: [LanguageService]},
  {path: 'imprint', component: ImprintComponent, data: {isLogin: false}, canActivate: [LanguageService]},
  {path: 'privacy', component: PrivacyPolicyComponent, data: {isLogin: false}, canActivate: [LanguageService]},

  {
    path: 'game-application', canActivate: [AuthGuardService, LanguageService], children: [
      {path: '', component: GameApplicationOverviewComponent},
      {path: 'create', component: GameApplicationCreateComponent},
      {path: ':id', component: GameApplicationDetailViewComponent},
      {path: ':id/details', component: GameApplicationDetailViewComponent},
    ]
  },
  {
    path: 'game', canActivate: [AuthGuardService, LanguageService], children: [
      {path: ':id/details', component: GameDetailComponent},
      {path: ':id/parameters', component: Glicko2RatingParametersComponent, canActivate: [AuthGuardUserService]},
      {path: 'access', component: GameAccessComponent},
      {path: ':id/access', component: GameAccessValidateComponent},
    ]
  },
  {
    path: 'statistics', canActivate: [AuthGuardService, LanguageService], children: [
      {path: '', component: StatisticsComponent},
      {path: ':gameId', component: StatisticsComponent}
    ]
  },
  {
    path: 'developer', canActivate: [AuthGuardService, LanguageService], children: [
      {path: 'games', component: DeveloperGameListComponent},
      {path: '', component: DeveloperDashboardComponent}
    ]
  },
  {
    path: 'admin', canActivate: [AuthGuardAdminService, LanguageService], children: [
      {path: '', component: AdminDashboardComponent},
      {path: 'game-application', component: GameApplicationAdminOverviewComponent},
      {path: 'game-application/:id', component: GameApplicationAdminDetailViewComponent},
      {path: 'users/detail/:id', component: UserDetailComponent, data: {isAdminView: true}},
    ]
  },
  {path: 'lobby/:id', component: LobbyComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: 'profile', component: UserDetailComponent, canActivate: [AuthGuardService, LanguageService], data: {isAdminView: false}},
  {path: 'reset-password', component: PasswordResetComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: 'queue/:id', component: InQueueViewComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: 'friends', component: FriendDashboardComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: 'users/:id', component: UserViewComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: 'post-game/:id', component: PostGameViewComponent, canActivate: [AuthGuardService, LanguageService]},
  {path: '**', redirectTo: ''},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {paramsInheritanceStrategy: "always"})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
