import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () => import('./features/home/home.routes').then(m => m.HOME_ROUTES),
    title: 'Device Inventory',
  },
  {
    path: 'shelves',
    loadChildren: () => import('./features/shelf/shelf.routes').then(m => m.SHELF_ROUTES),
    title: 'Shelf Management',
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full',
  },
  {
    path: '**', // Wildcard route for 404 pages
    redirectTo: 'home',
  },
];