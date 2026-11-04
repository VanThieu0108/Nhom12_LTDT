export function parseCsv(text) {
  const rows = [];
  let row = [];
  let field = '';
  let inQuotes = false;
  let fieldClosed = false;

  for (let index = 0; index < text.length; index += 1) {
    const character = text[index];

    if (inQuotes) {
      if (character === '"' && text[index + 1] === '"') {
        field += '"';
        index += 1;
      } else if (character === '"') {
        inQuotes = false;
        fieldClosed = true;
      } else {
        field += character;
      }
      continue;
    }

    if (fieldClosed && character !== ',' && character !== '\r' && character !== '\n' && character !== ' ' && character !== '\t') {
      throw new Error(`Unexpected character after a quoted CSV field at offset ${index}.`);
    }
    if (character === '"' && field.length === 0 && !fieldClosed) {
      inQuotes = true;
    } else if (character === ',') {
      row.push(field.trim());
      field = '';
      fieldClosed = false;
    } else if (character === '\n' || character === '\r') {
      if (character === '\r' && text[index + 1] === '\n') index += 1;
      row.push(field.trim());
      if (row.some((cell) => cell !== '')) rows.push(row);
      row = [];
      field = '';
      fieldClosed = false;
    } else {
      if (character === '"' && !fieldClosed) {
        throw new Error(`Unexpected quote in an unquoted CSV field at offset ${index}.`);
      }
      if (!fieldClosed) field += character;
    }
  }

  if (inQuotes) throw new Error('CSV ends inside a quoted field.');
  row.push(field.trim());
  if (row.some((cell) => cell !== '')) rows.push(row);
  return rows;
}

export function csvObjects(text, requiredHeaders) {
  const rows = parseCsv(text.replace(/^\uFEFF/, ''));
  if (!rows.length) throw new Error('CSV file is empty.');

  const headers = rows[0].map((header) => header.trim().toLowerCase());
  if (new Set(headers).size !== headers.length) throw new Error('CSV contains duplicate column names.');
  const missing = requiredHeaders.filter((header) => !headers.includes(header));
  if (missing.length) throw new Error(`CSV is missing required columns: ${missing.join(', ')}.`);

  return rows.slice(1).map((cells, index) => {
    if (cells.length !== headers.length) {
      throw new Error(`CSV row ${index + 2} has ${cells.length} values; expected ${headers.length}.`);
    }
    return Object.fromEntries(headers.map((header, column) => [header, cells[column]]));
  });
}
