import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../components/sidebar/sidebar';

@Component({
  selector: 'app-layout-page',
  imports: [RouterOutlet, Sidebar],
  templateUrl: './layout-page.html',
})
export class LayoutPage {}
