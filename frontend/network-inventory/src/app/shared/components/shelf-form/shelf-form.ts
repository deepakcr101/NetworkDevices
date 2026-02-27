import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ShelfService } from '../../../core/services/shelf-service';
import { DialogService } from '../../services/dialog';
import { DIALOG_DATA } from '../../services/dialog.config';
import { Shelf } from '../../../core/models/shelf';

@Component({
  selector: 'app-shelf-form',
  // ... template remains the same ...
  template: `
    <form [formGroup]="shelfForm" (ngSubmit)="submitForm()">
      <h2>{{ mode === 'create' ? 'Create Shelf' : 'Edit Shelf' }}</h2>
      <div class="form-field">
        <label for="shelfName">Shelf Name</label>
        <input id="shelfName" formControlName="shelfName" />
      </div>
      <div class="form-field">
        <label for="partName">Part Name</label>
        <input id="partName" formControlName="partName" />
      </div>
      <div class="actions">
        <button type="button" class="button-secondary" (click)="closeDialog()">Cancel</button>
        <button type="submit" class="button-primary" [disabled]="shelfForm.invalid">
          {{ mode === 'create' ? 'Create' : 'Update' }}
        </button>
      </div>
    </form>
  `,
  imports: [ReactiveFormsModule],
  styleUrls: ['./shelf-form.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShelfForm {
  private readonly shelfApi = inject(ShelfService);
  private readonly dialogService = inject(DialogService);
  private readonly fb = inject(FormBuilder);

  private readonly dialogData: { mode: 'create' | 'edit', shelf?: Shelf } = inject(DIALOG_DATA, { optional: true }) || { mode: 'create' };
  
  readonly mode = this.dialogData.mode;
  readonly shelf: Partial<Shelf> = this.dialogData.shelf || {};

  readonly shelfForm: FormGroup = this.fb.group({
    shelfName: [this.shelf.shelfName || '', Validators.required],
    partName: [this.shelf.partName || '', Validators.required],
  });

  submitForm(): void {
    if (this.shelfForm.invalid) {
      return;
    }

    if (this.mode === 'create') {
      this.shelfApi.createShelf(this.shelfForm.value).subscribe({
        next: () => this.dialogService.close('success'),
        error: (err) => alert(`Error creating shelf: ${err.message}`),
      });
    } else if (this.mode === 'edit' && this.shelf?.shelfId) {
      
      this.shelfApi.updateShelf(this.shelf.shelfId, this.shelfForm.value).subscribe({
        next: () => this.dialogService.close('success'),
        error: (err) => alert(`Error updating shelf: ${err.message}`),
      });
    }
  }

  closeDialog(): void {
    this.dialogService.close();
  }
}