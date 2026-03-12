import { Component, inject, Input } from '@angular/core';
//import { HomePage } from '../../../features/home/home-page/home-page';
//import { DialogService } from '../../services/dialog';
//import { DialogService } from '../../services/dialog';
import { Router ,RouterLink,RouterLinkActive} from '@angular/router';
//import { ShelfForm } from '../shelf-form/shelf-form';
//import { ShelfPage } from '../../../features/shelf/shelf-page/shelf-page';
//import { HeaderService } from '../../services/header-service';
import { DialogService } from '../../services/dialog';
import { DeviceCreateForm } from '../../../features/devices/create/create-device-form/create-device-form';
import { ShelfForm } from '../shelf-form/shelf-form';

@Component({
  standalone: true,
  selector: 'app-global-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './global-header.html',
  styleUrl: './global-header.scss',
})
export class GlobalHeader {
  @Input() myHeading: string = 'Network Inventory Management System';

  private readonly dialogService = inject(DialogService);
    
    openCreateDevice(): void {
        // Create form doesn't need any inputs
        const dialogRef = this.dialogService.open(DeviceCreateForm);
    
        dialogRef.subscribe((result) => {
          if (result === 'success') {
            alert('Device created successfully!');
          }
        });
    }
  
    openCreateShelf(): void {
      // The dialog returns an observable that we can subscribe to.
          const dialogRef = this.dialogService.open(ShelfForm, { mode: 'create' });
      
          dialogRef.subscribe(result => {
            // If the dialog was closed with a 'success' message, reload the shelf list.
            if (result === 'success') {
              alert('Shelf created successfully!');
              //this.loadShelves();
            }
          });
    }
}
