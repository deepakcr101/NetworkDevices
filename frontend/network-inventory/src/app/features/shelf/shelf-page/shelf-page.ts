import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { Shelf } from '../../../core/models/shelf';
import { ShelfService } from '../../../core/services/shelf-service';
import { DialogService } from '../../../shared/services/dialog';
import { ShelfForm } from '../../../shared/components/shelf-form/shelf-form';
import { Router } from '@angular/router';
import { TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
//import { HeaderService } from '../../../shared/services/header-service';
import { takeUntil, Subject } from 'rxjs';


interface State {
  shelves: Shelf[];
  status: 'loading' | 'loaded' | 'error';
  error: string | null;
}

@Component({
  selector: 'app-shelf-page',
  templateUrl: './shelf-page.html',
  styleUrls: ['./shelf-page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule,]
})
export class ShelfPage {
  private readonly shelfApi = inject(ShelfService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);
  
  //a subject to manage unsubscription and prevent memory leaks
  private readonly destroy$ = new Subject<void>();

  readonly state = signal<State>({
    shelves: [],
    status: 'loading',
    error: null,
  });

  constructor() {
    this.loadShelves();
    //this.listenForActions();
  }
  
  
  readonly searchQuery = signal('');
  readonly filteredShelves = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const shelves = this.state().shelves;
    if (!query) {
      return shelves;
    }
    return shelves.filter(shelf =>
      shelf.shelfName.toLowerCase().includes(query) ||
      shelf.partName.toLowerCase().includes(query)
    );
  });

  loadShelves(): void {
    this.state.set({ shelves: [], status: 'loading', error: null });
    this.shelfApi.getShelves().subscribe({
      next: (shelves) => {
        //console.log('Received shelves:', shelves[0]);
        this.state.update(s => ({ ...s, shelves, status: 'loaded' }));
      },
      error: (err) => this.state.update(s => ({ ...s, status: 'error', error: err.message })),
    });
  }


  openCreateShelfForm(): void {
    // The dialog returns an observable that we can subscribe to.
    const dialogRef = this.dialogService.open(ShelfForm, { mode: 'create' });

    dialogRef.subscribe(result => {
      // If the dialog was closed with a 'success' message, reload the shelf list.
      if (result === 'success') {
        alert('Shelf created successfully!');
        this.loadShelves();
      }
    });
  }


  openEditShelfForm(shelf: Shelf): void {
    const dialogRef = this.dialogService.open(ShelfForm, { mode: 'edit', shelf });

    dialogRef.subscribe(result => {
      // If the dialog was closed with a 'success' message, reload the shelf list.
      if (result === 'success') {
        alert('Shelf updated successfully!');
        this.loadShelves();
      }
    });
  }


  deleteShelf(shelfId: string): void {
    if (!confirm('Are you sure you want to delete this shelf? This action cannot be undone.')) {
      return;
    }

    this.shelfApi.deleteShelf(shelfId).subscribe({
      next: () => {
        alert('Shelf deleted successfully!');
        // to refresh the data from the server.
        this.loadShelves();
      },
      error: (err) => {
        console.error('Failed to delete shelf:', err);
        alert(`Error deleting shelf: ${err.message}`);
      },
    });
  }

  openHomePage(): void {
    // Navigate back to the home page using the router.
    this.router.navigate(['/']);
  }

  // // Method to be called when the component is destroyed
  // ngOnDestroy(): void {
  //   this.destroy$.next();
  //   this.destroy$.complete();
  // }

}
