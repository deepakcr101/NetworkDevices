// src/app/shared/components/device-summary-card/device-summary-card.ts
import { ChangeDetectionStrategy, Component, inject, signal, effect } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DeviceService } from '../../../core/services/device-service';
import { DialogService } from '../../../shared/services/dialog';
import { DeviceSummary } from '../../../core/models/device';
import { AllocateShelfDialog } from '../allocate-shelf-dialog/allocate-shelf-dialog';

@Component({
  selector: 'app-device-summary-card',
  templateUrl: './device-summary-card.html',
  styleUrls: ['./device-summary-card.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceSummaryCard {
  private readonly route = inject(ActivatedRoute);
  private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);

  // Read deviceId from route (string). Using a signal is fine; read with this.deviceId()
  readonly deviceId = signal<string>(this.route.snapshot.paramMap.get('deviceId')!);

  readonly summary = signal<DeviceSummary | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  // Load summary whenever deviceId changes
  private readonly load = effect(() => {
    const id = this.deviceId();
    if (!id) return;

    this.loading.set(true);
    this.error.set(null);

    this.deviceService.getDeviceSummary(id).subscribe({
      next: (s) => { this.summary.set(s); this.loading.set(false); },
      error: (err) => { this.error.set(err?.error || err?.message || 'Failed to load summary'); this.loading.set(false); },
    });
  });

  
  addShelf(deviceId: string, shelfPositionId: string): void {
    const dialogRef = this.dialogService.open(AllocateShelfDialog, { deviceId, shelfPositionId });
    dialogRef.subscribe((result) => {
      if (result === 'success') this.reloadSummary(deviceId);
    });
  }

  
  freeShelfPosition(deviceId: string, shelfPositionId: string): void {
    if (!confirm('Are you sure you want to free this shelf position?')) return;
    this.deviceService.freeShelfPosition(deviceId, shelfPositionId).subscribe({
      next: () => { alert('Shelf position freed successfully!'); this.reloadSummary(deviceId); },
      error: (err) => alert(`Error: ${err.message}`),
    });
  }

  private reloadSummary(deviceId: string): void {
    this.deviceService.getDeviceSummary(deviceId).subscribe({
      next: (s) => this.summary.set(s),
      error: (err) => alert(`Failed to refresh summary: ${err.message}`),
    });
  }
}