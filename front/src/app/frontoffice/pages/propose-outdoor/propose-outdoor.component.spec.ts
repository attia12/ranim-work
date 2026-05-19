import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ProposeOutdoorComponent } from './propose-outdoor.component';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';

const mockResponse = { id: 1, name: 'Wild Camp', status: 'PENDING' };

describe('ProposeOutdoorComponent', () => {
  let component: ProposeOutdoorComponent;
  let fixture: ComponentFixture<ProposeOutdoorComponent>;
  let outdoorService: jasmine.SpyObj<OutdoorCampsiteService>;

  beforeEach(async () => {
    outdoorService = jasmine.createSpyObj('OutdoorCampsiteService', ['propose']);

    await TestBed.configureTestingModule({
      declarations: [ProposeOutdoorComponent],
      imports: [RouterTestingModule, ReactiveFormsModule],
      providers: [
        { provide: OutdoorCampsiteService, useValue: outdoorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProposeOutdoorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form initializes with required fields', () => {
    expect(component.form.contains('name')).toBeTrue();
    expect(component.form.contains('country')).toBeTrue();
    expect(component.form.contains('city')).toBeTrue();
  });

  it('difficulties list contains EASY, MODERATE, HARD', () => {
    expect(component.difficulties).toEqual(['EASY', 'MODERATE', 'HARD']);
  });

  it('form is invalid when required fields missing', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('form is valid with required fields filled', () => {
    component.form.patchValue({ name: 'Wild Camp', country: 'Tunisia', city: 'Beja' });
    expect(component.form.valid).toBeTrue();
  });

  it('name field requires minimum 3 characters', () => {
    component.form.get('name')!.setValue('AB');
    expect(component.form.get('name')!.valid).toBeFalse();
    component.form.get('name')!.setValue('ABC');
    expect(component.form.get('name')!.valid).toBeTrue();
  });

  it('hasError() returns true for invalid touched field', () => {
    component.form.get('name')!.markAsTouched();
    expect(component.hasError('name')).toBeTrue();
  });

  it('hasError() returns false for valid field', () => {
    component.form.get('name')!.setValue('Wild Camp');
    component.form.get('name')!.markAsTouched();
    expect(component.hasError('name')).toBeFalse();
  });

  it('submit() marks all touched when form invalid', () => {
    spyOn(component.form, 'markAllAsTouched');
    component.submit();
    expect(component.form.markAllAsTouched).toHaveBeenCalled();
    expect(outdoorService.propose).not.toHaveBeenCalled();
  });

  it('submit() calls outdoorService.propose() with form values', () => {
    outdoorService.propose.and.returnValue(of(mockResponse as any));
    component.form.patchValue({ name: 'Wild Camp', country: 'Tunisia', city: 'Beja' });
    component.submit();
    expect(outdoorService.propose).toHaveBeenCalledWith(jasmine.objectContaining({
      name: 'Wild Camp', country: 'Tunisia', city: 'Beja'
    }));
  });

  it('submit() sets success = true on success', () => {
    outdoorService.propose.and.returnValue(of(mockResponse as any));
    component.form.patchValue({ name: 'Wild Camp', country: 'Tunisia', city: 'Beja' });
    component.submit();
    expect(component.success).toBeTrue();
    expect(component.submitting).toBeFalse();
  });

  it('submit() sets error on failure', () => {
    outdoorService.propose.and.returnValue(
      throwError(() => ({ error: { error: 'Validation failed' } }))
    );
    component.form.patchValue({ name: 'Wild Camp', country: 'Tunisia', city: 'Beja' });
    component.submit();
    expect(component.error).toBe('Validation failed');
    expect(component.submitting).toBeFalse();
  });
});
