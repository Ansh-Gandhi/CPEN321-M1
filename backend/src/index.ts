import { createApp } from './app';
import { env } from './config/env';
import { PixelRelayService } from './services/pixelRelay';

const app = createApp();

const server = app.listen(env.port, () => {
  console.log(`Server listening on port ${env.port}`);
});

// Start WebSocket relay service for Button 2 live pixel updates
const pixelRelay = new PixelRelayService(server);

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    pixelRelay.close();
    server.close(() => {
      process.exit(0);
    });
  });
}
