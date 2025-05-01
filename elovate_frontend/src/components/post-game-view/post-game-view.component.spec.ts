import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostGameViewComponent } from './post-game-view.component';

describe('PostGameViewComponent', () => {
  let component: PostGameViewComponent;
  let fixture: ComponentFixture<PostGameViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostGameViewComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PostGameViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
