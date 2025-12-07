import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkDrag, CdkDragEnd, CdkDragMove } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-node-widget',
  standalone: true,
  imports: [CommonModule, CdkDrag],
  template: `
    <div class="absolute p-0 rounded-2xl shadow-2xl border transition-all duration-300 group select-none"
         style="width: 240px;"
         cdkDrag
         [cdkDragFreeDragPosition]="{x: x, y: y}"
         (cdkDragMoved)="onDragMoved($event)"
         (cdkDragEnded)="onDragEnded($event)"
         [ngClass]="getNodeClasses()">
         
       <!-- Glow Effect -->
       <div class="absolute -inset-px rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 bg-gradient-to-r from-transparent via-white/10 to-transparent blur-sm -z-10"></div>

       <!-- Header -->
       <div class="p-4 flex items-center gap-3 border-b border-white/5 bg-white/5 rounded-t-2xl">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center text-xl shadow-inner"
             [ngClass]="getIconBackground()">
             {{ getIcon() }}
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="font-bold text-sm text-slate-100 truncate">{{ data.label }}</h3>
          <p class="text-[10px] text-slate-400 font-mono uppercase tracking-wider">{{ data.type }}</p>
        </div>
       </div>

       <!-- Body -->
       <div class="p-4 bg-slate-950/40 rounded-b-2xl backdrop-blur-sm">
           <div class="text-xs text-slate-300 leading-relaxed" *ngIf="data.data?.description">
             {{ data.data.description }}
           </div>
           
           <!-- Attributes/Details based on type -->
           <div class="mt-2 space-y-1" *ngIf="data.type === 'LLM'">
               <div class="flex justify-between text-[10px]">
                   <span class="text-slate-500">Model</span>
                   <span class="text-slate-300 font-mono">{{ data.data?.model }}</span>
               </div>
           </div>
       </div>
      
      <!-- Connectors -->
      
      <!-- Input Connector (Left) -->
      <div class="absolute top-[45px] -left-3 w-6 h-6 flex items-center justify-center group/conn cursor-crosshair"
           (mouseup)="onMouseUpInput($event)"
           *ngIf="data.type !== 'START'">
           <div class="w-3 h-3 bg-slate-400 rounded-full border-2 border-slate-900 transition-all group-hover/conn:bg-white group-hover/conn:scale-125 hover:shadow-[0_0_10px_rgba(255,255,255,0.5)]"></div>
      </div>

      <!-- Output Connector (Right) -->
      <div class="absolute top-[45px] -right-3 w-6 h-6 flex items-center justify-center group/conn cursor-crosshair"
           (mousedown)="onMouseDownOutput($event)"
           *ngIf="data.type !== 'OUTPUT'">
           <div class="w-3 h-3 bg-slate-400 rounded-full border-2 border-slate-900 transition-all group-hover/conn:bg-white group-hover/conn:scale-125 hover:shadow-[0_0_10px_rgba(255,255,255,0.5)]"></div>
      </div>

    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class NodeWidgetComponent {
  @Input() data: any;
  @Input() x: number = 0;
  @Input() y: number = 0;
  
  // Events
  @Output() startConnection = new EventEmitter<MouseEvent>();
  @Output() finishConnection = new EventEmitter<MouseEvent>();
  
  // We re-emit the CDK events so parent can handle global graph updates
  // Note: We could bind directly in parent template too, but this keeps interface clean-ish
  @Output() cdkDragMoved = new EventEmitter<CdkDragMove>();
  @Output() cdkDragEnded = new EventEmitter<CdkDragEnd>();

  onDragMoved(event: CdkDragMove) {
      this.cdkDragMoved.emit(event);
  }

  onDragEnded(event: CdkDragEnd) {
      this.cdkDragEnded.emit(event);
  }

  onMouseDownOutput(event: MouseEvent) {
      this.startConnection.emit(event);
  }

  onMouseUpInput(event: MouseEvent) {
      this.finishConnection.emit(event);
  }

  // --- Styling Helpers ---

  getNodeClasses() {
      const type = this.data.type;
      const base = "backdrop-blur-xl border-white/10 ";
      
      switch(type) {
          case 'START': return base + "bg-slate-900/80 shadow-blue-500/10 hover:border-blue-500/50";
          case 'LLM':   return base + "bg-slate-900/80 shadow-purple-500/10 hover:border-purple-500/50";
          case 'TOOL':  return base + "bg-slate-900/80 shadow-emerald-500/10 hover:border-emerald-500/50";
          default:      return base + "bg-slate-900/80 shadow-slate-500/10 hover:border-slate-500/50";
      }
  }

  getIconBackground() {
      switch(this.data.type) {
          case 'START': return 'bg-blue-500/20 text-blue-400';
          case 'LLM':   return 'bg-purple-500/20 text-purple-400';
          case 'TOOL':  return 'bg-emerald-500/20 text-emerald-400';
          default:      return 'bg-slate-500/20 text-slate-400';
      }
  }

  getIcon() {
      switch(this.data.type) {
          case 'START': return '🚀';
          case 'LLM':   return '🧠';
          case 'OUTPUT': return '🏁';
          case 'TOOL':  return '🛠️';
          default:      return '📦';
      }
  }
}
