import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../components/sidebar/sidebar';
import { ToastContainer } from '../../../core/notifications/toast-container/toast-container';

@Component({
  selector: 'app-layout-page',
  imports: [RouterOutlet, Sidebar, ToastContainer],
  templateUrl: './layout-page.html',
})
export class LayoutPage {}
