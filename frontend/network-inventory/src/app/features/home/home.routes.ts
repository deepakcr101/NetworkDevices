import { Routes } from '@angular/router';
import { HomePage } from './home-page/home-page';

// This defines the routes specific to the "home" feature.
export const HOME_ROUTES: Routes = [
  {
    path: '', // The default path for this feature (e.g., /home)
    component: HomePage,
  },
];