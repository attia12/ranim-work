// Module: Outdoor Campsite & Booking | Layer: Frontend Component (Smart)
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OutdoorCampsiteService } from '../../../services/outdoor-campsite.service';

@Component({
  selector: 'app-propose-outdoor',
  templateUrl: './propose-outdoor.component.html',
  styleUrl: './propose-outdoor.component.css'
})
export class ProposeOutdoorComponent {

  form!: FormGroup;
  submitting = false;
  error = '';
  success = false;

  difficulties = ['EASY', 'MODERATE', 'HARD'];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private outdoorService: OutdoorCampsiteService
  ) {
    this.form = this.fb.group({
      name:             ['', [Validators.required, Validators.minLength(3)]],
      description:      [''],
      country:          ['', Validators.required],
      city:             ['', Validators.required],
      latitude:         [''],
      longitude:        [''],
      pictures:         [''],
      naturalFeatures:  [''],
      accessDifficulty: ['']
    });
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.error = '';

    this.outdoorService.propose(this.form.value).subscribe({
      next: () => {
        this.submitting = false;
        this.success = true;
        setTimeout(() => this.router.navigate(['/my-proposals']), 2000);
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.error || 'Submission failed. Please try again.';
      }
    });
  }

  hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }
}
