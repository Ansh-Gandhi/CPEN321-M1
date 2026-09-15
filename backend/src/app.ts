import express, { type Express } from 'express';
import os from 'os';

/**
 * Decode HTML entities in trivia text
 */
function decodeHTMLEntities(text: string): string {
  if (!text) return '';
  return text
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'")
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&eacute;/g, 'é')
    .replace(/&deg;/g, '°');
}

/**
 * Get the server's local time formatted as hh:mm:ss GMT±hh:mm
 */
function getFormattedServerTime(): string {
  const now = new Date();

  // getTimezoneOffset() returns difference in minutes between UTC and local time
  const offsetMs = now.getTimezoneOffset();
  const offsetHours = Math.floor(Math.abs(offsetMs) / 60);
  const offsetMinutes = Math.abs(offsetMs) % 60;

  const sign = offsetMs <= 0 ? '+' : '-';

  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');

  const offsetHourStr = String(offsetHours).padStart(2, '0');
  const offsetMinStr = String(offsetMinutes).padStart(2, '0');

  return `${hours}:${minutes}:${seconds} GMT${sign}${offsetHourStr}:${offsetMinStr}`;
}

/**
 * Get the server's public IP address with fallback to local interface or environment variable
 */
async function getServerIP(req?: express.Request): Promise<string> {
  if (process.env.PUBLIC_IP) {
    return process.env.PUBLIC_IP;
  }

  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 2000);
    const response = await fetch('https://api.ipify.org?format=json', {
      signal: controller.signal,
    });
    clearTimeout(timeout);

    if (response.ok) {
      const data = (await response.json()) as { ip?: string };
      if (data.ip) {
        return data.ip;
      }
    }
  } catch {
    // Ignore external fetch error and fallback to network interfaces / request headers
  }

  if (req) {
    const forwarded = req.headers['x-forwarded-for'];
    if (typeof forwarded === 'string' && forwarded.length > 0) {
      const firstIp = forwarded.split(',')[0];
      if (firstIp) {
        return firstIp.trim();
      }
    }
  }

  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    const iface = interfaces[name];
    if (iface) {
      for (const addr of iface) {
        if (addr.family === 'IPv4' && !addr.internal) {
          return addr.address;
        }
      }
    }
  }

  return '127.0.0.1';
}

export function createApp(): Express {
  const app = express();

  app.use(express.json());

  // Health check endpoint
  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  // Button 1 APIs

  /**
   * GET /api/server-ip
   * Returns the server's public IP address
   */
  app.get('/api/server-ip', async (req, res) => {
    const ip = await getServerIP(req);
    res.json({ ip });
  });

  /**
   * GET /api/server-time
   * Returns the server's local time formatted as hh:mm:ss GMT±hh:mm
   */
  app.get('/api/server-time', (_req, res) => {
    const time = getFormattedServerTime();
    res.json({ time });
  });

  /**
   * GET /api/my-name
   * Returns developer's first and last name
   */
  app.get('/api/my-name', (_req, res) => {
    res.json({
      firstName: 'Ansh',
      lastName: 'Gandhi',
    });
  });

  /**
   * POST /api/auth/google
   * Authenticates user via Google token/payload
   */
  app.post('/api/auth/google', (req, res) => {
    const { name, email } = req.body || {};
    res.json({
      success: true,
      message: 'Authenticated successfully',
      user: {
        name: name || 'Google User',
        email: email || '',
      },
    });
  });

  // Button 3 API

  /**
   * GET /api/trivia
   * Fetches a random trivia question for Button 3 surprise
   */
  app.get('/api/trivia', async (_req, res) => {
    try {
      const response = await fetch('https://opentdb.com/api.php?amount=1&type=multiple');
      if (response.ok) {
        const data = (await response.json()) as {
          results?: Array<{
            category: string;
            type: string;
            difficulty: string;
            question: string;
            correct_answer: string;
            incorrect_answers: string[];
          }>;
        };

        if (data.results && data.results.length > 0) {
          const item = data.results[0];
          if (item) {
            return res.json({
              category: decodeHTMLEntities(item.category),
              difficulty: decodeHTMLEntities(item.difficulty),
              question: decodeHTMLEntities(item.question),
              correctAnswer: decodeHTMLEntities(item.correct_answer),
              incorrectAnswers: item.incorrect_answers.map(decodeHTMLEntities),
            });
          }
        }
      }
    } catch {
      // Ignore network error and fallback to default trivia question below
    }

    // Fallback trivia question
    return res.json({
      category: 'General Knowledge',
      difficulty: 'easy',
      question: 'What is the capital of Canada?',
      correctAnswer: 'Ottawa',
      incorrectAnswers: ['Toronto', 'Vancouver', 'Montreal'],
    });
  });

  // 404 handler
  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });

  return app;
}
