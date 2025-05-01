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
import {Component} from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-developer-cockpit',
  templateUrl: './developer-dashboard.component.html',
  styleUrl: './developer-dashboard.component.scss'
})
export class DeveloperDashboardComponent {

  constructor(private router: Router,) {
  }

  goToGameApplications(): void {
    this.router.navigate(['/game-application']);
  }

  goToCreateGameApplication(): void {
    this.router.navigate(['/game-application/create']);
  }
}
