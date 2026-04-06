import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OutdoorTripsComponent } from './outdoor-trips.component';

describe('OutdoorTripsComponent', () => {
  let component: OutdoorTripsComponent;
  let fixture: ComponentFixture<OutdoorTripsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OutdoorTripsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OutdoorTripsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
