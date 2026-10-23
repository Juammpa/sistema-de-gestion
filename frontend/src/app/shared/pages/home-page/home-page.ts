import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Logo } from '../../components/logo/logo';

@Component({
  selector: 'app-home-page',
  imports: [RouterLink, Logo],
  templateUrl: './home-page.html',
})
export class HomePage {}
