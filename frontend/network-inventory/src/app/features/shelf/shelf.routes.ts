import { Routes } from '@angular/router';
import { ShelfPage } from './shelf-page/shelf-page';

export const SHELF_ROUTES: Routes = [
  {
    path: '',
    component: ShelfPage, // /shelves
    title: 'Shelf Management',
  },
];