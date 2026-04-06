import { Component, OnInit } from '@angular/core';
import { AssignmentService } from '../../../services/Assignment.service';
import { Assignment } from '../../../models/Assignment.model';

@Component({
  selector: 'app-assignments',
  templateUrl: './assignments.component.html',
  styleUrl: './assignments.component.css'
})
export class AssignmentsComponent implements OnInit {
  assignments: Assignment[] = [];
  showForm = false;
  isEditing = false;
  currentAssignmentId: number | null = null;

  formData: Assignment = {
    assignmentId: 0,
    eventId: 0,
    eventTitle: '',
    guideId: 0,
    guideName: '',
    roleDescription: ''
  };

  constructor(private assignmentService: AssignmentService) { }

  ngOnInit() {
    this.loadAssignments();
  }

  loadAssignments() {
    this.assignmentService.getAll().subscribe({
      next: (data) => {
        this.assignments = data;
      },
      error: (err) => {
        console.error('Error loading assignments:', err);
      }
    });
  }

  openForm() {
    this.showForm = true;
    this.isEditing = false;
    this.formData = {
      assignmentId: 0,
      eventId: 0,
      eventTitle: '',
      guideId: 0,
      guideName: '',
      roleDescription: ''
    };
  }

  editAssignment(assignment: Assignment) {
    this.isEditing = true;
    this.showForm = true;
    this.currentAssignmentId = assignment.assignmentId;
    this.formData = { ...assignment };
  }

  saveAssignment() {
    if (!this.formData.eventTitle || !this.formData.guideName || !this.formData.roleDescription) {
      alert('Please fill in all required fields.');
      return;
    }

    if (this.isEditing && this.currentAssignmentId) {
      this.assignmentService.update(this.currentAssignmentId, this.formData).subscribe({
        next: () => {
          alert('Assignment updated successfully!');
          this.loadAssignments();
          this.closeForm();
        },
        error: (err) => {
          console.error('Error updating assignment:', err);
          alert('Failed to update assignment.');
        }
      });
    } else {
      this.assignmentService.create(this.formData).subscribe({
        next: () => {
          alert('Assignment created successfully!');
          this.loadAssignments();
          this.closeForm();
        },
        error: (err) => {
          console.error('Error creating assignment:', err);
          alert('Failed to create assignment.');
        }
      });
    }
  }

  deleteAssignment(assignmentId: number) {
    if (confirm('Are you sure you want to delete this assignment?')) {
      this.assignmentService.delete(assignmentId).subscribe({
        next: () => {
          alert('Assignment deleted successfully!');
          this.loadAssignments();
        },
        error: (err) => {
          console.error('Error deleting assignment:', err);
          alert('Failed to delete assignment.');
        }
      });
    }
  }

  closeForm() {
    this.showForm = false;
    this.isEditing = false;
    this.currentAssignmentId = null;
  }
}
