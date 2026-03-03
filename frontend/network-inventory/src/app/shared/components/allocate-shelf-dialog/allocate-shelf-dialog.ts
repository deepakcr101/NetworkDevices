// src/app/shared/components/allocate-shelf-dialog/allocate-shelf-dialog.ts
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Shelf } from '../../../core/models/shelf';
import { ShelfService } from '../../../core/services/shelf-service';
import { DeviceService } from '../../../core/services/device-service';
import { DialogService } from '../../../shared/services/dialog';
import { DIALOG_DATA } from '../../../shared/services/dialog.config';
import { ShelfForm } from '../shelf-form/shelf-form';

interface State {
  shelves: Shelf[];
  status: 'loading' | 'loaded' | 'error';
}

@Component({
  selector: 'app-allocate-shelf-dialog',
  standalone: true, 
  templateUrl: './allocate-shelf-dialog.html',
  styleUrls: ['./allocate-shelf-dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AllocateShelfDialog {
  private readonly shelfService = inject(ShelfService);
  private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);

  private readonly dialogData: { deviceId: string; shelfPositionId: string } = inject(DIALOG_DATA);

  readonly state = signal<State>({ shelves: [], status: 'loading' });

  constructor() {
    this.loadAvailableShelves();
  }

  loadAvailableShelves(): void {
    this.state.set({ shelves: [], status: 'loading' });
    this.shelfService.getAvailableShelves().subscribe({
      next: (shelves) => this.state.update(s => ({ ...s, shelves, status: 'loaded' })),
      error: () => this.state.update(s => ({ ...s, status: 'error' })),
    });
  }

  allocateShelf(shelfId: string): void {
    this.deviceService
      .allocateShelf(this.dialogData.deviceId, this.dialogData.shelfPositionId, shelfId)
      .subscribe({
        next: () => this.dialogService.close('success'),
        error: (err) => alert(`Error allocating shelf: ${err.message}`),
      });
  }

  openCreateAndAllocateShelf(): void {
    const createDialogRef = this.dialogService.open(ShelfForm, { mode: 'create' });
    createDialogRef.subscribe((result) => {
      if (result === 'success'){
        this.loadAvailableShelves();
      }
    });
  }
}