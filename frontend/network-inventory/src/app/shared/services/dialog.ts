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

@Injectable({ providedIn: 'root' })
export class DialogService {
  private readonly appRef = inject(ApplicationRef);
  private readonly injector = inject(EnvironmentInjector);
  private readonly document = inject(DOCUMENT);

  private dialogComponentRef?: ComponentRef<Dialog>;
  private contentComponentRef?: any; // ComponentRef<unknown>
  private previouslyFocusedElement?: HTMLElement;
   private result$?: Subject<any>;
   
  open<T>(componentType: Type<T>, data?: any): Observable<any> {
    this.previouslyFocusedElement = this.document.activeElement as HTMLElement;

    // Create a new Subject for each dialog instance
    this.result$ = new Subject<any>();

    const dialogInjector = Injector.create({
      providers: [{ provide: DIALOG_DATA, useValue: data }],
      parent: this.injector,
    });

    const contentComponentRef = createComponent(componentType, {
      environmentInjector: this.injector,
      elementInjector: dialogInjector,
    });

    this.dialogComponentRef = createComponent(Dialog, {
      environmentInjector: this.injector,
      projectableNodes: [[contentComponentRef.location.nativeElement]],
    });

    this.dialogComponentRef.instance.close.subscribe(() => this.close());

    this.appRef.attachView(contentComponentRef.hostView);
    this.appRef.attachView(this.dialogComponentRef.hostView);

    this.document.body.appendChild(this.dialogComponentRef.location.nativeElement);
    
    // ### CHANGE 2: Return the Subject as an Observable ###
    return this.result$.asObservable();
  }

  // ### CHANGE 3: Update the `close` method to accept a result ###
  close(result?: any): void {
    if (!this.dialogComponentRef) {
      return;
    }

    this.appRef.detachView(this.dialogComponentRef.hostView);
    this.dialogComponentRef.destroy();
    this.dialogComponentRef = undefined;

    this.previouslyFocusedElement?.focus();

    // ### CHANGE 4: Emit the result and complete the Subject ###
    if (this.result$) {
      this.result$.next(result);
      this.result$.complete();
      this.result$ = undefined;
    }
  }
}
