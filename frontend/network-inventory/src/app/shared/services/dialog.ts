import {
  Injectable,
  inject,
  ApplicationRef,
  createComponent,
  EnvironmentInjector,
  ComponentRef,
  Injector,
  Type,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Dialog } from '../components/dialog/dialog';
import { DIALOG_DATA } from './dialog.config';

@Injectable({ providedIn: 'root' })
export class DialogService {
  private readonly appRef = inject(ApplicationRef);
  private readonly injector = inject(EnvironmentInjector);
  private readonly document = inject(DOCUMENT);

  private dialogComponentRef?: ComponentRef<Dialog>;
  private contentComponentRef?: any; // ComponentRef<unknown>
  private previouslyFocusedElement?: HTMLElement;

  open<T>(componentType: Type<T>, data?: any): void {
    // 0) Store focused element
    this.previouslyFocusedElement = this.document.activeElement as HTMLElement;

    // 1) Injector that provides the raw payload to DIALOG_DATA
    const dialogInjector = Injector.create({
      providers: [{ provide: DIALOG_DATA, useValue: data }],
      parent: this.injector,
    });

    // 2) Create the content component (e.g., DeviceSummaryCard)
    this.contentComponentRef = createComponent(componentType, {
      environmentInjector: this.injector,
      elementInjector: dialogInjector,
    });

    // 3) Create the dialog wrapper and project content inside
    this.dialogComponentRef = createComponent(Dialog, {
      environmentInjector: this.injector,
      projectableNodes: [[this.contentComponentRef.location.nativeElement]],
    });

    // 4) Close subscription
    this.dialogComponentRef.instance.close.subscribe(() => this.close());

    // 5) Attach both views
    this.appRef.attachView(this.contentComponentRef.hostView);
    this.appRef.attachView(this.dialogComponentRef.hostView);

    // 6) Append to body
    this.document.body.appendChild(this.dialogComponentRef.location.nativeElement);
  }

  close(): void {
    if (!this.dialogComponentRef) return;

    // Detach and destroy content component
    if (this.contentComponentRef) {
      this.appRef.detachView(this.contentComponentRef.hostView);
      this.contentComponentRef.destroy();
      this.contentComponentRef = undefined;
    }

    // Detach and destroy dialog wrapper
    this.appRef.detachView(this.dialogComponentRef.hostView);
    this.dialogComponentRef.destroy();
    this.dialogComponentRef = undefined;

    // Restore focus
    this.previouslyFocusedElement?.focus();
  }
}