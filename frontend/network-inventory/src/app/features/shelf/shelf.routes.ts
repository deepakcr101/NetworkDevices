import { Routes } from '@angular/router';
import { ShelfPage } from './shelf-page/shelf-page'; 

export const SHELF_ROUTES: Routes = [
  {
    path: '', // Default path for the "shelves" feature
    component: ShelfPage,
  },
];