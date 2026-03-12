import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { GlobalHeader } from './shared/components/global-header/global-header';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, GlobalHeader],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('network-inventory');
}
