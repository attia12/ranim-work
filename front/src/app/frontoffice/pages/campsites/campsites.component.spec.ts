import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { CampsitesComponent } from './campsites.component';

describe('CampsitesComponent', () => {
  let component: CampsitesComponent;
  let fixture: ComponentFixture<CampsitesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampsitesComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(CampsitesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});