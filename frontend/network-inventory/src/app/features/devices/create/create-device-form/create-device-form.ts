import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DeviceFields, DeviceFieldsModel } from '../../../../shared/components/device-fields/device-fields';
import { DeviceService } from '../../../../core/services/device-service';
import { DialogService } from '../../../../shared/services/dialog';
import { CreateDevicePayload } from '../../../../core/models/device';

@Component({
  selector: 'app-device-create-form',
  template: `
    <app-device-fields
      [initial]="initial"
      [allowShelfPositionsEdit]="true"
      (submitted)="handleSubmit($event)"
    />
  `,
  standalone: true,
  imports: [DeviceFields],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceCreateForm {
  private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);

  initial: Partial<DeviceFieldsModel> = {
    deviceName: '',
    deviceType: '',
    partNumber: '',
    buildingName: '',
    numberOfShelfPositions: '1',
  };

  private toInt(v: unknown): number {
    if (typeof v === 'number') return Math.trunc(v);
    if (typeof v === 'string' && v.trim() !== '') {
      const n = Number(v);
      return Number.isFinite(n) ? Math.trunc(n) : NaN;
    }
    return NaN;
  }

  handleSubmit(model: DeviceFieldsModel) {
    const n = this.toInt(model.numberOfShelfPositions);
    if (!Number.isFinite(n) || n < 1) {
      alert('Number of Shelf Positions must be an integer ≥ 1');
      return;
    }

    const payload: CreateDevicePayload = {
      deviceName: model.deviceName,
      deviceType: model.deviceType,
      partNumber: model.partNumber,
      buildingName: model.buildingName,
      numberOfShelfPositions: n,
    };

    this.deviceService.createDevice(payload).subscribe({
      next: (deviceId) => {
        console.log('Created device ID:', deviceId);
        this.dialogService.close('success');
      },
      error: (err) => {
        console.error('Create Error:', err);
        alert(this.extractServerMessage(err));
      },
    });
  }

  private extractServerMessage(err: any): string {
    const raw = err?.error;
    if (typeof raw === 'string') {
      const s = raw.trim();
      if (s.startsWith('{') && s.endsWith('}')) {
        try {
          const obj = JSON.parse(s);
          return obj.message || s;
        } catch {
          return s;
        }
      }
      return s;
    }
    if (raw && typeof raw === 'object' && 'message' in raw) {
      return (raw as any).message;
    }
    return err?.message || 'Server error while creating device';
  }
}