import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  template: `
    <div class="w-64 bg-slate-900 border-r border-slate-700 flex flex-col h-full bg-opacity-90 backdrop-blur-md">
      <div class="p-4 border-b border-slate-700 bg-gradient-to-r from-slate-900 to-slate-800">
        <h2 class="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-400 flex items-center gap-2">
          <span>AutoFlow AI</span>
        </h2>
      </div>
      
      <div class="p-4 overflow-y-auto custom-scrollbar flex-1">
        <div class="mb-6">
          <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">Herramientas</h3>
          <div class="grid grid-cols-1 gap-2">
             <!-- Tools list placeholder -->
             <div class="p-3 bg-slate-800 rounded-lg border border-slate-700 cursor-move hover:border-blue-500 hover:shadow-[0_0_10px_rgba(59,130,246,0.3)] transition-all group duration-300">
                <div class="flex items-center gap-3">
                  <span class="text-emerald-400 group-hover:scale-110 transition-transform">🤖</span>
                  <div>
                    <div class="font-medium text-slate-200 group-hover:text-blue-300 transition-colors">LLM Node</div>
                    <div class="text-xs text-slate-500">Procesamiento IA</div>
                  </div>
                </div>
             </div>
          </div>
        </div>
      </div>
      
      <div class="p-4 border-t border-slate-800 bg-slate-900/50">
        <div class="text-xs text-slate-500 text-center">
          v2.0.0 • Java/Angular
        </div>
      </div>
    </div>
  `
})
export class SidebarComponent {}
