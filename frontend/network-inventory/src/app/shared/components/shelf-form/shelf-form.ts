import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ReactiveFormsModule,FormControl,FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ShelfService } from '../../../core/services/shelf-service';
import { DialogService } from '../../services/dialog';
import { DIALOG_DATA } from '../../services/dialog.config';

@Component({
  selector: 'app-shelf-form',
  template: `
    <form [formGroup]="shelfForm" (ngSubmit)="submitForm()">
      <h2>{{ mode === 'create' ? 'Create Shelf' : 'Edit Shelf' }}</h2>

      <label>
        Shelf Name
        <input formControlName="shelfName" placeholder="Enter shelf name" />
      </label>

      <!-- <label>
        Shelf Type
        <input formControlName="shelfType" placeholder="Enter shelf type" />
      </label> -->

      <label>
        Part Name
        <input formControlName="partName" placeholder="Enter part name" />
      </label>

      <!-- <label>
        Serial Number
        <input formControlName="serialNumber" placeholder="Enter serial number" />
      </label> -->

      <div class="actions">
        <button type="submit" [disabled]="shelfForm.invalid">
          {{ mode === 'create' ? 'Create' : 'Update' }}
        </button>
        <button type="button" (click)="closeDialog()">Cancel</button>
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

  readonly mode: 'create' | 'edit' = inject(DIALOG_DATA).mode;
  readonly shelf = inject(DIALOG_DATA).shelf || {};

  readonly shelfForm: FormGroup = this.fb.group({
    shelfName: [this.shelf.shelfName || '', Validators.required],
    //shelfType: [this.shelf.shelfType || '', Validators.required],
    partName: [this.shelf.partName || '', Validators.required],
    //serialNumber: [this.shelf.serialNumber || '', Validators.required],
  });

  submitForm(): void {
    if (this.mode === 'create') {
      console.log('Creating shelf with data:', this.shelfForm.value);
      this.shelfApi.createShelf(this.shelfForm.value).subscribe({
        next: () => this.dialogService.close(),
        error: (err) => alert(`Error creating shelf: ${err.message}`),
      });
    } else {
      this.shelfApi.updateShelf(this.shelf.id, this.shelfForm.value).subscribe({
        next: () => this.dialogService.close(),
        error: (err) => alert(`Error updating shelf: ${err.message}`),
      });
    }
  }

  closeDialog(): void {
    this.dialogService.close();
  }
}
