import { ChangeDetectionStrategy, Component, output, ViewChild, ElementRef, afterNextRender } from '@angular/core';

@Component({
  selector: 'app-dialog',
  template: `
    <div class="dialog-backdrop" (click)="close.emit()"></div>
    <div
      #dialogPanel
      class="dialog-panel"
      role="dialog"
      aria-modal="true"
      aria-labelledby="dialog-title"
      tabindex="-1"
    >
      <button class="close-button" aria-label="Close dialog" (click)="close.emit()">
        &times;
      </button>
      <div class="dialog-content">
        <!-- The dynamic content (e.g., DeviceSummaryCard) will be projected here -->
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styleUrls: ['./dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(keydown.escape)': 'onEscape($any($event))',
  },
})
export class Dialog {
  // Signals-style output is fine
  close = output<void>();

  @ViewChild('dialogPanel') dialogPanel!: ElementRef<HTMLElement>;

  constructor() {
    // Focus the panel once rendered (accessibility)
    afterNextRender(() => {
      this.dialogPanel?.nativeElement?.focus();
    });
  }

  onEscape(event: KeyboardEvent): void {
    event.preventDefault();
    this.close.emit();
  }
}