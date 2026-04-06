import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { OutdoorTripsComponent } from './outdoor-trips.component';

describe('OutdoorTripsComponent', () => {
  let component: OutdoorTripsComponent;
  let fixture: ComponentFixture<OutdoorTripsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OutdoorTripsComponent],
      imports: [HttpClientTestingModule, RouterTestingModule]
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
