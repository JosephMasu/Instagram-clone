import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import http from 'node:http';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');
const gatewayUrl = process.env['API_GATEWAY_URL'] ?? 'http://127.0.0.1:8080';

const app = express();
const angularApp = new AngularNodeAppEngine();

app.use('/api', (req, res) => {
  const target = new URL(req.originalUrl, gatewayUrl);
  const headers: http.OutgoingHttpHeaders = { host: target.host };
  for (const [key, value] of Object.entries(req.headers)) {
    if (value === undefined) {
      continue;
    }
    const lower = key.toLowerCase();
    if (
      lower === 'connection' ||
      lower === 'keep-alive' ||
      lower === 'transfer-encoding' ||
      lower === 'upgrade' ||
      lower === 'host'
    ) {
      continue;
    }
    headers[key] = value;
  }

  const proxyReq = http.request(
    target,
    {
      method: req.method,
      headers,
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode ?? 502, proxyRes.headers);
      proxyRes.pipe(res);
    },
  );

  proxyReq.on('error', () => {
    if (!res.headersSent) {
      res.status(502).json({
        status: 502,
        error: 'Bad Gateway',
        message:
          'Cannot reach the API gateway. Start the backend and keep it on port 8080.',
      });
    }
  });

  req.pipe(proxyReq);
});

app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

export const reqHandler = createNodeRequestHandler(app);
