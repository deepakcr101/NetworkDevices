import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Shelf } from '../../../core/models/shelf';
import { ShelfService } from '../../../core/services/shelf-service';
import { DialogService } from '../../../shared/services/dialog';
import { ShelfForm } from '../../../shared/components/shelf-form/shelf-form';

interface State {
  shelves: Shelf[];
  status: 'loading' | 'loaded' | 'error';
  error: string | null;
}

@Component({
  selector: 'app-shelf-page',
  template: `
    <header class="page-header">
      <h1>Shelf Management</h1>
      <button class="button-primary" (click)="openCreateShelfForm()">Create Shelf</button>
    </header>

    <main>
      <h2>Available Shelves</h2>
      @switch (state().status) {
        @case ('loading') {
          <p aria-live="polite">Loading shelves...</p>
        }
        @case ('loaded') {
          @if (state().shelves.length > 0) {
            <table>
              <thead>
                <tr>
                  <th>Shelf Name</th>
                  <!-- //<th>Type</th> -->
                  <th>Part Name</th>
                  <th>Device Name</th>
                  <th>Shelf Position ID</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (shelf of state().shelves; track shelf.shelfid) {
                  <tr>
                    <td>{{ state().shelves.indexOf(shelf) + 1 }}</td>
                    <td>{{ shelf.shelfName }}</td>
                    
                    <td>{{ shelf.partName }}</td>
                    <td>{{ shelf.deviceId }}</td>
                    <td>{{shelf.shelfPositionId}}</td>
                    <td>
                      <button (click)="openEditShelfForm(shelf)">Edit</button>
                      <button (click)="deleteShelf(shelf.shelfid)">Delete</button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          } @else {
            <p>No shelves found. Create one to get started!</p>
          }
        }
        @case ('error') {
          <p role="alert" class="error-message">
            Failed to load shelves. Please try again later. ({{ state().error }})
          </p>
        }
      }
    </main>
  `,
  styleUrls: ['./shelf-page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShelfPage {
private readonly shelfApi = inject(ShelfService);
  private readonly dialogService = inject(DialogService);

  readonly state = signal<State>({
    shelves: [],
    status: 'loading',
    error: null,
  });

  constructor() {
    this.loadShelves();
  }

  loadShelves(): void {
    this.state.set({ shelves: [], status: 'loading', error: null });
    this.shelfApi.getAvailableShelves().subscribe({
      next: (shelves) => this.state.update(s => ({ ...s, shelves, status: 'loaded' })),
      error: (err) => this.state.update(s => ({ ...s, status: 'error', error: err.message })),
    });
  }

  openCreateShelfForm(): void {
    this.dialogService.open(ShelfForm, { mode: 'create' });
  }

  openEditShelfForm(shelf: Shelf): void {
    this.dialogService.open(ShelfForm, { mode: 'edit', shelf });
  }

  deleteShelf(shelfId: string): void {
    if (!confirm('Are you sure you want to delete this shelf?')) {
      return;
    }

    this.shelfApi.deleteShelf(shelfId).subscribe({
      next: () => this.loadShelves(), // Reload shelves after deletion
      error: (err) => alert(`Error deleting shelf: ${err.message}`),
    });
  }
}
