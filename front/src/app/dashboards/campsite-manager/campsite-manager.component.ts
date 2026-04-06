import { Component, OnInit } from '@angular/core';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-campsite-manager',
  templateUrl: './campsite-manager.component.html',
  styleUrls: ['./campsite-manager.component.css']
})
export class CampsiteManagerComponent implements OnInit {
  currentUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(u => this.currentUser = u);
  }
}
