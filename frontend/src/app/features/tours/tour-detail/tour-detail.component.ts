import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TourMapComponent } from '../../../shared/tour-map.component';
import { AuthImagePipe } from '../../../shared/auth-image.pipe';
import {
  LucideAngularModule, ArrowLeft, Edit, Trash2, MapPin, Clock,
  Calendar, Plus, Star, TrendingUp, Award
} from 'lucide-angular';
import { TourDetailViewModel } from './tour-detail.viewmodel';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, AuthImagePipe, LucideAngularModule],
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

  ngOnInit(): void {
    this.vm.init();
  }
}
