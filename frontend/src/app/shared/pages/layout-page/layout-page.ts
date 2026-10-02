import { Component } from '@angular/core';
import { Sidebar } from '../../components/sidebar/sidebar';
import { ToastContainer } from '../../../core/notifications/toast-container/toast-container';

@Component({
  selector: 'app-layout-page',
  imports: [Sidebar, ToastContainer],
  templateUrl: './layout-page.html',
  host: { class: 'block' },
})
export class LayoutPage {}
