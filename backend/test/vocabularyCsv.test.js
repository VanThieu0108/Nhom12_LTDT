import test from 'node:test';
import assert from 'node:assert/strict';
import { csvObjects, parseCsv } from '../src/utils/csv.js';
import { parseVocabularyCsv } from '../src/scripts/importVocabulary.js';

test('CSV parser supports quoted commas, escaped quotes, and CRLF rows', () => {
  assert.deepEqual(parseCsv('word,meaning\r\n"red, apple","A ""fruit"""\r\n'), [
    ['word', 'meaning'],
    ['red, apple', 'A "fruit"']
  ]);
});

test('CSV object parser strips BOM and validates required headers and row widths', () => {
  assert.deepEqual(csvObjects('\uFEFFword_en,meaning_vi\nCat,Con mèo\n', ['word_en', 'meaning_vi']), [
    { word_en: 'Cat', meaning_vi: 'Con mèo' }
  ]);
  assert.throws(() => csvObjects('word_en\nCat,Extra\n', ['word_en']), /expected 1/);
  assert.throws(() => csvObjects('word_en\nCat\n', ['word_en', 'meaning_vi']), /missing required columns/);
});

test('vocabulary CSV validates rows and normalizes optional media fields', () => {
  const rows = parseVocabularyCsv(
    'word_en,meaning_vi,difficulty_level,category,phonetic,audio_url,image_url\n' +
    'Apple,Quả táo,1,Fruits,/ˈæp.əl/,,\n'
  );
  assert.deepEqual(rows[0], {
    word_en: 'Apple',
    meaning_vi: 'Quả táo',
    difficulty_level: 1,
    category: 'Fruits',
    phonetic: '/ˈæp.əl/',
    audio_url: null,
    image_url: null
  });
  assert.throws(() => parseVocabularyCsv(
    'word_en,meaning_vi,difficulty_level,category\nDog,Con chó,0,Animals\n'
  ), /Invalid CSV row 2/);
});
