import { spawnSync } from 'node:child_process';
import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

const nodeTests = spawnSync(process.execPath,
  ['--test', '--test-reporter=tap', 'test/flyway-migration.test.js', 'test/codebase-inventory.test.js'],
  { cwd: process.cwd(), encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 });

const gradleArgs = [
  'cleanTest', 'test',
  '--tests', 'com.gap.api.Controller.OrderControllerTest',
  '--tests', 'com.gap.api.Service.OrderServiceTest',
  '--tests', 'com.gap.api.Service.WorkflowEngineServiceTest',
  '--tests', 'com.gap.api.Service.WorkflowTemplateServiceTest',
  '--tests', 'com.gap.api.Controller.WorkflowTemplateControllerTest',
  '--tests', 'com.gap.api.Integration.WorkflowTemplateServiceIntegrationTest',
  '--tests', 'com.gap.api.Controller.AcademicCalendarControllerTest',
  '--tests', 'com.gap.api.Service.AcademicCalendarServiceTest',
  '--tests', 'com.gap.api.Controller.CourseControllerTest',
  '--tests', 'com.gap.api.Service.CourseServiceTest',
  '--tests', 'com.gap.api.Integration.CourseServiceIntegrationTest',
  '--tests', 'com.gap.api.Controller.InstitutionControllerTest',
  '--tests', 'com.gap.api.Service.InstitutionServiceTest',
  '--tests', 'com.gap.api.Controller.UserControllerTest',
  '--tests', 'com.gap.api.Service.UserServiceTest',
  '--tests', 'com.gap.api.Controller.OrgUnitControllerTest',
  '--tests', 'com.gap.api.Service.OrgUnitServiceTest',
  '--tests', 'com.gap.api.Integration.OrgUnitServiceIntegrationTest',
  '--tests', 'com.gap.api.Controller.RoleControllerTest',
  '--tests', 'com.gap.api.Service.RoleServiceTest',
  '--tests', 'com.gap.api.Integration.RoleServiceIntegrationTest',
  '--tests', 'com.gap.api.Integration.OrderServiceIntegrationTest',
];
const gradle = process.platform === 'win32'
  ? spawnSync('cmd.exe', ['/d', '/s', '/c', '.\\gradlew.bat', ...gradleArgs], {
    cwd: process.cwd(),
    encoding: 'utf8',
    maxBuffer: 64 * 1024 * 1024,
  })
  : spawnSync('./gradlew', gradleArgs, {
  cwd: process.cwd(),
  encoding: 'utf8',
  maxBuffer: 64 * 1024 * 1024,
});

if (nodeTests.error) console.error(nodeTests.error);
if (gradle.stdout) process.stderr.write(gradle.stdout);
if (gradle.stderr) process.stderr.write(gradle.stderr);

const cases = [];
for (const line of (nodeTests.stdout || '').split(/\r?\n/)) {
  const match = line.match(/^\s*(not )?ok\s+\d+\s+-\s+(.*)$/);
  if (match) cases.push({ title: match[2], failed: Boolean(match[1]), skipped: /#\s*SKIP\b/i.test(match[2]) });
}

function decodeXml(value) {
  return value.replace(/&quot;/g, '"').replace(/&apos;/g, "'")
    .replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
}

const reportsDir = join(process.cwd(), 'build', 'test-results', 'test');
let reportCount = 0;
try {
  const reports = readdirSync(reportsDir).filter((name) => name.startsWith('TEST-') && name.endsWith('.xml'));
  reportCount = reports.length;
  for (const filename of reports) {
    const xml = readFileSync(join(reportsDir, filename), 'utf8');
    for (const match of xml.matchAll(/<testcase\b([^>]*?)(?:\/>|>([\s\S]*?)<\/testcase>)/g)) {
      const name = match[1].match(/\bname="([^"]*)"/)?.[1];
      if (!name) continue;
      const body = match[2] || '';
      cases.push({
        title: decodeXml(name),
        failed: /<(?:failure|error)\b/.test(body),
        skipped: /<skipped\b/.test(body),
      });
    }
  }
} catch (error) {
  console.error(`Could not read JUnit reports: ${error.message}`);
}

if (gradle.status === 0 && reportCount === 0) {
  console.error('Gradle succeeded but produced no JUnit XML reports.');
}

console.log('TAP version 13');
cases.forEach((testCase, index) => {
  const status = testCase.failed ? 'not ok' : 'ok';
  const skip = testCase.skipped ? ' # SKIP' : '';
  console.log(`${status} ${index + 1} - ${testCase.title}${skip}`);
});
console.log(`1..${cases.length}`);

if (nodeTests.error || nodeTests.status !== 0 || gradle.error || gradle.status !== 0 || reportCount === 0 || cases.some((item) => item.failed)) {
  process.exitCode = 1;
}
