// src/app/core/services/shelf.api.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createShelfPayload, Shelf, ShelfLite, updateShelfPayload } from '../models/shelf';

@Injectable({
  providedIn: 'root',
})
export class ShelfService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/v1/shelves'; 

  getShelves(): Observable<Shelf[]> {
    return this.http.get<Shelf[]>(this.apiUrl);
  }

  // Gets shelves that are not currently installed in any device
  getAvailableShelves(): Observable<ShelfLite[]> {
    return this.http.get<ShelfLite[]>(`${this.apiUrl}/available`);
  }

  createShelf(shelfData: Omit<Shelf, 'id'>): Observable<createShelfPayload> {
    return this.http.post<createShelfPayload>(this.apiUrl, shelfData);
  }

  updateShelf(shelfId: string, shelfData: Partial<Shelf>): Observable<updateShelfPayload> {
    return this.http.patch<updateShelfPayload>(`${this.apiUrl}/${shelfId}`, shelfData);
  }

  deleteShelf(shelfId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${shelfId}`);
  }
}
