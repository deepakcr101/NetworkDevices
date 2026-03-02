import { Routes } from '@angular/router';
import { HomePage } from './home-page/home-page';
import { DeviceSummaryCard } from '../../shared/components/device-summary-card/device-summary-card';

export const HOME_ROUTES: Routes = [
  { path: '', component: HomePage, title: 'Device Inventory' },
  { path: 'summary/:deviceId', component: DeviceSummaryCard, title: 'Device Summary' },
];