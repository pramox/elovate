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
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from '../components/header/header.component';
import {HomeComponent} from '../components/home/home.component';
import {LoginRegisterComponent} from '../components/login-register/login-register.component';
import {ApiKeyComponent} from '../components/api-key/api-key.component';
import {GameDetailComponent} from '../components/game/game-detail/game-detail.component';
import {DialogComponent} from '../components/dialog/dialog.component';
import {
  GameApplicationCreateComponent
} from '../components/game-application/game-application-create/game-application-create.component';
import {
  GameApplicationOverviewComponent
} from '../components/game-application/game-application-overview/game-application-overview.component';
import {
  GameApplicationDetailViewComponent
} from '../components/game-application/game-application-detail-view/game-application-detail-view.component';
import {
  GameApplicationAdminOverviewComponent
} from '../components/game-application/game-application-admin-overview/game-application-admin-overview.component';
import {
  GameApplicationAdminDetailViewComponent
} from '../components/game-application/game-application-admin-detail-view/game-application-admin-detail-view.component';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {DropdownModule} from "primeng/dropdown";
import {ConfirmPopupModule} from "primeng/confirmpopup";
import {ButtonModule} from "primeng/button";
import {ToastModule} from "primeng/toast";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {AuthService} from "../services/users/auth.service";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {NgxPaginationModule} from "ngx-pagination";
import {GameAccessComponent} from "../components/game/game-access/game-access.component";
import {GameAccessValidateComponent} from "../components/game/game-access-validate/game-access-validate.component";
import {HighlightJsModule} from "ngx-highlight-js";
import {ToastService} from "../services/shared/toast.service";
import {AuthInterceptor} from "../config/interceptor"
import {ImageCropperModule} from "ngx-image-cropper";
import {UserDetailComponent} from "../components/users/detail/user-detail.component";
import {CarouselModule} from "primeng/carousel";
import {SidebarModule} from "primeng/sidebar";
import {ConfirmationService} from "primeng/api";
import {DeveloperDashboardComponent} from "../components/developer-dashboard/developer-dashboard.component";
import {FriendDashboardComponent} from "../components/friend-dashboard/friend-dashboard.component";
import {UserViewComponent} from "../components/users/user-view/user-view.component";
import {PrivacyPolicyComponent} from "../components/privacy-policy/privacy-policy.component";
import {ImprintComponent} from "../components/imprint/imprint.component";
import {StatisticsComponent} from "../components/statistics/statistics.component";
import {NgxChartsModule} from "@swimlane/ngx-charts";
import {LanguageService} from "../services/i18n/LanguageService";

@NgModule({
  declarations: [
    AppComponent,
    ApiKeyComponent,
    DialogComponent,
    GameApplicationAdminDetailViewComponent,
    GameApplicationAdminOverviewComponent,
    GameApplicationCreateComponent,
    GameApplicationDetailViewComponent,
    GameApplicationOverviewComponent,
    GameAccessComponent,
    GameAccessValidateComponent,
    GameDetailComponent,
    HeaderComponent,
    HomeComponent,
    LoginRegisterComponent,
    UserDetailComponent,
    StatisticsComponent,
    DeveloperDashboardComponent,
    FriendDashboardComponent,
    UserViewComponent,
    PrivacyPolicyComponent,
    ImprintComponent
    // add declarations here
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ToastModule,
    BrowserAnimationsModule,
    DropdownModule,
    ConfirmPopupModule,
    ButtonModule,
    ConfirmDialogModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    NgxPaginationModule,
    SidebarModule,
    HighlightJsModule,
    ImageCropperModule,
    CarouselModule,
    NgxChartsModule,
    ReactiveFormsModule,
    // add imports here
  ],
  providers: [
    AuthService,
    ToastService,
    ConfirmationService,
    LanguageService,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}
