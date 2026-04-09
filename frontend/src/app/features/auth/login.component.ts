import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginViewModel } from './login.viewmodel';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  providers: [LoginViewModel],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  protected readonly vm = inject(LoginViewModel);

  ngOnInit(): void {
    this.vm.init();
  }
}
