import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  inject,
  signal,
} from '@angular/core';
import {
  form,
  FormField,
  required,
  minLength,
} from '@angular/forms/signals';

export type DeviceFieldsModel = {
  deviceName: string;
  deviceType: string;
  partNumber: string;
  buildingName: string;
  numberOfShelfPositions: string | number; // keep as string in UI; container coerces
};

@Component({
  selector: 'app-device-fields',
  templateUrl: './device-fields.html',
  styleUrls: ['./device-fields.scss'],
  standalone: true,
  imports: [FormField],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceFields {
  /**
   * Initial values (for create or edit). If not provided, defaults are used.
   */
  @Input() set initial(value: Partial<DeviceFieldsModel> | undefined) {
    const v = value ?? {};
    this.model.set({
      deviceName: v.deviceName ?? '',
      deviceType: v.deviceType ?? '',
      partNumber: v.partNumber ?? '',
      buildingName: v.buildingName ?? '',
      numberOfShelfPositions: v.numberOfShelfPositions ?? '1',
    });
  }

  /**
   * Whether to allow editing the numberOfShelfPositions field. Usually:
   * - create: true (editable)
   * - edit: false (immutable in most systems)
   */
  @Input() allowShelfPositionsEdit = true;

  /**
   * Emits the current model when parent asks for submit.
   * The parent container calls (submitRequested.emit()) or just listens to (submitted).
   */
  @Output() submitted = new EventEmitter<DeviceFieldsModel>();

  // Local model
  model = signal<DeviceFieldsModel>({
    deviceName: '',
    deviceType: '',
    partNumber: '',
    buildingName: '',
    numberOfShelfPositions: '1',
  });

  // Form + validators
  form = form(this.model, (p) => {
    required(p.deviceName, { message: 'Device Name is required' });
    minLength(p.deviceName, 3, {
      message: 'Device Name must be at least 3 characters',
    });

    required(p.deviceType, { message: 'Device Type is required' });
    required(p.partNumber, { message: 'Part Number is required' });
    required(p.buildingName, { message: 'Building Name is required' });
    required(p.numberOfShelfPositions, {
      message: 'Number of Shelf Positions is required',
    });
  });

  /**
   * Let parent trigger submit via (submit) on <form>.
   * We validate here and bubble up only if valid.
   */
  onSubmit(ev: Event) {
    ev.preventDefault();
    const frm = this.form();
    if (frm.invalid()) {
      this.touchAllFields();
      return;
    }
    this.submitted.emit(this.model());
  }

  private touchAllFields(): void {
    this.form.deviceName().markAsTouched();
    this.form.deviceType().markAsTouched();
    this.form.partNumber().markAsTouched();
    this.form.buildingName().markAsTouched();
    this.form.numberOfShelfPositions().markAsTouched();
  }
}
