// src/app/shared/services/dialog.ts
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
import { Subject, Observable } from 'rxjs';
import { Dialog } from '../components/dialog/dialog';
import { DIALOG_DATA } from './dialog.config';

type DialogEntry = {
  dialogRef: ComponentRef<Dialog>;
  contentRef: ComponentRef<any>;
  result$: Subject<any>;
  previouslyFocused: HTMLElement | null;
};

@Injectable({ providedIn: 'root' })
export class DialogService {
  private readonly appRef = inject(ApplicationRef);
  private readonly injector = inject(EnvironmentInjector);
  private readonly document = inject(DOCUMENT);

  /** Support nested dialogs by keeping a stack of entries */
  private stack: DialogEntry[] = [];

  open<T>(componentType: Type<T>, data?: any): Observable<any> {
    const previouslyFocused = (this.document.activeElement as HTMLElement) ?? null;

    const dialogInjector = Injector.create({
      providers: [{ provide: DIALOG_DATA, useValue: data }],
      parent: this.injector,
    });

    // Create the dialog content (your feature component)
    const contentRef = createComponent(componentType, {
      environmentInjector: this.injector,
      elementInjector: dialogInjector,
    });

    // Create the dialog chrome/shell that hosts the projected content
    const dialogRef = createComponent(Dialog, {
      environmentInjector: this.injector,
      projectableNodes: [[contentRef.location.nativeElement]],
    });

    // Top-most result channel
    const result$ = new Subject<any>();
    this.stack.push({ dialogRef, contentRef, result$, previouslyFocused });

    // Wire shell 'close' output to close the top-most dialog
    dialogRef.instance.close.subscribe(() => this.close());

    // Attach views
    this.appRef.attachView(contentRef.hostView);
    this.appRef.attachView(dialogRef.hostView);

    // Put shell in DOM (content is projected inside it)
    this.document.body.appendChild(dialogRef.location.nativeElement);

    // (Optional) Raise z-index for stacking (last opened is on top)
    dialogRef.location.nativeElement.style.zIndex = (1000 + this.stack.length * 2).toString();

    return result$.asObservable();
  }

  
  close(result?: any): void {
    const entry = this.stack.pop(); // Get the most recent dialog from the stack
    if (!entry) return;

    const { dialogRef, contentRef, result$, previouslyFocused } = entry;

    // Detach & destroy content first
    this.appRef.detachView(contentRef.hostView);
    contentRef.destroy();

    // Then detach & destroy shell
    this.appRef.detachView(dialogRef.hostView);
    dialogRef.destroy();

    // Restore focus to the element active before this dialog opened
    previouslyFocused?.focus();

    // Resolve the result for the dialog that was opened
    result$.next(result);
    result$.complete();
  }

  /** Optional: close all open dialogs (useful on route change) */
  closeAll(): void {
    while (this.stack.length) this.close();
  }
}