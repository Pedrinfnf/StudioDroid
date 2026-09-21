// SPDX-License-Identifier: GPL-3.0-only
// Invoked by validate_m0.py; no npm packages or runtime code.
'use strict';
const fs = require('node:fs');
const assert = require('node:assert/strict');
const input = JSON.parse(fs.readFileSync(0, 'utf8'));
for (const pattern of input.patterns) new RegExp(pattern, 'u');
let count = 0;
for (const group of input.cases) {
  const regex = new RegExp(group.pattern, 'u');
  for (const [label, expected] of [['accept', true], ['reject', false]]) {
    for (const value of group[label]) {
      assert.equal(regex.test(value), expected, `${group.name}: ${JSON.stringify(value)}`);
      count++;
    }
  }
}
console.log(`PASS ECMAScript: ${input.patterns.length} patterns compiled; ${count} assertions`);
