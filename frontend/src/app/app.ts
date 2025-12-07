import { Component, OnInit, signal, computed, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './sidebar.component';
import { NodeWidgetComponent } from './node-widget.component';
import { HttpClient } from '@angular/common/http';
import { DragDropModule, CdkDragMove, CdkDragEnd } from '@angular/cdk/drag-drop';

interface Position { x: number; y: number; }
interface NodeData { 
  id: string; 
  type: string; 
  label: string; 
  data?: any;
  position: Position; 
}
interface Connection { 
  id: string; 
  from: string; 
  to: string; 
  fromHandle?: string;
  toHandle?: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, SidebarComponent, NodeWidgetComponent, DragDropModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  // Graph State
  nodes = signal<NodeData[]>([]);
  connections = signal<Connection[]>([]);
  
  // Interaction State
  pan = signal<Position>({ x: 0, y: 0 });
  zoom = signal<number>(1);
  isDraggingCanvas = false;
  lastMousePos: Position = { x: 0, y: 0 };
  
  // Connection Creation State
  tempConnection = signal<{ fromId: string; fromPos: Position; toPos: Position } | null>(null);

  // Execution State
  isRunning = false;
  lastResult: any = null;
  showResult = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadTemplate();
  }

  loadTemplate() {
    // Mock data for dev if API fails or is empty, or load actual
    this.http.get<any[]>('/api/workspaces/templates').subscribe({
      next: (templates) => {
        if (templates && templates.length > 0) {
          const tpl = templates[0];
          this.nodes.set(tpl.nodes.map((n: any) => ({
            ...n,
            id: n.id || crypto.randomUUID(), // Ensure IDs
            // Ensure unique position references to avoid hydration issues if needed
            position: { ...n.position } 
          })));
          this.connections.set(tpl.connections || []);
        } else {
            this.initDemoData();
        }
      },
      error: () => this.initDemoData()
    });
  }

  initDemoData() {
     this.nodes.set([
         { id: '1', type: 'START', label: 'Chat Trigger', position: { x: 100, y: 200 }, data: { description: 'User sends a message' } },
         { id: '2', type: 'LLM', label: 'Gemini Pro', position: { x: 500, y: 150 }, data: { model: 'gemini-1.5-pro' } },
         { id: '3', type: 'TOOL', label: 'Search Web', position: { x: 500, y: 350 }, data: { source: 'Google' } },
         { id: '4', type: 'OUTPUT', label: 'Response', position: { x: 900, y: 250 }, data: {} }
     ]);
     this.connections.set([
         { id: 'c1', from: '1', to: '2' },
         { id: 'c2', from: '1', to: '3' },
         { id: 'c3', from: '2', to: '4' },
         { id: 'c4', from: '3', to: '4' }
     ]);
  }

  // --- Canvas Interaction (Pan/Zoom) ---

  @HostListener('wheel', ['$event'])
  onWheel(event: WheelEvent) {
    if (event.ctrlKey || event.metaKey) {
        event.preventDefault();
        const zoomSpeed = 0.001;
        const newZoom = Math.max(0.1, Math.min(3, this.zoom() - event.deltaY * zoomSpeed));
        this.zoom.set(newZoom);
    } else {
        // Optional: Pan on scroll? Standard is usually create zoom on ctrl+scroll, native scroll otherwise.
        // We will stick to native overflow for now unless strictly requested otherwise, 
        // BUT user asked for "graph implementation", so infinite canvas is better.
        // Let's implement pan on wheel or click-drag background.
    }
  }

  onCanvasMouseDown(event: MouseEvent) {
    if ((event.target as HTMLElement).closest('.node-widget')) return; // Ignore if clicking node
    this.isDraggingCanvas = true;
    this.lastMousePos = { x: event.clientX, y: event.clientY };
  }

  @HostListener('window:mousemove', ['$event'])
  onWindowMouseMove(event: MouseEvent) {
    // Handle Canvas Panning
    if (this.isDraggingCanvas) {
      const dx = event.clientX - this.lastMousePos.x;
      const dy = event.clientY - this.lastMousePos.y;
      this.pan.update(p => ({ x: p.x + dx, y: p.y + dy }));
      this.lastMousePos = { x: event.clientX, y: event.clientY };
    }

    // Handle Temp Connection dragging
    if (this.tempConnection()) {
        const rect = (document.querySelector('.canvas-container') as HTMLElement).getBoundingClientRect();
        // Convert screen coordinates to graph coordinates
        const scale = this.zoom();
        const mouseX = (event.clientX - rect.left - this.pan().x) / scale;
        const mouseY = (event.clientY - rect.top - this.pan().y) / scale;
        
        this.tempConnection.update(curr => curr ? { ...curr, toPos: { x: mouseX, y: mouseY } } : null);
    }
  }

  @HostListener('window:mouseup')
  onWindowMouseUp() {
    this.isDraggingCanvas = false;
    if (this.tempConnection()) {
        // Cancel connection if dropped on nothing
        this.tempConnection.set(null);
    }
  }

  // --- Node Interaction ---

  onNodeDragMoved(event: CdkDragMove, node: NodeData) {
    // Provide real-time position updates for connections
    const newPos = event.source.getFreeDragPosition();
    // We don't verify update the model yet, just UI update implicitly via CDK
    // But we need to update connections.
    
    // Ideally we update the signal so connections rerender
    // CAUTION: Frequent signal updates might be heavy. 
    // Optimization: Modify the SVG paths directly or use a computed signal that depends on a "version" tick?
    // For now, let's try direct updates.
    this.nodes.update(nodes => nodes.map(n => n.id === node.id ? { ...n, position: newPos } : n));
  }

  onNodeDragEnded(event: CdkDragEnd, node: NodeData) {
      const newPos = event.source.getFreeDragPosition();
      this.nodes.update(nodes => nodes.map(n => n.id === node.id ? { ...n, position: newPos } : n));
  }


  // --- Connection Creation ---

  startConnection(nodeId: string, event: MouseEvent) {
    event.stopPropagation();
    event.preventDefault();
    const node = this.nodes().find(n => n.id === nodeId);
    if (!node) return;

    // Starting point (right side of node)
    // Approximate connector position relative to node center/top-left
    const startX = node.position.x + 240; // Approx width
    const startY = node.position.y + 40;  // Approx half height

    this.tempConnection.set({
        fromId: nodeId,
        fromPos: { x: startX, y: startY },
        toPos: { x: startX, y: startY } // Initial
    });
  }

  finishConnection(targetNodeId: string, event: MouseEvent) {
      const temp = this.tempConnection();
      if (!temp) return;
      if (temp.fromId === targetNodeId) return; // Self connection

      // Check if connection exists
      const exists = this.connections().some(c => c.from === temp.fromId && c.to === targetNodeId);
      if (!exists) {
          this.connections.update(c => [...c, {
              id: crypto.randomUUID(),
              from: temp.fromId,
              to: targetNodeId
          }]);
      }
      this.tempConnection.set(null);
      event.stopPropagation();
  }


  // --- Helper Methods ---

  getConnectorPosition(nodeId: string, type: 'input'|'output'): Position {
      const node = this.nodes().find(n => n.id === nodeId);
      if (!node) return { x: 0, y: 0 };
      // These offsets must match CSS layout
      if (type === 'output') return { x: node.position.x + 240, y: node.position.y + 45 }; 
      return { x: node.position.x, y: node.position.y + 45 };
  }

  generatePath(start: Position, end: Position): string {
    const startX = start.x;
    const startY = start.y;
    const endX = end.x;
    const endY = end.y;

    const dist = Math.abs(endX - startX);
    const controlDist = Math.max(dist * 0.5, 50);

    const cp1x = startX + controlDist;
    const cp1y = startY;
    const cp2x = endX - controlDist;
    const cp2y = endY;

    return `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`;
  }

  getConnectionPath(conn: Connection): string {
      const start = this.getConnectorPosition(conn.from, 'output');
      const end = this.getConnectorPosition(conn.to, 'input');
      return this.generatePath(start, end);
  }

  getTempPath(): string {
      const t = this.tempConnection();
      if (!t) return '';
      return this.generatePath(t.fromPos, t.toPos);
  }

  runFlow() {
    this.isRunning = true;
    const workspaceId = 'tpl-basic-chat';
    const payload = { input: "Start execution" };

    this.http.post(`/api/workspaces/${workspaceId}/trigger`, payload).subscribe({
      next: (res) => {
        this.lastResult = res;
        this.isRunning = false;
        this.showResult = true;
      },
      error: (err) => {
        this.lastResult = { error: err.message };
        this.isRunning = false;
      }
    });
  }
}
