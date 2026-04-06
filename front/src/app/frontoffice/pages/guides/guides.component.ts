import { Component, OnInit } from '@angular/core';
import { GuideService } from '../../../services/Guide.service';
import { Guide } from '../../../models/Guide.model';

@Component({
  selector: 'app-guides',
  templateUrl: './guides.component.html',
  styleUrl: './guides.component.css'
})
export class GuidesComponent implements OnInit {
  guides: Guide[] = [];
  showForm = false;
  isEditing = false;
  currentGuideId: number | null = null;

  formData: Guide = {
    guideId: 0,
    fullName: '',
    experienceYears: 0
  };

  constructor(private guideService: GuideService) { }

  ngOnInit() {
    this.loadGuides();
  }

  loadGuides() {
    this.guideService.getAll().subscribe({
      next: (data) => {
        this.guides = data;
      },
      error: (err) => {
        console.error('Error loading guides:', err);
      }
    });
  }

  openForm() {
    this.showForm = true;
    this.isEditing = false;
    this.formData = {
      guideId: 0,
      fullName: '',
      experienceYears: 0
    };
  }

  editGuide(guide: Guide) {
    this.isEditing = true;
    this.showForm = true;
    this.currentGuideId = guide.guideId;
    this.formData = { ...guide };
  }

  saveGuide() {
    if (!this.formData.fullName || this.formData.experienceYears < 0) {
      alert('Please fill in all fields correctly.');
      return;
    }

    if (this.isEditing && this.currentGuideId) {
      this.guideService.update(this.currentGuideId, this.formData).subscribe({
        next: () => {
          alert('Guide updated successfully!');
          this.loadGuides();
          this.closeForm();
        },
        error: (err) => {
          console.error('Error updating guide:', err);
          alert('Failed to update guide.');
        }
      });
    } else {
      this.guideService.create(this.formData).subscribe({
        next: () => {
          alert('Guide created successfully!');
          this.loadGuides();
          this.closeForm();
        },
        error: (err) => {
          console.error('Error creating guide:', err);
          alert('Failed to create guide.');
        }
      });
    }
  }

  deleteGuide(guideId: number) {
    if (confirm('Are you sure you want to delete this guide?')) {
      this.guideService.delete(guideId).subscribe({
        next: () => {
          alert('Guide deleted successfully!');
          this.loadGuides();
        },
        error: (err) => {
          console.error('Error deleting guide:', err);
          alert('Failed to delete guide.');
        }
      });
    }
  }

  closeForm() {
    this.showForm = false;
    this.isEditing = false;
    this.currentGuideId = null;
  }
}
