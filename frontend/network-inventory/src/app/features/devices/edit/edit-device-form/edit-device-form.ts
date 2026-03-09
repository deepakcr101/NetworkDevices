import {
  ChangeDetectionStrategy,
  Component,
  Input,
  Inject,
  Optional,
  inject,
  signal,
} from '@angular/core';
import { Device } from '../../../../core/models/device';
import { DeviceService } from '../../../../core/services/device-service';
import { DialogService } from '../../../../shared/services/dialog';
import { DIALOG_DATA } from '../../../../shared/services/dialog.config'; // ensure path

@Component({
  selector: 'app-device-edit-form',
  standalone: true,
  templateUrl: './edit-device-form.html',
  styleUrls: ['./edit-device-form.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceEditForm {
  private readonly deviceService = inject(DeviceService);
  private readonly dialogService = inject(DialogService);

  // single source of truth
  private _device: Device | null = null;

  // (Optional) still keep the @Input setter if we ever open by input binding
  @Input()
  set device(value: Device | null) {
    //console.log('[DeviceEditForm] @Input device →', value);
    this.setDevice(value);
  }

  // read from DIALOG_DATA (what DialogService actually provides)
  constructor(@Optional() @Inject(DIALOG_DATA) data?: any) {
    console.log('[DeviceEditForm] DIALOG_DATA →', data);
    if (data?.device) {
      this.setDevice(data.device as Device);
    }
  }

  private setDevice(value: Device | null) {
    this._device = value;
    if (value) {
      this.model.set({
        deviceName: value.deviceName ?? '',
        deviceType: value.deviceType ?? '',
        partNumber: value.partNumber ?? '',
        buildingName: value.buildingName ?? '',
      });
    }
  }

  enable = {
    deviceName: signal(false),
    deviceType: signal(false),
    partNumber: signal(false),
    buildingName: signal(false),
  };

  model = signal({
    deviceName: '',
    deviceType: '',
    partNumber: '',
    buildingName: '',
  });

  onSubmit(e: Event) {
    e.preventDefault();

    if (!this._device?.deviceId) {
      alert('Device is not loaded. Please close and reopen the dialog.');
      return;
    }

    const m = this.model();
    const inc = this.enable;
    const payload: Record<string, any> = {};
    if (inc.deviceName())   payload['deviceName']   = m.deviceName;
    if (inc.deviceType())   payload['deviceType']   = m.deviceType;
    if (inc.partNumber())   payload['partNumber']   = m.partNumber;
    if (inc.buildingName()) payload['buildingName'] = m.buildingName;

    if (Object.keys(payload).length === 0) {
      this.dialogService.close('success');
      return;
    }

    this.deviceService.updateDevice(this._device.deviceId, payload).subscribe({
      next: () => { alert('Device updated successfully'); this.dialogService.close('success'); },
      error: (err) => { alert('Error updating device: ' + (err?.error || err?.message || 'Unknown error')); }
    });
  }
}