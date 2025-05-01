import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendDashboardComponent } from './friend-dashboard.component';

describe('FriendDashboardComponent', () => {
  let component: FriendDashboardComponent;
  let fixture: ComponentFixture<FriendDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendDashboardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(FriendDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
