import type { Server } from 'http';
import { WebSocket, WebSocketServer } from 'ws';

const UPSTREAM_URL = 'wss://8.229.22.124';

export class PixelRelayService {
  private wss: WebSocketServer | null = null;
  private upstreamWs: WebSocket | null = null;
  private isConnectingUpstream = false;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(server: Server) {
    // Set up local WebSocket server attached to HTTP server at /ws/pixels
    this.wss = new WebSocketServer({ server, path: '/ws/pixels' });

    this.wss.on('connection', (clientWs) => {
      console.log('[PixelRelay] New Android client connected.');

      // Send initial welcome/status ping if needed
      clientWs.on('close', () => {
        console.log('[PixelRelay] Android client disconnected.');
      });

      clientWs.on('error', (err) => {
        console.error('[PixelRelay] Client WebSocket error:', err.message);
      });
    });

    // Start connecting to upstream course server
    this.connectUpstream();
  }

  private connectUpstream() {
    if (this.isConnectingUpstream) return;
    this.isConnectingUpstream = true;

    console.log(`[PixelRelay] Connecting to upstream: ${UPSTREAM_URL}...`);

    try {
      this.upstreamWs = new WebSocket(UPSTREAM_URL, {
        rejectUnauthorized: false, // Accept IP-based SSL certificates from upstream
      });

      this.upstreamWs.on('open', () => {
        this.isConnectingUpstream = false;
        console.log('[PixelRelay] Connected to upstream WebSocket server.');
      });

      this.upstreamWs.on('message', (data) => {
        const payload = data.toString();
        // Relay payload immediately to all connected local WebSocket clients without batching or modification
        this.broadcast(payload);
      });

      this.upstreamWs.on('close', () => {
        this.isConnectingUpstream = false;
        console.warn('[PixelRelay] Upstream connection closed. Reconnecting in 3 seconds...');
        this.scheduleReconnect();
      });

      this.upstreamWs.on('error', (err) => {
        this.isConnectingUpstream = false;
        console.error('[PixelRelay] Upstream WebSocket error:', err.message);
        this.upstreamWs?.close();
      });
    } catch (err) {
      this.isConnectingUpstream = false;
      console.error('[PixelRelay] Failed to initiate upstream connection:', err);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => {
      this.connectUpstream();
    }, 3000);
  }

  private broadcast(payload: string) {
    if (!this.wss) return;

    for (const client of this.wss.clients) {
      if (client.readyState === WebSocket.OPEN) {
        client.send(payload);
      }
    }
  }

  public close() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.upstreamWs?.close();
    this.wss?.close();
  }
}
