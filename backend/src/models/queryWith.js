import { query as databaseQuery } from '../config/db.js';

export function queryWith(client, text, values) {
  return client ? client.query(text, values) : databaseQuery(text, values);
}
