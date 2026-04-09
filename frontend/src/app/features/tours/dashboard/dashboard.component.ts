import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../../shared/navbar.component';
import { TourMapComponent } from '../../../shared/tour-map.component';
import { LucideAngularModule, Search, Plus, Upload, Download, MapPin, Clock } from 'lucide-angular';
import { DashboardViewModel } from './dashboard.viewmodel';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, TourMapComponent, LucideAngularModule],
  providers: [DashboardViewModel],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  protected readonly vm = inject(DashboardViewModel);

  readonly SearchIcon = Search;
  readonly PlusIcon = Plus;
  readonly UploadIcon = Upload;
  readonly DownloadIcon = Download;
  readonly MapPinIcon = MapPin;
  readonly ClockIcon = Clock;

  ngOnInit(): void {
    this.vm.init();
  }
}
