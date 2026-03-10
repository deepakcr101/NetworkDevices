import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { TitleCasePipe,DatePipe } from '@angular/common';
import { Device } from '../../../core/models/device';
import { DeviceService } from '../../../core/services/device-service';
import { DeviceSummaryCard } from '../../../shared/components/device-summary-card/device-summary-card';
import { DialogService } from '../../../shared/services/dialog';
import { DeviceFields } from '../../../shared/components/device-fields/device-fields';
import { DeviceCreateForm } from '../../devices/create/create-device-form/create-device-form';
import { DeviceEditForm } from '../../devices/edit/edit-device-form/edit-device-form';
import { Router } from '@angular/router';
import { ShelfForm } from '../../../shared/components/shelf-form/shelf-form';
import { FormsModule } from '@angular/forms';

interface State {
  devices: Device[];
  status: 'loading' | 'loaded' | 'error';
  error: string | null;
}

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TitleCasePipe,FormsModule,DatePipe] // Import pipes directly into standalone components
})
export class HomePage {
  private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);

  readonly state = signal<State>({
    devices: [],
    status: 'loading',
    error: null,
  });

  constructor() {
    this.loadDevices();
  }
  
  readonly searchQuery = signal('');
  readonly filteredDevices = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const devices = this.state().devices;
    if(!query) {
      return devices;
    }
    return devices.filter(device =>
      device.deviceName.toLowerCase().includes(query) || 
      device.deviceType.toLowerCase().includes(query) ||
      device.partNumber.toLowerCase().includes(query) ||
      device.buildingName.toLowerCase().includes(query)
    );
  });

  loadDevices(): void {
    this.state.set({ devices: [], status: 'loading', error: null });
    this.deviceService.getDevices().subscribe({
      next: (devices) => this.state.update(s => ({ ...s, devices, status: 'loaded' })),
      error: (err) => this.state.update(s => ({ ...s, status: 'error', error: err.message })),
    });
  }

  openDeviceCard(device: Device): void {
    // console.log(device);
    this.router.navigate(['/home/summary', device.deviceId]);
  }

  deleteDevice(deviceId: string): void {
    if (!confirm('Are you sure you want to delete this device?')) {
      return;
    }

    this.deviceService.deleteDevice(deviceId).subscribe({
      next: () => {
        // Refresh the device list after deletion
        this.loadDevices();
        alert('Device deleted successfully!');
      },
      error: (err: { message: any; }) => {
        console.error('Failed to delete device:', err);
        alert(`Error: ${err.message}`);
      },
    });
  }

  


openEditDeviceForm(device: Device): void {
  const dialogRef = this.dialogService.open(DeviceEditForm, {
    device, // this will be provided as DIALOG_DATA
  });
  dialogRef.subscribe(result => {
    if (result === 'success') this.loadDevices();
  });
}


openCreateDeviceForm(): void {
  // Create form doesn't need any inputs
  const dialogRef = this.dialogService.open(DeviceCreateForm);

  dialogRef.subscribe((result) => {
    if (result === 'success') {
      this.loadDevices();
    }
  });
}


  openShelfPage(): void {
    this.router.navigate(['/shelves']);
  }


}
