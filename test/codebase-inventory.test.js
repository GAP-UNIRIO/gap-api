import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import test from 'node:test';

function listFiles(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const fullPath = path.join(directory, entry.name);
    return entry.isDirectory() ? listFiles(fullPath) : [fullPath];
  });
}

test('@spec:AC-014 todo arquivo de produção está mapeado em uma tarefa', () => {
  const specRoot = path.join(process.cwd(), '.spec', 'features');
  const taskFiles = listFiles(specRoot).filter((file) => path.basename(file) === 'tasks.md');
  const mapped = new Set();

  for (const taskFile of taskFiles) {
    const content = readFileSync(taskFile, 'utf8');
    for (const line of content.split(/\r?\n/)) {
      const match = line.match(/^\s*-\s*Arquivos\s*:\s*(.+)$/i);
      if (!match) continue;
      for (const file of match[1].split(',').map((value) => value.trim())) {
        mapped.add(file.replaceAll('\\', '/'));
      }
    }
  }

  const productionFiles = listFiles(path.join(process.cwd(), 'src', 'main'));
  const unmapped = productionFiles
    .map((file) => path.relative(process.cwd(), file).replaceAll('\\', '/'))
    .filter((file) => !mapped.has(file));

  assert.deepEqual(unmapped, [], `Arquivos de produção sem tarefa:\n${unmapped.join('\n')}`);
});
