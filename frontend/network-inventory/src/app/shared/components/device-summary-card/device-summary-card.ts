import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DeviceSummary } from '../../../core/models/device';
import { DIALOG_DATA } from '../../../shared/services/dialog.config';
import { DeviceService } from '../../../core/services/device-service';

@Component({
  selector: 'app-device-summary-card',
  template: `
    @if (summary(); as s) {
      <div class="summary-header">
        <h2 id="dialog-title">{{ s.device.deviceName }}</h2>
        <p>
          {{ s.device.deviceType }} | {{ s.device.partNumber }} | {{ s.device.buildingName }}
        </p>
      </div>

      <div class="shelf-positions">
        <h3>Shelf Positions ({{ s.device.numberOfShelfPositions }})</h3>
        <ul class="positions-grid">
          @for (sp of s.positions; track sp.shelfPositionId) {
            <li class="position-card" [class.occupied]="sp.isOccupied">
              <div class="position-header">
                <strong>Index {{ sp.index }}</strong>
                <span class="status-badge" [class.occupied]="sp.isOccupied" [class.free]="!sp.isOccupied">
                  {{ sp.isOccupied ? 'OCCUPIED' : 'FREE' }}
                </span>
              </div>

              @if (sp.isOccupied && sp.shelf) {
                <div class="shelf-details">
                  <p><strong>Shelf:</strong> {{ sp.shelf.shelfName }}</p>
                  <p><strong>Part #:</strong> {{ sp.shelf.partName }}</p>
                </div>
                <button (click)="freeShelfPosition(s.device.deviceId, sp.shelfPositionId)">
                  Free Position
                </button>
              } @else {
                <div class="shelf-details empty"><p>Empty</p></div>
                <button (click)="addShelf(sp.shelfPositionId)">Add Shelf</button>
              }
            </li>
          }
        </ul>
      </div>
    } @else {
      <p>Loading device details...</p>
    }
  `,
  styleUrls: ['./device-summary-card.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceSummaryCard {
  private readonly deviceService = inject(DeviceService);

  // Receive the raw summary injected by DialogService (Option A)
  private readonly initialSummary: DeviceSummary = inject(DIALOG_DATA) as DeviceSummary;

  // Component state: full summary
  readonly summary = signal<DeviceSummary>(this.initialSummary);

  freeShelfPosition(deviceId: string, shelfPositionId: string): void {
    if (!confirm('Are you sure you want to free this shelf position?')) {
      return;
    }

    this.deviceService.freeShelfPosition(deviceId, shelfPositionId).subscribe({
      next: () => {
        // Update the local state to reflect the freed shelf position
        const updatedPositions = this.summary().positions.map(position => 
            position.shelfPositionId === shelfPositionId
              ? { ...position, isOccupied: false, shelf: undefined }
              : position
          );
        this.summary.update(state => ({
          ...state,
          positions: updatedPositions,
        }));
        alert('Shelf position freed successfully!');
      },
      error: (err: { message: any; }) => {
        console.error('Failed to free shelf position:', err);
        alert(`Error: ${err.message}`);
      },
    });
  }

  addShelf(shelfPositionId: string): void {
    console.log(`TODO: Implement 'Add Shelf' flow for shelf position ${shelfPositionId}`);
    alert('"Add Shelf" functionality is not yet implemented.');
  }
}