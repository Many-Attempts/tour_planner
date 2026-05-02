import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TourMapComponent } from '../../../shared/tour-map.component';
import { NavbarComponent } from '../../../shared/navbar.component';
import { AuthImagePipe } from '../../../shared/auth-image.pipe';
import {
  LucideAngularModule, ArrowLeft, Edit, Trash2, MapPin, Clock,
  Calendar, Plus, Star, TrendingUp, Award, Cloud
} from 'lucide-angular';
import { TourDetailViewModel } from './tour-detail.viewmodel';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, NavbarComponent, AuthImagePipe, LucideAngularModule],
  providers: [TourDetailViewModel],
  templateUrl: './tour-detail.component.html',
  styleUrl: './tour-detail.component.scss'
})
export class TourDetailComponent implements OnInit {
  protected readonly vm = inject(TourDetailViewModel);

  readonly ArrowLeftIcon = ArrowLeft;
  readonly EditIcon = Edit;
  readonly Trash2Icon = Trash2;
  readonly MapPinIcon = MapPin;
  readonly ClockIcon = Clock;
  readonly CalendarIcon = Calendar;
  readonly PlusIcon = Plus;
  readonly StarIcon = Star;
  readonly TrendingUpIcon = TrendingUp;
  readonly AwardIcon = Award;
  readonly CloudIcon = Cloud;

  ngOnInit(): void {
    this.vm.init();
  }
}
