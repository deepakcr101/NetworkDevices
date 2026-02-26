import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { Device } from '../../../core/models/device';
import { DeviceService } from '../../../core/services/device-service';
import { DeviceSummaryCard } from '../../../shared/components/device-summary-card/device-summary-card';
import { DialogService } from '../../../shared/services/dialog';

interface State {
  devices: Device[];
  status: 'loading' | 'loaded' | 'error';
  error: string | null;
}

@Component({
  selector: 'app-home-page',
  // Since this template is becoming larger, we can move it to an external file
  // for better organization, but inline is fine for now as requested.
  template: `
    <header class="page-header">
      <h1>Welcome to My Inventory</h1>
      <div class="actions">
        <button class="button-primary" (click)="openCreateDeviceForm()">Create Device</button>
        <button class="button-secondary" (click)="openCreateShelfForm()">Create Shelf</button>
      </div>
    </header>

    <main>
      <h2>Available Devices</h2>
      @switch (state().status) {
        @case ('loading') {
          <p aria-live="polite">Loading devices...</p>
        }
        @case ('loaded') {
          @if (state().devices.length > 0) {
            <div class="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Device Name</th>
                    <th>Type</th>
                    <th>Part Number</th>
                    <th>Building</th>
                    <th># Shelf Positions</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (device of state().devices; track device.deviceId) {
                    <tr>
                      <td>{{ device.deviceName }}</td>
                      <td>{{ device.deviceType | titlecase }}</td>
                      <td>{{ device.partNumber }}</td>
                      <td>{{ device.buildingName }}</td>
                      <td>{{ device.numberOfShelfPositions }}</td>
                      <td>
                        <button (click)="openDeviceCard(device)">View Summary</button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          } @else {
            <p>No devices found. Create one to get started!</p>
          }
        }
        @case ('error') {
          <p role="alert" class="error-message">
            Failed to load devices. Please try again later. ({{ state().error }})
          </p>
        }
      }
    </main>
  `,
  styleUrls: ['./home-page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TitleCasePipe] // Import pipes directly into standalone components
})
export class HomePage {
private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);

  readonly state = signal<State>({
    devices: [],
    status: 'loading',
    error: null,
  });

  constructor() {
    this.loadDevices();
  }

  loadDevices(): void {
    this.state.set({ devices: [], status: 'loading', error: null });
    this.deviceService.getDevices().subscribe({
      next: (devices) => this.state.update(s => ({ ...s, devices, status: 'loaded' })),
      error: (err) => this.state.update(s => ({ ...s, status: 'error', error: err.message })),
    });
  }

  
openDeviceCard(device: Device): void {
  console.log('Opening summary for:', device.deviceName);

  this.deviceService.getDeviceSummary(device.deviceId).subscribe({
    next: (summary) => {
      // Pass the raw summary directly
      this.dialogService.open(DeviceSummaryCard, summary);
    },
    error: (err) => {
      console.error('Failed to retrieve device summary:', err);
      alert(`Error retrieving device summary: ${err.message}`);
    }
  });
}


  openCreateDeviceForm(): void {
    console.log('Opening create device form...');
    // Logic to open DeviceForm in a dialog will be added here.
  }

  openCreateShelfForm(): void {
    console.log('Opening create shelf form...');
    // Logic to open ShelfForm in a dialog will be added here.
  }
}
