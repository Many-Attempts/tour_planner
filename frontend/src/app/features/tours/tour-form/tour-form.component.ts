import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { TourMapComponent } from '../../../shared/tour-map.component';
import { NavbarComponent } from '../../../shared/navbar.component';
import { LucideAngularModule, ArrowLeft, Save, MapPin } from 'lucide-angular';
import { TourFormViewModel } from './tour-form.viewmodel';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, NavbarComponent, LucideAngularModule],
  providers: [TourFormViewModel],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.scss'
})
export class TourFormComponent implements OnInit {
  protected readonly vm = inject(TourFormViewModel);

  @ViewChild('tourForm') tourForm!: NgForm;

  readonly ArrowLeftIcon = ArrowLeft;
  readonly SaveIcon = Save;
  readonly MapPinIcon = MapPin;

  ngOnInit(): void {
    this.vm.init();
  }

  onSubmit(): void {
    if (this.tourForm.invalid) {
      Object.values(this.tourForm.controls).forEach(c => c.markAsTouched());
    }
    this.vm.submit(this.tourForm.valid ?? false);
  }
}
